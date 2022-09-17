package com.propwave.daotool.Friend.model;


import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class Friend {
    private int index;
    private String user;
    private String friend;
    private String friendName;
    private Timestamp createdAt;
}
