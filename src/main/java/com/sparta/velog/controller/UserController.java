package com.sparta.velog.controller;

import com.sparta.velog.dto.TokenDto;
import com.sparta.velog.dto.TokenRequestDto;
import com.sparta.velog.dto.UserRequestDto;
import com.sparta.velog.dto.UserResponseDto;
import com.sparta.velog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("/api/users")
@RestController
public class UserController {
    private final UserService userService;

    // 회원 가입 요청
    @PostMapping("/auth/signup")
    public ResponseEntity<UserResponseDto> signUp(@RequestBody @Valid UserRequestDto userRequestDto) {
        return ResponseEntity.ok(userService.signUp(userRequestDto));
    }

    // 로그인 요청
    @PostMapping("/auth/login")
    public ResponseEntity<TokenDto> login(@RequestBody @Valid UserRequestDto userRequestDto) {
        return ResponseEntity.ok(userService.login(userRequestDto));
    }

    // 아이디 중복 체크
    @GetMapping("/auth/dupcheck")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username) {
        Assert.hasText(username, "username이 비어있거나 null입니다.");
        return ResponseEntity.ok(userService.checkUsername(username));
    }

    // 로그아웃 요청
    @PostMapping("/logout")
    public ResponseEntity<TokenDto> logout() {
        return ResponseEntity.ok(userService.logout());
    }

    // 토큰 재발급
    @PostMapping("/renew")
    public ResponseEntity<TokenDto> renewToken(@RequestBody TokenRequestDto tokenRequestDto) {
        return ResponseEntity.ok(userService.renewToken(tokenRequestDto));
    }
}
