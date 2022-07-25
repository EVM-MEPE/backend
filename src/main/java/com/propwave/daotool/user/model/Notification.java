package com.propwave.daotool.user.model;


import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Notification {
    private int index;
    private String user;
    private int type;
    private int friendReq;
    private int friend;
    private int follow;
    private String message;
    private boolean isChecked;
    private Timestamp createdAt;
}
