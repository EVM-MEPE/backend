package com.propwave.daotool.user;

import com.propwave.daotool.badge.model.*;
import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.config.BaseResponseStatus;
import static com.propwave.daotool.config.BaseResponseStatus.*;
import com.propwave.daotool.user.model.User;
import com.propwave.daotool.wallet.UserWalletDao;
import com.propwave.daotool.wallet.model.UserWallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

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

    // Userid, wallet address로 userWallet 가져오기
    public UserWallet getUserWalletByWalletAddressAndUserId(String userId, String walletAddress) throws BaseException{
        try{
            return userDao.getUserWalletByWalletAddressAndUserId(userId, walletAddress);
        }catch(Exception exception){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public int isUserWalletByWalletAddressAndUserIdExist(String userId, String walletAddress) throws BaseException{
        try{
            return userDao.isUserWalletByWalletAddressAndUserIdExist(userId, walletAddress);
        }catch(Exception exception){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    // 지갑 주소가 wallet에 있는지 확인
    public int isWalletExist(String walletAddress) throws BaseException {
        try{
            return userDao.isWalletExist(walletAddress);
        } catch(Exception exception){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    // 지갑 주소가 userWallet에서 로그인 용으로 이미 있는지 확인
    public int isWalletExistForLogin(String walletAddress) throws BaseException{
        try{
            System.out.println("provider: 지갑 소 이미 로그인용으로 있나 확인해보자");
            return userDao.isWalletExistForLogin(walletAddress);
        }catch(Exception exception){
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
            return userDao.getAllUserWalletByUserId(userId);
        } catch(Exception exception){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }


    public List<Map<String, Object>> getAllBadge(String walletAddress) throws BaseException{
        try{
            // 유저의 뱃지 이름 가져오기
            List<BadgeWallet> allBadgeWallet = userDao.getAllBadgeWallet(walletAddress);
            // 뱃지 내용 모으기
            List<Map<String, Object>> allBadge = new ArrayList<>();
            for(BadgeWallet badgeWallet: allBadgeWallet){
                String badgeName = badgeWallet.getBadgeName();
                Badge badge = userDao.getBadge(badgeName);

                // 뱃지에 참여중인 사람의 수도 같이 return
                List<BadgeWallet> badgeWallets = userDao.getBadgeWalletByBadgeName(badgeName);

                Map<String, Object> tmp = new HashMap<>();
                tmp.put("name", badge.getName());
                tmp.put("image", badge.getImage());
                tmp.put("explanation", badge.getExplanation());
                tmp.put("createdAt", badge.getCreatedAt());
                tmp.put("joinedWalletCount", badgeWallets.size());

                allBadge.add(tmp);
            }
            return allBadge;
        } catch(Exception exception){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public List<GetBadgesRes> getBadges(String walletAddress){
        // 뱃지 이름, join 한 날짜 가져오기
        List<BadgeJoinedAt> badgeJoinedAt = userDao.getBadgeJoinedAt(walletAddress);

        List<GetBadgesRes> getBadgesRes = new ArrayList<>();
        //뱃지 이름가지고 이름, 이미지 가져오기 -> getbadgeres 만들기
        for(BadgeJoinedAt badge: badgeJoinedAt){
            // badge 의 이름하고 이미지 가져옴
            BadgeNameImage badgeTmp = userDao.getBadgeNameImage(badge.getBadgeName());
            //
            GetBadgesRes badgeResTmp = new GetBadgesRes(badge.getBadgeName(), badgeTmp.getImage(), badge.getJoinedAt());
            getBadgesRes.add(badgeResTmp);
        }
        return getBadgesRes;
    }

    public int checkUser(String userId){
        return userDao.checkUser(userId);
    }

    // 지갑이 남에게도 있는지 여부 확인
    int isWalletSomeoneElse(String userId, String walletAddress) throws BaseException{
        try {
            return userDao.isWalletSomeoneElse(userId, walletAddress);
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 나에게 대시보드 용도 있는지 여부
    int isWalletMyDashboard(String userId,String walletAddress) throws BaseException{
        try {
            return userDao.isWalletMyDashboard(userId, walletAddress);
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
