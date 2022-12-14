package com.propwave.daotool.wallet.model;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BadgeRequest {
    private int index;
    private String user;
    private String badgeName;
    private String srcWalletAddress;
    private String destWalletAddress;
    private boolean completed;
    private Timestamp createdAt;
    private Timestamp completedAt;
}
