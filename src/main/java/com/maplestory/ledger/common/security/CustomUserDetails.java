package com.maplestory.ledger.common.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security의 UserDetails 구현체.
 *
 * JWT에서 추출한 사용자 정보(userId, nickname)를 담는 래퍼 클래스입니다.
 * JwtAuthFilter에서 SecurityContext에 등록되며,
 * 컨트롤러에서 @AuthenticationPrincipal CustomUserDetails userDetails로 주입받아 사용합니다.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String nickname;

    public CustomUserDetails(Long userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }

    @Override
    public String getUsername() { return nickname; }

    @Override
    public String getPassword() { return null; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(); }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
