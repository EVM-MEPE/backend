package com.propwave.daotool.badge.model;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class GetBadgesRes {
    private String name;
    private String image;
    private Timestamp joinedAt;
}
