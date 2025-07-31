package com.deepdirect.deepwebide_be.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 400 BAD REQUEST
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다."),
    PASSWORDS_DO_NOT_MATCH(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    PHONE_NUMBER_ALREADY_USED(HttpStatus.BAD_REQUEST, "기존 전화번호와 동일합니다."),
    REPOSITORY_NAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST,"이미 동일한 이름의 레포지토리가 존재합니다."),
    REPOSITORY_NOT_SHARED(HttpStatus.BAD_REQUEST, "공유된 레포지토리가 아닙니다."),
    INVALID_ENTRY_CODE(HttpStatus.BAD_REQUEST, "올바른 입장 코드가 아닙니다."),
    REPOSITORY_MEMBER_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "최대 인원이 초과되어 입장할 수 없습니다."),
    INVALID_USERNAME(HttpStatus.BAD_REQUEST, "이름은 한글 2자 이상만 입력 가능합니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "비밀번호는 영어 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다."),
    VERIFICATION_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다."),
    NOT_OWNER_CHANGE(HttpStatus.BAD_REQUEST, "오너만 이름을 변경할 수 있습니다."), //오너만 이름 변경!!
    NOT_OWNER_DELETE(HttpStatus.BAD_REQUEST, "오너만 삭제할 수 있습니다."), //오너만 삭제 가능
    REPOSITORY_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "레포지토리 이름은 50자 이하여야 합니다."), // 레포지토리 이름 길이 제한
    NOT_OWNER_TO_SHARE(HttpStatus.BAD_REQUEST, "오너만 공유를 할 수 있습니다."),
    NOT_OWNER_TO_UNSHARE(HttpStatus.BAD_REQUEST, "오너만 공유를 취소할 수 있습니다."),
    CANNOT_DELETE_SHARED_REPOSITORY(HttpStatus.BAD_REQUEST, "공유 중인 레포지토리는 삭제할 수 없습니다."),
    NOT_MEMBER(HttpStatus.BAD_REQUEST,"레포지토리 멤버가 아닙니다."),
    CANNOT_KICK_SELF(HttpStatus.BAD_REQUEST,"자기 자신은 강퇴할 수 없습니다."),
    OAUTH_TOKEN_ERROR(HttpStatus.BAD_REQUEST, "OAuth 토큰 요청에 실패했습니다."),
    OAUTH_USER_INFO_ERROR(HttpStatus.BAD_REQUEST, "OAuth 사용자 정보를 가져올 수 없습니다."),
    OAUTH_EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "GitHub에서 이메일 정보를 가져올 수 없습니다."),
    FILE_NOT_FOUND(HttpStatus.BAD_REQUEST, "파일을 찾을 수 없습니다."),
    INVALID_PARENT_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일/폴더 타입입니다."),
    DUPLICATE_FILE_NAME(HttpStatus.BAD_REQUEST,"동일한 이름의 파일이 이미 존재합니다."),
    CANNOT_MOVE_TO_CHILD(HttpStatus.BAD_REQUEST, "자신의 하위 폴더로 이동할 수 없습니다."),
    CANNOT_OPEN_FOLDER(HttpStatus.BAD_REQUEST, "폴더는 열 수 없습니다."),
    FILE_CONTENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "파일 내용이 존재하지 않습니다."),
    CANNOT_SAVE_FOLDER(HttpStatus.BAD_REQUEST, "폴더는 저장할 수 없습니다."),
    HISTORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 히스토리를 찾을 수 없습니다."),
    MISSING_WS_PARAMS(HttpStatus.BAD_REQUEST, "WebSocket 연결에 필요한 Param이 누락되었습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST,"검색 키워드를 입력해주세요."),
    REPOSITORY_EXECUTION_FAILED(HttpStatus.BAD_REQUEST, "레포지토리를 찾을 수 없습니다."),
    UNSUPPORTED_REPOSITORY_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 레포지토리 타입입니다."),
    REPOSITORY_FILES_NOT_FOUND(HttpStatus.BAD_REQUEST, "레포지토리 파일을 찾을 수 없습니다."),
    FILE_TREE_CONVERSION_FAILED(HttpStatus.BAD_REQUEST, "파일 트리 변환에 실패했습니다."),
    FILE_UPLOAD_FAIL(HttpStatus.BAD_REQUEST, "파일 업로드에 실패했습니다."),
    PARENT_ID_REQUIRED(HttpStatus.BAD_REQUEST, "부모 폴더 ID는 필수입니다."),
    FILE_EXTENSION_REQUIRED(HttpStatus.BAD_REQUEST, "파일 확장자는 필수입니다."),

    // 401 UNAUTHORIZED
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않았습니다."),
    WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "아이디 혹은 비밀번호가 일치하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "Access Token이 누락되었습니다."),

    // 403 FORBIDDEN
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    ENTRY_CODE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "오너만 확인할 수 있습니다."),
    ENTRY_CODE_REISSUE_DENIED(HttpStatus.FORBIDDEN, "해당 레포지토리의 소유자만 입장 코드를 재발급할 수 있습니다."),
    NOT_OWNER_TO_KICK(HttpStatus.FORBIDDEN,"해당 레포의 소유자만 멤버를 강퇴할 수 있습니다."),

    // 404 NOT FOUND
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "인증 요청 기록이 없습니다."),
    REPOSITORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않은 레포지토리 입니다."),
    ENTRY_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "입장 코드를 찾을 수 없습니다."),
    CHAT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅 메시지를 찾을 수 없습니다."),


    // 409 CONFLICT
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    EMAIL_ALREADY_VERIFIED(HttpStatus.CONFLICT, "이미 인증이 완료된 계정입니다."),
    DUPLICATE_NAME_AND_PHONE(HttpStatus.CONFLICT, "해당 이름과 연락처로 이미 가입된 회원이 있습니다."),
    ALREADY_VERIFIED(HttpStatus.CONFLICT, "이미 인증이 완료된 요청입니다."),

    // 500 INTERNAL SERVER ERROR
    ENTRY_CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "입장 코드 생성에 실패했습니다. 다시 시도해주세요."),
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SMS 발송 실패"),
    TEMPLATE_DOWNLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "레포지토리 템플릿 파일 초기화에 실패했습니다. 잠시 후 다시 시도해주세요."),
    NO_AVAILABLE_PORT(HttpStatus.INTERNAL_SERVER_ERROR, "사용 가능한 실행 포트가 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
