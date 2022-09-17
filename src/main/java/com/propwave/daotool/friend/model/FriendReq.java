package com.propwave.daotool.friend.model;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class FriendReq {
    private int index;
    private String reqFrom;
    private String reqTo;
    private String reqNickname;
    private boolean isAccepted;
    private boolean isRejected;
    private Timestamp createdAt;
}
