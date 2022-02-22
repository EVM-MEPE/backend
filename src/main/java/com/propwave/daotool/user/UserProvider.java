package com.propwave.daotool.user;

import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.config.BaseResponseStatus;
import com.propwave.daotool.wallet.UserWalletDao;
import com.propwave.daotool.wallet.model.UserWallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

//Provider: Read 비즈니스 로직 처리
@Service
public class UserProvider {

    private UserDao userDao;
    private UserWalletDao userWalletDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    public UserProvider(UserDao userDao, UserWalletDao userWalletDao){
        this.userDao = userDao;
        this.userWalletDao = userWalletDao;
    }

    //회원가입 여부 확인
    public List<UserWallet> isWalletRegistered(String walletAddress) throws BaseException {
        try {
            List<UserWallet> userWallet = userWalletDao.getUserWallet(walletAddress);
            return userWallet;
        } catch (Exception exception) {
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

}
