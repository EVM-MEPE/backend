package com.propwave.daotool.utils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetNFT {
    public String getEthNft(String chain, String walletAddress){
        System.out.println("getEthNft");
        System.out.println(chain+"::"+ walletAddress);

        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-api-key", Secret.NFT_API_KEY);
        String body = "";

        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);

        ResponseEntity<String> responseEntity = null;
        try{
            responseEntity = rest.exchange("https://deep-index.moralis.io/api/v2/"+walletAddress+"/nft?chain="+chain+"&format=decimal", HttpMethod.GET, requestEntity, String.class);
        }catch(Exception e){
            return "";
        }
        HttpStatus httpStatus = responseEntity.getStatusCode();
        int status = httpStatus.value();
        String response = responseEntity.getBody();
        System.out.println("Response status: " + status);
        System.out.println(response);

        return response;
    }

    public String getStargazeNft(String walletAddress){

        System.out.println("getStarNft");
        walletAddress = "stars" + walletAddress.substring(6);
        System.out.p

        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        String body = "";

        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
        ResponseEntity<String> responseEntity = null;
        try{
            responseEntity = rest.exchange("https://nft-api.stargaze-apis.com/api/v1beta/profile/"+walletAddress+"/nfts", HttpMethod.GET, requestEntity, String.class);
        }catch(Exception e){
            return "";
        }

        HttpStatus httpStatus = responseEntity.getStatusCode();
        int status = httpStatus.value();
        String response = responseEntity.getBody();
        System.out.println("Response status: " + status);
        System.out.println(response);

        return response;
    }

    public JSONArray getAllEthChainNft(String walletAddress) throws ParseException {
        ArrayList<String> chainList = new ArrayList<>();
        chainList.add("eth");
        chainList.add("polygon");

        JSONArray result = new JSONArray();

        for(String chain:chainList){
            String res = getEthNft(chain, walletAddress);

            if(res.equals("")){
                continue;
            }
            result.addAll(fromJSONtoNftList(res));
        }
        return result;
    }

    public String getNftMetaData(String tokenUri){
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        String body = "";
        System.out.println(tokenUri);

        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
        ResponseEntity<String> responseEntity = rest.exchange(tokenUri, HttpMethod.GET, requestEntity, String.class);
        HttpStatus httpStatus = responseEntity.getStatusCode();
        int status = httpStatus.value();
        String response = responseEntity.getBody();
        System.out.println("Response status: " + status);
        System.out.println(response);
        return response;
    }

    public JSONObject fromJSONtoNFT(String result) throws ParseException {

        JSONParser jsonParser = new JSONParser();
        Object obj = jsonParser.parse(result);
        return (JSONObject) obj;
    }

    public JSONArray fromJSONtoNftList(String result) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject obj = (JSONObject)jsonParser.parse(result);
        JSONArray arr = (JSONArray) obj.get("result");
        return arr;
    }
}
