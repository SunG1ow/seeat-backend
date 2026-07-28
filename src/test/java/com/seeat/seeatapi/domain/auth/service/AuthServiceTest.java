package com.seeat.seeatapi.domain.auth.service;

import com.seeat.seeatapi.domain.auth.dto.request.SignupRequest;
import com.seeat.seeatapi.domain.auth.dto.response.SignupResponse;
import com.seeat.seeatapi.domain.member.entity.Member;
import com.seeat.seeatapi.domain.member.entity.MemberRole;
import com.seeat.seeatapi.domain.member.repository.MemberRepository;
import com.seeat.seeatapi.domain.member.repository.SellerBusinessInfoRepository;
import com.seeat.seeatapi.global.exception.BusinessException;
import com.seeat.seeatapi.global.exception.ErrorCode;
import com.seeat.seeatapi.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SellerBusinessInfoRepository sellerBusinessInfoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("1-1 회원가입 성공")
    void signup_success() {
        // given
        SignupRequest request = new SignupRequest(
                "test@seeat.com", "password123", "BUYER", "테스트유저", "010-1234-5678"
        );
        when(memberRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        SignupResponse response = authService.signup(request);

        // then
        assertThat(response.email()).isEqualTo("test@seeat.com");
        assertThat(response.role()).isEqualTo("BUYER");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("1-1 회원가입 실패 - 이메일 중복")
    void signup_fail_duplicateEmail() {
        // given
        SignupRequest request = new SignupRequest(
                "duplicate@seeat.com", "password123", "BUYER", "테스트유저", "010-1234-5678"
        );
        when(memberRepository.existsByEmail(request.email())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("1-2 로그인 실패 - 존재하지 않는 이메일")
    void login_fail_memberNotFound() {
        // given
        com.seeat.seeatapi.domain.auth.dto.request.LoginRequest request =
                new com.seeat.seeatapi.domain.auth.dto.request.LoginRequest("notfound@seeat.com", "password123");
        when(memberRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
    }
}