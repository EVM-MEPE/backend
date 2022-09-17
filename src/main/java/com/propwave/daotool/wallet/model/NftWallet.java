package com.propwave.daotool.wallet.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NftWallet {
    private int index;
    private String nftAddress;
    private int nftTokenId;
    private int userWalletIndex;
    private int amount;
    private boolean hidden;
}
