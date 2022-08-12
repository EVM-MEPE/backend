package com.propwave.daotool.user.model;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class CommentWithInfo {
    private int commentIdx;
    private String commentTo;
    private String commentFrom;
    private String commentMessage;
    private boolean isPinned;
    private boolean isHide;
    private Timestamp createdAt;
    private String userNickname;
    private String profileImg;
    private String friendNickname;
}
