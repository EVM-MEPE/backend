package com.propwave.daotool.wallet.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class Nft {
    private String address;
    private int tokenID;
    private String contentType;
    private String name;
    private String description;
    private String image;
    private String chain;
    private String tokenUri;
    private int is_valid;
    private String date;
    private int index;
}
