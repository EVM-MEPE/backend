package com.propwave.daotool.user.model;

import lombok.*;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class user {
    private String id;
    private String profileImage;
    private String introduction;
    private String url;
    private int hits;
    private Timestamp createdAt;
}
