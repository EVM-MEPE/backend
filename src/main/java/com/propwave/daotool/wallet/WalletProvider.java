package com.propwave.daotool.wallet;

import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.config.BaseResponse;
import com.propwave.daotool.config.BaseResponseStatus;
import com.propwave.daotool.user.UserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WalletProvider {
    private final WalletDao walletDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    public WalletProvider(WalletDao walletDao){
        this.walletDao = walletDao;
    }

    public int isWalletExist(String walletAddress) throws BaseException {
        try{
            return walletDao.isWalletExist(walletAddress);
        } catch(Exception exception){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }
}
