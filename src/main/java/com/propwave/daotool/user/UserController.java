package com.propwave.daotool.user;

import com.propwave.daotool.badge.model.Badge;
import com.propwave.daotool.badge.model.GetBadgesRes;
import com.propwave.daotool.commons.S3Uploader;
import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.config.BaseResponse;
import com.propwave.daotool.config.jwt.SecurityService;
import com.propwave.daotool.user.model.User;
import com.propwave.daotool.wallet.model.UserWallet;
//import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

import static com.propwave.daotool.config.BaseResponseStatus.*;

@RestController
@RequestMapping("/users")
public class UserController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final S3Uploader s3Uploader;

    @Autowired
    private final UserProvider userProvider;
    @Autowired
    private final UserService userService;
    @Autowired
    private SecurityService securityService;

    public UserController(S3Uploader s3Uploader, UserProvider userProvider, UserService userService, SecurityService securityService){
        this.s3Uploader = s3Uploader;
        this.userProvider = userProvider;
        this.userService = userService;
        this.securityService = securityService;
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

    @PostMapping("/signup")
    public BaseResponse<Map<String, Object>> UserSignUp(@RequestBody Object json){
        Map<String, Object> json2 = (Map<String, Object>) json;
        Map<String, Object> userInfo = (Map<String, Object>) json2.get("userInfo");

        List<Map<String, Object>> wallets = (List<Map<String, Object>>) json2.get("wallet");
        //1. 사용자 정보 받아서 1) 지갑 만들고, 2) User 만들고, 3) UserWallet 만들고.
        //1) User 만들기
        try{

            //1. user 만들기
            // image S3에 올리기 -> 일단 예시로 한것임...! (test용)
            File file = new File((String) userInfo.get("profileImage"));
            FileInputStream input = new FileInputStream(file);
            MultipartFile multipartFile = new MockMultipartFile("file",
                    file.getName(),
                    "text/plain",
                    IOUtils.toByteArray(input));

            String imagePath = s3Uploader.upload(multipartFile, "media/user/profileImage");
            User newUser = userService.createUser(userInfo, imagePath);
            //newUser의 JWT 토큰 만들기
            String token = securityService.createToken(newUser.getId(), (120*1000*60)); // 토큰 유효시간 2시간
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", newUser.getId());
            map.put("token", token);



            // 2. 지갑 만들기
            for(Map<String, Object> wallet : wallets){
                //1. 지갑 만들기
                // 로그인 가능이면 token 만들어줘야할듯

                // 지갑 객체가 이미 있는 친구인지 확인하기
                String walletAddress = (String) wallet.get("address");
                if (userProvider.isWalletExist(walletAddress)==0) {
                    //없으면 객체 만들기
                    userService.createWallet(walletAddress);
                }
                //2. userWallet 만들기
                userService.createUserWallet(wallet, newUser.getId());
            }

            return new BaseResponse<>(map);
        } catch (IOException e) {
            e.printStackTrace();
            return new BaseResponse<>(S3_UPLOAD_ERROR);
        } catch (BaseException e) {
            e.printStackTrace();
            return new BaseResponse<>(e.getStatus());
        }
    }

    @PostMapping("/login")
    public BaseResponse<Map<String, Object>> userLogin(@RequestBody Map<String, String> walletAddress) throws BaseException {
        // 유저가 로그인하면 줄거? token, user 모든 정보, 연결된 지갑들의 모든 뱃지
        // 1. 유저 정보
        // 해당 주소에 연결된 user의 모든 정보 가져오기
        List<UserWallet> userWalletList = userProvider.getAllUserWalletByWallet(walletAddress.get("walletAddress"));
        String userId = null;
        User user = null;
        for (UserWallet userWallet : userWalletList) {
            if (userWallet.isLoginAvailable()) {
                userId = userWallet.getUser();
                userService.addHit(userId);
                user = userProvider.getUser(userId);
                break;
            }
        }
        // 해당 user의 모든 지갑 정보 가져오기
        List<UserWallet> userWalletListByUser = userProvider.getAllUserWalletByUserId(userId);

        // 유저의 viewDataAvailable이 true인 친구들의 뱃지 데려오기... 힘들다 힘들어
        List<Badge> badges = new ArrayList<>();
        List<Badge> badgeTmp = new ArrayList<>();
        for (UserWallet userWallet : userWalletListByUser) {
            System.out.println(userWallet.isViewDataAvailable());
            if (userWallet.isViewDataAvailable()) {
                badgeTmp = userProvider.getAllBadge(userWallet.getWalletAddress());
                badges.addAll(badgeTmp);
            }
        }
        // 뱃지 중복 제거
        HashSet<Badge> set = new HashSet<Badge>(badges);
        List<Badge> newAllBadge = new ArrayList<Badge>(set);

        String token = securityService.createToken(user.getId(), (120*1000*60)); // 토큰 유효시간 2시간

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user", user);
        result.put("token", token);
        result.put("wallets", userWalletListByUser);
        result.put("badges", newAllBadge);

        return new BaseResponse<>(result);
    }

    @GetMapping("/mypage")
    public BaseResponse<Map<String, Object>> userLogin(@RequestParam String userId) throws BaseException {
        // 유저가 로그인하면 줄거? token, user 모든 정보, 연결된 지갑들의 모든 뱃지
        // 1. 유저 정보
        userService.addHit(userId);
        User user = userProvider.getUser(userId);

        // 해당 주소에 연결된 user의 모든 정보 가져오기
        List<UserWallet> userWalletList = userProvider.getAllUserWalletByUserId(userId);

        // 유저의 viewDataAvailable이 true인 친구들의 뱃지 데려오기... 힘들다 힘들어
        List<Badge> badges = new ArrayList<>();
        List<Badge> badgeTmp = new ArrayList<>();
        for (UserWallet userWallet : userWalletList) {
            System.out.println(userWallet.isViewDataAvailable());
            if (userWallet.isViewDataAvailable()) {
                badgeTmp = userProvider.getAllBadge(userWallet.getWalletAddress());
                badges.addAll(badgeTmp);
            }
        }
        // 뱃지 중복 제거
        HashSet<Badge> set = new HashSet<Badge>(badges);
        List<Badge> newAllBadge = new ArrayList<Badge>(set);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user", user);
        result.put("wallets", userWalletList);
        result.put("badges", newAllBadge);

        return new BaseResponse<>(result);
    }

//    @PatchMapping("/{userId}/profile")
//    public BaseResponse<String> editUserProfile(@PathVariable("userId") int userId, @RequestBody String token){
//        //
//    }

    //사용자가 가진 뱃지 불러오기
    @GetMapping("/badges")
    public BaseResponse<List<GetBadgesRes>> getBadges(@RequestParam("userId") String userId) throws BaseException {
        if(userProvider.checkUser(userId)==0){
            return new BaseResponse<>(USER_NOT_EXISTS);
        }

        // 해당 user의 모든 지갑 정보 가져오기
        List<UserWallet> userWalletListByUser = userProvider.getAllUserWalletByUserId(userId);

        List<GetBadgesRes> getBadgesResList = new ArrayList<>();
        for (UserWallet userWallet : userWalletListByUser) {
            System.out.println(userWallet);
            if (userWallet.isViewDataAvailable()) {
                getBadgesResList.addAll(userProvider.getBadges(userWallet.getWalletAddress()));
            }
        }
        return new BaseResponse<>(getBadgesResList);
    }
}
