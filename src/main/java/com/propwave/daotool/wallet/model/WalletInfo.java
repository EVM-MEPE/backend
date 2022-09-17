package com.propwave.daotool.wallet.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class WalletInfo {
    private String address;
    private String walletType;
    private String walletTypeImage;
}
