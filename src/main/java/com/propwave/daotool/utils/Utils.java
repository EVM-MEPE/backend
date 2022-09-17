package com.propwave.daotool.utils;

import com.propwave.daotool.commons.S3Uploader;
import com.propwave.daotool.user.UserProvider;
import com.propwave.daotool.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.propwave.daotool.config.jwt.SecurityService;

@Component
public class Utils {
    @Autowired
    private SecurityService securityService;
    public Utils(SecurityService securityService){
        this.securityService = securityService;
    }

    public boolean isUserJwtTokenAvailable(String jwtToken, String userID){
        // check jwt token
        String subject;
        try{
            subject = securityService.getSubject(jwtToken);
        } catch(Exception e){
            return false;
        }
        return subject.equals(userID);
    }
}
