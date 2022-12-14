package com.propwave.daotool.badge;

import com.propwave.daotool.badge.model.Badge;
import com.propwave.daotool.badge.model.BadgeWallet;
import com.propwave.daotool.badge.model.UserSimple;
import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.config.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/badges")
@CrossOrigin(origins="*")
public class BadgeController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final BadgeProvider badgeProvider;

    public BadgeController(BadgeProvider badgeProvider, BadgeService badgeService){
        this.badgeProvider = badgeProvider;
    }

    // 모든 뱃지 불러오기
    @GetMapping("/")
    public BaseResponse<List<Map<String, Object>>> getAllBadges(@RequestParam String orderBy){
        System.out.println("#11 - badge: get all badges api start");
        List<Map<String, Object>> badges = badgeProvider.getAllBadges(orderBy);
        return new BaseResponse<>(badges);
    }

    // 뱃지의 사용자 불러오기
    @GetMapping("/users")
    public BaseResponse<Map<String,Object>> getBadgeUsers(
            @RequestParam String badgeName,
            @RequestParam("orderBy") String orderBy) throws BaseException
    {
        System.out.println("#12 - badge: get user's all badges api start");
        //1. 뱃지 정보
        Badge badge = badgeProvider.getBadgeInfo(badgeName);
        //2. 뱃지월렛 joinedAt, walletAddress -> 해당 뱃지를 가진 모든 badgeWallet 가져오기
        List<BadgeWallet> badgeWallets = badgeProvider.getBadgeWallet(badgeName, orderBy);
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
