package com.propwave.daotool.utils;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GetNFT {
    public String getNft(String chain, String walletAddress){
        System.out.println("getEthNft");
        System.out.println(chain+"::"+ walletAddress);
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-api-key", "Fqy1IZbNGAK0zhZqzcHFiKe3jvTEyGAr2QNW6mZOzbfudyqZlmELouMzTNGSBl6d");
        String body = "";

        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
        ResponseEntity<String> responseEntity = rest.exchange("https://deep-index.moralis.io/api/v2/"+walletAddress+"/nft?chain="+chain+"&format=decimal", HttpMethod.GET, requestEntity, String.class);
        HttpStatus httpStatus = responseEntity.getStatusCode();
        int status = httpStatus.value();
        String response = responseEntity.getBody();
        System.out.println("Response status: " + status);
        System.out.println(response);

        return response;
    }

    public String getNftMetaData(String tokenUri){
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        String body = "";

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
}
