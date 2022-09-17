package com.propwave.daotool.wallet.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class BadgeTarget {
    private int index;
    private String badgeName;
    private int targetIdx;
}
