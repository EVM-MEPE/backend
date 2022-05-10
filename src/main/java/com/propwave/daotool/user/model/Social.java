package com.propwave.daotool.user.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Social {
    private String userId;
    private String twitter;
    private String facebook;
    private String discord;
    private String link;
}
