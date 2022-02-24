package com.propwave.daotool.user;

import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.user.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserDao userDao;
    private final UserProvider userProvider;

    public UserService(UserDao userDao, UserProvider userProvider){
        this.userDao = userDao;
        this.userProvider = userProvider;
    }

//    public User createUser(Object userInfo) throws BaseException{
//        if ()
//    }
}
