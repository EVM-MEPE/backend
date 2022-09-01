package com.propwave.daotool.user.model;

import lombok.*;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserWallet {
    private int index;
    private String user;
    private String walletAddress;
    private String chain;
    private Timestamp createdAt;
}
