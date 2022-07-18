package com.propwave.daotool.utils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GetPOAP {
    public String getPOAP(String walletAddress){
        System.out.println("get POAP!");
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-Key", Secret.POAP_API_KEY);
        String body = "";

        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
        ResponseEntity<String> responseEntity = rest.exchange("https://api.poap.tech/actions/scan/"+walletAddress, HttpMethod.GET, requestEntity, String.class);
        HttpStatus httpStatus = responseEntity.getStatusCode();
        int status = httpStatus.value();
        String response = responseEntity.getBody();
        System.out.println("Response status: " + status);
        System.out.println(response);

        return response;
    }


    public JSONObject fromJSONtoPOAP(String result) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        Object obj = jsonParser.parse(result);
        return (JSONObject) obj;
    }
}



