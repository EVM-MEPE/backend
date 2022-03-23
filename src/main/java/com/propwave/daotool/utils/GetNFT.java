package com.propwave.daotool.utils;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class GetNFT {
    public void getEthNft(){
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-api-key", "Fqy1IZbNGAK0zhZqzcHFiKe3jvTEyGAr2QNW6mZOzbfudyqZlmELouMzTNGSBl6d");
        String body = "";

        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
        String address = "0x07B0ea6D444B9B66F3A7709FB1fA75BcDCD67A16";
        ResponseEntity<String> responseEntity = rest.exchange("https://deep-index.moralis.io/api/v2/"+address+"/nft?format=decimal", HttpMethod.GET, requestEntity, String.class);
        HttpStatus httpStatus = responseEntity.getStatusCode();
        int status = httpStatus.value();
        String response = responseEntity.getBody();
        System.out.println("Response status: " + status);
        System.out.println(response);
    }

    public void getKlayNft(){

    }

    public static void main(String[] args){
        GetNFT getNFT = new GetNFT();
        getNFT.getEthNft();
    }
}
