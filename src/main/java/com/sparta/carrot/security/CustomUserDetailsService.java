package com.sparta.carrot.security;

import com.sparta.carrot.domain.UserEntity;
import com.sparta.carrot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String username) {
        return userRepository.findByUsername(username)
                .map(this::createUser)
                .orElseThrow(() -> new UsernameNotFoundException(
                        username +
                                "이 DB에 존재하지 않습니다.")
                );
    }

    // DB 에 User 값이 존재한다면 UserDetails 객체로 만들어서 리턴
    private User createUser(UserEntity user) {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(user.getAuthority());

        return new User(String.valueOf(user.getId()),
                user.getPassword(),
                Collections.singleton(grantedAuthority)
        );
    }
}
