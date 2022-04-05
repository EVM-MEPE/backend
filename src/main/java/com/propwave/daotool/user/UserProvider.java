package com.propwave.daotool.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.propwave.daotool.badge.model.*;
import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.config.BaseResponseStatus;
import static com.propwave.daotool.config.BaseResponseStatus.*;

import com.propwave.daotool.user.model.*;
import com.propwave.daotool.wallet.UserWalletDao;
import com.propwave.daotool.wallet.model.UserWallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

//Provider: Read 비즈니스 로직 처리
@Service
public class UserProvider {

    private final UserDao userDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    public UserProvider(UserDao userDao, UserWalletDao userWalletDao){
        this.userDao = userDao;
    }

    //회원가입 여부 확인
    public int checkUserSignupAlready(String walletAddress) throws BaseException {
        List<UserWallet> userWallets = userDao.getAllUserWalletByWalletId(walletAddress);
        int login = 0;
        for (UserWallet userWallet : userWallets) {
            if (userWallet.isLoginAvailable()) {
                login = 1;
                return login;
            }
        }
        return login;
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
            return userDao.getUserAllInfo(id);
        } catch(Exception exception){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    //User의 profileImage Path불러오기
    public String getUserImagePath(String userId) throws BaseException{
        try{
            return userDao.getUserImagePath(userId);
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
            return userDao.isUserWalletExist(userId, walletAddress);
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

//    public int isWalletExistForLogin(String walletAddress, String user) throws BaseException{
//        try{
//            System.out.println("provider: 지갑 소 이미 로그인용으로 있나 확인해보자");
//            return userDao.isWalletExistForLogin(user, walletAddress);
//        }catch(Exception exception){
//            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
//        }
//    }

    public int isWalletExistForLoginNotMe(String walletAddress, String user) throws BaseException{
        try{
            System.out.println("provider: 지갑 주소 이미 나 제외 로그인용으로 있나 확인해보자");
            return userDao.isWalletExistForLoginNotMe(user, walletAddress);
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

    public List<Map<String, Object>> getAllUserWalletByUserId(String userId) throws BaseException{
        try{
            ObjectMapper objectMapper = new ObjectMapper();

            List<UserWallet> userWallets = userDao.getAllUserWalletByUserId(userId);
            List<Map<String, Object>> allUserWallets = new ArrayList<>();
            System.out.println("aaa1");
            for(UserWallet userWallet: userWallets){
                String walletAddress = userWallet.getWalletAddress();
                String walletChain = userWallet.getChain();

                Chain chain = userDao.getChainInfo(walletChain);
                System.out.println("aaa2");
                WalletInfo walletInfo = userDao.getWalletInfo(walletAddress);
                System.out.println("aaa3");

                Map<String, Object> userWalletMap = objectMapper.convertValue(userWallet, Map.class);
                Map<String, Object> chianMap = objectMapper.convertValue(chain, Map.class);
                Map<String, Object> walletInfoMap = objectMapper.convertValue(walletInfo, Map.class);
                System.out.println("aaa4");

                userWalletMap.replace("chain",chianMap);
                userWalletMap.replace("walletAddress",walletInfoMap);

                allUserWallets.add(userWalletMap);
            }
            System.out.println("size:"+allUserWallets.size());
            return allUserWallets;

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
                //Badge badge = userDao.getBadge(badgeName);

                Map<String, Object> badgeMap = getBadgeInfo(badgeName);
                badgeMap.put("hide", badgeWallet.getHide());
                badgeMap.put("joinedAt", badgeWallet.getJoinedAt());
                allBadge.add(badgeMap);
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
    public int isWalletSomeoneElse(String userId, String walletAddress) throws BaseException{
        try {
            return userDao.isWalletSomeoneElse(userId, walletAddress);
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 나에게 대시보드 용도 있는지 여부
    public int isWalletMyDashboard(String userId,String walletAddress) throws BaseException{
        try {
            return userDao.isWalletMyDashboard(userId, walletAddress);
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<BadgeRequest> getAllBadgeRequest() throws BaseException{
        return userDao.getAllBadgeRequest();
    }

    public boolean checkBadge(String badgeName) throws BaseException {
        try{
            int result =  userDao.checkBadge(badgeName);
            return result == 1;
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public Map<String, Object> getBadgeInfo(String badgeName){
        ObjectMapper objectMapper = new ObjectMapper();

        Badge badge = userDao.getBadge(badgeName);
        Chain chain = userDao.getChain(badge.getChain());
        List<BadgeTarget> badgeTargets = userDao.getBadgeTarget(badgeName);

        Map<String, String> targetMap = new HashMap<>();

        for(BadgeTarget badgeTarget:badgeTargets){
            Target target = userDao.getTarget(badgeTarget.getTargetIdx());
            targetMap.put("target", target.getTarget());
        }

        List<BadgeWallet> badgeWallets = userDao.getBadgeWalletByBadgeName(badgeName);  // 뱃지에 참여중인 사람의 수도 같이 return

        Map<String, Object> badge_map = objectMapper.convertValue(badge, Map.class);
        Map<String, Object> chain_map = objectMapper.convertValue(chain, Map.class);


        badge_map.replace("chain",chain_map);
        badge_map.put("targets",targetMap);
        badge_map.put("joinedWalletCount", badgeWallets.size());

        return badge_map;
    }

}
