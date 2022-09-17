package com.propwave.daotool.user;

import com.propwave.daotool.friend.FriendDao;
import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.config.BaseResponseStatus;

import static com.propwave.daotool.config.BaseResponseStatus.*;

import com.propwave.daotool.user.model.*;
import com.propwave.daotool.user.model.UserWallet;

import com.propwave.daotool.wallet.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.*;

//Provider: Read 비즈니스 로직 처리
@Service
public class UserProvider {
    final public static String DEFAULT_USER_PROFILE_IMAGE = "https://daotool.s3.ap-northeast-2.amazonaws.com/static/user/d1b5e5d6-fc89-486b-99d6-b2a6894f9eafprofileimg-default.png";

    private final UserDao userDao;
    private final FriendDao friendDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    public UserProvider(UserDao userDao, FriendDao friendDao){
        this.userDao = userDao;
        this.friendDao = friendDao;
    }

    public int checkUserIdExist(String id) throws BaseException{
        try{
            return userDao.checkUserIdExist(id);
        }catch (Exception exception) {
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    //User 정보 불러오기 -> ID로
    public User getUser(String id) throws BaseException{
        try{
            return userDao.getUserInfo(id);
        } catch(Exception exception){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    //User의 profileImage Path불러오기
    public String getUserImagePath(String userId) throws BaseException{
        try{
            return userDao.getUserImagePath(userId);
        }catch(EmptyResultDataAccessException e1){
            return DEFAULT_USER_PROFILE_IMAGE;
        } catch(Exception e2){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public List<ProfileImg> getProfileImgHistory(String userID) throws BaseException {
        try{
            return userDao.getProfileImgHistory(userID);
        }catch(Exception e){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public Social getSocial(String userId) throws BaseException{
        try{
            return userDao.getSocial(userId);
        } catch(Exception exception){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public List<Notification> getUserNotificationList(String userID){
        return userDao.getUserNotificationList(userID);
    }

    public boolean isUncheckedNotificationLeft(String userID){
        return userDao.isUncheckedNotificationLeft(userID);
    }

    public Notification getNotification(int index){
        return userDao.getNotification(index);
    }

    public Map<String, Object> getUserList(String orderBy, String userID) throws BaseException {
        Map<String, Object> res = new HashMap<>();
        List<Map<String, Object>> resUserList = new ArrayList<>();

        List<User> userList = userDao.getUserList(orderBy);
        int count = 1;
        for(User user: userList){
            Map<String, Object> map = new HashMap<>();
            String profileImg = getUserImagePath(user.getId());
            map.put("user", user);
            map.put("profileImg", profileImg);

            resUserList.add(map);
            if(user.getId().equals(userID)){
                System.out.println(count);
                res.put("me", count);
            }
            count += 1;
        }
        res.put("list", resUserList);
        return res;
    }

    public Comment getComment(String userID, String friendID, String message){
        return userDao.getComment(userID, friendID, message);
    }

    public Comment getComment(int commentIdx){
        return userDao.getComment(commentIdx);
    }

    public Optional<Comment> getOptionalComment(int commentIdx){
        return userDao.getOptionalComment(commentIdx);
    }

    public List<CommentWithInfo> getAllCommentsExceptPinnedForUser(String userID){
        return userDao.getAllCommentsExceptPinnedForUser(userID);
    }

    public List<CommentWithInfo> getAllPinnedCommentsForUser(String userID){
        return userDao.getAllPinnedCommentsForUser(userID);
    }

    public List<CommentWithInfo> getAllCommentsExceptHidden(String userID){
        return userDao.getAllCommentsExceptHidden(userID);
    }

    public List<CommentWithInfo> getNRecentComments(int count, String userID){
        return userDao.getNRecentComments(count, userID);
    }

}
