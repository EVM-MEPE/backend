package com.propwave.daotool.wallet.model;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Transaction {
    private int index;
    private String toWalletAddress;
    private String fromWalletAddress;
    private String toUser;
    private String fromUser;
    private String gasPrice;
    private String gas;
    private String value;
    private String chainID;
    private String memo;
    private String udenom;
    private String walletType;
    private String txHash;
    private Timestamp createdAt;
}
