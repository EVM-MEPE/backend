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
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
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
        List<Map<String, Object>> badges = new ArrayList<>();
        List<Map<String, Object>> badgeTmp = new ArrayList<>();
        for (UserWallet userWallet : userWalletListByUser) {
            System.out.println(userWallet.isViewDataAvailable());
            if (userWallet.isViewDataAvailable()) {
                badgeTmp = userProvider.getAllBadge(userWallet.getWalletAddress());
                badges.addAll(badgeTmp);
            }
        }
        // 뱃지 중복 제거
        HashSet<Map<String, Object>> set = new HashSet<Map<String, Object>>(badges);
        List<Map<String, Object>> newAllBadge = new ArrayList<Map<String, Object>>(set);

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
        List<Map<String, Object>> badges = new ArrayList<>();
        List<Map<String, Object>> badgeTmp = new ArrayList<>();
        for (UserWallet userWallet : userWalletList) {
            System.out.println(userWallet.isViewDataAvailable());
            if (userWallet.isViewDataAvailable()) {
                badgeTmp = userProvider.getAllBadge(userWallet.getWalletAddress());
                badges.addAll(badgeTmp);
            }
        }
        // 뱃지 중복 제거
        HashSet<Map<String, Object>> set = new HashSet<Map<String, Object>>(badges);
        List<Map<String, Object>> newAllBadge = new ArrayList<Map<String, Object>>(set);

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
    public BaseResponse<List<GetBadgesRes>> getBadges(@RequestParam("userId") String userId) throws BaseException
    {
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

    //login용 지갑 추가
    @PostMapping("/wallets/login")
    public BaseResponse<String> addLoginWallet(@RequestBody Map<String, Object> request){
            //1. 사용자 정보 받아서 1) 지갑 만들고, 2) UserWallet 만들고.
            try{
                String newWalletAddress = (String) request.get("address");
                // 경우 1. 지갑이 아예 없었던 새로인 친구인 경우
                if (userProvider.isWalletExist(newWalletAddress)==0) {
                    //없으면 객체 만들기 -> 경우 1. 완전 처음 들어오는 지갑인 경우
                    userService.createWallet(newWalletAddress);
                    //2. userWallet 만들기
                    request.put("walletName", "");
                    request.put("loginAvailable", true);
                    request.put("viewDataAvailable", false);
                    userService.createUserWallet(request, (String) request.get("user"));
                    return new BaseResponse<>("로그인 지갑이 추가되었습니다.");
                }
                else{
                    System.out.println("지갑 있음!");
                    String user = (String)request.get("user");
                    // 경우 4. 지갑이 이미 있고, 다른 유저의 로그인용으로 있는 경우
                    if(userProvider.isWalletExistForLogin(newWalletAddress)==1){
                        System.out.println("경우4");
                        return new BaseResponse<>(WALLET_ALREADY_EXIST_FOR_LOGIN);
                    }
                    // 경우 2. 지갑자체는 이미 있고, 이 유저한테는 지갑이 없는 경우
                    if(userProvider.isUserWalletByWalletAddressAndUserIdExist(user, newWalletAddress)==0){
                        //2. userWallet 만들기
                        System.out.println("경우2");
                        request.put("walletName", "");
                        request.put("loginAvailable", true);
                        request.put("viewDataAvailable", false);
                        userService.createUserWallet(request, (String) request.get("user"));
                        return new BaseResponse<>("로그인 지갑이 추가되었습니다.");
                    }
                    // 경우 3. 지갑은 이미 있고, 이 유저한테 지갑이 대시보드용으로 있는 경우
                    UserWallet userWallet = userProvider.getUserWalletByWalletAddressAndUserId(user, newWalletAddress);
                    if(!userWallet.isLoginAvailable() && userWallet.isViewDataAvailable()){
                        System.out.println("경우3");
                        userService.makeLoginAvailable(userWallet.getIndex());
                        return new BaseResponse<>("로그인 지갑이 추가되었습니다.");
                    }
                    System.out.println("경우 없는 경우...");
                    return new BaseResponse<>(RESPONSE_ERROR);
                }
        } catch (BaseException e) {
                return new BaseResponse<>((e.getStatus()));
        }
    }

    //login용 지갑 삭제
    @DeleteMapping("/wallets/login")
    public BaseResponse<String> deleteLoginWallet(@RequestBody Map<String, String> request) throws BaseException {
        System.out.println("지갑 삭제 고");
        String userId = request.get("userId");
        String walletAddress = request.get("walletAddress");
        // 1. 지갑이 남에게 있는지 여부
        int isWalletSomeoneElse = userProvider.isWalletSomeoneElse(userId, walletAddress);
        System.out.println(isWalletSomeoneElse);
        // 2. 나에게 대시보드 용도 있는지 여부
        int isWalletMyDashboard = userProvider.isWalletMyDashboard(userId, walletAddress);
        System.out.println(isWalletMyDashboard);

        // 상황 1. 나에게만 지갑이 있고 Only 로그인용
        if (isWalletSomeoneElse==0 && isWalletMyDashboard == 0){
            // userWallet 삭제, wallet 삭제
            System.out.println("상황 1. 나에게만 지갑이 있고 Only 로그인용");
            userService.deleteUserWallet(userId, walletAddress);
            userService.deleteWallet(walletAddress);
            return new BaseResponse<>("상황1) 나에게만 지갑이 있고 Only 로그인용 -> userWallet 삭제, wallet 삭제");
        }
        // 상황 2. 나에게만 지갑이 있고, 대시보드용도 있음 & 상황 4. 남에게 지갑이 있고, 대시보드용도 있음
        else if (isWalletMyDashboard == 1){
            // userWallet의 login을 0으로 변경
            System.out.println("상황 2. 나에게만 지갑이 있고, 대시보드용도 있음 & 상황 4. 남에게 지갑이 있고, 대시보드용도 있음");
            userService.makeLoginUnavailable(userId, walletAddress);
            return new BaseResponse<>("상황2, 4) 대시보드용 있음 -> userWallet의 login을 0으로 변경");
        }
        // 상황 3. 남에게 지갑이 있고, 나에게 Only 로그인용
        else if (isWalletSomeoneElse==1 && isWalletMyDashboard == 0){
            System.out.println("상황 3. 남에게 지갑이 있고, 나에게 Only 로그인용");
            // 내 userWallet 삭제
            userService.deleteUserWallet(userId, walletAddress);
            return new BaseResponse<>("상황3) 남에게 지갑이 있고, 나에게 Only 로그인용 -> 내 userWallet 삭제");
        }
        // 그 외
        else {
            System.out.println("경우 없는 경우...");
            return new BaseResponse<>(RESPONSE_ERROR);
        }

    }
}
