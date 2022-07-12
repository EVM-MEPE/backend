package com.propwave.daotool.user.model;

import lombok.*;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class User {
    private String id;
    //private String profileImage;
    private String introduction;
    private String url;
    private int hits;
    private int todayHits;
    private Timestamp createdAt;
    private int nftRefreshLeft;
    private String backImage;
    private String nickname;
    private int index;
}
