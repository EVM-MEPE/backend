package com.propwave.daotool.config.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/security")
public class SecurityController {
    @Autowired
    private SecurityService securityService;


    //json 형태니까 Object
    // Request param -> ? 쓸수있음
    // 원래면 로그인이니까 포스트 방식으로 할거임. 지금은 걍 보여줄려고 GET 방식일 뿐!
    @GetMapping("/create/token")
    public Map<String, Object> createToken(@RequestParam(value="subject") String subject){
        String token = securityService.createToken(subject, (60*1000*60));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("result", token);
        return map;
    }

    @GetMapping("/get/subject")
    public Map<String, Object> getSubject(@RequestParam(value="token") String token){
        String subject = securityService.getSubject(token);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("result", subject);
        return map;
    }

}
