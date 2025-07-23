package com.deepdirect.deepwebide_be.member.controller;

import com.deepdirect.deepwebide_be.global.dto.ApiResponseDto;
import com.deepdirect.deepwebide_be.member.dto.request.FindEmailRequest;
import com.deepdirect.deepwebide_be.member.dto.request.SignInRequest;
import com.deepdirect.deepwebide_be.member.dto.request.SignUpRequest;
import com.deepdirect.deepwebide_be.member.dto.response.FindEmailResponse;
import com.deepdirect.deepwebide_be.member.dto.response.SignInResponse;
import com.deepdirect.deepwebide_be.member.dto.response.SignUpResponse;
import com.deepdirect.deepwebide_be.member.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(
    origins = {
        "http://localhost:5173",
    },
    allowCredentials = "true"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        SignUpResponse response = userService.signup(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.of(201, "회원가입이 완료되었습니다.", response));
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponseDto<SignInResponse>> signIn(
            @Valid @RequestBody SignInRequest signInRequest,
            HttpServletResponse servletResponse
    ) {
        // 1. 서비스에서 로그인 + 토큰 2개 발급
        SignInResponse response = userService.signIn(signInRequest, servletResponse);
        // response(본문)는 AccessToken만, RefreshToken은 쿠키로 헤더에 내려감!
        return ResponseEntity.ok(ApiResponseDto.of(200, "로그인에 성공했습니다.", response));
    }

    @PostMapping("/signout")
    public ResponseEntity<ApiResponseDto<Void>> signOut(
            @RequestHeader("Authorization") String authorizationHeader,
            HttpServletResponse response
    ) {
        userService.signOut(authorizationHeader, response);
        return ResponseEntity.ok(ApiResponseDto.of(200, "로그아웃 되었습니다.", null));
    }

    @PostMapping("/email/find")
    public ResponseEntity<ApiResponseDto<FindEmailResponse>> findEmail(@Valid @RequestBody FindEmailRequest request) {
        FindEmailResponse response = new FindEmailResponse(userService.findEmail(request));
        return ResponseEntity.ok(ApiResponseDto.of(200, "이메일(아이디)을 찾았습니다.", response));
    }
}
