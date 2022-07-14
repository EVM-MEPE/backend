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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.*;

//Provider: Read 비즈니스 로직 처리
@Service
public class UserProvider {
    final static String DEFAULT_USER_PROFILE_IMAGE = "https://daotool.s3.ap-northeast-2.amazonaws.com/static/user/d1b5e5d6-fc89-486b-99d6-b2a6894f9eafprofileimg-default.png";

    private final UserDao userDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    public UserProvider(UserDao userDao, UserWalletDao userWalletDao){
        this.userDao = userDao;
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

    public List<String> getAllUserByWallet(String walletAddress) throws BaseException {
        try{
            List<UserWallet> userWallets = userDao.getAllUserByWallet(walletAddress);
            List<String> users = new ArrayList<String>();
            for(UserWallet userWallet:userWallets){
                users.add(userWallet.getUser());
            }
            return users;

        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<Friend> getAllFriends(String userId){
        return userDao.getAllFriends(userId);
    }

    public int getFriendsCount(String userId){
        return userDao.getFriendsCount(userId);
    }

    public String getStatusOfFriendReq(String reqFrom, String reqTo) throws BaseException {
        try{
            FriendReq friendReq = userDao.getFriendReq(reqFrom, reqTo);
            if(friendReq.isAccepted()){
                return "friend";
            }else{
                return "wait";
            }
        }catch(EmptyResultDataAccessException e){
            return "none";
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
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

    public List<UserWalletAndInfo> getAllUserWalletByUserId(String userId) throws BaseException{
        try{
            List<UserWallet> userWallets = userDao.getAllUserWalletByUserId(userId);
            List<UserWalletAndInfo> userWalletsWithInfo = new ArrayList<>();

            for(UserWallet userWallet: userWallets){
                String walletAddress = userWallet.getWalletAddress();
                WalletInfo walletInfo = userDao.getWalletInfo(walletAddress);

                UserWalletAndInfo tmp = new UserWalletAndInfo(userWallet.getIndex(), userWallet.getUser(), userWallet.getWalletAddress(), walletInfo.getWalletType(), walletInfo.getWalletTypeImage(), userWallet.getCreatedAt());
                userWalletsWithInfo.add(tmp);
            }
            return userWalletsWithInfo;

        } catch(Exception exception){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

//    public List<Map<String, Object>> getAllUserWalletByUserId(String userId) throws BaseException{
//        try{
//            ObjectMapper objectMapper = new ObjectMapper();
//
//            List<UserWallet> userWallets = userDao.getAllUserWalletByUserId(userId);
//            System.out.println(userId);
//            System.out.println(userWallets);
//            List<Map<String, Object>> allUserWallets = new ArrayList<>();
//            System.out.println("aaa1");
//            for(UserWallet userWallet: userWallets){
//                String walletAddress = userWallet.getWalletAddress();
//                String walletChain = userWallet.getChain();
//                System.out.println(walletAddress+"       d        "+walletChain);
//
//                Chain chain = userDao.getChainInfo(walletChain);
//                System.out.println("aaa2");
//                WalletInfo walletInfo = userDao.getWalletInfo(walletAddress);
//                System.out.println("aaa3");
//
//                Map<String, Object> userWalletMap = objectMapper.convertValue(userWallet, Map.class);
//                Map<String, Object> chianMap = objectMapper.convertValue(chain, Map.class);
//                Map<String, Object> walletInfoMap = objectMapper.convertValue(walletInfo, Map.class);
//                System.out.println("aaa4");
//
//                userWalletMap.replace("chain",chianMap);
//                userWalletMap.replace("walletAddress",walletInfoMap);
//
//                allUserWallets.add(userWalletMap);
//            }
//            System.out.println("size:"+allUserWallets.size());
//            return allUserWallets;
//
//        } catch(Exception exception){
//            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
//        }
//    }


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

    public List<UserWallet> getAllUserWalletForDashBoardByUserId(String userId){
        return userDao.getAllUserWalletForDashBoardByUserId(userId);
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

    public WalletInfo getWallet(String walletAddress) throws BaseException {
        try{
            return userDao.getWalletInfo(walletAddress);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

//    public List<Nft> getNftsBywalletIdx(int walletIdx){
//        List<NftWallet> nftWallets = userDao.getNftWallets(walletIdx);
//        List<Nft> nfts = new ArrayList<>();
//        for(NftWallet nftWallet:nftWallets){
//            int nftIdx = nftWallet.getNftIndex();
//            Nft nft = userDao.getNFT(nftIdx);
//            nfts.add(nft);
//        }
//
//        return nfts;
//    }

    public List<NftForDashboard> getNftDashboardInfoByUserId(String userId){
        // get all user's userWallet
        List<UserWallet> userWalletList = userDao.getAllUserWalletByUserId(userId);
        List<NftForDashboard> nftForDashboardList = new ArrayList<>();
        // get wallets' all nfts
        for(UserWallet userWallet:userWalletList){
            List<NftWallet> nftWalletList = userDao.getNftWallets(userWallet.getIndex());
                for(NftWallet nftWallet:nftWalletList){
                Nft nft = userDao.getNFT(nftWallet.getNftAddress(), nftWallet.getNftTokenId());
                NftForDashboard nftForDashboard = new NftForDashboard(nft.getIndex(), nftWallet.getIndex(), nft.getAddress(), nft.getTokenID(), nftWallet.isHidden(), nft.getImage());
                nftForDashboardList.add(nftForDashboard);
            }
        }
        return removeNftDashboardDuplicated(nftForDashboardList);
    }

    // util
    public List<NftForDashboard> removeNftDashboardDuplicated(List<NftForDashboard> nftForDashboardList){
        List<String> nftNameList = new ArrayList<>();
        List<NftForDashboard> dupRemovedNftForDashboardList = new ArrayList<>();
        for(NftForDashboard nftForDashboard:nftForDashboardList){
            String name = nftForDashboard.getAddress() + " " + nftForDashboard.getTokenID();
            if(nftNameList.contains(name)){
                continue;
            }
            nftNameList.add(name);
            dupRemovedNftForDashboardList.add(nftForDashboard);
        }
        return dupRemovedNftForDashboardList;
    }

    public int getNftRefreshLeft(String userId)throws BaseException {
        try{
            return userDao.getNftRefreshLeft(userId);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<User> getFollowingList(String userID){
        List<Follow> followingList = userDao.getFollowingList(userID);
        List<User> followingListWithUserInfo = new ArrayList<>();
        for(Follow follow:followingList){
            String following = follow.getFollowing();
            User user = userDao.getUserInfo(following);
            followingListWithUserInfo.add(user);
        }
        return followingListWithUserInfo;
    }

    public List<User> getFollowerList(String userID){
        List<Follow> followerList = userDao.getFollowerList(userID);
        List<User> followerListWithUserInfo = new ArrayList<>();
        for(Follow follow:followerList){
            String follower = follow.getUser();
            User user = userDao.getUserInfo(follower);
            followerListWithUserInfo.add(user);
        }
        return followerListWithUserInfo;
    }

    public int getFollowerCount(String userID){
        return userDao.getFollowerCount(userID);
    }

    public int getFollowingCount(String userID){
        return userDao.getFollowingCount(userID);
    }

    public int isFollowing(String userID1, String userID2){
        return userDao.isFollowExist(userID1, userID2);
    }

}
