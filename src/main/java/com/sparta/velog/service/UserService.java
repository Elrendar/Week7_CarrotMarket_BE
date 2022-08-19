package com.sparta.velog.service;

import com.sparta.velog.domain.RefreshToken;
import com.sparta.velog.domain.UserEntity;
import com.sparta.velog.dto.TokenDto;
import com.sparta.velog.dto.TokenRequestDto;
import com.sparta.velog.dto.UserRequestDto;
import com.sparta.velog.dto.UserResponseDto;
import com.sparta.velog.exception.ExceptionCode;
import com.sparta.velog.exception.runtime.DuplicateUserInfoException;
import com.sparta.velog.exception.runtime.InvalidJWTException;
import com.sparta.velog.exception.runtime.RefreshTokenNotFoundException;
import com.sparta.velog.repository.RefreshTokenRepository;
import com.sparta.velog.repository.UserRepository;
import com.sparta.velog.security.jwt.TokenProvider;
import com.sparta.velog.util.SecurityUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @Transactional
    public UserResponseDto signUp(UserRequestDto userRequestDto) {
        // 비밀번호 2개가 서로 다를 경우
        if (!userRequestDto.getPassword1().equals(userRequestDto.getPassword2())) {
            log.info("입력한 비밀번호가 서로 다릅니다.");
            throw new IllegalArgumentException("입력한 비밀번호가 서로 다릅니다.");
        }

        if (!checkUsername(userRequestDto.getUsername())) {
            log.info("이미 가입되어 있는 유저입니다");
            throw new DuplicateUserInfoException("이미 가입되어 있는 유저입니다");
        }

        return UserResponseDto.of(
                userRepository.save(
                        UserEntity.of(userRequestDto, passwordEncoder)));
    }

    public boolean checkUsername(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Transactional
    public TokenDto login(UserRequestDto userRequestDto) {
        // 1. Login 화면에서 입력 받은 username/pw 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userRequestDto.getUsername(), userRequestDto.getPassword1());

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        Authentication authentication;
        try {
            authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        } catch (AuthenticationException e) {
            log.info("아이디, 혹은 비밀번호가 잘못되었습니다.");
            throw new BadCredentialsException("아이디, 혹은 비밀번호가 잘못되었습니다.");
        }

        // // 3. 검증이 끝나면 해당 정보로 db에서 UserEntity를 검색
        var userId = Long.valueOf(authentication.getName());
        // var userEntity = userRepository.findById(userId)
        //         .orElseThrow(
        //                 () -> new UsernameNotFoundException("userId: " + userId +
        //                         "는 존재하지 않는 회원입니다."));

        // 4. 인증 정보와 PK값을 넣고 JWT 토큰 생성
        TokenDto tokenDto = tokenProvider.createTokenDto(authentication, userId);

        // 5. RefreshToken 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .key(userId)
                .value(tokenDto.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);

        // 6. 토큰 발급
        return tokenDto;
    }

    @Transactional
    public TokenDto logout() {
        var userId = SecurityUtil.getCurrentUserIdByLong();
        var token = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new RefreshTokenNotFoundException(
                        "userId: " + userId + "의 리프레쉬 토큰을 찾을 수 없습니다.")
                );

        refreshTokenRepository.delete(token);

        return tokenProvider.createEmptyTokenDto();
    }

    @Transactional
    public TokenDto renewToken(TokenRequestDto tokenRequestDto) {
        // 1. Refresh Token 검증
        try {
            tokenProvider.validateToken(tokenRequestDto.getRefreshToken());
        } catch (SecurityException | MalformedJwtException e) {
            log.info(ExceptionCode.INVALID_SIGNATURE_TOKEN.getMessage());
            throw new InvalidJWTException(
                    ExceptionCode.INVALID_SIGNATURE_TOKEN.getMessage());
        } catch (ExpiredJwtException e) {
            log.info(ExceptionCode.EXPIRED_TOKEN.getMessage());
            throw new InvalidJWTException(
                    ExceptionCode.EXPIRED_TOKEN.getMessage());
        } catch (UnsupportedJwtException e) {
            log.info(ExceptionCode.UNSUPPORTED_TOKEN.getMessage());
            throw new InvalidJWTException(ExceptionCode.UNSUPPORTED_TOKEN.getMessage());
        } catch (IllegalArgumentException e) {
            log.info(ExceptionCode.WRONG_TOKEN.getMessage());
            throw new InvalidJWTException(ExceptionCode.WRONG_TOKEN.getMessage());
        } catch (Exception e) {
            log.info(ExceptionCode.UNKNOWN_ERROR.getMessage());
            throw new InvalidJWTException(ExceptionCode.UNKNOWN_ERROR.getMessage());
        }

        // 2. Access Token 에서 userId(PK) 가져오기
        Authentication authentication = tokenProvider.getAuthentication(tokenRequestDto.getAccessToken());
        var userId = Long.parseLong(authentication.getName());

        // 3. 리프레쉬 토큰 저장소에서 userId(PK) 를 기반으로 토큰 가져옴
        RefreshToken savedRefreshToken = refreshTokenRepository.findById(userId)
                .orElseThrow(() ->
                        new RefreshTokenNotFoundException("로그아웃 된 사용자입니다."));

        // 4. Refresh Token 일치하는지 검사
        if (!savedRefreshToken.getValue().equals(tokenRequestDto.getRefreshToken())) {
            throw new InvalidJWTException("토큰의 유저 정보가 일치하지 않습니다.");
        }
        // 리프레쉬 토큰 만료시간 검증 필요

        // 5. Access Token 에서 가져온 userId(PK)를 다시 새로운 토큰의 클레임에 넣고 토큰 생성
        TokenDto tokenDto = tokenProvider.createTokenDto(authentication, userId);

        // 6. db의 리프레쉬 토큰 정보 업데이트
        RefreshToken newRefreshToken =
                savedRefreshToken.withValue(tokenDto.getRefreshToken());
        refreshTokenRepository.save(newRefreshToken);

        // 토큰 발급
        return tokenDto;
    }
}
