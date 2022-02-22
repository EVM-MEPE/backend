package com.propwave.daotool.config;

import lombok.Getter;

/**
 * 에러 코드 관리
 */
@Getter
public enum BaseResponseStatus {

    SUCCESS(true, 200, "요청에 성공하였습니다."),

    //

    /**
     * 2000 : Request 오류
     */
    // Common
    REQUEST_ERROR(false, 2000, "입력값을 확인해주세요."),
    EMPTY_JWT(false, 2001, "JWT를 입력해주세요."),
    INVALID_JWT(false, 2002, "유효하지 않은 JWT입니다."),
    INVALID_USER_JWT(false,2003,"권한이 없는 유저의 접근입니다."),

    // users
    USERS_EMPTY_USER_ID(false, 2010, "유저 아이디 값을 확인해주세요."),
    USERS_STATUS_NOT_ACTIVATED(false,2011,"활성화된 유저가 아닙니다."),
    USERS_EMPTY_USER_EMAIL(false, 2012, "유저 이메일 값을 확인해주세요"),
    EMAIL_NOT_FOUND(false, 400, "없는 유저 이메일입니다."),

    // friend
    FRIEND_ID_INAPPROPRIATE_FORMAT(false, 2100, "친구 ID값을 형식에 맞게 넣어주세요."),

    // [POST] /users
    POST_USERS_EMPTY_EMAIL(false, 2015, "이메일을 입력해주세요."),
    POST_USERS_INVALID_EMAIL(false, 2016, "이메일 형식을 확인해주세요."),
    POST_USERS_EXISTS_EMAIL(false,2017,"중복된 이메일입니다."),
    POST_USERS_EMPTY_PASSWORD(false,2018,"비밀번호를 입력해주세요."),
    POST_USERS_INVALID_PASSWORD(false,2019,"비밀번호 형식을 확인해주세요."),
    POST_USERS_EMPTY_NICKNAME(false, 400, "닉네임을 입력해주세요."),

    NO_USER_NUMBER_MINUS(false, 2020,"USER 번호가 음수입니다."),
    SAME_USER_NUMBER(false,2021,"USER 번호가 같습니다"),

    POST_ADMIN_INVALID_SCORE(false, 2030, "잘못된 평점 형식입니다."),
    POST_ADMIN_ID_NULL(false, 2030,"noticeBoardId 값이 없습니다."),


    /**
     * 3000 : Response 오류
     */
    // Common
    RESPONSE_ERROR(false, 3000, "값을 불러오는데 실패하였습니다."),

    // [POST] /users
    DUPLICATED_EMAIL(false, 3013, "중복된 이메일입니다."),
    FAILED_TO_LOGIN(false,3014,"없는 아이디거나 비밀번호가 틀렸습니다."),
    FAILED_TO_LOGIN_CAUSED_BY_EMAIL(false,3015,"이메일을 확인해주세요."),
    FAILED_TO_DELETE_USER(false, 400, "회원탈퇴에 실패했습니다."),


    /**
     * 4000 : Database, Server 오류
     */
    DATABASE_ERROR(false, 401, "데이터베이스 연결에 실패하였습니다."),
    SERVER_ERROR(false, 4001, "서버와의 연결에 실패하였습니다."),

    //[PATCH] /users/{userIdx}
    MODIFY_FAIL_USERNAME(false,4014,"유저네임 수정 실패"),
    MODIFY_FAIL_REPORT(false,4015,"유저 신고횟수 추가 실패"),

    PASSWORD_ENCRYPTION_ERROR(false, 4011, "비밀번호 암호화에 실패하였습니다."),
    PASSWORD_DECRYPTION_ERROR(false, 4012, "비밀번호 복호화에 실패하였습니다.");


    // 5000 : 필요시 만들어서 쓰세요
    // 6000 : 필요시 만들어서 쓰세요


    private final boolean isSuccess;
    private final int code;
    private final String message;

    private BaseResponseStatus(boolean isSuccess, int code, String message) { //BaseResponseStatus 에서 각 해당하는 코드를 생성자로 맵핑
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}