package com.propwave.daotool.badge.model;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class userBadge {
    private int index;
    private int walletId;
    private String badgeName;
    private Timestamp joinedAt;
}
