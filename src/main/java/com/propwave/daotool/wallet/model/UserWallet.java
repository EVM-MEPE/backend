package com.propwave.daotool.wallet.model;

import lombok.*;

import javax.persistence.Access;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserWallet {
    private int index;
    private String user;
    private String walletAddress;
    private boolean loginAvailable;
    private boolean viewDataAvailable;
    private String walletName;
    private Timestamp createdAt;
    private String chain;
}
