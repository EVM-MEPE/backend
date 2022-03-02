package com.propwave.daotool.user;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.user.model.User;
import com.propwave.daotool.wallet.model.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.propwave.daotool.config.BaseResponseStatus.*;

@Service
public class UserService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserDao userDao;
    private final UserProvider userProvider;

    public UserService(UserDao userDao, UserProvider userProvider){
        this.userDao = userDao;
        this.userProvider = userProvider;
    }

    public User createUser(Map<String, Object> userInfo, String profileImageS3path) throws BaseException{
        try{
            User newUser = userDao.createUser(userInfo, profileImageS3path);
            return newUser;
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public String createWallet(String walletAddress) throws BaseException {
        try{
            String newWallet = userDao.createWallet(walletAddress);
            return newWallet;
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public String createUserWallet(Map<String, Object> wallet, String userId) throws BaseException {
        try{
            String newUserWallet = userDao.createUserWallet(wallet, userId);
            return newUserWallet;
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void addHit(String userId) throws BaseException{
        try{
            userDao.addHit(userId);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
