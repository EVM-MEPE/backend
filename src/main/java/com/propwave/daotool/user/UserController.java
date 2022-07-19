package com.propwave.daotool.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.propwave.daotool.commons.S3Uploader;
import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.config.BaseResponse;
import com.propwave.daotool.config.jwt.SecurityService;
import com.propwave.daotool.user.model.*;
import com.propwave.daotool.utils.GetNFT;
import com.propwave.daotool.wallet.model.UserWallet;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.profiles.Profile;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;

import static com.propwave.daotool.config.BaseResponseStatus.*;

@RestController
@CrossOrigin(origins="*")
public class UserController {
    final static String DEFAULT_USER_PROFILE_IMAGE = "https://daotool.s3.ap-northeast-2.amazonaws.com/static/user/d1b5e5d6-fc89-486b-99d6-b2a6894f9eafprofileimg-default.png";

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final S3Uploader s3Uploader;

    @Autowired
    private final UserProvider userProvider;
    @Autowired
    private final UserService userService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private final GetNFT getNFT;


    public UserController(GetNFT getNFT, S3Uploader s3Uploader, UserProvider userProvider, UserService userService, SecurityService securityService){
        this.s3Uploader = s3Uploader;
        this.userProvider = userProvider;
        this.userService = userService;
        this.securityService = securityService;
        this.getNFT = getNFT;
    }

    // ---------------------------------------------------- 로그인, 회원가입 ----------------------------------------------------

//    // 기존 회원가입 여부 확인
//    @PostMapping("/users/check")
//    public BaseResponse<Integer> checkUserSignupAlready(@RequestParam("walletAddress") String walletAddress) throws BaseException {
//            System.out.println("#01 - check signup api start");
//            int result = userProvider.checkUserSignupAlready(walletAddress);
//            return new BaseResponse<>(result);
//    }

    /**
     ******************************** 회원가입, 로그인 ********************************
     **/

    // 회원가입: id 중복 체크
    @GetMapping("/users/check")
    public BaseResponse<Integer> checkUserExist(@RequestParam("userID") String userID) throws BaseException {
        System.out.println("\n Check user Exist \n");
        int result = userProvider.checkUserIdExist(userID);
        return new BaseResponse<>(result);
    }

//    @GetMapping("users/create")
//    public BaseResponse<User> createUser(@RequestParam("userID") String userID) throws BaseException{
//        System.out.println("\n Create user\n");
//        User newUser = userService.createUser(userID);
//        return new BaseResponse<>(newUser);
//    }

    @PostMapping("users/create")
    public BaseResponse<Map<String, Object>> createUserWithAWallet(@RequestParam("userID") String userID, @RequestBody Map<String, String> req) throws BaseException{
        System.out.println("\n Create user with one wallet\n");
        Map<String, Object> newUser = userService.createUser(userID);

        userService.addWalletToUser(userID, req.get("walletAddress"), req.get("walletType"));
        return new BaseResponse<>(newUser);
    }


