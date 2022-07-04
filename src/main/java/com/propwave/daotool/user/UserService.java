package com.propwave.daotool.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.propwave.daotool.badge.model.BadgeWallet;
import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.config.BaseResponse;
import com.propwave.daotool.config.jwt.SecurityService;
import com.propwave.daotool.user.model.*;
import com.propwave.daotool.utils.GetNFT;
import com.propwave.daotool.wallet.model.UserWallet;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.util.*;

import static com.propwave.daotool.config.BaseResponseStatus.*;

@Service
public class UserService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserDao userDao;
    private final UserProvider userProvider;
    @Autowired
    private final GetNFT getNFT;
    private final SecurityService securityService;

    public UserService(GetNFT getNFT, UserDao userDao, UserProvider userProvider, SecurityService securityService){
        this.userDao = userDao;
        this.userProvider = userProvider;
        this.getNFT = getNFT;
        this.securityService = securityService;
    }

    public Map<String, Object> createUser(String userID) throws BaseException{
        try{
            //newUser의 JWT 토큰 만들기
            String jwtToken = securityService.createToken(userID, (360*1000*60)); // 토큰 유효시간 6시간
            User newUser = userDao.createUser(userID);
            System.out.println(newUser);

            ObjectMapper objectMapper = new ObjectMapper().registerModule(new SimpleModule());
            Map<String, Object> res = objectMapper.convertValue(newUser, Map.class);
            res.put("jwtToken", jwtToken);

            return res;
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public User createUser(Map<String, Object> userInfo, String profileImageS3Path) throws BaseException{
        try{
            userInfo.replace("profileImage", profileImageS3Path);
            return userDao.createUser(userInfo);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public User createUser(UserSignupReq userSignupReq, String profileImageS3Path) throws BaseException{
        try{
            Map<String, Object> userInfo = new LinkedHashMap<>();
            userInfo.put("id", userSignupReq.getId());
            userInfo.put("profileImage",profileImageS3Path);
            userInfo.put("introduction", userSignupReq.getIntroduction());
            userInfo.put("url", userSignupReq.getUrl());

            return userDao.createUser(userInfo);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public User editUser(Map<String, String> userInfo, String profileImageS3Path) throws BaseException{
        try{
            System.out.println(userInfo);
            System.out.println(profileImageS3Path);
            userInfo.put("profileImage", profileImageS3Path);
            return userDao.editUser(userInfo);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public UserSocial editUserProfileAndSocial(String userID, String json) throws BaseException, JsonProcessingException {
        try{
            ObjectMapper objectMapper = new ObjectMapper().registerModule(new SimpleModule());
            Map<String, String> req = objectMapper.readValue(json, new TypeReference<>() {});

            User changedUser = userDao.editUserProfile(userID, req.get("profileName"), req.get("introduction"), req.get("url"));
            // 사용자 social이 이미 있느지 확인하고, 없으면 새로만드는 방식으로 가야함!!!
            Social social = userDao.getSocial(userID);
            Social changedSocial;
            if(social == null){
                changedSocial = userDao.createUserSocial(userID, req.get("twitter"), req.get("facebook"), req.get("discord"), req.get("link"));
            }
            else{
                changedSocial = userDao.changeUserSocial(userID, req.get("twitter"), req.get("facebook"), req.get("discord"), req.get("link"));
            }

            return new UserSocial(changedUser.getId(), changedUser.getProfileImage(), changedUser.getIntroduction(), changedUser.getUrl(), changedUser.getHits(), changedUser.getTodayHits(), changedUser.getCreatedAt(), changedUser.getNftRefreshLeft(), changedUser.getBackImage(), changedUser.getNickname(), changedUser.getIndex(),
                    changedSocial.getTwitter(), changedSocial.getFacebook(), changedSocial.getDiscord(), changedSocial.getLink());
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public int editUserProfileImg(String userID, String profileImagePath) throws BaseException {
        try{
            return userDao.editUserProfileImg(userID, profileImagePath);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public int editUserBackImg(String userID, String backImagePath) throws BaseException {
        try{
            return userDao.editUserBackImg(userID, backImagePath);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }



    public int addWalletToUser(String userID, String walletAddress, String walletType){
        //이미 있는 지갑인지 확인하기
        int walletExist = userDao.isWalletExist(walletAddress);
        //이미 나에게 있는 지갑인지 확인하기
        int walletExistToMe = userDao.isUserWalletExist(userID, walletAddress);
        if(walletExistToMe==1){
            return -1;
        }

        if(walletExist == 0){
            userDao.createWallet(walletAddress, walletType);
        }
        return userDao.createUserWallet(userID, walletAddress);
    }

    public int deleteUser(String userId) throws BaseException{
        try{
            return userDao.deleteUser(userId);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public int createFriendReq(String reqTo, String reqFrom, String reqNickname) throws BaseException {
        try{
            // make friendReq record
            return userDao.createFriendReq(reqTo, reqFrom, reqNickname);

            // make alarm to reqTo
            //userDao.createAlarm();
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public int acceptFriend(boolean accepted, String reqTo, String reqFrom, String toNickname) throws BaseException {
        try{
            if(accepted){
                //1. friend Req accept로 바꾸기
                userDao.updateFriendReq(reqTo, reqFrom);
                //2. friend record 만들기
                String fromNickname = userDao.getFriendReqNickname(reqFrom, reqTo);
                userDao.createFriend(reqTo, reqFrom, toNickname);   // to에게 from이라는 친구가 to Nickname 이라는 이름으로 생김
                return userDao.createFriend(reqFrom, reqTo, fromNickname);
                //3. 알람 만들기
            } else{
                return userDao.deleteFriendReq(reqFrom, reqTo);
            }
        }catch(Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public Friend editFriendNickname(String user, String friend, String newNickname){
        return userDao.editFriendNickname(user, friend, newNickname);
    }

    public int createFollow(String reqTo, String reqFrom){
        int checkFollowExist = userDao.isFollowExist(reqTo, reqFrom);
        if(checkFollowExist == 1){
            return -2;
        }
        return userDao.createFollow(reqTo, reqFrom);
    }

    public int deleteFollow(String reqTo, String reqFrom){
        return userDao.deleteFollow(reqTo, reqFrom);
    }


    // ----------------------------------------------

    public List<String> createWallet(String userId, List<Map<String, Object>> wallets, String when) throws BaseException {
        List<String> successWallets = new ArrayList<>();
        try {
            // 2. 지갑 만들기
            for (Map<String, Object> wallet : wallets) {
                //1. 지갑 만들기
                // 지갑 객체가 이미 있는 친구인지 확인하기
                System.out.println(wallet.get("walletAddress"));
                String walletAddress = (String) wallet.get("walletAddress");
                String walletType = (String) wallet.get("walletType");
                System.out.println(userDao.isWalletExist(walletAddress));
                if (userDao.isWalletExist(walletAddress) == 0) {
                    //없으면 객체 만들기
                    System.out.println("지갑 객체 없음");
                    userDao.createWallet(walletAddress, walletType);
                    System.out.println("in if, create wallet success");
                }
                System.out.println("out if, create userwallet");
                //2. userWallet 만들기
                System.out.println(wallet.get("walletAddress"));
                if (when.equals("login")){
                    userDao.createUserWalletForLogin(wallet, userId);
                }else{
                    userDao.createUserWalletForDashBoard(wallet, userId);
                }

                successWallets.add(walletAddress);
            }
            return successWallets;
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<String> addWalletWhenSignUp(String userId, List<Map<String, Object>> wallets) throws BaseException {
        List<String> successWallets = new ArrayList<>();
        try {
            // 2. 지갑 만들기
            for (Map<String, Object> wallet : wallets) {
                //1. 지갑 만들기
                System.out.println(wallet.get("walletAddress"));
                String walletAddress = (String) wallet.get("walletAddress");
                String walletType = (String) wallet.get("walletType");
                System.out.println(userDao.isWalletExist(walletAddress));

                if (userDao.isWalletExist(walletAddress) == 0) {
                    //없으면 객체 만들기
                    System.out.println("지갑 객체 없음");
                    userDao.createWallet(walletAddress, walletType);
                    System.out.println("in if, create wallet success");
                }
                System.out.println("out if, create userwallet");
                //2. userWallet 만들기
                System.out.println(wallet.get("walletAddress"));
                userDao.createUserWallet(wallet, userId);

            successWallets.add(walletAddress);
        }
            return successWallets;
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public String createWallet(String walletAddress, String walletType) throws BaseException {
        try{
            System.out.println(walletAddress);
            String newWallet = userDao.createWallet(walletAddress, walletType);
            return newWallet;
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public String createUserWallet(Map<String, Object> wallet, String userId) throws BaseException {
        try{
            System.out.println(wallet);
            return userDao.createUserWallet(wallet, userId);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

//    public String createUserWallet(WalletSignupReq wallet, String userId) throws BaseException {
//        try{
//            String newUserWallet = userDao.createUserWallet(wallet, userId);
//            return newUserWallet;
//        }catch(Exception exception){
//            throw new BaseException(DATABASE_ERROR);
//        }
//    }

    public void addHit(String userId) throws BaseException{
        try{
            userDao.addHit(userId);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void makeLoginAvailable(int index)throws BaseException{
        try{
            userDao.makeLoginAvailable(index);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public int makeLoginUnavailable(String userId, String walletAddress) throws BaseException{
        try{
            return userDao.makeLoginUnavailable(userId, walletAddress);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public int deleteUserWallet(String userId, String walletAddress) throws BaseException{
        try{
            return userDao.deleteUserWallet(userId, walletAddress);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public int deleteWallet(String walletAddress) throws BaseException{
        try{
            return userDao.deleteWallet(walletAddress);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // dashboard용 지갑 추가하기
    public int createDashboardWallet(String userId, Map<String, Object> wallet) throws BaseException {
        String walletAddress = (String) wallet.get("walletAddress");
        wallet.put("loginAvailable", false);
        wallet.put("viewDataAvailable", true);
        wallet.put("user", userId);
//        int isUserWalletExistMe = userDao.isUserWalletExist(userId, walletAddress);
//        System.out.println(isUserWalletExistMe);
//        int isUserWalletExistOther = userDao.isUserWalletExist(walletAddress);
//        System.out.println(isUserWalletExistOther);
        // 걍 1. 지갑 존재 여부 존재?-> wallet 객체 안만듦 이거만 하면 되는거 아닌가?
        if(userDao.isWalletExist(walletAddress)==0){
            userDao.createWallet(walletAddress, (String)wallet.get("walletType"));
        }
        userDao.createUserWalletForDashBoard(wallet, userId);
        return 1;

//        // 1. 나게에 있는지 여부 확인
//        if (userDao.isUserWalletExist(userId, walletAddress)==1){
//            // 나에게 있는 경우 -> 대시보드용으로 추가하기
//            System.out.println("경우 3. 나에게 지갑이 있는 경우 -> 대시보드용으로 추가하기!");
//            return userDao.makeViewDataAvailable(userId, walletAddress);
//        }
//        // 2. 나에게 없다면, 남에게 있는지 확인
//        else if (userDao.isUserWalletExist(walletAddress)==1){
//            // 남에게 있는 경우 -> 나의 UserWallet 생성하기
//            System.out.println("경우 2. 나에게 지갑이 없고 남에게 있는 경우 -> UserWallet 추가");
//            userDao.createUserWallet(wallet);
//            return 1;
//        }
//        else if (userDao.isUserWalletExist(walletAddress)==0){
//            // 남에게도 없는 경우 -> wallet 생성, userWallet 생성
//            System.out.println("경우 1. 나에게도, 남에게도 지갑이 없는 경우 -> UserWallet, Wallet 추가");
//            userDao.createWallet(walletAddress, (String)wallet.get("walletType"));
//            userDao.createUserWallet(wallet);
//            return 1;
//        }
//        else{
//            System.out.println("경우 없는 경우");
//            throw new BaseException(RESPONSE_ERROR);
//        }
    }

    // 대시보드용 지갑 수정하기
    public int updateDashboardWallet(String userId, Map<String, Object> wallet) throws BaseException{
        wallet.put("user", userId);
        try {
            return userDao.editUserWallet(wallet);
        }catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 대시보드용 지갑 삭제하기
    public int deleteDashboardWallet(String userId, Map<String, Object> wallet) throws BaseException{
        System.out.println("service getin");
        wallet.put("user", userId);

        int walletIndex = (int) wallet.get("walletIndex");
        UserWallet userWallet = userDao.getUserWalletByIndex(walletIndex);
        String orgWalletAddress = userWallet.getWalletAddress();
//
//
//        // 1. 지갑이 남에게 있는지 여부
//        int isWalletSomeoneElse = userProvider.isWalletSomeoneElse(userId, orgWalletAddress);
//        System.out.println(isWalletSomeoneElse);
//        // 2. 나에게 로그인 용도 있는지 여부
//        int isWalletMyLogin = userDao.isWalletExistForLogin(userId, orgWalletAddress);
//        System.out.println(isWalletMyLogin);
//
//        // 상황 1. 나에게만 지갑이 있고 Only 대시보드용
//        if (isWalletSomeoneElse==0 && isWalletMyLogin == 0){
//            // userWallet 삭제, wallet 삭제
//            System.out.println("상황 1. 나에게만 지갑이 있고 Only 대시보드용");
//            userDao.deleteUserWallet(walletIndex);
//            if(userDao.isUserWalletExist(orgWalletAddress)==1){
//                return 1;
//            }
//            return userDao.deleteWallet(orgWalletAddress);
//       }
//        // 상황 2. 나에게만 지갑이 있고, 로그인도 있음 & 상황 4. 남에게 지갑이 있고, 로그인도 있음
//        else if (isWalletMyLogin == 1){
//            // userWallet의 dashboard를 0으로 변경
//            System.out.println("상황 2. 나에게만 지갑이 있고, 로그인도 있음 & 상황 4. 남에게 지갑이 있고, 로그인도 있음");
//            return userDao.makeViewDataUnavailable(walletIndex);
//        }
//        // 상황 3. 남에게 지갑이 있고, 나에게 Only 대시보드용
//        else if (isWalletSomeoneElse==1 && isWalletMyLogin == 0){
//            System.out.println("상황 3. 남에게 지갑이 있고, 나에게 Only 대시보드용");
//            // 내 userWallet 삭제
//            return userDao.deleteUserWallet(walletIndex);
//        }
//        // 그 외
//        else {
//            System.out.println("경우 없는 경우...");
//            throw new BaseException(RESPONSE_ERROR);
//        }

        // index 해당하는거 지우고 userWallet 남아있으면 wallet 냅두고!
        userDao.deleteUserWallet(walletIndex);
        if(userDao.isUserWalletExist(orgWalletAddress)==0){
            userDao.deleteWallet(orgWalletAddress);
        }
        return 1;
    }

    public BadgeRequest createBadgeRequest(String badgeName, Map<String, String> request){
        request.put("badgeName", badgeName);
        return userDao.createBadgeRequest(request);
    }

    public BadgeRequest processBadgeRequest(int index) throws BaseException {
        //1. 해당 인덱스에 해당하는 badgeRequest 가져오기
        try {
            BadgeRequest badgeRequest = userDao.getBadgeRequest(index);
            System.out.println(badgeRequest);
            // 2. badgeWallet 추가하기
            BadgeWallet newBadgeWallet = userDao.createBadgeWallet(badgeRequest.getDestWalletAddress(), badgeRequest.getBadgeName());
            System.out.println(newBadgeWallet);
            //3. badgeReqeust 값 수정하기
            return userDao.updateBadgeRequest(index);
        }catch(Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    public void getNFTRefresh(String walletAddress, String api_chain, String chain, int userWalletIndex) throws ParseException {
        String nftResult = getNFT.getNft(api_chain, walletAddress);
        JSONObject jsonObject = getNFT.fromJSONtoNFT(nftResult);

        // 이미 있는 NFT인지 확인하기
        System.out.println("total NFT : "+jsonObject.get("total"));
        List<JSONObject> results = (List<JSONObject>) jsonObject.get("result");
        int newNffIdx;

        for(JSONObject result: results){
            String token_address = (String) result.get("token_address");
            int tokenId =  Integer.parseInt((String)result.get("token_id"));
            if(userDao.isNFTExist(token_address, tokenId)==0){
                System.out.println("Not Existed NFT");
                String tokenUri = (String) result.get("token_uri");
                if(tokenUri.endsWith(".json")){
                    tokenUri = tokenUri.substring(0, tokenUri.length()-5);
                }
                String metaData = getNFT.getNftMetaData(tokenUri);
                JSONObject metaJsonObject = getNFT.fromJSONtoNFT(metaData);

                userDao.createNFT(result, metaJsonObject, chain);
            }
            if(userDao.isNFTWalletExist(token_address, tokenId, userWalletIndex)==0){
                System.out.println("Not Existed wallet");
                userDao.createNFTWallet(token_address, tokenId, userWalletIndex, Integer.parseInt((String) result.get("amount")));
            }
            System.out.println("Already exists");

        }
    }

    public void reduceRefreshNftCount(String userId){
        userDao.reduceRefreshNftCount(userId);
    }
}
