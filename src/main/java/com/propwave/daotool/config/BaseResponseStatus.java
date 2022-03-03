package com.propwave.daotool.config;

import lombok.Getter;

/**
 * 에러 코드 관리
 */
@Getter
public enum BaseResponseStatus {

    SUCCESS(true, 200, "요청에 성공하였습니다."),

    REQUEST_ERROR(false, 404, "입력값을 확인해주세요."),

    USER_NOT_EXISTS(false, 401, "존재하지 않는 유저입니다."),

    RESPONSE_ERROR(false, 404, "값을 불러오는데 실패하였습니다."),

    //badge
    NO_BADGE_EXIST(false, 401, "해당하는 뱃지가 없습니다."),
    WALLET_ALREADY_EXIST_FOR_LOGIN(false, 401, "지갑이 이미 로그인용으로 등록되어있습니다."),

    /**
     * 4000 : Database, Server 오류
     */
    DATABASE_ERROR(false, 401, "데이터베이스 연결에 실패하였습니다."),
    S3_UPLOAD_ERROR(false, 401, "이미지 업로드에 실패했습니다.");



    private final boolean isSuccess;
    private final int code;
    private final String message;

    private BaseResponseStatus(boolean isSuccess, int code, String message) { //BaseResponseStatus 에서 각 해당하는 코드를 생성자로 맵핑
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}