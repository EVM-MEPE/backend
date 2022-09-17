package com.propwave.daotool.wallet;

import com.propwave.daotool.config.BaseException;
import com.propwave.daotool.config.BaseResponseStatus;
import com.propwave.daotool.user.model.UserWallet;
import com.propwave.daotool.wallet.model.UserWalletAndInfo;
import com.propwave.daotool.utils.GetNFT;
import com.propwave.daotool.utils.GetPOAP;
import com.propwave.daotool.wallet.model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

import static com.propwave.daotool.config.BaseResponseStatus.DATABASE_ERROR;

@Service
public class WalletService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final WalletDao walletDao;
    private final GetNFT getNFT;
    private final GetPOAP getPOAP;

    public WalletService(WalletDao walletDao, GetNFT getNFT, GetPOAP getPOAP){
        this.walletDao = walletDao;
        this.getNFT = getNFT;
        this.getPOAP = getPOAP;
    }

    public int addWalletToUser(String userID, String walletAddress, String walletType){
        //이미 있는 지갑인지 확인하기
        int walletExist = walletDao.isWalletExist(walletAddress);
        //이미 나에게 있는 지갑인지 확인하기
        int walletExistToMe = walletDao.isUserWalletExist(userID, walletAddress);
        if(walletExistToMe==1){
            return -1;
        }

        if(walletExist == 0){
            walletDao.createWallet(walletAddress, walletType);
        }
        return walletDao.createUserWallet(userID, walletAddress);
    }

    public int deleteUserWallet(String userId, String walletAddress) throws BaseException {
        try{
            return walletDao.deleteUserWallet(userId, walletAddress);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void reduceRefreshNftCount(String userId){
        walletDao.reduceRefreshNftCount(userId);
    }

    public List<Map<String, Object>> getPoapMypageWithNoDB(String userId) throws ParseException, BaseException {
        // user의 모든 지갑 불러오기
        List<UserWalletAndInfo> userWalletAndInfos = getAllUserWalletByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        int our_event_id = 57439;
        List<Map<String, Object>> ourPoapList = new ArrayList<>();

        for(UserWalletAndInfo userWallet: userWalletAndInfos){
            // POAP 가져오기
            String walletAddress = userWallet.getWalletAddress();
            String poapResult = getPOAP.getPOAP(walletAddress);
            if(poapResult.equals("")){
                continue;
            }
            JSONArray jsonList = getPOAP.fromJSONtoPOAPList(poapResult);
            for(Object json:jsonList){
                JSONObject jsonObject = (JSONObject) json;
                Map<Object, Object> event = (Map) jsonObject.get("event");
                Map<String, Object> tmp = new HashMap<>();
                Long id = (Long)event.get("id");

                if(id.intValue() == our_event_id){
                    Map<String, Object> ourPOAP = new HashMap<>();
                    ourPOAP.put("event_id", our_event_id);
                    ourPOAP.put("image_url", event.get("image_url"));
                    ourPOAP.put("createdAt", jsonObject.get("created"));
                    ourPoapList.add(ourPOAP);
                    continue;
                }

                tmp.put("event_id", event.get("id"));
                tmp.put("image_url", event.get("image_url"));
                tmp.put("createdAt", jsonObject.get("created"));

                result.add(tmp);
            }

        }


        result.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return -(Timestamp.valueOf((String) o1.get("createdAt"))).compareTo(Timestamp.valueOf((String) o2.get("createdAt")));
            }
        });

        for(Map<String, Object> element:ourPoapList){
            result.add(0, element);
        }

        if(result.size()>8){
            result = result.subList(0,8);
        }

        return result;
    }

    public Map<String, Object> getNftMypageWithNoDB(String userId) throws ParseException, BaseException{
        System.out.println("get nft!!!");
        // user의 모든 지갑 불러오기
        List<UserWalletAndInfo> userWalletAndInfos = getAllUserWalletByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> ethR = new ArrayList<>();
        List<Map<String, Object>> polyR = new ArrayList<>();
        List<Map<String, Object>> cosR = new ArrayList<>();
        List<Map<String, Object>> solR = new ArrayList<>();

        for(UserWalletAndInfo userWallet: userWalletAndInfos){
            // POAP 가져오기
            String walletAddress = userWallet.getWalletAddress();
            String wallet = walletDao.getWalletChain(walletAddress);
            JSONArray jsonList = null;
            if(wallet.equals("Keplr")){
                jsonList = getNFT.getNFTs("stargaze", walletAddress);

                for(Object json:jsonList){
                    JSONObject jsonObject = (JSONObject) json;

                    Map<String, Object> tmp = new HashMap<>();
                    String imgUrl = checkImgUrl((String)jsonObject.get("image"));
                    tmp.put("image", imgUrl);
                    //tmp.put("image", jsonObject.get("image"));
                    tmp.put("title", jsonObject.get("name"));
                    tmp.put("obtainedAt", "none");
                    tmp.put("hidden", false);
                    cosR.add(tmp);
                }

            }else if(wallet.equals("Metamask")){
                JSONArray EjsonList = getNFT.getNFTs("eth", walletAddress);
                JSONArray PjsonList = getNFT.getNFTs("polygon", walletAddress);

                for(Object json:EjsonList){
                    JSONObject jsonObject = (JSONObject) json;
                    String token_uri = (String) jsonObject.get("token_uri");
                    Map<String, String> hashMap = new HashMap();
                    String metadata = getNFT.externalGetAPIInterface(token_uri, "", hashMap);
                    try{
                        if(metadata.isEmpty()){
                            continue;
                        }
                    }catch(NullPointerException e){
                        try{
                            metadata = (String) jsonObject.get("metadata");
                        }catch(NullPointerException e2){
                            continue;
                        }
                    }

                    Map<String, String> metadataJson = (Map) getNFT.fromJSONtoNFT(metadata);
                    Map<String, Object> tmp = new HashMap<>();
                    String imgUrl = checkImgUrl(metadataJson.get("image"));
                    tmp.put("image", imgUrl);
                    //tmp.put("image", metadataJson.get("image"));
                    tmp.put("title", metadataJson.get("name"));
                    tmp.put("obtainedAt", jsonObject.get("block_number"));
                    tmp.put("hidden", false);
                    ethR.add(tmp);
                }

                for(Object json:PjsonList){
                    JSONObject jsonObject = (JSONObject) json;
                    String token_uri = (String) jsonObject.get("token_uri");
                    Map<String, String> hashMap = new HashMap();
                    String metadata = getNFT.externalGetAPIInterface(token_uri, "", hashMap);

                    try{
                        if(metadata.isEmpty()){
                            continue;
                        }
                    }catch(NullPointerException e){
                        try{
                            metadata = (String) jsonObject.get("metadata");
                        }catch(NullPointerException e2){
                            continue;
                        }
                    }

                    Map<String, String> metadataJson = (Map) getNFT.fromJSONtoNFT(metadata);
                    Map<String, Object> tmp = new HashMap<>();
                    String imgUrl = checkImgUrl(metadataJson.get("image"));
                    tmp.put("image", imgUrl);
                    //tmp.put("image", metadataJson.get("image"));
                    tmp.put("title", metadataJson.get("name"));
                    tmp.put("obtainedAt", jsonObject.get("block_number"));
                    tmp.put("hidden", false);
                    polyR.add(tmp);
                }
            }else if(wallet.equals("Phantom")){
                jsonList = getNFT.getNFTs("solana", walletAddress);

                for(Object json:jsonList){
                    JSONObject jsonObject = (JSONObject) json;

                    Map<String, Object> tmp = new HashMap<>();
                    String imgUrl = checkImgUrl((String)jsonObject.get("image"));
                    tmp.put("image", imgUrl);
                    tmp.put("title", jsonObject.get("name"));
                    tmp.put("obtainedAt", "none");
                    tmp.put("hidden", false);
                    solR.add(tmp);
                }
            }
        }

        ethR.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return -(Float.compare(Float.parseFloat((String) o1.get("obtainedAt")),Float.parseFloat((String) o2.get("obtainedAt"))));
            }
        });

        polyR.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return -(Float.compare(Float.parseFloat((String) o1.get("obtainedAt")),Float.parseFloat((String) o2.get("obtainedAt"))));
            }
        });

        cosR.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return (((String) o1.get("title")).compareTo((String) o2.get("title")));
            }
        });

        solR.sort(new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return (((String) o1.get("title")).compareTo((String) o2.get("title")));
            }
        });

        if(ethR.size()>8){
            ethR = ethR.subList(0,8);
        }

        if(polyR.size()>8){
            polyR = polyR.subList(0,8);
        }

        result.put("ethereum", ethR);
        result.put("polygon", polyR);
        result.put("keplr", cosR);
        result.put("solana", solR);

        return result;
    }

    // 지갑 주소가 wallet에 있는지 확인
    public int isWalletExist(String walletAddress) throws BaseException {
        try{
            return walletDao.isWalletExist(walletAddress);
        } catch(Exception exception){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public List<String> getAllUserByWallet(String walletAddress) throws BaseException {
        try{
            List<UserWallet> userWallets = walletDao.getAllUserByWallet(walletAddress);
            List<String> users = new ArrayList<>();
            for(UserWallet userWallet:userWallets){
                users.add(userWallet.getUser());
            }
            return users;

        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<UserWalletAndInfo> getAllUserWalletByUserId(String userId) throws BaseException{
        try{
            List<UserWallet> userWallets = walletDao.getAllUserWalletByUserId(userId);
            List<UserWalletAndInfo> userWalletsWithInfo = new ArrayList<>();

            for(UserWallet userWallet: userWallets){
                String walletAddress = userWallet.getWalletAddress();
                WalletInfo walletInfo = walletDao.getWalletInfo(walletAddress);

                UserWalletAndInfo tmp = new UserWalletAndInfo(userWallet.getIndex(), userWallet.getUser(), userWallet.getWalletAddress(), walletInfo.getWalletType(), walletInfo.getWalletTypeImage(), userWallet.getChain(), userWallet.getCreatedAt());
                userWalletsWithInfo.add(tmp);
            }
            return userWalletsWithInfo;

        } catch(Exception exception){
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public List<NftForDashboard> getNftDashboardInfoByUserId(String userId){
        // get all user's userWallet
        List<UserWallet> userWalletList = walletDao.getAllUserWalletByUserId(userId);
        List<NftForDashboard> nftForDashboardList = new ArrayList<>();
        // get wallets' all nfts
        for(UserWallet userWallet:userWalletList){
            List<NftWallet> nftWalletList = walletDao.getNftWallets(userWallet.getIndex());
            for(NftWallet nftWallet:nftWalletList){
                Nft nft = walletDao.getNFT(nftWallet.getNftAddress(), nftWallet.getNftTokenId());
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

    public int getRefreshLeft(String userId)throws BaseException {
        try{
            return walletDao.getRefreshLeft(userId);
        }catch(Exception exception){
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<PoapWithDetails> getUserPoaps(String userId){
        List<UserWallet> userWallets = walletDao.getAllUserWalletByUserId(userId);

        // 2. 각 지갑에 있는 Poap 모두 가져오기
        List<PoapWithDetails> userPoaps = new ArrayList<>();
        for(UserWallet userWallet:userWallets){
            List<PoapWithDetails> poaps = walletDao.getPoapWithDetailsByWalletAddress(userWallet.getWalletAddress());
            userPoaps.addAll(poaps);
        }
        return userPoaps;
    }

    public List<Poap> getAllPoaps(){
        return walletDao.getAllPoaps();
    }

    public Transaction getTransaction(int trxIdx){
        return walletDao.getTransaction(trxIdx);
    }

    public TokenReq getTokenReq(int tokenReqIdx){
        return walletDao.getTokenReq(tokenReqIdx);
    }

    public int saveRemit(Map<String, String> remitRes){
        return walletDao.saveRemit(remitRes);
    }

    public int createTokenRequest(Map<String, String> tokenRequest){
        return walletDao.createTokenRequest(tokenRequest);
    }

    public List<Transaction> getAllTransaction(String userID){
        return walletDao.getAllTransaction(userID);
    }


    //    public void getPoapRefresh(String walletAddress, String userID) throws ParseException {
//        // POAP 가져오기
//        String poapResult = getPOAP.getPOAP(walletAddress);
//        JSONArray jsonList = getPOAP.fromJSONtoPOAPList(poapResult);
//
//        // DB에 있는 내 POAP 가져오기
//        List<PoapWithDetails> oldPoapList = userProvider.getUserPoaps(userID);
//        List<Integer> oldTokenIDList = new ArrayList<>();
//        oldPoapList.forEach((k) -> {
//            oldTokenIDList.add(k.getToken_id());
//        });
//
//
//        // DB에 있는 모든 POAP
//        List<Poap> allPoaps = userProvider.getAllPoaps();
//        List<Integer> allEventIdList = new ArrayList<>();
//        allPoaps.forEach((k) -> {
//            allEventIdList.add(k.getEvent_id());
//        });
//
//        // 이미 있는 포압 -> 패스, 없어진 포압 -> 디비 수정 및 삭제, 추가된 포압 -> 디비 추가 및 생성
//        for(Object json:jsonList){
//            JSONObject jsonObject = (JSONObject) json;
//            // 1. 이미 있는 경우 -> list에서 삭제
//            int new_token_id = (int) jsonObject.get("tokenId");
//            if(oldTokenIDList.contains(new_token_id)){
//                oldPoapList.forEach((k) -> {
//                    if(k.getToken_id() == new_token_id){
//                        oldPoapList.remove(k);
//                    }
//                });
//            }
//            else{
//                // 새로운 poap
//                // 1. pOap 자체의 존재 여부 확인 & 생성
//                Map<Object, Object> event = (Map) jsonObject.get("event");
//
//                if(!allEventIdList.contains((int)event.get("id"))){
//                    userDao.createPoap(event);
//                }
//                // 2. poapWallet 생성
//                if(jsonObject.containsKey("migrated")){
//                    userDao.createPoapWallet((int)event.get("id"), (int)jsonObject.get("tokenId"), walletAddress, (Timestamp)jsonObject.get("created"), (Timestamp)jsonObject.get("modified"));
//                }else{
//                    userDao.createPoapWallet((int)event.get("id"), (int)jsonObject.get("tokenId"), walletAddress, (Timestamp)jsonObject.get("created"));
//                }
//                // 3. uaserWalletPoap 생성
//
//            }
//        }
//        // 이미 있는 NFT인지 확인하기
//        System.out.println("total NFT : "+jsonObject.get("total"));
//        List<JSONObject> results = (List<JSONObject>) jsonObject.get("result");
//        int newNffIdx;
//
//        for(JSONObject result: results){
//            String token_address = (String) result.get("token_address");
//            int tokenId =  Integer.parseInt((String)result.get("token_id"));
//            if(userDao.isNFTExist(token_address, tokenId)==0){
//                System.out.println("Not Existed NFT");
//                String tokenUri = (String) result.get("token_uri");
//                if(tokenUri.endsWith(".json")){
//                    tokenUri = tokenUri.substring(0, tokenUri.length()-5);
//                }
//                String metaData = getNFT.getNftMetaData(tokenUri);
//                JSONObject metaJsonObject = getNFT.fromJSONtoNFT(metaData);
//
//                userDao.createNFT(result, metaJsonObject, chain);
//            }
//            if(userDao.isNFTWalletExist(token_address, tokenId, userWalletIndex)==0){
//                System.out.println("Not Existed wallet");
//                userDao.createNFTWallet(token_address, tokenId, userWalletIndex, Integer.parseInt((String) result.get("amount")));
//            }
//            System.out.println("Already exists");
//
//        }
//    }

//    public void getNFTRefresh(String walletAddress, String api_chain, String chain, int userWalletIndex) throws ParseException {
//        String nftResult = getNFT.getEthNft(api_chain, walletAddress);
//        JSONObject jsonObject = getNFT.fromJSONtoNFT(nftResult);
//
//        // 이미 있는 NFT인지 확인하기
//        System.out.println("total NFT : "+jsonObject.get("total"));
//        List<JSONObject> results = (List<JSONObject>) jsonObject.get("result");
//        int newNffIdx;
//
//        for(JSONObject result: results){
//            String token_address = (String) result.get("token_address");
//            int tokenId =  Integer.parseInt((String)result.get("token_id"));
//            if(walletDao.isNFTExist(token_address, tokenId)==0){
//                System.out.println("Not Existed NFT");
//                String tokenUri = (String) result.get("token_uri");
//                if(tokenUri.endsWith(".json")){
//                    tokenUri = tokenUri.substring(0, tokenUri.length()-5);
//                }
//                Map<String, String> hashMap = new HashMap();
//                String metaData = getNFT.externalGetAPIInterface(tokenUri, "", hashMap);
//                JSONObject metaJsonObject = getNFT.fromJSONtoNFT(metaData);
//
//                walletDao.createNFT(result, metaJsonObject, chain);
//            }
//            if(walletDao.isNFTWalletExist(token_address, tokenId, userWalletIndex)==0){
//                System.out.println("Not Existed wallet");
//                walletDao.createNFTWallet(token_address, tokenId, userWalletIndex, Integer.parseInt((String) result.get("amount")));
//            }
//            System.out.println("Already exists");
//
//        }
//    }



    public String checkImgUrl(String imgUrl){
        if(imgUrl.startsWith("ipfs://")){
            System.out.println(imgUrl);
            imgUrl = "https://ipfs.io/ipfs/" + imgUrl.substring(7);
            System.out.println(imgUrl);
        }
        return imgUrl;
    }
}
