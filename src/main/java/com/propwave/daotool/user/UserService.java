package com.propwave.daotool.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.propwave.daotool.friend.FriendDao;
import com.propwave.daotool.friend.model.Follow;
import com.propwave.daotool.friend.model.Friend;
import com.propwave.daotool.friend.model.FriendReq;
import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.config.jwt.SecurityService;
import com.propwave.daotool.user.model.*;
import com.propwave.daotool.utils.GetNFT;
import com.propwave.daotool.utils.GetPOAP;
import com.propwave.daotool.wallet.WalletService;
import com.propwave.daotool.wallet.model.TokenReq;
import com.propwave.daotool.wallet.model.Transaction;
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
    private final WalletService walletService;
    private final SecurityService securityService;

    public UserService(UserDao userDao, FriendDao friendDao, UserProvider userProvider, WalletService walletService, SecurityService securityService){
        this.userDao = userDao;
        this.userProvider = userProvider;
        this.friendDao = friendDao;
        this.walletService = walletService;
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

    public int deleteUser(String userId) throws BaseException{
        try{
            return userDao.deleteUser(userId);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

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
            case 5: Follow follow = friendDao.getFollow(optionIdx[0]);
                    message = follow.getUser() + " starts following you.";
                    return userDao.createNotification(userID, type, message, optionIdx);
            case 7: Transaction trx = walletService.getTransaction(optionIdx[0]);
                    message = trx.getFromUser() + " sends you token. Check it out.";
                    return userDao.createNotification(userID, type, message, optionIdx);
            case 8: TokenReq tokenReq = walletService.getTokenReq(optionIdx[0]);
                    message = tokenReq.getFromUser() + " requests" + tokenReq.getReqTokenAmount() + ". /n"+ tokenReq.getMemo() +"/n Would you like to send?";
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

    public void addHit(String userId) throws BaseException{
        try{
            userDao.addHit(userId);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

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
