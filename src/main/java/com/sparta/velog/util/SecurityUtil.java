package com.sparta.velog.util;

import com.sparta.velog.security.CustomUserDetails;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Slf4j
@NoArgsConstructor
public class SecurityUtil {
    public static long getCurrentUserIdByLong() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.debug("Security Context에 인증 정보가 없습니다.");
            throw new UsernameNotFoundException(
                    "Security Context에 인증 정보가 없습니다.");
        }

        long userId = 0L;
        if (authentication.getPrincipal() instanceof UserDetails) {
            CustomUserDetails springSecurityUser = (CustomUserDetails) authentication.getPrincipal();
            userId = springSecurityUser.getUserId();
        }

        return userId;
    }
}
