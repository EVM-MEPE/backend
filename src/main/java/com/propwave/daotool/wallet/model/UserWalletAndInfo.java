package com.propwave.daotool.wallet.model;

import lombok.*;

import java.sql.Timestamp;


@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserWalletAndInfo {
    private int index;
    private String user;
    private String walletAddress;
    private String walletType;
    private String walletIcon;
    private String chain;
    private Timestamp createdAt;
}
