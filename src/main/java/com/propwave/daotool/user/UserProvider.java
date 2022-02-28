package com.propwave.daotool.user;

import com.propwave.daotool.badge.model.Badge;
import com.propwave.daotool.badge.model.BadgeWallet;
import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.config.BaseResponseStatus;
import com.propwave.daotool.user.model.User;
import com.propwave.daotool.wallet.UserWalletDao;
import com.propwave.daotool.wallet.model.UserWallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
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

    //User 정보 불러오기 -> ID로
    public User getUser(String id) throws BaseException{
        try{
            User user = userDao.getUserAllInfo(id);
            return user;
        } catch(Exception exception){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public int isWalletExist(String walletAddress) throws BaseException {
        try{
            return userDao.isWalletExist(walletAddress);
        } catch(Exception exception){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public List<UserWallet> getAllUserWalletByWallet(String walletAddress) throws BaseException{
        try{
            return userDao.getAllUserWalletByWalletId(walletAddress);
        } catch(Exception exception){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public List<UserWallet> getAllUserWalletByUserId(String userId) throws BaseException{
        try{
            System.out.println(userDao.getAllUserWalletByUserId("test1"));
            return userDao.getAllUserWalletByUserId(userId);
        } catch(Exception exception){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public List<Badge> getAllBadge(String walletAddress) throws BaseException{
        try{
            // 유저의 뱃지 이름 가져오기
            List<BadgeWallet> allBadgeWallet = userDao.getAllBadgeWallet(walletAddress);
            // 뱃지 내용 모으기
            List<Badge> allBadge = new ArrayList<>();
            for(BadgeWallet badgeWallet: allBadgeWallet){
                String badgeName = badgeWallet.getBadgeName();
                allBadge.add(userDao.getBadge(badgeName));
            }
            // 뱃지 중복 제거
            HashSet<Badge> set = new HashSet<Badge>(allBadge);
            List<Badge> newAllBadge = new ArrayList<Badge>(set);

            return newAllBadge;
        } catch(Exception exception){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }
}
