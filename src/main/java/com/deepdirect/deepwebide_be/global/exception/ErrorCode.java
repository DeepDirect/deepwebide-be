package com.deepdirect.deepwebide_be.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 400 BAD REQUEST
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력 값입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다."),
    PASSWORDS_DO_NOT_MATCH(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    PASSWORD_SAME_AS_OLD(HttpStatus.BAD_REQUEST, "기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다."),
    PHONE_NUMBER_ALREADY_USED(HttpStatus.BAD_REQUEST, "기존 전화번호와 동일합니다."),
    INVALID_STATUS_VALUE(HttpStatus.BAD_REQUEST, "상태 값은 'active' 또는 ''만 가능합니다."),
    ALREADY_IN_TARGET_STATUS(HttpStatus.BAD_REQUEST, "이미 해당 상태입니다."),
    USERNAME_NOT_MATCH(HttpStatus.BAD_REQUEST, "사용자 이름이 일치하지 않습니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다."),
    PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "비밀번호 확인이 필요합니다."),
    NO_CHANGES_REQUESTED(HttpStatus.BAD_REQUEST, "변경된 정보가 없습니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST,"유효하지 않은 닉네임입니다."),
    INVALID_PHONE_NUMBER(HttpStatus.BAD_REQUEST,"유효하지 않은 전화번호입니다."),
    INVALID_CART_ITEM(HttpStatus.BAD_REQUEST, "장바구니 아이템이 유효하지 않습니다."),
    ORDER_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 주문입니다."),
    ORDER_ALREADY_SHIPPING(HttpStatus.BAD_REQUEST, "이미 배송중인 주문입니다."),
    ORDER_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "이미 취소된 주문입니다."),
    REPOSITORY_NAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST,"이미 동일한 이름의 레포지토리가 존재합니다."),
    REPOSITORY_NOT_SHARED(HttpStatus.BAD_REQUEST, "공유된 레포지토리가 아닙니다."),
    ENTRY_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "입장 코드가 만료되었습니다."),



    INVALID_USERNAME(HttpStatus.BAD_REQUEST, "이름은 한글 2자 이상만 입력 가능합니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "비밀번호는 영어 소문자, 숫자, 특수문자를 모두 포함해야 합니다."),
    INVALID_ORDER_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "잘못된 주문 상태 변경 요청입니다."),
    STATS_NOT_FOUND(HttpStatus.BAD_REQUEST, "통계 정보가 존재하지 않습니다."),
    PRODUCT_SALES_RANKING_NOT_FOUND(HttpStatus.BAD_REQUEST, "상품 판매 랭킹 정보가 존재하지 않습니다."),
    PAYMENT_PENDING_NOT_FOUND(HttpStatus.BAD_REQUEST, "결제 대기 정보가 존재하지 않습니다."),

    INVALID_CART_QUANTITY(HttpStatus.BAD_REQUEST, "장바구니 수량은 1 이상이어야 합니다."),
    EXCEEDS_PRODUCT_STOCK(HttpStatus.BAD_REQUEST, "장바구니 수량이 상품 재고를 초과합니다."),

    VERIFICATION_CODE_INVALID(HttpStatus.BAD_REQUEST, "인증번호가 올바르지 않습니다."),
    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다."),

    NOT_OWNER(HttpStatus.BAD_REQUEST, "오너만 이름을 변경할 수 있습니다."), //오너만 이름 변경!!
    REPOSITORY_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "레포지토리 이름은 50자 이하여야 합니다."), // 레포지토리 이름 길이 제한
    NOT_OWNER_TO_SHARE(HttpStatus.BAD_REQUEST, "오너만 공유를 할 수 있습니다."),
    NOT_OWNER_TO_UNSHARE(HttpStatus.BAD_REQUEST, "오너만 공유를 취소할 수 있습니다."),
    CANNOT_DELETE_SHARED_REPOSITORY(HttpStatus.BAD_REQUEST, "공유 중인 레포지토리는 삭제할 수 없습니다."),

    // 401 UNAUTHORIZED
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않았습니다."),
    WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "아이디 혹은 비밀번호가 일치하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    ACCESS_TOKEN_REQUIRED(HttpStatus.UNAUTHORIZED, "AccessToken이 필요합니다."),
    NOT_ADMIN(HttpStatus.UNAUTHORIZED, "관리자 권한이 없습니다."),

    // 403 FORBIDDEN
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "이메일 인증을 완료해야 로그인할 수 있습니다."),
    ENTRY_CODE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "오너만 확인할 수 있습니다."),
    ENTRY_CODE_REISSUE_DENIED(HttpStatus.FORBIDDEN, "해당 레포지토리의 소유자만 입장 코드를 재발급할 수 있습니다."),


    // 404 NOT FOUND
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이메일로 등록된 사용자가 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 카테고리를 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상품을 찾을 수 없습니다."),
    MEMBER_DELETED(HttpStatus.NOT_FOUND, "로그인할 수 없는 사용자입니다."),
    USER_PAID_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자의 촘 사용 금액을 찾을 수 없습니다."),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니를 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    PRODUCT_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "상품 이미지가 존재하지 않습니다."),
    VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "인증 요청 기록이 없습니다."),
    REPOSITORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않은 레포지토리 입니다."),
    ENTRY_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "입장 코드를 찾을 수 없습니다."),


    // 409 CONFLICT
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    EMAIL_ALREADY_VERIFIED(HttpStatus.CONFLICT, "이미 인증이 완료된 계정입니다."),
    DUPLICATE_NAME_AND_PHONE(HttpStatus.CONFLICT, "해당 이름과 연락처로 이미 가입된 회원이 있습니다."),

    // 500 INTERNAL SERVER ERROR
    ENTRY_CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "입장 코드 생성에 실패했습니다. 다시 시도해주세요."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SMS 발송 실패");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
