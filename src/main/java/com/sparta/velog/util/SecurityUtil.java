package com.sparta.velog.util;

import io.jsonwebtoken.lang.Assert;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

@Slf4j
@NoArgsConstructor
public class SecurityUtil {
    public static Long getCurrentUserIdByLong() {
        String userId = getUserIdFromHolder().orElse(null);

        try {
            Assert.notNull(userId);
        } catch (IllegalArgumentException e) {
            log.debug("Security Context에 담긴 인증 정보가 null입니다.");
            throw new UsernameNotFoundException(
                    "Security Context에 담긴 인증 정보가 null입니다.");
        }

        return Long.parseLong(userId);
    }

    public static Optional<String> getCurrentUserIdByString() {
        return getUserIdFromHolder();
    }

    private static Optional<String> getUserIdFromHolder() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.debug("Security Context에 인증 정보가 없습니다.");
            return Optional.empty();
        }

        String userId = null;
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
            userId = springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            userId = (String) authentication.getPrincipal();
        }

        return Optional.ofNullable(userId);
    }
}
