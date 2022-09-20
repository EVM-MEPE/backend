package com.propwave.daotool.utils;
import lombok.val;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class GetNFT {
    public JSONArray getNFTs(String network, String walletAddress) throws ParseException {
        /*
         * network: eth, polygon, stargaze, solana, evmos
         * */

        JSONArray result = null;
        String url = null;
        String body = "";
        Map<String, String> header = new HashMap<String, String>();
        int type = 1;
        switch (network) {
            case "eth":
            case "polygon":
                url = "https://deep-index.moralis.io/api/v2/" + walletAddress + "/nft?chain=" + network + "&format=decimal";
                header.clear();
                header.put("x-api-key", Secret.MORALIS_NFT_API_KEY);
                break;
            case "stargaze":
                type = 2;
                walletAddress = "stars" + walletAddress.substring(6);
                url = "https://nft-api.stargaze-apis.com/api/v1beta/profile/" + walletAddress + "/nfts";
                break;
            case "solana":
                type = 2;
                url = "https://api-mainnet.magiceden.dev/v2/wallets/" + walletAddress + "/tokens?offset=0&limit=100&listStatus=both";
                break;
            case "Evmos":
                type = 3;
                url = "https://api.covalenthq.com/v1/9001/address/" + walletAddress + "/balances_v2/?quote-currency=KRW&format=JSON&nft=true&no-nft-fetch=true&key=" + Secret.EVMOS_NFT_API_KEY;
                break;
            default:
                return result;
        }
        JSONArray res = getNftResult(url, body, header, type);

        return res;
    }

    public JSONArray getNftResult(String url, String body, Map<String, String> additionalHeaders, int type) throws ParseException {
        String apiStringRes = externalGetAPIInterface(url, body, additionalHeaders);
        JSONParser jsonParser = new JSONParser();
        JSONArray JsonArrRes = new JSONArray();
        if(type==1){
            try{
                JSONObject obj = (JSONObject)jsonParser.parse(apiStringRes);
                JsonArrRes = (JSONArray) obj.get("result");
            }catch(Exception e){
                System.out.println();
            }
        }else if(type == 3){
            System.out.println(apiStringRes);
            JSONObject obj = (JSONObject)jsonParser.parse(apiStringRes);
            obj = (JSONObject)obj.get("data");
            JSONArray arr_ = (JSONArray)obj.get("items");
            List<JSONObject> list = (List<JSONObject>) arr_.stream()
                            .filter(json -> ((String)((JSONObject)json).get("type")).equals("nft"))
                                    .collect(Collectors.toList());
            JsonArrRes = getEvmosNFT(list);
            System.out.println(JsonArrRes);
        }else{
            JsonArrRes = (JSONArray)jsonParser.parse(apiStringRes);
        }
        return JsonArrRes;
    }


    public String externalGetAPIInterface(String url, String body, Map<String, String> additionalHeaders){
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        additionalHeaders.forEach((strKey, strValue)->{
            headers.add(strKey, strValue);
        });
        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
        ResponseEntity<String> responseEntity = null;
        try{
            responseEntity = rest.exchange(url, HttpMethod.GET, requestEntity, String.class);
        }catch(Exception e){
            return "";
        }
        return responseEntity.getBody();
    }

    public JSONArray getEvmosNFT(List<JSONObject> list) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONArray res = new JSONArray();
        for(JSONObject jsonObject:list){
            String contractAdd = (String)jsonObject.get("contract_address");
            JSONArray nftList = (JSONArray)jsonObject.get("nft_data");
            for(Object obj: nftList){
                JSONObject jsonObject1 = (JSONObject) obj;
                String tokenID = (String)jsonObject1.get("token_id");
                Map<String, String> header = new HashMap<>();
                String metadata = externalGetAPIInterface("https://cache.orbitmarket.io/metadata?address="+contractAdd+"&token="+tokenID+"&type=721","", header);
                JSONObject newJsonObject= (JSONObject)jsonParser.parse(metadata);
                newJsonObject.put("token_id", tokenID);
                newJsonObject.put("contract_address", contractAdd);
                res.add(newJsonObject);
            }
        }
        return res;
    }


    public String getEthNft(String chain, String walletAddress){
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-api-key", Secret.MORALIS_NFT_API_KEY);
        String body = "";

        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);

        ResponseEntity<String> responseEntity = null;
        try{
            responseEntity = rest.exchange("https://deep-index.moralis.io/api/v2/"+walletAddress+"/nft?chain="+chain+"&format=decimal", HttpMethod.GET, requestEntity, String.class);
        }catch(Exception e){
            return "";
        }
        //HttpStatus httpStatus = responseEntity.getStatusCode();
        //int status = httpStatus.value();
        String response = responseEntity.getBody();

        return response;
    }

    public JSONObject fromJSONtoNFT(String result) throws ParseException {

        JSONParser jsonParser = new JSONParser();
        Object obj = jsonParser.parse(result);
        return (JSONObject) obj;
    }



//    public String getStargazeNft(String walletAddress){
//
//        System.out.println("getStarNft");
//        walletAddress = "stars" + walletAddress.substring(6);
//
//        RestTemplate rest = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        String body = "";
//
//        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
//        ResponseEntity<String> responseEntity = null;
//        try{
//            responseEntity = rest.exchange("https://nft-api.stargaze-apis.com/api/v1beta/profile/"+walletAddress+"/nfts", HttpMethod.GET, requestEntity, String.class);
//        }catch(Exception e){
//            return "";
//        }
//
//        //HttpStatus httpStatus = responseEntity.getStatusCode();
//        //int status = httpStatus.value();
//        String response = responseEntity.getBody();
//        //System.out.println("Response status: " + status);
//        //System.out.println(response);
//
//        return response;
//    }
//
//    public String getSolanaNft(String walletAddress){
//        System.out.println("get Solana nft");
//
//        RestTemplate rest = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        String body = "";
//
//        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
//        ResponseEntity<String> responseEntity = null;
//        try{
//            responseEntity = rest.exchange("https://api-mainnet.magiceden.dev/v2/wallets/"+walletAddress+"/tokens?offset=0&limit=100&listStatus=both", HttpMethod.GET, requestEntity, String.class);
//        }catch(Exception e){
//
//        }
//        //HttpStatus httpStatus = responseEntity.getStatusCode();
//        //int status = httpStatus.value();
//        String response = responseEntity.getBody();
//        //System.out.println("Response status: " + status);
//        //System.out.println(response);
//
//        return response;
//    }

//    public JSONArray getAllEthChainNft(String walletAddress) throws ParseException {
//        ArrayList<String> chainList = new ArrayList<>();
//        chainList.add("eth");
//        chainList.add("polygon");
//
//        JSONArray result = new JSONArray();
//        JSONArray ethR = new JSONArray();
//        JSONArray polyR = new JSONArray();
//
//        for(String chain:chainList){
//            String res = getEthNft(chain, walletAddress);
//
//            if(res.equals("")){
//                continue;
//            }
//            if(chain.equals("eth")){
//                result.addAll(fromJSONtoNftList(res));
//            }
//
//        }
//        return result;
//    }


//    public JSONArray fromJSONtoNftList(String result) throws ParseException {
//        JSONParser jsonParser = new JSONParser();
//        JSONArray arr = new JSONArray();
//        try{
//            JSONObject obj = (JSONObject)jsonParser.parse(result);
//            arr = (JSONArray) obj.get("result");
//        }catch(Exception e){
//            System.out.println();
//        }
//
//        return arr;
//    }
}
