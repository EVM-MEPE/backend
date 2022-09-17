package com.propwave.daotool.wallet.model;

import lombok.*;

import java.sql.Date;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PoapWithDetails {
    private int event_id;
    private String fancy_id;
    private String name;
    private String event_url;
    private String img_url;
    private String country;
    private String city;
    private String description;
    private int year;
    private Date start_date;
    private Date end_date;
    private Date expiry_date;
    private int supply_total;
    // add
    private int poapWalletIndex;
    private int token_id;
    private String walletAddress;
    private int supply_order;
    private Timestamp createdAt;
    private Timestamp migratedAt;

    private int userWalletPoapIndex;
    private String user;
    private boolean isHide;
}