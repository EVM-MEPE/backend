package com.propwave.daotool.Friend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.propwave.daotool.Friend.model.Follow;
import com.propwave.daotool.Friend.model.Friend;
import com.propwave.daotool.Friend.model.FriendReq;
import com.propwave.daotool.Friend.model.FriendWithFriendImg;
import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.user.UserDao;
import com.propwave.daotool.user.UserProvider;
import com.propwave.daotool.user.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.propwave.daotool.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class FriendService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FriendDao friendDao;
    private final UserDao userDao;

    public FriendService(FriendDao friendDao, UserDao userdao){
        this.friendDao = friendDao;
        this.userDao = userdao;
    }

    public int createFriendReq(String reqTo, String reqFrom, String reqNickname) throws BaseException {
        try{
            return friendDao.createFriendReq(reqTo, reqFrom, reqNickname);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public boolean checkFriendReqExist(String reqTo, String reqFrom){
        FriendReq friendReq;
        try{
            friendReq = friendDao.getFriendReq(reqTo, reqFrom);
        }catch(EmptyResultDataAccessException e){
            return false;
        }
        return !friendReq.isAccepted() && !friendReq.isRejected();
    }

    public FriendReq getFriendReq(String reqTo, String reqFrom){
        return friendDao.getFriendReq(reqTo, reqFrom);
    }

    public FriendReq getFriendReq(int index){
        return friendDao.getFriendReq(index);
    }

    public String getStatusOfFriendReq(String reqFrom, String reqTo) throws BaseException {
        try{
            FriendReq friendReq = friendDao.getFriendReq(reqTo, reqFrom);
            if(friendReq.isAccepted()){
                return "friend";
            }else{
                return "wait";
            }
        }catch(EmptyResultDataAccessException e){
            return "none";
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }

    }

    public boolean checkFriendExist(String user1, String user2){
        try{
            friendDao.getFriend(user1, user2);
        }catch(EmptyResultDataAccessException e){
            return false;
        }
        return true;
    }

    public int acceptFriend(boolean accepted, String reqTo, String reqFrom, String toNickname) throws BaseException {
        try{
            if(accepted){
                //1. friend Req accept로 바꾸기
                friendDao.updateFriendReq(reqTo, reqFrom);
                //2. friend record 만들기
                String fromNickname = friendDao.getFriendReqNickname(reqFrom, reqTo);
                friendDao.createFriend(reqTo, reqFrom, toNickname);   // to에게 from이라는 친구가 to Nickname 이라는 이름으로 생김
                return friendDao.createFriend(reqFrom, reqTo, fromNickname);
            } else{
                return friendDao.deleteFriendReq(reqFrom, reqTo);
            }
        }catch(Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public Friend editFriendNickname(String user, String friend, String newNickname){
        return friendDao.editFriendNickname(user, friend, newNickname);
    }

    public int createFollow(String reqTo, String reqFrom){
        int checkFollowExist = friendDao.isFollowExist(reqTo, reqFrom);
        if(checkFollowExist == 1){
            return -2;
        }
        return friendDao.createFollow(reqTo, reqFrom);
    }

    public int deleteFollow(String reqTo, String reqFrom){
        return friendDao.deleteFollow(reqTo, reqFrom);
    }

    public Friend getFriend(String user, String friend){
        return friendDao.getFriend(user, friend);
    }

    public Friend getFriend(int index){
        return friendDao.getFriend(index);
    }

    public List<FriendWithFriendImg> getAllFriendsWithFriendImg(String userId) {
        return friendDao.getAllFriendsWithFriendImg(userId);
    }

    public int getFriendsCount(String userId){
        return friendDao.getFriendsCount(userId);
    }

    public Map<String, String> getFriendNickname(String userID, String friendID){
        // user가 friend를 뭘로 저장했는지 확인하기
        Map<String, String> res = new HashMap<>();
        String nickname = friendDao.getFriendReqNickname(userID, friendID);
        res.put("user", userID);
        res.put("friend", friendID);
        res.put("friendNickname", nickname);
        return res;
    }





    public Follow getFollow(String reqTo, String reqFrom){
        return friendDao.getFollow(reqTo, reqFrom);
    }

    public Follow getFollow(int index){
        return friendDao.getFollow(index);
    }

    public List<Map<String, Object>> getFollowingList(String userID){
        List<Follow> followingList = friendDao.getFollowingList(userID);
        List<Map<String, Object>> followingListWithUserInfo = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new SimpleModule());

        for(Follow follow:followingList){
            String following = follow.getFollowing();
            User user = userDao.getUserInfo(following);
            String profileImg;
            try{
                profileImg = userDao.getUserImagePath(user.getId());
            }catch(EmptyResultDataAccessException e1){
                profileImg= UserProvider.DEFAULT_USER_PROFILE_IMAGE;
            }
            Map<String, Object> userMap = objectMapper.convertValue(user, Map.class);

            Timestamp userCreatedAt = user.getCreatedAt();
            userMap.replace("createdAt", userCreatedAt);
            userMap.put("profileImage", profileImg);

            followingListWithUserInfo.add(userMap);
        }
        return followingListWithUserInfo;
    }

    public List<Map<String, Object>> getFollowerList(String userID){
        List<Follow> followerList = friendDao.getFollowerList(userID);
        List<Map<String, Object>> followerListWithUserInfo = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new SimpleModule());

        for(Follow follow:followerList){
            String follower = follow.getUser();
            User user = userDao.getUserInfo(follower);
            String profileImg;
            try{
                profileImg = userDao.getUserImagePath(user.getId());
            }catch(EmptyResultDataAccessException e1){
                profileImg= UserProvider.DEFAULT_USER_PROFILE_IMAGE;
            }
            Map<String, Object> userMap = objectMapper.convertValue(user, Map.class);
            Timestamp userCreatedAt = user.getCreatedAt();
            userMap.replace("createdAt", userCreatedAt);
            userMap.put("profileImage", profileImg);
            followerListWithUserInfo.add(userMap);
        }
        return followerListWithUserInfo;
    }

    public int getFollowerCount(String userID){
        return friendDao.getFollowerCount(userID);
    }

    public int getFollowingCount(String userID){
        return friendDao.getFollowingCount(userID);
    }

    public int isFollowing(String userID1, String userID2){
        return friendDao.isFollowExist(userID1, userID2);
    }

    public int addFollow(String reqTo){
        return friendDao.addFollow(reqTo);
    }

    public int reduceFollow(String reqTo){
        return friendDao.reduceFollow(reqTo);
    }


}
