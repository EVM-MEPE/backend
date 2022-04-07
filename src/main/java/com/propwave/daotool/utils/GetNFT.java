package com.propwave.daotool.utils;

import com.propwave.daotool.user.model.Nft;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

public class GetNFT {
    public void getEthNft(String chain, String walletAddress){
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
    }

//    public Nft fromJSONtoNFT(String result) {
//
//        JSONObject rjson = new JSONObject(result);
//        JSONArray items  = rjson.getJSONArray("items");
//        List<ItemDto> ret = new ArrayList<>();
//        for (int i=0; i<items.length(); i++) {
//            JSONObject itemJson = items.getJSONObject(i);
//            System.out.println(itemJson);
//            ItemDto itemDto = new ItemDto(itemJson);
//            ret.add(itemDto);
//        }
//        return ret;
//    }


//    public static void main(String[] args){
//        GetNFT getNFT = new GetNFT();
//        getNFT.getEthNft("polygon", "0x07B0ea6D444B9B66F3A7709FB1fA75BcDCD67A16");
//    }
}
