package com.propwave.daotool.user.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access= AccessLevel.PROTECTED)
public class UserSignupReq {
    private String id;
    //private String profileImage;
    private String introduction;
    private String url;
}