    @PostMapping("wallets/create")
    public BaseResponse<String> addWalletToUser(@RequestBody Map<String, String> req){
        System.out.println("\n Add Wallet \n");

        // check jwt token
        String jwtToken = req.get("jwtToken");
        String userID = req.get("userID");

        if(!isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        int result = userService.addWalletToUser(userID, req.get("walletAddress"), req.get("walletType"));
        if(result==-1){
            return new BaseResponse<>(WALLET_ALREADY_EXIST_TO_USER);
        }

        return new BaseResponse<>("successfully add wallet to user");
    }

    @PostMapping("wallets/delete")
    public BaseResponse<String> deleteWalletToUser(@RequestBody Map<String, String> req) throws BaseException {
        System.out.println("\n Delete Wallet \n");

        // check jwt token
        String jwtToken = req.get("jwtToken");
        String userID = req.get("userID");

        if(!isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        userService.deleteUserWallet(req.get("userID"), req.get("walletAddress"));
        return new BaseResponse<>("successfully delete wallet to user");
    }

    @GetMapping("users/login")
    public BaseResponse<Map<String, Object>> login(@RequestParam("userID") String userID) throws BaseException {
        System.out.println("\n Login \n");
        List<UserWalletAndInfo> userWalletList = userProvider.getAllUserWalletByUserId(userID);

        //User의 JWT 토큰 만들기
        String jwtToken = securityService.createToken(userID, (360*1000*60)); // 토큰 유효시간 6시간

        Map<String, Object> res = new HashMap<>();
        res.put("userWallets", userWalletList);
        res.put("userToken", jwtToken);

        return new BaseResponse<>(res);
    }

    @PatchMapping("users/profile")
    public BaseResponse<UserSocial> editProfile(@RequestParam(value= "userID") String userID,
                                            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                            @RequestParam(value = "backImage", required = false) MultipartFile backImage,
                                            @RequestParam(value = "json") String json) throws IOException, BaseException {
        System.out.println("\n edit Profile \n");

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new SimpleModule());
        Map<String, String> req = objectMapper.readValue(json, new TypeReference<>() {});

        // check jwt token
        String jwtToken = req.get("jwtToken");

        if(!isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        UserSocial userSocial = userService.editUserProfileAndSocial(userID, json);

        System.out.println("new version 1");

        try{
            if(!profileImage.isEmpty()){
                String profileImagePath = s3Uploader.upload(profileImage, "media/user/profileImage");
                System.out.println(profileImagePath);
                userService.editUserProfileImg(userID, profileImagePath);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println("Not changing profile Image");
        }

        try{
            if(!backImage.isEmpty()){
                String backImagePath = s3Uploader.upload(backImage, "media/user/backImage");
                System.out.println(backImagePath);
                userService.editUserBackImg(userID, backImagePath);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println("Not changing back Image");
        }
        return new BaseResponse<>(userSocial);
    }

    // 유저의 지갑을 모두 불러옴
    @GetMapping("wallets/users")
    public BaseResponse<List<String>> getUsersfromWallet(@RequestParam("walletAddress") String walletAddress) throws BaseException {
        System.out.println("\n Get users from wallet \n");
        if(userProvider.isWalletExist(walletAddress)==0){
            return new BaseResponse<>(NO_WALLET_EXIST);
        }
        else{
            List<String> users = userProvider.getAllUserByWallet(walletAddress);
            return new BaseResponse<>(users);
        }
    }

    /**
     ******************************** mypage ********************************
     **/
    @GetMapping("mypage")
    public BaseResponse<Map<String, Object>> getUserInfo(@RequestParam("userID") String userID) throws BaseException {
        User user = userProvider.getUser(userID);
        String profileImg = userProvider.getUserImagePath(userID);
        int friendCount = userProvider.getFriendsCount(userID);
        int followerCount = userProvider.getFollowerCount(userID);
        int followingCount = userProvider.getFollowingCount(userID);
        Social social = userProvider.getSocial(userID);
        List<UserWalletAndInfo> walletLists = userProvider.getAllUserWalletByUserId(userID);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new SimpleModule());
        Map<String, Object> userMap = objectMapper.convertValue(user, Map.class);
        Map<String, Object> socialMap = objectMapper.convertValue(social, Map.class);

        Timestamp userCreatedAt = user.getCreatedAt();
        userMap.replace("createdAt", userCreatedAt);


        Map<String, Object> result = new HashMap<>();
        result.put("user", userMap);
        result.put("friendCount", friendCount);
        result.put("followerCount", followerCount);
        result.put("followingCount", followingCount);
        result.put("social", socialMap);
        result.put("walletList", walletLists);
        result.put("profileImg", profileImg);

        userService.addHit(userID);

        return new BaseResponse<>(result);
    }

    @GetMapping("profileHistory")
    public BaseResponse<List<ProfileImg>> getProfileImgHistory(@RequestParam("userID") String userID) throws BaseException {
        List<ProfileImg> profileImgList = userProvider.getProfileImgHistory(userID);
        return new BaseResponse<>(profileImgList);
    }


    /**
     ******************************** friend ********************************
     **/

    @PostMapping("friends/request")
    public BaseResponse<String> requestFriend(@RequestParam("user") String reqTo, @RequestBody Map<String, String> json) throws BaseException {
        // check jwt token
        String jwtToken = json.get("jwtToken");
        String userID = json.get("reqFrom");

        if(!isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }
        userService.createFriendReq(reqTo, json.get("reqFrom"), json.get("reqNickname"));
        return new BaseResponse<>("successfully make friend request");
    }

    @PatchMapping("friends/request")
    public BaseResponse<String> friendRequestProcess(@RequestParam("accept") boolean isAccepted, @RequestBody Map<String, String> json) throws BaseException {
        // check jwt token
        String jwtToken = json.get("jwtToken");
        String userID = json.get("reqTo");

        if(!isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        userService.acceptFriend(isAccepted, json.get("reqTo"), json.get("reqFrom"), json.get("reqNickname"));
        return new BaseResponse<>("successfully process friend request");
    }

    @PatchMapping("friends/nickname")
    public BaseResponse<Friend> editFriendNickname(@RequestBody Map<String, String> json){
        // check jwt token
        String jwtToken = json.get("jwtToken");
        String userID = json.get("user");

        if(!isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        Friend newNickname = userService.editFriendNickname(json.get("user"), json.get("friend"), json.get("newNickname"));
        return new BaseResponse<>(newNickname);
    }

    @GetMapping("friends")
    public BaseResponse<List<Friend>> getAllFriendsList(@RequestParam("userID") String userId){
        List<Friend> friendList = userProvider.getAllFriends(userId);
        return new BaseResponse<>(friendList);
    }

    @GetMapping("friends/number")
    public BaseResponse<Integer> getFriendsCount(@RequestParam("userID") String userId){
        int friendsCount = userProvider.getFriendsCount(userId);
        return new BaseResponse<>(friendsCount);
    }

    @PostMapping("friends/request/status")
    public BaseResponse<String> getStatusOfFriendReq(@RequestParam("reqFrom") String reqFrom, @RequestParam("reqTo") String reqTo, @RequestBody Map<String, String> json) throws BaseException {
        // check jwt token
        String jwtToken = json.get("jwtToken");

        if(!isUserJwtTokenAvailable(jwtToken, reqFrom)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }
        String status = userProvider.getStatusOfFriendReq(reqFrom, reqTo);
        return new BaseResponse<>(status);
    }

    /**
    ******************************** nft ********************************
    **/

//    @GetMapping("mypage/refreshCollections")
//    public BaseResponse<Map<Object, Object>> getCollectionsRefresh(@RequestParam("userID") String userId) throws ParseException, BaseException {
//        //1. refresh가 0번 이상 남았는지 확인하기
//        int refreshLeft = userProvider.getRefreshLeft(userId);
//        if(refreshLeft<=0){
//            return new BaseResponse<>(NO_REFRESH_LEFT);
//        }
//
//        // 2. 이 인간의 모든 지갑 불러오기
//        List<UserWalletAndInfo> userWallets = userProvider.getAllUserWalletByUserId(userId);
//
//        //3. POAP 모두 불러오기
//        for(UserWalletAndInfo userWallet:userWallets) {
//            userService.getPoapRefresh(userWallet.getWalletAddress(), userWallet.getUser());
//        }
//
//        //4. NFT 모두 불러오기
//
//
//        //5. POAP, NFT 불러온거 가져오기
//        Map<Object, Object> result = new HashMap<>();
//        List<Nft> nftList = userProvider.getUserNfts(userId);
//        List<PoapWithDetails> poapList = userProvider.getUserPoaps(userId);
//        result.put("nftList", nftList);
//        result.put("poapList", poapList);
//
//        //5. 기타
//        userService.reduceRefreshNftCount(userId);
//
//        return new BaseResponse<>)(result);
//    }

    @GetMapping("nfts/refresh")
    public BaseResponse<String> getNftRefresh(@RequestParam("userId") String userId) throws BaseException, ParseException {
        //1. 이 인간의 Dashboard 지갑 다불러오기
        List<UserWalletAndInfo> userWallets =  userProvider.getAllUserWalletByUserId(userId);
        for(UserWalletAndInfo userWallet:userWallets){
            String walletAddress = userWallet.getWalletAddress();

            String api_chain = "polygon";
            String chain = "Polygon";
            userService.getNFTRefresh(walletAddress, api_chain, chain, userWallet.getIndex());

            api_chain = "eth";
            chain = "Ethereum";
            userService.getNFTRefresh(walletAddress, api_chain, chain, userWallet.getIndex());

            api_chain = "avalanche";
            chain = "Avalanche";
            userService.getNFTRefresh(walletAddress, api_chain, chain, userWallet.getIndex());

            userService.reduceRefreshNftCount(userId);
        }
        return new BaseResponse<>("refresh success");
    }


    @GetMapping("nfts")
    public BaseResponse<List<NftForDashboard>> getMyNfts(@RequestParam("userId") String userId){
        // 유저의 모든 nftWallet 불러오기
        List<NftForDashboard> nftForDashboardList = userProvider.getNftDashboardInfoByUserId(userId);
        return new BaseResponse<>(nftForDashboardList);
    }

    @GetMapping("nfts/refreshLeft")
    public BaseResponse<Integer> getNftRefreshLeft(@RequestParam("userId") String userId) throws BaseException {
        int nftRefreshLeft = userProvider.getRefreshLeft(userId);
        return new BaseResponse<>(nftRefreshLeft);
    }

    /**
     ******************************** follow ********************************
     **/

    @PostMapping("following/request")
    public BaseResponse<String> requestFollow(@RequestParam("userID") String reqTo, @RequestBody Map<String, String> json) throws BaseException {
        // check jwt token
        String jwtToken = json.get("jwtToken");
        String reqFrom = json.get("reqFrom");

        if(!isUserJwtTokenAvailable(jwtToken, reqFrom)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        int result = userService.createFollow(reqTo, json.get("reqFrom"));
        if(result==-2){
            return new BaseResponse<>(FOLLOW_ALREADY_EXIST);
        }
        return new BaseResponse<>("successfully follow " + reqTo);
    }

    @PostMapping("following/delete")
    public BaseResponse<String> deleteFollow(@RequestParam("userID") String reqTo, @RequestBody Map<String, String> json) throws BaseException {
        // check jwt token
        String jwtToken = json.get("jwtToken");
        String reqFrom = json.get("reqFrom");

        if(!isUserJwtTokenAvailable(jwtToken, reqFrom)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        userService.deleteFollow(reqTo, json.get("reqFrom"));
        return new BaseResponse<>("successfully delete follow " + reqTo);
    }

    @GetMapping("following/following")
    public BaseResponse<List<User>> getFollowingList(@RequestParam("userID") String userID){
        List<User> followingList= userProvider.getFollowingList(userID);
        return new BaseResponse<>(followingList);
    }

    @GetMapping("following/follower")
    public BaseResponse<List<User>> getFollowerList(@RequestParam("userID") String userID){
        List<User> followerList= userProvider.getFollowerList(userID);
        return new BaseResponse<>(followerList);
    }

    @GetMapping("following")
    public BaseResponse<Boolean> isFollowing(@RequestParam("userID1") String userID1, @RequestParam("userID2") String userID2){
        int isFollowing = userProvider.isFollowing(userID1, userID2);
        boolean result;
        result = isFollowing == 1;
        return new BaseResponse<>(result);
    }

//    // 회원가입 -> 사용자 정보 생성하기
//    @PostMapping("/users/signup/user")
//    public BaseResponse<Map<String, Object>> UserSignUp(@RequestParam("profileImage") MultipartFile profileImage, @RequestParam("json") String json) throws BaseException, JsonProcessingException {
//        System.out.println("#02-1 - signup userCreate api start");
//
//        // 받은 내용 user로 object 변형하기
//        ObjectMapper objectMapper = new ObjectMapper().registerModule(new SimpleModule());
//        UserSignupReq userSignupReq = objectMapper.readValue(json, new TypeReference<>() {});
//
//        //1) User 만들기
//        // 이미 있는 id 인지 확인하기
//        if(userProvider.checkUserIdExist((userSignupReq.getId())) == 1){
//            return new BaseResponse<>(USER_ID_ALREADY_EXIST);
//        }
//        try{
//            //1. user 만들기
//            // image S3에 올리기 -> 일단 예시로 한것임...! (test용)
//            // image upload 하기
//            String imagePath = s3Uploader.upload(profileImage, "media/user/profileImage");
//            System.out.println(imagePath);
//            User newUser = userService.createUser(userSignupReq, imagePath);
//
//            //newUser의 JWT 토큰 만들기
//            String token = securityService.createToken(newUser.getId(), (120*1000*60)); // 토큰 유효시간 2시간
//            Map<String, Object> response = new LinkedHashMap<>();
//            response.put("id", newUser.getId());
//            response.put("token", token);
//            response.put("profileImageUrl", newUser.getProfileImage());
//            response.put("introduction", newUser.getIntroduction());
//            response.put("url", newUser.getUrl());
//            return new BaseResponse<>(response);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return new BaseResponse<>(S3_UPLOAD_ERROR);
//        } catch (BaseException e) {
//            e.printStackTrace();
//            return new BaseResponse<>(e.getStatus());
//        }
//    }
//
//    //회원가입 시 지갑 등록
//    @PostMapping(value = "/users/signup/wallets")
//    public BaseResponse<List<String>> UserSignUpWallet(@RequestBody Object json) throws BaseException, IOException {
//        System.out.println("#02-3 - signup create wallets api start");
//        Map<String, Object> json_map = (Map<String, Object>) json;
//        String userId = (String) json_map.get("user");
//        List<Map<String, Object>> wallets = (List<Map<String, Object>>) json_map.get("wallets");
//        List<String> successWallets = userService.addWalletWhenSignUp(userId, wallets);
//        return new BaseResponse<>(successWallets);
//    }
//
//    //유저 삭제
//    @DeleteMapping(value = "/users")
//    public BaseResponse<String> userDelete(@RequestBody Object json) throws BaseException {
//        System.out.println("user delete api start");
//        Map<String, String> json2 = (Map<String, String>) json;
//        String userId = json2.get("user");
//        String token = json2.get("token");
//
//        // TOken 확인
//        String subject = securityService.getSubject(token);
//        System.out.println("token 확인");
//
//        if(!subject.equals(userId)){
//            return new BaseResponse<>(USER_TOKEN_WRONG);
//        }
//
//        try{
//           userService.deleteUser(userId);
//           return new BaseResponse<>("user delete success");
//        } catch (BaseException e) {
//            e.printStackTrace();
//            return new BaseResponse<>(e.getStatus());
//        }
//    }
//
//    @PostMapping("/users/login")
//    public BaseResponse<Map<String, Object>> userLogin(@RequestBody Map<String, String> walletAddress) throws BaseException, JsonProcessingException {
//        System.out.println("#03 - login api start");
//        // 유저가 로그인하면 줄거? token, user 모든 정보, 연결된 지갑들의 모든 뱃지
//        // 1. 유저 정보
//        // 해당 주소에 연결된 user의 모든 정보 가져오기
//
//        List<UserWallet> userWalletList = userProvider.getAllUserWalletByWallet(walletAddress.get("walletAddress"));
//        String userId = null;
//        //User user = null;
//        for (UserWallet userWallet : userWalletList) {
//            if (userWallet.isLoginAvailable()) {
//                userId = userWallet.getUser();
//                break;
//            }
//        }
//        // 해당 user의 모든 지갑 정보 가져오기
//        List<Map<String, Object>> userWalletListByUser = userProvider.getAllUserWalletByUserId(userId);
//
//        String token = securityService.createToken(userId, (120*1000*60*3)); // 토큰 유효시간 6시간
//
//        Map<String, Object> result = new LinkedHashMap<>();
//        //result.put("user", user);
//        result.put("token", token);
//        //result.put("wallets", userWalletListByUser);
//        //result.put("badges", newAllBadge);
//
//        result.put("userID", userId);
//        result.put("walletAddress", walletAddress.get("walletAddress"));
//
//        return new BaseResponse<>(result);
//    }
//
//    @GetMapping("/users/mypage")
//    public BaseResponse<Map<String, Object>> userMypage(@RequestParam String userId) throws BaseException {
//        System.out.println("#04 - get mypage api start");
//        // 유저가 로그인하면 줄거? token, user 모든 정보, 연결된 지갑들의 모든 뱃지
//        // 1. 유저 정보
//        userService.addHit(userId);
//        User user = userProvider.getUser(userId);
//
//        // 해당 주소에 연결된 user의 모든 정보 가져오기
//        List<Map<String, Object>> userWalletList = userProvider.getAllUserWalletByUserId(userId);
//
//        // 유저의 viewDataAvailable이 true인 친구들의 뱃지 데려오기... 힘들다 힘들어
//        List<Map<String, Object>> badges = new ArrayList<>();
//        List<Map<String, Object>> badgeTmp = new ArrayList<>();
//        for (Map<String, Object> userWallet : userWalletList) {
//            System.out.println(userWallet);
//            System.out.println(userWallet.get("loginAvailable"));
//            if ((boolean)userWallet.get("viewDataAvailable")) {
//                Map<String, String> walletInfo = (Map<String, String>) userWallet.get("walletAddress");
//                badgeTmp = userProvider.getAllBadge((String)walletInfo.get("address"));
//                badges.addAll(badgeTmp);
//            }
//        }
//        // 뱃지 중복 제거
//        HashSet<Map<String, Object>> set = new HashSet<Map<String, Object>>(badges);
//        List<Map<String, Object>> newAllBadge = new ArrayList<Map<String, Object>>(set);
//
//        Map<String, Object> result = new LinkedHashMap<>();
//        result.put("user", user);
//        result.put("wallets", userWalletList);
//        result.put("badges", newAllBadge);
//
//        return new BaseResponse<>(result);
//    }
//
//    @PatchMapping("/users/mypage")
//    public BaseResponse<Map<String, String>> editUserProfile(@RequestParam("profileImageChanged") int profileImageChanged, @RequestParam(value = "newProfileImage", required = false) MultipartFile profileImage, @RequestParam("json") String json) throws BaseException, IOException {
//        System.out.println("#05 - mypage update api start");
//
//        ObjectMapper objectMapper = new ObjectMapper().registerModule(new SimpleModule());
//        Map<String,Object> request = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
//        System.out.println(request);
//
//        //1. 토큰 검증
//        String token = (String) request.get("userToken");
//        String subject = securityService.getSubject(token);
//        System.out.println(subject);
//        Map<String, String> userInfo = (Map<String, String>) request.get("userInfo");
//        if(!subject.equals(userInfo.get("preId"))){
//            return new BaseResponse<>(USER_TOKEN_WRONG);
//        }
//        //2. 사용자 정보 수정하기 -> 사용자 이미지가 default일 수도 있음.. 잘 띵킹
//        // 사용자의 이름이 변경된 경우, 변경된 이름이 이미 있는건지 확인!
//        if(!userInfo.get("preId").equals(userInfo.get("changedId"))){
//            if(userProvider.checkUserIdExist(userInfo.get("changedId")) == 1){ //
//                return new BaseResponse<>(USER_ID_ALREADY_EXIST);
//            }
//        }
//        String newToken = securityService.createToken(userInfo.get("changedId"), (120*1000*60));
//        //2-1. 이미가 변경되었다면 이미지 올리기
//        //2-1-1. 이미지 변경 여부 확인
//        String newImagePath;
//        if(profileImageChanged == 0){
//            newImagePath = userProvider.getUserImagePath(userInfo.get("changedId"));
//        }
//        else if(profileImage.getSize()==0){  // 프로필 안변함
//            newImagePath = DEFAULT_USER_PROFILE_IMAGE;
//        }
//        else{
//            newImagePath = s3Uploader.upload(profileImage, "media/user/profileImage");
//        }
//        User newUser = userService.editUser(userInfo, newImagePath);
//
//        //3. 지갑 처리하기
//        List<Map<String, Object>> wallets = (List<Map<String, Object>>) request.get("wallets");
//        for(Map<String, Object> wallet:wallets){
//            System.out.println(wallet.get("walletAddress"));
//            System.out.println(wallet.get("walletName"));
//            if(wallet.get("request").equals("add")){
//                System.out.println("대시보드용 지갑 추가");
//                String walletType = (String) wallet.get("walletType");
//                wallet.remove("request");
//                userService.createDashboardWallet(newUser.getId(), wallet);
//            }
//            else if(wallet.get("request").equals("patch")){
//                System.out.println("대시보드용 지갑 수정");
//                wallet.remove("request");
//                userService.updateDashboardWallet(newUser.getId(), wallet);
//            }
//            else if(wallet.get("request").equals("delete")){
//                System.out.println("대시보드용 지갑 삭제");
//                wallet.remove("request");
//                userService.deleteDashboardWallet(newUser.getId(), wallet);
//            }
//        }
//        Map<String, String> response = new HashMap<>();
//        response.put("token", newToken);
//        return new BaseResponse<>(response);
//    }
//
//    // ---------------------------------------------------- badges ----------------------------------------------------
//
//    //사용자가 가진 뱃지 불러오기
//    @GetMapping("/users/badges")
//    public BaseResponse<List<Map<String, Object>>> getBadges(@RequestParam("userId") String userId) throws BaseException
//    {
//        System.out.println("#06 - get user badges api start");
//        if(userProvider.checkUser(userId)==0){
//            return new BaseResponse<>(USER_NOT_EXISTS);
//        }
//
//        // 해당 user의 모든 지갑 정보 가져오기
//        List<Map<String, Object>> userWalletListByUser = userProvider.getAllUserWalletByUserId(userId);
//
//        //List<GetBadgesRes> getBadgesResList = new ArrayList<>();
//        List<Map<String, Object>> getBadgesResList = new ArrayList<>();
//        for (Map<String, Object> userWallet : userWalletListByUser) {
//            System.out.println(userWallet);
//            if ((boolean)userWallet.get("viewDataAvailable")) {
//                Map<String, String> wallet = (Map<String, String>) userWallet.get("walletAddress");
//                getBadgesResList.addAll(userProvider.getAllBadge(wallet.get("address")));
//            }
//        }
//        return new BaseResponse<>(getBadgesResList);
//    }
//
//    @GetMapping("/badges")
//    public BaseResponse<Map<String, Object>>retrieveBadge(@RequestParam("badgeName") String badgeName) throws BaseException {
//        System.out.println("get badge details api start");
//        if(!userProvider.checkBadge(badgeName)){
//            return new BaseResponse<>(NO_BADGE_EXIST);
//        }
//
//        // badge
//        Map<String, Object> result = userProvider.getBadgeInfo(badgeName);
//        return new BaseResponse<>(result);
//    }
//
//
//    //------------------------------- wallets
//
//    //login용 지갑 추가
//    @PostMapping("/users/wallets/login")
//    public BaseResponse<String> addLoginWallet(@RequestBody Map<String, Object> request){
//        System.out.println("#06 - get user badges api start");
//        //1. 사용자 정보 받아서 1) 지갑 만들고, 2) UserWallet 만들고.
//        try{
//            String newWalletAddress = (String) request.get("address");
//            // 경우 1. 지갑이 아예 없었던 새로인 친구인 경우
//            if (userProvider.isWalletExist(newWalletAddress)==0) {
//                //없으면 객체 만들기 -> 경우 1. 완전 처음 들어오는 지갑인 경우
//                String walletType = (String) request.get("walletType");
//                userService.createWallet(newWalletAddress, walletType);
//                //2. userWallet 만들기
//                request.put("walletAddress", newWalletAddress);
//                request.put("walletName", "");
//                request.put("loginAvailable", true);
//                request.put("viewDataAvailable", false);
//                userService.createUserWallet(request, (String) request.get("user"));
//                return new BaseResponse<>("로그인 지갑이 추가되었습니다.");
//            }
//            else{
//                System.out.println("지갑 있음!");
//                String user = (String)request.get("user");
//                // 경우 4. 지갑이 이미 있고, 다른 유저의 로그인용으로 있는 경우
//                if(userProvider.isWalletExistForLoginNotMe(newWalletAddress, user)==1){
//                    System.out.println("경우4");
//                    return new BaseResponse<>(WALLET_ALREADY_EXIST_FOR_LOGIN);
//                }
//                // 경우 2. 지갑자체는 이미 있고, 이 유저한테는 지갑이 없는 경우
//                if(userProvider.isUserWalletByWalletAddressAndUserIdExist(user, newWalletAddress)==0){
//                    //2. userWallet 만들기
//                    System.out.println("경우2");
//                    WalletInfo walletInfo = userProvider.getWallet(newWalletAddress);
//                    request.put("walletAddress", newWalletAddress);
//                    request.put("walletName", "");
//                    request.put("loginAvailable", true);
//                    request.put("viewDataAvailable", false);
//                    //request.put("")
//                    userService.createUserWallet(request, (String) request.get("user"));
//                    return new BaseResponse<>("로그인 지갑이 추가되었습니다.");
//                }
//                // 경우 3. 지갑은 이미 있고, 이 유저한테 지갑이 대시보드용으로 있는 경우
//                UserWallet userWallet = userProvider.getUserWalletByWalletAddressAndUserId(user, newWalletAddress);
//                if(!userWallet.isLoginAvailable() && userWallet.isViewDataAvailable()){
//                    System.out.println("경우3");
//                    userService.makeLoginAvailable(userWallet.getIndex());
//                    return new BaseResponse<>("로그인 지갑이 추가되었습니다.");
//                }
//                System.out.println("경우 없는 경우...");
//                return new BaseResponse<>(RESPONSE_ERROR);
//            }
//        } catch (BaseException e) {
//                return new BaseResponse<>((e.getStatus()));
//        }
//    }
//
//    //login용 지갑 삭제
//    @DeleteMapping("/users/wallets/login")
//    public BaseResponse<String> deleteLoginWallet(@RequestBody Map<String, String> request) throws BaseException {
//        System.out.println("#07 - delete wallet badges api start");
//        System.out.println("지갑 삭제 고");
//        String userId = request.get("userId");
//        String walletAddress = request.get("walletAddress");
//        // 1. 지갑이 남에게 있는지 여부
//        int isWalletSomeoneElse = userProvider.isWalletSomeoneElse(userId, walletAddress);
//        System.out.println(isWalletSomeoneElse);
//        // 2. 나에게 대시보드 용도 있는지 여부
//        int isWalletMyDashboard = userProvider.isWalletMyDashboard(userId, walletAddress);
//        System.out.println(isWalletMyDashboard);
//
//        // 상황 1. 나에게만 지갑이 있고 Only 로그인용
//        if (isWalletSomeoneElse==0 && isWalletMyDashboard == 0){
//            // userWallet 삭제, wallet 삭제
//            System.out.println("상황 1. 나에게만 지갑이 있고 Only 로그인용");
//            userService.deleteUserWallet(userId, walletAddress);
//            userService.deleteWallet(walletAddress);
//            return new BaseResponse<>("상황1) 나에게만 지갑이 있고 Only 로그인용 -> userWallet 삭제, wallet 삭제");
//        }
//        // 상황 2. 나에게만 지갑이 있고, 대시보드용도 있음 & 상황 4. 남에게 지갑이 있고, 대시보드용도 있음
//        else if (isWalletMyDashboard == 1){
//            // userWallet의 login을 0으로 변경
//            System.out.println("상황 2. 나에게만 지갑이 있고, 대시보드용도 있음 & 상황 4. 남에게 지갑이 있고, 대시보드용도 있음");
//            userService.makeLoginUnavailable(userId, walletAddress);
//            return new BaseResponse<>("상황2, 4) 대시보드용 있음 -> userWallet의 login을 0으로 변경");
//        }
//        // 상황 3. 남에게 지갑이 있고, 나에게 Only 로그인용
//        else if (isWalletSomeoneElse==1 && isWalletMyDashboard == 0){
//            System.out.println("상황 3. 남에게 지갑이 있고, 나에게 Only 로그인용");
//            // 내 userWallet 삭제
//            userService.deleteUserWallet(userId, walletAddress);
//            return new BaseResponse<>("상황3) 남에게 지갑이 있고, 나에게 Only 로그인용 -> 내 userWallet 삭제");
//        }
//        // 그 외
//        else {
//            System.out.println("경우 없는 경우...");
//            return new BaseResponse<>(RESPONSE_ERROR);
//        }
//
//    }
//
//    //badge 신청하기
//    @PostMapping("/admin/badges")
//    public BaseResponse<BadgeRequest> getBadgeRequest(@RequestParam("badgeName") String badgeName, @RequestBody Map<String, String> request){
//        System.out.println("#08 - apply badge api start");
//        BadgeRequest adminRequest = userService.createBadgeRequest(badgeName, request);
//        return new BaseResponse<>(adminRequest);
//    }
//
//    @GetMapping("/admin/badges")
//    public BaseResponse<List<BadgeRequest>> getAllBadgeRequest(@RequestParam String password) throws BaseException {
//        System.out.println("#09 - admin badge request api start");
//        if(!password.equals(ADMIN_PASSWORD)){
//            return new BaseResponse<>(PASSWORD_WRONG);
//        }
//        List<BadgeRequest> badgeRequests = userProvider.getAllBadgeRequest();
//        return new BaseResponse<>(badgeRequests);
//    }
//
//    @PostMapping("/admin/badge")
//    public BaseResponse<BadgeRequest> processBadgeRequest(@RequestParam("index") int index, @RequestBody Map<String, String> request) throws BaseException {
//        System.out.println("#10 - admin give badge api start");
//        String password = request.get("password");
//        if(!password.equals(ADMIN_PASSWORD)){
//            return new BaseResponse<>(PASSWORD_WRONG);
//        }
//        BadgeRequest badgeRequest = userService.processBadgeRequest(index);
//        return new BaseResponse<>(badgeRequest);
//    }
//
//    //------------------
//    // 이미지 올리기 - 로컬에서!
//    public String S3ImageUploadAtLocal(String imagePath, String S3DirPath) throws IOException {
//        System.out.println("받은 이미지 경로: "+ imagePath);
//        File file = new File(imagePath);
//        FileInputStream input = new FileInputStream(file);
//        MultipartFile multipartFile = new MockMultipartFile("file",
//                file.getName(),
//                "text/plain",
//                IOUtils.toByteArray(input));
//
//        return s3Uploader.upload(multipartFile, S3DirPath);
//    }
//
//    //------- test
//    @PostMapping("/images/upload")
//    public BaseResponse<String> uplaodImage(@RequestParam("profileImage") MultipartFile multipartFile ) throws IOException {
//        String imgUrl = s3Uploader.upload(multipartFile, "media/user/profileImage");
//        return new BaseResponse<>(imgUrl);
//    }
//
//


//
//
//    //public hideBadge
    public boolean isUserJwtTokenAvailable(String jwtToken, String userID){
        // check jwt token
        String subject;
        try{
            subject = securityService.getSubject(jwtToken);
        } catch(Exception e){
            return false;
        }

        return subject.equals(userID);
    }
}


