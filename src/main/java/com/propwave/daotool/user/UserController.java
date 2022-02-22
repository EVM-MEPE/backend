package com.propwave.daotool.user;

import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.config.BaseResponse;
import com.propwave.daotool.wallet.model.UserWallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final UserProvider userProvider;
    @Autowired
    private final UserService userService;

    public UserController(UserProvider userProvider, UserService userService){
        this.userProvider = userProvider;
        this.userService = userService;
    }

    // 기존 회원가입 여부 확인
    @PostMapping("/check")
    public BaseResponse<Integer> checkUserSignupAlready(@RequestBody String walletAddress) throws BaseException {
            List<UserWallet> result = userProvider.isWalletRegistered(walletAddress);
            if(result.isEmpty()){
                int login = 0;
                return new BaseResponse<>(login);
            }
            System.out.println(result.size());
            for(UserWallet userWallet : result){
                if (userWallet.isLoginAvailable()){
                    int login = 1;
                    return new BaseResponse<>(login);
                }
            }
            int login = 0;
            return new BaseResponse<>(login);
    }

}
