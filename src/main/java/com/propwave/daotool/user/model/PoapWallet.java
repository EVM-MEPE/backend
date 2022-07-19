package com.propwave.daotool.user.model;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PoapWallet {
    private int index;
    private int poap_event_id;
    private int token_id;
    private String walletAddress;
    private int supply_order;
    private Timestamp createdAt;
    private Timestamp migratedAt;
}
