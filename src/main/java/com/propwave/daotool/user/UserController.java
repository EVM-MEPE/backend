package com.propwave.daotool.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.propwave.daotool.friend.FriendService;
import com.propwave.daotool.friend.model.Follow;
import com.propwave.daotool.friend.model.Friend;
import com.propwave.daotool.friend.model.FriendReq;
import com.propwave.daotool.commons.S3Uploader;
import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.config.BaseResponse;
import com.propwave.daotool.config.jwt.SecurityService;
import com.propwave.daotool.user.model.*;
import com.propwave.daotool.utils.Utils;
import com.propwave.daotool.wallet.WalletService;
import com.propwave.daotool.wallet.model.UserWalletAndInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

import static com.propwave.daotool.config.BaseResponseStatus.*;

@RestController
@CrossOrigin(origins="*")
public class UserController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final S3Uploader s3Uploader;

    private final UserProvider userProvider;
    private final UserService userService;
    private final FriendService friendService;
    private final WalletService walletService;
    private final Utils utils;
    private final SecurityService securityService;


    public UserController(Utils utils, S3Uploader s3Uploader, UserProvider userProvider, UserService userService, FriendService friendService, WalletService walletService, SecurityService securityService){
        this.s3Uploader = s3Uploader;
        this.userProvider = userProvider;
        this.userService = userService;
        this.friendService = friendService;
        this.walletService = walletService;
        this.securityService = securityService;
        this.utils = utils;
    }

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

    @PostMapping("users/create")
    public BaseResponse<Map<String, Object>> createUserWithAWallet(@RequestParam("userID") String userID, @RequestBody Map<String, String> req){
        System.out.println("\n Create user with one wallet\n");
        Map<String, Object> newUser = userService.createUser(userID);

        walletService.addWalletToUser(userID, req.get("walletAddress"), req.get("walletType"));
        userService.createNotification(userID, 1, -1);
        return new BaseResponse<>(newUser);
    }

    @GetMapping("users/login")
    public BaseResponse<Map<String, Object>> login(@RequestParam("userID") String userID) throws BaseException {
        System.out.println("\n Login \n");
        List<UserWalletAndInfo> userWalletList = walletService.getAllUserWalletByUserId(userID);

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

        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
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

    /**
     ******************************** mypage ********************************
     **/
    @GetMapping("mypage")
    public BaseResponse<Map<String, Object>> getMyPageUserInfo(@RequestParam("userID") String userID) throws BaseException{
        User user = userProvider.getUser(userID);
        String profileImg = userProvider.getUserImagePath(userID);
        int friendCount = friendService.getFriendsCount(userID);
        int followerCount = friendService.getFollowerCount(userID);
        int followingCount = friendService.getFollowingCount(userID);
        Social social = userProvider.getSocial(userID);
        List<UserWalletAndInfo> walletLists = walletService.getAllUserWalletByUserId(userID);
        List<CommentWithInfo> pinnedCommentWithInfoList = userProvider.getAllPinnedCommentsForUser(userID);
        int pinnedCommentCount = pinnedCommentWithInfoList.size();

        if(pinnedCommentCount<3){
            List<CommentWithInfo> commentRecentLists = userProvider.getNRecentComments(3-pinnedCommentCount, userID);
            pinnedCommentWithInfoList.addAll(commentRecentLists);
        }


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
        result.put("comments", pinnedCommentWithInfoList);

        userService.addHit(userID);

        return new BaseResponse<>(result);
    }

    @GetMapping("profileHistory")
    public BaseResponse<List<ProfileImg>> getProfileImgHistory(@RequestParam("userID") String userID) throws BaseException {
        List<ProfileImg> profileImgList = userProvider.getProfileImgHistory(userID);
        return new BaseResponse<>(profileImgList);
    }

    @PostMapping("profileHistory/delete")
    public BaseResponse<String> deleteProfileImgHistory(@RequestParam("userID") String userID, @RequestBody Map<String, Object> json){
        String jwtToken = (String) json.get("jwtToken");
        int profileIndex = (int) json.get("profileIndex");
        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        userService.deleteProfileImgHistory(userID, profileIndex);
        return new BaseResponse<>("successfully delete profile Img");
    }

    @PostMapping("profileHistory")
    public BaseResponse<String> hideProfileImgHistory(@RequestParam("userID") String userID, @RequestParam("profileIndex") int profileIndex, @RequestParam("hide") boolean hide, @RequestBody Map<String, String> json){
        String jwtToken = json.get("jwtToken");
        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        userService.hideProfileImgHistory(userID, profileIndex, hide);
        if(hide){
            return new BaseResponse<>("successfully hide profile Img");
        }
        return new BaseResponse<>("successfully unhide profile Img");
    }


    /**
     ******************************** notification ********************************
     **/
    @PostMapping("notification/all")
    public BaseResponse<List<Map<String, Object>>> retrieveNotification(@RequestParam("userID") String userID, @RequestBody Map<String, String> json) throws BaseException {
        // check jwt token
        String jwtToken = json.get("jwtToken");
        List<Map<String, Object>> res = new ArrayList<>();

        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        List<Notification> notificationList = userProvider.getUserNotificationList(userID);
        for(Notification notification:notificationList){
            userService.checkNotification(notification.getIndex());

            Map<String, Object> tmp = new HashMap<>();
            User user = userProvider.getUser(notification.getUser());
            switch(notification.getType()){
                case 1: user = userProvider.getUser(notification.getUser());
                        tmp.put("img", userProvider.getUserImagePath(user.getId()));
                        break;
                case 2: int friendReqIndex = notification.getFriendReq();
                        FriendReq friendReq = friendService.getFriendReq(friendReqIndex);
                        String reqID = friendReq.getReqFrom();
                        user = userProvider.getUser(reqID);
                        tmp.put("img", userProvider.getUserImagePath(user.getId()));
                        break;
                case 3: int friendIndex = notification.getFriend();
                        Friend friend = friendService.getFriend(friendIndex);
                        String friendID = friend.getFriend();
                        user = userProvider.getUser(friendID);
                        tmp.put("img", userProvider.getUserImagePath(user.getId()));
                        break;
                case 4:
                        int commentIdx = notification.getComment();
                        Comment comment = userProvider.getComment(commentIdx);
                        user = userProvider.getUser(comment.getCommentFrom());
                        tmp.put("img", userProvider.getUserImagePath(user.getId()));
                        break;
                case 5: int followIndex = notification.getFollow();
                        Follow follow = friendService.getFollow(followIndex);
                        String followID = follow.getUser();
                        user = userProvider.getUser(followID);
                        tmp.put("img", userProvider.getUserImagePath(user.getId()));
                        break;
                case 6: user = userProvider.getUser(notification.getUser());
                        tmp.put("img", "https://daotool.s3.ap-northeast-2.amazonaws.com/static/etc/61d4c43b-d8d0-45bb-a437-8af1977812a1mepe+poap+-+amazing+mepe.svg");
                        break;
                default:
                        break;
            }
            tmp.put("notiSender", user.getId());
            tmp.put("notification", notification);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            LocalDateTime a = now.toLocalDateTime();
            LocalDateTime b = notification.getCreatedAt().toLocalDateTime();
            long minuites = ChronoUnit.MINUTES.between(b, a);
            tmp.put("minitesAgo", minuites);

            res.add(tmp);
        }

        return new BaseResponse<>(res);
    }

    @PostMapping("notification/left")
    public BaseResponse<Boolean> checkNotificationLeft(@RequestParam("userID") String userID, @RequestBody Map<String, String> json){
        // check jwt token
        String jwtToken = json.get("jwtToken");

        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        boolean res = userProvider.isUncheckedNotificationLeft(userID);
        return new BaseResponse<>(res);
    }

    @PostMapping("notification")
    public BaseResponse<Map<String, Object>> getOneNotification(@RequestParam("notiID") int notiID, @RequestBody Map<String, String> json) throws BaseException {
        // check jwt token
        String userID = json.get("userID");
        String jwtToken = json.get("jwtToken");

        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        Map<String, Object> res = new HashMap<>();
        userService.checkNotification(notiID);
        Notification notification = userProvider.getNotification(notiID);

        User user = userProvider.getUser(notification.getUser());
        switch(notification.getType()){
            case 1: user = userProvider.getUser(notification.getUser());
                break;
            case 2: int friendReqIndex = notification.getFriendReq();
                FriendReq friendReq = friendService.getFriendReq(friendReqIndex);
                String reqID = friendReq.getReqFrom();
                user = userProvider.getUser(reqID);
                break;
            case 3: int friendIndex = notification.getFriend();
                Friend friend = friendService.getFriend(friendIndex);
                String friendID = friend.getFriend();
                user = userProvider.getUser(friendID);
                break;
            case 4:
                break;
            case 5: int followIndex = notification.getFollow();
                Follow follow = friendService.getFollow(followIndex);
                String followID = follow.getUser();
                user = userProvider.getUser(followID);
                break;
            default:
                break;
        }
        res.put("notiSender", user.getId());
        res.put("img", userProvider.getUserImagePath(user.getId()));
        res.put("notification", notification);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        LocalDateTime a = now.toLocalDateTime();
        LocalDateTime b = notification.getCreatedAt().toLocalDateTime();
        long minuites = ChronoUnit.MINUTES.between(b, a);
        res.put("minitesAgo", minuites);

        return new BaseResponse<>(res);
    }

    @DeleteMapping("notification")
    public BaseResponse<String> deleteANotification(@RequestParam("notiID") int notiID, @RequestBody Map<String, String> json){
        // check jwt token
        String userID = json.get("userID");
        String jwtToken = json.get("jwtToken");

        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        userService.deleteANotification(notiID);
        return new BaseResponse<>("Delete a notification Successfully!");
    }

    @DeleteMapping("notification/all")
    public BaseResponse<String> deleteAllNotification(@RequestBody Map<String, String> json){
        // check jwt token
        String userID = json.get("userID");
        String jwtToken = json.get("jwtToken");

        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        userService.deleteAllNotification(userID);
        return new BaseResponse<>("Delete all notifications Successfully!");
    }

    /**
     ******************************** userList ********************************
     **/

    @PostMapping("userList")
    public BaseResponse<Map<String, Object>> getUserList(@RequestParam("orderBy") String orderBy, @RequestBody Map<String, String> json) throws BaseException {
        String userID = json.get("userID");

        Map<String, Object> userListCreatedAt = userProvider.getUserList("createdAt", userID);
        List<Map<String, Object>> listCreatedAt = (List<Map<String, Object>>) userListCreatedAt.get("list");
        Map<String, Object> userListTodayHits = userProvider.getUserList("todayHits", userID);
        List<Map<String, Object>> listTodayHits = (List<Map<String, Object>>) userListTodayHits.get("list");
        Map<String, Object> userListTodayFollows = userProvider.getUserList("todayFollows", userID);
        List<Map<String, Object>> listTodayFollows = (List<Map<String, Object>>) userListTodayFollows.get("list");

        Map<String, Object> res = new HashMap<>();
        switch (orderBy) {
            case "createdAt":
                res.put("userList", listCreatedAt);
                break;
            case "todayHits":
                res.put("userList", listTodayHits);
                break;
            case "todayFollows":
                res.put("userList", listTodayFollows);
                break;
            default:
                return new BaseResponse<>(REQUEST_ERROR);
        }
        System.out.println(listCreatedAt.get(0));
        res.put("topCreatedAt", listCreatedAt.get(0));
        res.put("topTodayHits", listTodayHits.get(0));
        res.put("topTodayFollows", listTodayFollows.get(0));

        if(!userID.equals("")){
            Map<String, Object> me = new HashMap<>();
            me.put("createdAt", userListCreatedAt.get("me"));
            me.put("todayHits", userListTodayHits.get("me"));
            me.put("todayFollows", userListTodayFollows.get("me"));

            res.put("myRecord", me);
        }

        return new BaseResponse<>(res);
    }

    /*
    ******************************** comment ********************************
    **/
    @GetMapping("comments/new")
    public BaseResponse<Map<String, String>> getMyInfoForNewComment(@RequestParam("userID") String userID, @RequestParam("friendID") String friendID) throws BaseException {
        Friend friendInfo = friendService.getFriend(friendID, userID);
        User user = userProvider.getUser(userID);
        String userImg = userProvider.getUserImagePath(userID);

        Map<String,String> res = new HashMap<>();
        res.put("userID", user.getId());
        res.put("userNickname", user.getNickname());
        res.put("userImg", userImg);
        res.put("userFriendNickname", friendInfo.getFriendName());
        res.put("friendID",friendID);

        return new BaseResponse<>(res);
    }

    @PostMapping("comments/new")
    public BaseResponse<String> createNewComment(@RequestParam("friendID") String friendID, @RequestBody Map<String, String> json){
        // check jwt token
        String userID = json.get("userID");
        String jwtToken = json.get("jwtToken");
        String message = json.get("message");

        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        userService.createComment(userID, friendID, message);
        Comment comment = userProvider.getComment(userID, friendID, message);
        userService.createNotification(friendID, 4, comment.getIndex());

        return new BaseResponse<>("Success to create a new comment");

    }

    @PostMapping("comments/all")
    public BaseResponse<Map<String, Object>> getAllCommentsForUser(@RequestParam("userID") String userID, @RequestBody Map<String, String> json){
        String jwtToken = json.get("jwtToken");
        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        List<CommentWithInfo> commentsList = userProvider.getAllCommentsExceptPinnedForUser(userID);
        List<CommentWithInfo> pinnedCommentList = userProvider.getAllPinnedCommentsForUser(userID);

        Map<String, Object> res = new HashMap<>();
        res.put("commentsList", commentsList);
        res.put("pinnedComemntList", pinnedCommentList);


        return new BaseResponse<>(res);
    }

    @PostMapping("comments/hidden")
    public BaseResponse<String> hideComment(@RequestParam("userID") String userID, @RequestParam("commentIdx") int commentIdx, @RequestParam("hide") boolean hide,@RequestBody Map<String, String> json){
        String jwtToken = json.get("jwtToken");
        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        userService.hideComment(commentIdx, userID, hide);
        if(hide){
            return new BaseResponse<>("hide Successfully!");
        }
        return new BaseResponse<>("unhide Successfully!");
    }

    @PostMapping("comments/unhidden")
    public BaseResponse<List<CommentWithInfo>> getCommentsWithoutHiddenComments(@RequestParam("userID") String userID, @RequestBody Map<String, String> json){
        String jwtToken = json.get("jwtToken");
        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        List<CommentWithInfo> commentWithInfoList = userProvider.getAllCommentsExceptHidden(userID);
        return new BaseResponse<>(commentWithInfoList);
    }

    @PostMapping("comments/pinned")
    public BaseResponse<String> pinComments(@RequestParam("userID") String userID, @RequestParam("pin") boolean pin, @RequestBody Map<String, Object> json){
        String jwtToken = (String) json.get("jwtToken");
        ArrayList<Integer> commentsIdxList = (ArrayList<Integer>) json.get("idxArr");

        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        for(int idx: commentsIdxList){
            userService.pinComment(idx, pin);
        }
        if(pin){
            return new BaseResponse<>("pin Successfully!");
        }
        return new BaseResponse<>("unpin Successfully!");

    }

    @GetMapping("comments")
    public BaseResponse<Optional<Comment>> getCommentByIdx(@RequestParam("index") int idx){
        Optional<Comment> comment = userProvider.getOptionalComment(idx);
        return new BaseResponse<>(comment);
    }
}


