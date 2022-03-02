package com.propwave.daotool.badge.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserDataAvailable {
    private String user;
    private boolean viewDataAvailable;
}
