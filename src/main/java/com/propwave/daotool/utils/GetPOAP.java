package com.propwave.daotool.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.propwave.daotool.DaoToolApplication;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

@Component
public class GetPOAP {
    public String getPOAP(String walletAddress){
        System.out.println("get POAP!");
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-Key", Secret.POAP_API_KEY);
        String body = "";

        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
        ResponseEntity<String> responseEntity = null;
        try{
            responseEntity = rest.exchange("https://api.poap.tech/actions/scan/"+walletAddress, HttpMethod.GET, requestEntity, String.class);
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


    public JSONArray fromJSONtoPOAPList(String result) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONArray obj = (JSONArray)jsonParser.parse(result);
        return obj;
    }

    /**
     * @param JSONObject
     * @apiNote JSONObject를 Map<String, String> 형식으로 변환처리.
     * @return Map<String,String>
     * **/
    public static Map<String, Object> getMapFromJsonObject(JSONObject jsonObj){
        Map<String, Object> map = null;

        try {
            map = new ObjectMapper().readValue(jsonObj.toString(), Map.class);
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return map;
    }
//
//    public static void main(String[] args) {
//        GetPOAP a = new GetPOAP();
//        a.getPOAP("0xf9F3Ea76C7Be559B4D4C9B3Ee2c3E05484A84420");
//    }

}



