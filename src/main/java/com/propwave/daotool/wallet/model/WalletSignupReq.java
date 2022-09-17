package com.propwave.daotool.wallet.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class WalletSignupReq {
    String walletAddress;
    String walletName;
    String walletIcon;
    String loginAvailable;
    String viewDataAvailable;
    String chain;
}
