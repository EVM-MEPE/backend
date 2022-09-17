package com.propwave.daotool.Friend;

import com.propwave.daotool.Friend.model.Follow;
import com.propwave.daotool.Friend.model.Friend;
import com.propwave.daotool.Friend.model.FriendReq;
import com.propwave.daotool.Friend.model.FriendWithFriendImg;
import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.config.BaseResponse;
import com.propwave.daotool.user.UserService;
import com.propwave.daotool.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.propwave.daotool.config.BaseResponseStatus.*;


@RestController
@CrossOrigin(origins="*")
public class FriendController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final FriendService friendService;
    private final UserService userService;
    private final Utils utils;

    public FriendController(FriendService friendService, Utils utils, UserService userService){
        this.friendService = friendService;
        this.utils = utils;
        this.userService = userService;
    }



    @PostMapping("friends/request")
    public BaseResponse<String> requestFriend(@RequestParam("user") String reqTo, @RequestBody Map<String, String> json) throws BaseException {
        // check jwt token
        String jwtToken = json.get("jwtToken");
        String userID = json.get("reqFrom");

        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }
        if(friendService.checkFriendReqExist(reqTo, userID)){
            return new BaseResponse<>(FRIEND_REQ_ALREADY_EXIST);
        }
        if(friendService.getStatusOfFriendReq(userID, reqTo).equals("friend")){
            return new BaseResponse<>(FRIEND_ALREADY_EXIST);
        }

        friendService.createFriendReq(reqTo, json.get("reqFrom"), json.get("reqNickname"));
        FriendReq friendReq = friendService.getFriendReq(reqTo, json.get("reqFrom"));
        userService.createNotification(reqTo, 2, friendReq.getIndex());
        return new BaseResponse<>("successfully make friend request");
    }

    @PatchMapping("friends/request")
    public BaseResponse<String> friendRequestProcess(@RequestParam("accept") boolean isAccepted, @RequestBody Map<String, String> json) throws BaseException {
        // check jwt token
        String jwtToken = json.get("jwtToken");
        String userID = json.get("reqTo");

        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }
        if(friendService.checkFriendExist(userID, json.get("reqFrom"))){
            return new BaseResponse<>(FRIEND_ALREADY_EXIST);
        }
        friendService.acceptFriend(isAccepted, json.get("reqTo"), json.get("reqFrom"), json.get("reqNickname"));
        if(isAccepted){
            Friend friend = friendService.getFriend(json.get("reqFrom"), json.get("reqTo"));
            userService.createNotification(userID, 3, friend.getIndex());
        }
        return new BaseResponse<>("successfully process friend request");
    }

    @PatchMapping("friends/nickname")
    public BaseResponse<Friend> editFriendNickname(@RequestBody Map<String, String> json){
        // check jwt token
        String jwtToken = json.get("jwtToken");
        String userID = json.get("user");

        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        Friend newNickname = friendService.editFriendNickname(json.get("user"), json.get("friend"), json.get("newNickname"));
        return new BaseResponse<>(newNickname);
    }

    @GetMapping("friends")
    public BaseResponse<List<FriendWithFriendImg>> getAllFriendsList(@RequestParam("userID") String userId){
        List<FriendWithFriendImg> friendList = friendService.getAllFriendsWithFriendImg(userId);
        return new BaseResponse<>(friendList);
    }

    @GetMapping("friends/number")
    public BaseResponse<Integer> getFriendsCount(@RequestParam("userID") String userId){
        int friendsCount = friendService.getFriendsCount(userId);
        return new BaseResponse<>(friendsCount);
    }

    @PostMapping("friends/request/status")
    public BaseResponse<String> getStatusOfFriendReq(@RequestParam("reqFrom") String reqFrom, @RequestParam("reqTo") String reqTo, @RequestBody Map<String, String> json) throws BaseException {
        // check jwt token
        String jwtToken = json.get("jwtToken");

        if(!utils.isUserJwtTokenAvailable(jwtToken, reqFrom)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }
        String status = friendService.getStatusOfFriendReq(reqFrom, reqTo);
        return new BaseResponse<>(status);
    }

    //********************************* Follow

    @PostMapping("following/request")
    public BaseResponse<String> requestFollow(@RequestParam("userID") String reqTo, @RequestBody Map<String, String> json){
        // check jwt token
        String jwtToken = json.get("jwtToken");
        String reqFrom = json.get("reqFrom");

        if(!utils.isUserJwtTokenAvailable(jwtToken, reqFrom)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        int result = friendService.createFollow(reqTo, json.get("reqFrom"));
        if(result==-2){
            return new BaseResponse<>(FOLLOW_ALREADY_EXIST);
        }
        Follow follow = friendService.getFollow(reqTo, json.get("reqFrom"));
        userService.createNotification(reqTo, 5, follow.getIndex());
        friendService.addFollow(reqTo);
        return new BaseResponse<>("successfully follow " + reqTo);
    }

    @PostMapping("following/delete")
    public BaseResponse<String> deleteFollow(@RequestParam("userID") String reqTo, @RequestBody Map<String, String> json){
        // check jwt token
        String jwtToken = json.get("jwtToken");
        String reqFrom = json.get("reqFrom");

        if(!utils.isUserJwtTokenAvailable(jwtToken, reqFrom)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        friendService.deleteFollow(reqTo, json.get("reqFrom"));
        friendService.reduceFollow(reqTo);
        return new BaseResponse<>("successfully delete follow " + reqTo);
    }

    @GetMapping("following/following")
    public BaseResponse<List<Map<String, Object>>> getFollowingList(@RequestParam("userID") String userID){
        List<Map<String, Object>> followingList= friendService.getFollowingList(userID);
        return new BaseResponse<>(followingList);
    }

    @GetMapping("following/follower")
    public BaseResponse<List<Map<String, Object>>> getFollowerList(@RequestParam("userID") String userID){
        List<Map<String, Object>> followerList= friendService.getFollowerList(userID);
        return new BaseResponse<>(followerList);
    }

    @GetMapping("following")
    public BaseResponse<Boolean> isFollowing(@RequestParam("userID1") String userID1, @RequestParam("userID2") String userID2){
        int isFollowing = friendService.isFollowing(userID1, userID2);
        boolean result;
        result = isFollowing == 1;
        return new BaseResponse<>(result);
    }
}
