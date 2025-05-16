package com.project.makecake.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.project.makecake.exceptionhandler.CustomException;
import com.project.makecake.exceptionhandler.ErrorCode;
import com.project.makecake.domain.user.User;
import com.project.makecake.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

// 시큐리티가 filter들을 가지고 있는데 그 필터중에 BasicAuthenticationFilter 가 있다.
// 권한이나 인증이 필요한 특정 주소를 요청했을 때 위 필터를 무조건 타게 되어있다.
// 만약에 인증이나 권한이 필요한 주소가 아니라면 이 필터를 타지 않음.
@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private UserRepository userRepository;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository){
        super(authenticationManager);
        this.userRepository = userRepository;
    }

    // 인증이나 권한이 필요한 주소요청이 있을 때 해당 필터를 탐.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("인증이나 권한이 필요한 주소 요청 필터단계");

        String jwtHeader = request.getHeader(JwtProperties.HEADER_STRING);
        log.info("헤더 jwt : " + jwtHeader);

        // header에서 jwt토큰이 없거나 Bearer 타입이 아니면 다시 필터를 타게함.
        if (jwtHeader == null || !jwtHeader.startsWith(JwtProperties.TOKEN_PREFIX)) {
            chain.doFilter(request,response);
            return;
        }
        // 토큰 가져오기
        String jwtToken = request.getHeader(JwtProperties.HEADER_STRING).replace(JwtProperties.TOKEN_PREFIX, "");
        // 토큰에서 유효시간 빼기
        Date expireDate = JWT.require(Algorithm.HMAC256(JwtProperties.secretKey)).build().verify(jwtToken).getClaim("expireDate").asDate();
        Date now = new Date();
        log.info("토큰 유효시간 : " + expireDate + " 현재시간 : " + now);
        // 유효시간 검증
        if (expireDate.before(now)) {
            throw new CustomException(ErrorCode.EXPIRED_JWT);
        }
        // 토큰에서 username 검증
        String username = "";
        try {
            username = JWT.require(Algorithm.HMAC256(JwtProperties.secretKey)).build().verify(jwtToken).getClaim("username").asString();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.WRONG_JWT);
        }
        if (username != null) {
            log.info("username 정상 : " + username);

            User userEntity = userRepository.findByUsername(username).orElse(null);
            // 회원 검증
            if (userEntity == null) {
                throw new CustomException(ErrorCode.USER_NOT_FOUND);
            }

            UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            // 강제로 시큐리티 세션에 접근하여 authentication 객체를 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request,response);
    }
}
