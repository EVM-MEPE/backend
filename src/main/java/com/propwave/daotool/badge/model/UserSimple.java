package com.propwave.daotool.badge.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserSimple {
    private String id;
    private String profileImage;
}
