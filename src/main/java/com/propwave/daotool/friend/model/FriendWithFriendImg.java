package com.propwave.daotool.friend.model;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FriendWithFriendImg {
    private int index;
    private String user;
    private String friend;
    private String friendProfileImg;
    private String friendName;
    private Timestamp createdAt;
}
