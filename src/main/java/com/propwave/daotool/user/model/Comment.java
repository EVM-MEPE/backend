package com.propwave.daotool.user.model;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class Comment {
    private int index;
    private String commentTo;
    private String commentFrom;
    private String message;
    private boolean isPinned;
    private boolean isHide;
    private Timestamp createdAt;
}
