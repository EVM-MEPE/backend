package com.propwave.daotool.user.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class Chain {
    private String name;
    private String image;
    private int index;
}
