package com.propwave.daotool.wallet.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NftForDashboard {
    private int nftIndex;
    private int nftWalletIndex;
    private String address;
    private int tokenID;
    private boolean hidden;
    private String image;
}
