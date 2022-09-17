package com.propwave.daotool.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.propwave.daotool.Friend.FriendDao;
import com.propwave.daotool.Friend.model.Follow;
import com.propwave.daotool.Friend.model.Friend;
import com.propwave.daotool.Friend.model.FriendReq;
import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.config.jwt.SecurityService;
import com.propwave.daotool.user.model.*;
import com.propwave.daotool.utils.GetNFT;
import com.propwave.daotool.utils.GetPOAP;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

import static com.propwave.daotool.config.BaseResponseStatus.*;

@Service
public class UserService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserDao userDao;
    private final UserProvider userProvider;
    private final FriendDao friendDao;
    @Autowired
    private final GetNFT getNFT;
    @Autowired
    private final GetPOAP getPOAP;
    private final SecurityService securityService;

    public UserService(GetNFT getNFT, UserDao userDao, FriendDao friendDao, UserProvider userProvider, SecurityService securityService, GetPOAP getPOAP){
        this.userDao = userDao;
        this.userProvider = userProvider;
        this.friendDao = friendDao;
        this.getNFT = getNFT;
        this.getPOAP = getPOAP;
        this.securityService = securityService;
    }

    public Map<String, Object> createUser(String userID) {
        //newUser의 JWT 토큰 만들기
        String jwtToken = securityService.createToken(userID, (360*1000*60)); // 토큰 유효시간 6시간
        User newUser = userDao.createUser(userID);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new SimpleModule());
        Map<String, Object> res = objectMapper.convertValue(newUser, Map.class);

        Timestamp userCreatedAt = newUser.getCreatedAt();
        res.replace("createdAt", userCreatedAt);

        res.put("jwtToken", jwtToken);

        return res;
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

            return new UserSocial(changedUser.getId(), changedUser.getIntroduction(), changedUser.getUrl(), changedUser.getHits(), changedUser.getTodayHits(), changedUser.getCreatedAt(), changedUser.getNftRefreshLeft(), changedUser.getBackImage(), changedUser.getNickname(), changedUser.getIndex(),
                    changedSocial.getTwitter(), changedSocial.getFacebook(), changedSocial.getDiscord(), changedSocial.getLink());
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public int deleteProfileImgHistory(String userID, int profileIndex){
        return userDao.deleteProfileImgHistory(userID, profileIndex);
    }

    public int hideProfileImgHistory(String userID, int profileIndex, boolean hide){
        return userDao.hideProfileImgHistory(userID, profileIndex, hide);
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

//    public int createFriendReq(String reqTo, String reqFrom, String reqNickname) throws BaseException {
//        try{
//            // make friendReq record
//            return userDao.createFriendReq(reqTo, reqFrom, reqNickname);
//
//            // make alarm to reqTo
//            //userDao.createAlarm();
//        }catch(Exception exception){
//            throw new BaseException(DATABASE_ERROR);
//        }
//    }

//    public int acceptFriend(boolean accepted, String reqTo, String reqFrom, String toNickname) throws BaseException {
//        try{
//            if(accepted){
//                //1. friend Req accept로 바꾸기
//                userDao.updateFriendReq(reqTo, reqFrom);
//                //2. friend record 만들기
//                String fromNickname = userDao.getFriendReqNickname(reqFrom, reqTo);
//                userDao.createFriend(reqTo, reqFrom, toNickname);   // to에게 from이라는 친구가 to Nickname 이라는 이름으로 생김
//                return userDao.createFriend(reqFrom, reqTo, fromNickname);
//                //3. 알람 만들기
//            } else{
//                return userDao.deleteFriendReq(reqFrom, reqTo);
//            }
//        }catch(Exception exception) {
//            throw new BaseException(DATABASE_ERROR);
//        }
//    }
//
//    public Friend editFriendNickname(String user, String friend, String newNickname){
//        return userDao.editFriendNickname(user, friend, newNickname);
//    }
//
//    public int createFollow(String reqTo, String reqFrom){
//        int checkFollowExist = userDao.isFollowExist(reqTo, reqFrom);
//        if(checkFollowExist == 1){
//            return -2;
//        }
//        return userDao.createFollow(reqTo, reqFrom);
//    }
//
//    public int deleteFollow(String reqTo, String reqFrom){
//        return userDao.deleteFollow(reqTo, reqFrom);
//    }

    public int createNotification(String userID, int type, int... optionIdx){
        // type: 1 - welcome, 2 - friend req, 3 - friend ok, 4 - comment, 5 - follow
        String message;
        switch(type){
            case 1: message = "Welcome! You are a member of MEPE from now on.";
                    System.out.println("1 notification");
                    return userDao.createNotification(userID, type, message);
            case 2: FriendReq friendReq = friendDao.getFriendReq(optionIdx[0]);
                    message = "A friend request came from "+ friendReq.getReqFrom() + ". Accept the friend request and check out the nickname your friend gave you.";
                    return userDao.createNotification(userID, type, message, optionIdx);
            case 3: Friend friend = friendDao.getFriend(optionIdx[0]);
                    message = friend.getFriend()+" accepted your friend request. check out the nickname your friend gave you.";
                    return userDao.createNotification(friend.getUser(), type, message, optionIdx);
            case 4: Comment comment = userDao.getComment(optionIdx[0]);
                    message = comment.getCommentFrom() + " left a comment for you. Check out the comments your friend wrote to you.";
                    return userDao.createNotification(comment.getCommentTo(), type, message, optionIdx);
            case 5:
                    Follow follow = friendDao.getFollow(optionIdx[0]);
                    message = follow.getUser() + " starts following you.";
                    return userDao.createNotification(userID, type, message, optionIdx);
            default:
                    break;
        }
        return -1;
    }

    public int checkNotification(int index){
        return userDao.checkNotification(index);
    }

    public int deleteANotification(int notiID) {
        return userDao.deleteANotification(notiID);
    }

    public int deleteAllNotification(String userID){
        return userDao.deleteAllNotification(userID);
    }

    // ----------------------------------------------

    public void addHit(String userId) throws BaseException{
        try{
            userDao.addHit(userId);
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

    public void getNFTRefresh(String walletAddress, String api_chain, String chain, int userWalletIndex) throws ParseException {
        String nftResult = getNFT.getEthNft(api_chain, walletAddress);
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
                Map<String, String> hashMap = new HashMap();
                String metaData = getNFT.externalGetAPIInterface(tokenUri, "", hashMap);
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

//    public void getPoapRefresh(String walletAddress, String userID) throws ParseException {
//        // POAP 가져오기
//        String poapResult = getPOAP.getPOAP(walletAddress);
//        JSONArray jsonList = getPOAP.fromJSONtoPOAPList(poapResult);
//
//        // DB에 있는 내 POAP 가져오기
//        List<PoapWithDetails> oldPoapList = userProvider.getUserPoaps(userID);
//        List<Integer> oldTokenIDList = new ArrayList<>();
//        oldPoapList.forEach((k) -> {
//            oldTokenIDList.add(k.getToken_id());
//        });
//
//
//        // DB에 있는 모든 POAP
//        List<Poap> allPoaps = userProvider.getAllPoaps();
//        List<Integer> allEventIdList = new ArrayList<>();
//        allPoaps.forEach((k) -> {
//            allEventIdList.add(k.getEvent_id());
//        });
//
//        // 이미 있는 포압 -> 패스, 없어진 포압 -> 디비 수정 및 삭제, 추가된 포압 -> 디비 추가 및 생성
//        for(Object json:jsonList){
//            JSONObject jsonObject = (JSONObject) json;
//            // 1. 이미 있는 경우 -> list에서 삭제
//            int new_token_id = (int) jsonObject.get("tokenId");
//            if(oldTokenIDList.contains(new_token_id)){
//                oldPoapList.forEach((k) -> {
//                    if(k.getToken_id() == new_token_id){
//                        oldPoapList.remove(k);
//                    }
//                });
//            }
//            else{
//                // 새로운 poap
//                // 1. pOap 자체의 존재 여부 확인 & 생성
//                Map<Object, Object> event = (Map) jsonObject.get("event");
//
//                if(!allEventIdList.contains((int)event.get("id"))){
//                    userDao.createPoap(event);
//                }
//                // 2. poapWallet 생성
//                if(jsonObject.containsKey("migrated")){
//                    userDao.createPoapWallet((int)event.get("id"), (int)jsonObject.get("tokenId"), walletAddress, (Timestamp)jsonObject.get("created"), (Timestamp)jsonObject.get("modified"));
//                }else{
//                    userDao.createPoapWallet((int)event.get("id"), (int)jsonObject.get("tokenId"), walletAddress, (Timestamp)jsonObject.get("created"));
//                }
//                // 3. uaserWalletPoap 생성
//
//            }
//        }
//        // 이미 있는 NFT인지 확인하기
//        System.out.println("total NFT : "+jsonObject.get("total"));
//        List<JSONObject> results = (List<JSONObject>) jsonObject.get("result");
//        int newNffIdx;
//
//        for(JSONObject result: results){
//            String token_address = (String) result.get("token_address");
//            int tokenId =  Integer.parseInt((String)result.get("token_id"));
//            if(userDao.isNFTExist(token_address, tokenId)==0){
//                System.out.println("Not Existed NFT");
//                String tokenUri = (String) result.get("token_uri");
//                if(tokenUri.endsWith(".json")){
//                    tokenUri = tokenUri.substring(0, tokenUri.length()-5);
//                }
//                String metaData = getNFT.getNftMetaData(tokenUri);
//                JSONObject metaJsonObject = getNFT.fromJSONtoNFT(metaData);
//
//                userDao.createNFT(result, metaJsonObject, chain);
//            }
//            if(userDao.isNFTWalletExist(token_address, tokenId, userWalletIndex)==0){
//                System.out.println("Not Existed wallet");
//                userDao.createNFTWallet(token_address, tokenId, userWalletIndex, Integer.parseInt((String) result.get("amount")));
//            }
//            System.out.println("Already exists");
//
//        }
//    }


    public void reduceRefreshNftCount(String userId){
        userDao.reduceRefreshNftCount(userId);
    }

    public List<Map<String, Object>> getPoapMypageWithNoDB(String userId) throws ParseException, BaseException {
        // user의 모든 지갑 불러오기
        List<UserWalletAndInfo> userWalletAndInfos = userProvider.getAllUserWalletByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        int our_event_id = 57439;
        List<Map<String, Object>> ourPoapList = new ArrayList<>();

        for(UserWalletAndInfo userWallet: userWalletAndInfos){
            // POAP 가져오기
            String walletAddress = userWallet.getWalletAddress();
            String poapResult = getPOAP.getPOAP(walletAddress);
            if(poapResult.equals("")){
                continue;
            }
            JSONArray jsonList = getPOAP.fromJSONtoPOAPList(poapResult);
            for(Object json:jsonList){
                JSONObject jsonObject = (JSONObject) json;
                Map<Object, Object> event = (Map) jsonObject.get("event");
                Map<String, Object> tmp = new HashMap<>();
                Long id = (Long)event.get("id");

                if(id.intValue() == our_event_id){
                    Map<String, Object> ourPOAP = new HashMap<>();
                    ourPOAP.put("event_id", our_event_id);
                    ourPOAP.put("image_url", event.get("image_url"));
                    ourPOAP.put("createdAt", jsonObject.get("created"));
                    ourPoapList.add(ourPOAP);
                    continue;
                }

                tmp.put("event_id", event.get("id"));
                tmp.put("image_url", event.get("image_url"));
                tmp.put("createdAt", jsonObject.get("created"));

                result.add(tmp);
            }

        }


        result.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return -(Timestamp.valueOf((String) o1.get("createdAt"))).compareTo(Timestamp.valueOf((String) o2.get("createdAt")));
            }
        });

        for(Map<String, Object> element:ourPoapList){
            result.add(0, element);
        }

        if(result.size()>8){
            result = result.subList(0,8);
        }

        return result;
    }

    public Map<String, Object> getNftMypageWithNoDB(String userId) throws ParseException, BaseException{
        System.out.println("get nft!!!");
        // user의 모든 지갑 불러오기
        List<UserWalletAndInfo> userWalletAndInfos = userProvider.getAllUserWalletByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> ethR = new ArrayList<>();
        List<Map<String, Object>> polyR = new ArrayList<>();
        List<Map<String, Object>> cosR = new ArrayList<>();
        List<Map<String, Object>> solR = new ArrayList<>();

        for(UserWalletAndInfo userWallet: userWalletAndInfos){
            // POAP 가져오기
            String walletAddress = userWallet.getWalletAddress();
            String wallet = userDao.getWalletChain(walletAddress);
            JSONArray jsonList = null;
            if(wallet.equals("Keplr")){
                jsonList = getNFT.getNFTs("stargaze", walletAddress);

                for(Object json:jsonList){
                    JSONObject jsonObject = (JSONObject) json;

                    Map<String, Object> tmp = new HashMap<>();
                    String imgUrl = checkImgUrl((String)jsonObject.get("image"));
                    tmp.put("image", imgUrl);
                    //tmp.put("image", jsonObject.get("image"));
                    tmp.put("title", jsonObject.get("name"));
                    tmp.put("obtainedAt", "none");
                    tmp.put("hidden", false);
                    cosR.add(tmp);
                }

            }else if(wallet.equals("Metamask")){
                JSONArray EjsonList = getNFT.getNFTs("eth", walletAddress);
                JSONArray PjsonList = getNFT.getNFTs("polygon", walletAddress);

                for(Object json:EjsonList){
                    JSONObject jsonObject = (JSONObject) json;
                    String token_uri = (String) jsonObject.get("token_uri");
                    Map<String, String> hashMap = new HashMap();
                    String metadata = getNFT.externalGetAPIInterface(token_uri, "", hashMap);
                    try{
                        if(metadata.isEmpty()){
                            continue;
                        }
                    }catch(NullPointerException e){
                        try{
                            metadata = (String) jsonObject.get("metadata");
                        }catch(NullPointerException e2){
                            continue;
                        }
                    }

                    Map<String, String> metadataJson = (Map) getNFT.fromJSONtoNFT(metadata);
                    Map<String, Object> tmp = new HashMap<>();
                    String imgUrl = checkImgUrl(metadataJson.get("image"));
                    tmp.put("image", imgUrl);
                    //tmp.put("image", metadataJson.get("image"));
                    tmp.put("title", metadataJson.get("name"));
                    tmp.put("obtainedAt", jsonObject.get("block_number"));
                    tmp.put("hidden", false);
                    ethR.add(tmp);
                }

                for(Object json:PjsonList){
                    JSONObject jsonObject = (JSONObject) json;
                    String token_uri = (String) jsonObject.get("token_uri");
                    Map<String, String> hashMap = new HashMap();
                    String metadata = getNFT.externalGetAPIInterface(token_uri, "", hashMap);

                    try{
                        if(metadata.isEmpty()){
                            continue;
                        }
                    }catch(NullPointerException e){
                        try{
                            metadata = (String) jsonObject.get("metadata");
                        }catch(NullPointerException e2){
                            continue;
                        }
                    }

                    Map<String, String> metadataJson = (Map) getNFT.fromJSONtoNFT(metadata);
                    Map<String, Object> tmp = new HashMap<>();
                    String imgUrl = checkImgUrl(metadataJson.get("image"));
                    tmp.put("image", imgUrl);
                    //tmp.put("image", metadataJson.get("image"));
                    tmp.put("title", metadataJson.get("name"));
                    tmp.put("obtainedAt", jsonObject.get("block_number"));
                    tmp.put("hidden", false);
                    polyR.add(tmp);
                }
            }else if(wallet.equals("Phantom")){
                jsonList = getNFT.getNFTs("solana", walletAddress);

                for(Object json:jsonList){
                    JSONObject jsonObject = (JSONObject) json;

                    Map<String, Object> tmp = new HashMap<>();
                    String imgUrl = checkImgUrl((String)jsonObject.get("image"));
                    tmp.put("image", imgUrl);
                    tmp.put("title", jsonObject.get("name"));
                    tmp.put("obtainedAt", "none");
                    tmp.put("hidden", false);
                    solR.add(tmp);
                }
            }
        }

        ethR.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return -(Float.compare(Float.parseFloat((String) o1.get("obtainedAt")),Float.parseFloat((String) o2.get("obtainedAt"))));
            }
        });

        polyR.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return -(Float.compare(Float.parseFloat((String) o1.get("obtainedAt")),Float.parseFloat((String) o2.get("obtainedAt"))));
            }
        });

        cosR.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return (((String) o1.get("title")).compareTo((String) o2.get("title")));
            }
        });

        solR.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return (((String) o1.get("title")).compareTo((String) o2.get("title")));
            }
        });

        if(ethR.size()>8){
            ethR = ethR.subList(0,8);
        }

        if(polyR.size()>8){
            polyR = polyR.subList(0,8);
        }

        result.put("ethereum", ethR);
        result.put("polygon", polyR);
        result.put("keplr", cosR);
        result.put("solana", solR);

        return result;
    }

    public String checkImgUrl(String imgUrl){
        if(imgUrl.startsWith("ipfs://")){
            System.out.println(imgUrl);
            imgUrl = "https://ipfs.io/ipfs/" + imgUrl.substring(7);
            System.out.println(imgUrl);
        }
        return imgUrl;
    }

//
//    public int addFollow(String reqTo){
//        return userDao.addFollow(reqTo);
//    }
//
//    public int reduceFollow(String reqTo){
//        return userDao.reduceFollow(reqTo);
//    }

    public int createComment(String userID, String friendID, String message){
        return userDao.createComment(userID, friendID, message);
    }

    public int hideComment(int commentIdx, String userID, boolean hide){
        return userDao.hideComment(commentIdx, userID, hide);
    }

    public int pinComment(int commentIdx, boolean pin){
        return userDao.pinComment(commentIdx, pin);
    }

}
