package com.propwave.daotool.badge.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class BadgeNameImage {
    private String name;
    private String image;
}
