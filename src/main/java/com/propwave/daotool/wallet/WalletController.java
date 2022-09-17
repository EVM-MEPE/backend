package com.propwave.daotool.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.config.BaseResponse;
import com.propwave.daotool.user.UserProvider;
import com.propwave.daotool.user.UserService;
import com.propwave.daotool.user.model.User;
import com.propwave.daotool.wallet.model.UserWalletAndInfo;
import com.propwave.daotool.utils.Utils;
import com.propwave.daotool.wallet.model.NftForDashboard;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.propwave.daotool.config.BaseResponseStatus.*;


@RestController
@CrossOrigin(origins="*")
public class WalletController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final WalletService walletService;
    private final UserProvider userProvider;
    private final UserService userService;
    private final Utils utils;


    public WalletController(WalletService walletService, UserProvider userProvider, UserService userService, Utils utils){
        this.walletService = walletService;
        this.userProvider = userProvider;
        this.userService = userService;
        this.utils = utils;
    }

    @PostMapping("wallets/create")
    public BaseResponse<String> addWalletToUser(@RequestBody Map<String, String> req){
        String jwtToken = req.get("jwtToken");
        String userID = req.get("userID");

        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        int result = walletService.addWalletToUser(userID, req.get("walletAddress"), req.get("walletType"));
        if(result==-1){
            return new BaseResponse<>(WALLET_ALREADY_EXIST_TO_USER);
        }

        return new BaseResponse<>("successfully add wallet to user");
    }

    // 지갑으로부터 유저 불러오기
    @GetMapping("wallets/users")
    public BaseResponse<List<String>> getUsersfromWallet(@RequestParam("walletAddress") String walletAddress) throws BaseException {
        System.out.println("\n Get users from wallet \n");
        if(walletService.isWalletExist(walletAddress)==0){
            return new BaseResponse<>(NO_WALLET_EXIST);
        }
        else{
            List<String> users = walletService.getAllUserByWallet(walletAddress);
            return new BaseResponse<>(users);
        }
    }

    @GetMapping("users/wallet/all")
    public BaseResponse<List<UserWalletAndInfo>> getAllWalletFromUser(@RequestParam("userID") String userID) throws BaseException {
        List<UserWalletAndInfo> userWalletList = walletService.getAllUserWalletByUserId(userID);
        return new BaseResponse<>(userWalletList);
    }

    @PostMapping("wallets/delete")
    public BaseResponse<String> deleteWalletToUser(@RequestBody Map<String, String> req) throws BaseException {
        String jwtToken = req.get("jwtToken");
        String userID = req.get("userID");

        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        walletService.deleteUserWallet(req.get("userID"), req.get("walletAddress"));
        return new BaseResponse<>("successfully delete wallet to user");
    }

    @GetMapping("mypage/collections")
    public BaseResponse<Map<String, Object>> getMyPageCollections(@RequestParam("userID") String userID) throws BaseException, ParseException {
        User user = userProvider.getUser(userID);

        // mvp -> get poap list by api
        List<Map<String, Object>> poapList = walletService.getPoapMypageWithNoDB(userID);
        Map<String, Object> nftList = walletService.getNftMypageWithNoDB(userID);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new SimpleModule());
        Map<String, Object> userMap = objectMapper.convertValue(user, Map.class);

        Timestamp userCreatedAt = user.getCreatedAt();
        userMap.replace("createdAt", userCreatedAt);

        Map<String, Object> result = new HashMap<>();
        result.put("user", userMap);
        result.put("poapList", poapList);
        result.put("nftList", nftList);

        return new BaseResponse<>(result);
    }

    @GetMapping("nfts")
    public BaseResponse<List<NftForDashboard>> getMyNfts(@RequestParam("userId") String userId){
        // 유저의 모든 nftWallet 불러오기
        List<NftForDashboard> nftForDashboardList = walletService.getNftDashboardInfoByUserId(userId);
        return new BaseResponse<>(nftForDashboardList);
    }

    @GetMapping("nfts/refreshLeft")
    public BaseResponse<Integer> getNftRefreshLeft(@RequestParam("userId") String userId) throws BaseException {
        int nftRefreshLeft = walletService.getRefreshLeft(userId);
        return new BaseResponse<>(nftRefreshLeft);
    }

    /**
     ******************************** transaction ********************************
     **/

    @PostMapping("wallet/transactions/remitment")
    public BaseResponse<String> saveRemit(@RequestBody Map<String, String> remitRes){
        String jwtToken = remitRes.get("jwtToken");
        String userID = remitRes.get("fromWallet");

        if(!utils.isUserJwtTokenAvailable(jwtToken, userID)){
            return new BaseResponse<>(USER_TOKEN_WRONG);
        }

        walletService.saveRemit(remitRes);
        userService.createNotification(remitRes.get("toUser"))

        return new BaseResponse<>("Successfully send token");
    }


     /**
     ******************************** nft ********************************
     **/

//    @GetMapping("mypage/refreshCollections")
//    public BaseResponse<Map<Object, Object>> getCollectionsRefresh(@RequestParam("userID") String userId) throws ParseException, BaseException {
//        //1. refresh가 0번 이상 남았는지 확인하기
//        int refreshLeft = userProvider.getRefreshLeft(userId);
//        if(refreshLeft<=0){
//            return new BaseResponse<>(NO_REFRESH_LEFT);
//        }
//
//        // 2. 이 인간의 모든 지갑 불러오기
//        List<UserWalletAndInfo> userWallets = userProvider.getAllUserWalletByUserId(userId);
//
//        //3. POAP 모두 불러오기
//        for(UserWalletAndInfo userWallet:userWallets) {
//            userService.getPoapRefresh(userWallet.getWalletAddress(), userWallet.getUser());
//        }
//
//        //4. NFT 모두 불러오기
//
//
//        //5. POAP, NFT 불러온거 가져오기
//        Map<Object, Object> result = new HashMap<>();
//        List<Nft> nftList = userProvider.getUserNfts(userId);
//        List<PoapWithDetails> poapList = userProvider.getUserPoaps(userId);
//        result.put("nftList", nftList);
//        result.put("poapList", poapList);
//
//        //5. 기타
//        userService.reduceRefreshNftCount(userId);
//
//        return new BaseResponse<>)(result);
//    }
//
//    @GetMapping("nfts/refresh")
//    public BaseResponse<String> getNftRefresh(@RequestParam("userId") String userId) throws BaseException{
//        //1. 이 인간의 Dashboard 지갑 다불러오기
//        List<UserWalletAndInfo> userWallets =  walletService.getAllUserWalletByUserId(userId);
//        for(UserWalletAndInfo userWallet:userWallets){
//            String walletAddress = userWallet.getWalletAddress();
//
//            String api_chain = "polygon";
//            String chain = "Polygon";
//            walletService.getNFTRefresh(walletAddress, api_chain, chain, userWallet.getIndex());
//
//            api_chain = "eth";
//            chain = "Ethereum";
//            walletService.getNFTRefresh(walletAddress, api_chain, chain, userWallet.getIndex());
//
//            api_chain = "avalanche";
//            chain = "Avalanche";
//            walletService.getNFTRefresh(walletAddress, api_chain, chain, userWallet.getIndex());
//
//            walletService.reduceRefreshNftCount(userId);
//        }
//        return new BaseResponse<>("refresh success");
//    }
}