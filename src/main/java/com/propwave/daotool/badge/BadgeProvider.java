package com.propwave.daotool.badge;

import com.propwave.daotool.badge.model.*;
import com.propwave.daotool.config.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.propwave.daotool.config.BaseResponseStatus.*;

@Service
public class BadgeProvider {

    private BadgeDao badgeDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    public BadgeProvider(BadgeDao badgeDao){
        this.badgeDao = badgeDao;
    }

    public List<GetBadgesRes> getBadges(String walletAddress){
        // 뱃지 이름, join 한 날짜 가져오기
        List<BadgeJoinedAt> badgeJoinedAt = badgeDao.getBadgeJoinedAt(walletAddress);

        List<GetBadgesRes> getBadgesRes = new ArrayList<>();
        //뱃지 이름가지고 이름, 이미지 가져오기 -> getbadgeres 만들기
        for(BadgeJoinedAt badge: badgeJoinedAt){
            // badge 의 이름하고 이미지 가져옴
            BadgeNameImage badgeTmp = badgeDao.getBadgeNameImage(badge.getBadgeName());
            //
            GetBadgesRes badgeResTmp = new GetBadgesRes(badge.getBadgeName(), badgeTmp.getImage(), badge.getJoinedAt());
            getBadgesRes.add(badgeResTmp);
        }
        return getBadgesRes;
    }

    public int checkUser(String userId){
        return badgeDao.checkUser(userId);
    }

    public Badge getBadgeInfo(String badgeName) throws BaseException{
        try{
            Badge badge = badgeDao.getBadgeInfo(badgeName);
            return badge;
        } catch(Exception exception){
            throw new BaseException(NO_BADGE_EXIST);
        }
    }

    public List<BadgeWallet> getBadgeWallet(String badgeName){
        List<BadgeWallet> badgewallets = badgeDao.getBadgeWalletByBadgeName(badgeName);
        return badgewallets;
    }

    public List<UserSimple> getUserSimple(String walletId){
        // 해당 wallet을 가진 유저 모두 가져오기
        // 1. userWallet에서 유저 리스트 가져오기
        List<UserDataAvailable> userList = badgeDao.getUserNameByWallet(walletId);
        // 2. 유저 이름으로 유저 상세 정보 가져오기
        List<UserSimple> userSimpleList = new ArrayList<>();
        for(UserDataAvailable user: userList){
            if(user.isViewDataAvailable()){
                userSimpleList.addAll(badgeDao.getUserSimple(user.getUser()));
            }
        }
        return userSimpleList;
    }
}
