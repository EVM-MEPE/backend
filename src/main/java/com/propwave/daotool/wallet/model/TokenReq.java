package com.propwave.daotool.wallet.model;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TokenReq {
    private int index;
    private String reqWalletAddress;
    private float reqTokenAmount;
    private String toUser;
    private String fromUser;
    private String chainID;
    private String walletType;
    private String memo;
    private Timestamp createdAt;
}
