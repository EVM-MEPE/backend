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
    private int reqTokenAmount;
    private String memo;
    private String fromUser;
    private String toUser;
    private String walletType;
    private String chainID;
    private Timestamp createdAt;
}
