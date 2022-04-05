package com.propwave.daotool.user.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class Target {
    private int index;
    private String target;
}
