package com.propwave.daotool.user.model;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProfileImg {
    private int index;
    private String user;
    private String imgUrl;
    private boolean isHide;
    private Timestamp createdAt;
}
