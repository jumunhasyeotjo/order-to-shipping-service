package com.jumunhasyeotjo.order_to_shipping.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // 400 BAD_REQUEST
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "E001", "잘못된 요청입니다."),
    CREATE_VALIDATE_EXCEPTION(HttpStatus.BAD_REQUEST,"E002", "객체 생성에 실패했습니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "E003", "입력값 검증에 실패했습니다."),
    INVALID_JSON(HttpStatus.BAD_REQUEST, "E004",  "잘못된 JSON 형식입니다."),
    REQUIRED_VALUE_MISSING(HttpStatus.BAD_REQUEST, "E005", "필수 값이 누락되었습니다."),
    INVALID_FORMAT(HttpStatus.BAD_REQUEST, "E006", "잘못된 입력 형식입니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "E007", "해당 상태로 전환할 수 없습니다."),
    HUB_ORIGIN_EQUALS_DESTINATION(HttpStatus.BAD_REQUEST, "E008", "도착 허브와 출발 허브가 같을 수 없습니다."),
    INVALID_STATE_FOR_MODIFICATION(HttpStatus.BAD_REQUEST, "E009", "현재 상태에서는 정보를 수정할 수 없습니다."),
    INVALID_STATE_CANCEL(HttpStatus.BAD_REQUEST, "E009", "배송이 이미 시작되어 취소할 수 없습니다."),

    // 401 UNAUTHORIZED
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E010", "인증이 필요합니다."),

    // 403 FORBIDDEN
    FORBIDDEN(HttpStatus.FORBIDDEN, "E011", "접근이 거부되었습니다."),

    // 500 INTERNAL_SERVER_ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E012", "서버 에러가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}