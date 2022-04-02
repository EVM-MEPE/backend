package com.propwave.daotool.user;

import com.propwave.daotool.badge.model.BadgeWallet;
import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.user.model.BadgeRequest;
import com.propwave.daotool.user.model.User;
import com.propwave.daotool.user.model.UserSignupReq;
import com.propwave.daotool.user.model.WalletSignupReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
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

    public User createUser(Map<String, Object> userInfo, String profileImageS3Path) throws BaseException{
        try{
            userInfo.replace("profileImage", profileImageS3Path);
            return userDao.createUser(userInfo);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public User createUser(UserSignupReq userSignupReq, String profileImageS3Path) throws BaseException{
        try{
            Map<String, Object> userInfo = new LinkedHashMap<>();
            userInfo.put("id", userSignupReq.getId());
            userInfo.put("profileImage",profileImageS3Path);
            userInfo.put("introduction", userSignupReq.getIntroduction());
            userInfo.put("url", userSignupReq.getUrl());

            return userDao.createUser(userInfo);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public User editUser(Map<String, String> userInfo, String profileImageS3Path) throws BaseException{
        try{
            System.out.println(userInfo);
            System.out.println(profileImageS3Path);
            userInfo.put("profileImage", profileImageS3Path);
            System.out.println(userInfo);
            return userDao.editUser(userInfo);
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

    public String createWallet(String walletAddress, String walletType) throws BaseException {
        try{
            System.out.println(walletAddress);
            String newWallet = userDao.createWallet(walletAddress, walletType);
            return newWallet;
        } catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public String createUserWallet(Map<String, Object> wallet, String userId) throws BaseException {
        try{
            return userDao.createUserWallet(wallet, userId);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public String createUserWallet(WalletSignupReq wallet, String userId) throws BaseException {
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

    public void makeLoginAvailable(int index)throws BaseException{
        try{
            userDao.makeLoginAvailable(index);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public int makeLoginUnavailable(String userId, String walletAddress) throws BaseException{
        try{
            return userDao.makeLoginUnavailable(userId, walletAddress);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public int deleteUserWallet(String userId, String walletAddress) throws BaseException{
        try{
            return userDao.deleteUserWallet(userId, walletAddress);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public int deleteWallet(String walletAddress) throws BaseException{
        try{
            return userDao.deleteWallet(walletAddress);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // dashboard용 지갑 추가하기
    public int createDashboardWallet(String userId, Map<String, Object> wallet) throws BaseException {
        String walletAddress = (String) wallet.get("walletAddress");
        wallet.put("loginAvailable", false);
        wallet.put("viewDataAvailable", true);
        wallet.put("user", userId);
        int isUserWalletExistMe = userDao.isUserWalletExist(userId, walletAddress);
        System.out.println(isUserWalletExistMe);
        int isUserWalletExistOther = userDao.isUserWalletExist(walletAddress);
        System.out.println(isUserWalletExistOther);
        // 1. 나게에 있는지 여부 확인
        if (userDao.isUserWalletExist(userId, walletAddress)==1){
            // 나에게 있는 경우 -> 대시보드용으로 추가하기
            System.out.println("경우 3. 나에게 지갑이 있는 경우 -> 대시보드용으로 추가하기!");
            return userDao.makeViewDataAvailable(userId, walletAddress);
        }
        // 2. 나에게 없다면, 남에게 있는지 확인
        else if (userDao.isUserWalletExist(walletAddress)==1){
            // 남에게 있는 경우 -> 나의 UserWallet 생성하기
            System.out.println("경우 2. 나에게 지갑이 없고 남에게 있는 경우 -> UserWallet 추가");
            userDao.createUserWallet(wallet);
            return 1;
        }
        else if (userDao.isUserWalletExist(walletAddress)==0){
            // 남에게도 없는 경우 -> wallet 생성, userWallet 생성
            System.out.println("경우 1. 나에게도, 남에게도 지갑이 없는 경우 -> UserWallet, Wallet 추가");
            userDao.createWallet(walletAddress, (String)wallet.get("walletType"));
            userDao.createUserWallet(wallet);
            return 1;
        }
        else{
            System.out.println("경우 없는 경우");
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    // 대시보드용 지갑 수정하기
    public int updateDashboardWallet(String userId, Map<String, Object> wallet) throws BaseException{
        wallet.put("user", userId);
        try {
            return userDao.editUserWallet(wallet);
        }catch(Exception e){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 대시보드용 지갑 삭제하기
    public int deleteDashboardWallet(String userId, Map<String, Object> wallet) throws BaseException{
        wallet.put("user", userId);
        String walletAddress = (String) wallet.get("walletAddress");
        // 1. 지갑이 남에게 있는지 여부
        int isWalletSomeoneElse = userProvider.isWalletSomeoneElse(userId, walletAddress);
        System.out.println(isWalletSomeoneElse);
        // 2. 나에게 로그인 용도 있는지 여부
        int isWalletMyLogin = userDao.isWalletExistForLogin(userId, walletAddress);
        System.out.println(isWalletMyLogin);

        // 상황 1. 나에게만 지갑이 있고 Only 대시보드용
        if (isWalletSomeoneElse==0 && isWalletMyLogin == 0){
            // userWallet 삭제, wallet 삭제
            System.out.println("상황 1. 나에게만 지갑이 있고 Only 대시보드용");
            userDao.deleteUserWallet(userId, walletAddress);
            return userDao.deleteWallet(walletAddress);
       }
        // 상황 2. 나에게만 지갑이 있고, 로그인도 있음 & 상황 4. 남에게 지갑이 있고, 로그인도 있음
        else if (isWalletMyLogin == 1){
            // userWallet의 dashboard를 0으로 변경
            System.out.println("상황 2. 나에게만 지갑이 있고, 로그인도 있음 & 상황 4. 남에게 지갑이 있고, 로그인도 있음");
            return userDao.makeViewDataUnavailable(userId, walletAddress);
        }
        // 상황 3. 남에게 지갑이 있고, 나에게 Only 대시보드용
        else if (isWalletSomeoneElse==1 && isWalletMyLogin == 0){
            System.out.println("상황 3. 남에게 지갑이 있고, 나에게 Only 대시보드용");
            // 내 userWallet 삭제
            return userDao.deleteUserWallet(userId, walletAddress);
        }
        // 그 외
        else {
            System.out.println("경우 없는 경우...");
            throw new BaseException(RESPONSE_ERROR);
        }
    }

    public BadgeRequest createBadgeRequest(String badgeName, Map<String, String> request){
        request.put("badgeName", badgeName);
        return userDao.createBadgeRequest(request);
    }

    public BadgeRequest processBadgeRequest(int index) throws BaseException {
        //1. 해당 인덱스에 해당하는 badgeRequest 가져오기
        try {
            BadgeRequest badgeRequest = userDao.getBadgeRequest(index);
            System.out.println(badgeRequest);
            // 2. badgeWallet 추가하기
            BadgeWallet newBadgeWallet = userDao.createBadgeWallet(badgeRequest.getDestWalletAddress(), badgeRequest.getBadgeName());
            System.out.println(newBadgeWallet);
            //3. badgeReqeust 값 수정하기
            return userDao.updateBadgeRequest(index);
        }catch(Exception exception){
            throw new BaseException(RESPONSE_ERROR);
        }
            }
}
