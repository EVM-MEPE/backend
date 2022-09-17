package com.propwave.daotool.wallet.model;

import lombok.*;

import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Poap {
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
}
