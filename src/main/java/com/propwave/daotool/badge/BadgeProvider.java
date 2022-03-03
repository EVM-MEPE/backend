package com.propwave.daotool.badge;

import com.propwave.daotool.badge.model.*;
import com.propwave.daotool.config.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.propwave.daotool.config.BaseResponseStatus.*;

@Service
public class BadgeProvider {

    private BadgeDao badgeDao;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    public BadgeProvider(BadgeDao badgeDao){
        this.badgeDao = badgeDao;
    }


    public Badge getBadgeInfo(String badgeName) throws BaseException {
        try{
            System.out.println(badgeName);
            Badge badge = badgeDao.getBadgeInfo(badgeName);
            return badge;
        } catch (Exception exception){
            throw new BaseException(REQUEST_ERROR);
        }
    }

    public List<BadgeWallet> getBadgeWallet(String badgeName, String orderBy) throws BaseException {
        try{

            List<BadgeWallet> badgewallets = badgeDao.getBadgeWalletByBadgeName(badgeName, orderBy);
            return badgewallets;
        } catch(Exception e){
            throw new BaseException(NO_BADGE_EXIST);
        }

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

    public List<Map<String, Object>> getAllBadges(String orderBy){
        List<Map<String, Object>> badges = badgeDao.getAllBadges(orderBy);
        List<Map<String, Object>> result = new ArrayList<>();
        for(Map<String, Object> badge:badges){
            //1. badge의 badge wallet 수 가져오기
            int joinedWalletCount = badgeDao.getBadgeJoinedWalletCount((String) badge.get("name"));
            badge.put("joinedWalletCount", joinedWalletCount);
            result.add(badge);
        }



        // 만약 joined 순서대로면 orderby 해줘야함
        if (orderBy.equals("members") ){
            result = result.stream().sorted((o1, o2) -> o2.get("joinedWalletCount").toString().compareTo(o1.get("joinedWalletCount").toString()) ).collect(Collectors.toList());
            System.out.println(result);
        }

        return result;
    }
}
