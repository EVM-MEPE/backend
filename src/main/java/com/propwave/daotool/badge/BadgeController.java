package com.propwave.daotool.badge;

import com.propwave.daotool.badge.model.Badge;
import com.propwave.daotool.badge.model.BadgeWallet;
import com.propwave.daotool.badge.model.GetBadgesRes;
import com.propwave.daotool.badge.model.UserSimple;
import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.config.BaseResponse;
import com.propwave.daotool.user.UserProvider;
import com.propwave.daotool.wallet.model.UserWallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.propwave.daotool.config.BaseResponseStatus.*;

import java.util.*;

import static com.propwave.daotool.config.BaseResponseStatus.USER_NOT_EXISTS;

@RestController
@RequestMapping("/badges")
public class BadgeController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final BadgeProvider badgeProvider;
    @Autowired
    private final BadgeService badgeService;
    @Autowired
    private final UserProvider userProvider;

    public BadgeController(BadgeProvider badgeProvider, BadgeService badgeService, UserProvider userProvider){
        this.badgeProvider = badgeProvider;
        this.badgeService = badgeService;
        this.userProvider = userProvider;
    }

    //사용자가 가진 뱃지 불러오기
    @GetMapping("/")
    public BaseResponse<List<GetBadgesRes>> GetBadges(@RequestParam("userId") String userId) throws BaseException {
        if(badgeProvider.checkUser(userId)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        // 해당 user의 모든 지갑 정보 가져오기
        List<UserWallet> userWalletListByUser = userProvider.getAllUserWalletByUserId(userId);

        List<GetBadgesRes> getBadgesResList = new ArrayList<>();
        for (UserWallet userWallet : userWalletListByUser) {
            System.out.println(userWallet);
            if (userWallet.isViewDataAvailable()) {
                getBadgesResList.addAll(badgeProvider.getBadges(userWallet.getWalletAddress()));
            }
        }
        return new BaseResponse<>(getBadgesResList);
    }

    // 뱃지의 사용자 불러오기
    @GetMapping("/users")
    public BaseResponse<Map<String,Object>> getBadgeUsers(@RequestParam String badgeName) throws BaseException{
        //1. 뱃지 정보
        Badge badge = badgeProvider.getBadgeInfo(badgeName);
        //2. 뱃지월렛 joinedAt, walletAddress -> 해당 뱃지를 가진 모든 badgeWallet 가져오기
        List<BadgeWallet> badgeWallets = badgeProvider.getBadgeWallet(badgeName);
        //3. 유저월렛 user -> wallet의 user 가져오기
        List<Map<String, Object>> badgeUsers = new ArrayList<>();
        for(BadgeWallet badgeWallet:badgeWallets){
            List<UserSimple> users = badgeProvider.getUserSimple(badgeWallet.getWalletAddress());

            for(UserSimple user:users){
                Map<String, Object> badgeUser = new HashMap<>();
                badgeUser.put("user", user);
                badgeUser.put("walletAddress",badgeWallet.getWalletAddress());
                badgeUser.put("badgeJoinedDate", badgeWallet.getJoinedAt());
                badgeUsers.add(badgeUser);
            }

        }
        Map<String,Object> response = new HashMap<>();
        response.put("badgeName", badgeName);
        response.put("walletCount", badgeWallets.size());
        response.put("badgeUsers", badgeUsers);
        return new BaseResponse<>(response);
    }

}
