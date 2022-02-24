package com.propwave.daotool.badge.model;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class BadgeWallet {
    private int index;
    private String walletAddress;
    private String badgeName;
    private Timestamp joinedAt;
}
