package com.propwave.daotool.friend.model;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class Follow {
    private int index;
    private String user;
    private String following;
    private Timestamp createdAt;
}
