package com.propwave.daotool.badge;

import com.propwave.daotool.badge.model.Badge;
import com.propwave.daotool.badge.model.GetBadgesRes;
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

}
