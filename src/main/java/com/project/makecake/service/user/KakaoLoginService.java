package com.project.makecake.service.user;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.makecake.enums.UserRoleEnum;
import com.project.makecake.domain.user.User;
import com.project.makecake.repository.UserRepository;
import com.project.makecake.security.JwtProperties;
import com.project.makecake.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoLoginService {

    @Value("${kakao.client-id}")
    String kakaoClientId;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // 카카오 로그인
    public void kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {

        // 인가코드로 엑세스토큰 가져오기
        String accessToken = getAccessToken(code);

        // 엑세스토큰으로 유저정보 가져오기
        JsonNode kakaoUserInfo = getKakaoUserInfo(accessToken);

        // 유저확인 & 회원가입
        User foundUser = getUser(kakaoUserInfo);

        // 강제로 시큐리티 로그인
        UserDetailsImpl userDetails = securityLogin(foundUser);

        // jwt 토큰 발급
        jwtToken(response, userDetails);
    }

    // 인가 코드로 엑세스토큰 가져오기
    private String getAccessToken(String code) throws JsonProcessingException {

        // 헤더에 Content-type 지정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        System.out.println(code);
        // 바디에 필요한 정보 담기
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        // 새로운 코드
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", "https://make-cake.com/user/kakao/callback");
        body.add("code", code);

        // POST 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoToken = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST, kakaoToken,
                String.class
        );

        // response에서 엑세스토큰 가져오기
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseToken = objectMapper.readTree(responseBody);
        String accessToken = responseToken.get("access_token").asText();
        return accessToken;
    }

    // 엑세스토큰으로 유저정보 가져오기
    private JsonNode getKakaoUserInfo(String accessToken) throws JsonProcessingException {

        // 헤더에 엑세스토큰 담기
        HttpHeaders headers = new HttpHeaders();
        headers.add(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + accessToken);

        // POST 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUser = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUser,
                String.class
        );

        // response에서 유저정보 가져오기
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode kakaoUserInfo = objectMapper.readTree(responseBody);
        return kakaoUserInfo;
    }

    // 유저확인 & 회원가입
    private User getUser(JsonNode kakaoUserInfo) {

        // 유저정보 작성
        String providerId = kakaoUserInfo.get("id").asText();
        String provider = "kakao";
        String username = provider + "_" + providerId;
        String nickname = kakaoUserInfo.get("properties").get("nickname").asText();
        Optional<User> nicknameCheck = userRepository.findByNickname(nickname);
        if (nicknameCheck.isPresent()) {
            String tempNickname = nickname;
            int i = 1;
            while (true){
                nickname = tempNickname;
                nickname = nickname + "_" + i;
                Optional<User> nicknameCheck2 = userRepository.findByNickname(nickname);
                if (!nicknameCheck2.isPresent()) {
                    break;
                }
                i++;
            }
        }
        String password = passwordEncoder.encode(UUID.randomUUID().toString());
        String profileImgUrl = "https://makecake.s3.ap-northeast-2.amazonaws.com/PROFILE/ef771589-abc6-4ddd-951c-73cc2420aa2fKakaoTalk_20220329_214148108.png";
        UserRoleEnum role = UserRoleEnum.USER;

        // DB에서 username으로 가져오기 없으면 회원가입
        User foundUser = userRepository.findByUsername(username).orElse(null);
        if (foundUser == null){
            foundUser = User.builder()
                    .username(username)
                    .nickname(nickname)
                    .password(password)
                    .profileImgUrl(profileImgUrl)
                    .profileImgName(null)
                    .role(role)
                    .provider(provider)
                    .providerId(providerId)
                    .providerEmail(null)
                    .build();
            userRepository.save(foundUser);
        }
        return foundUser;
    }

    // 강제로 시큐리티 로그인
    private UserDetailsImpl securityLogin(User foundUser) {

        // userDetails 생성
        UserDetailsImpl userDetails = new UserDetailsImpl(foundUser);
        log.info("kakao 로그인 완료 : " + userDetails.getUser().getUsername());
        // UsernamePasswordAuthenticationToken 발급
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        // 강제로 시큐리티 세션에 접근하여 authentication 객체를 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return userDetails;
    }

    // jwt 토큰 발급
    private void jwtToken(HttpServletResponse response, UserDetailsImpl userDetails) {

        String jwtToken = JWT.create()
                // 토큰이름
                .withSubject("JwtToken : " + userDetails.getUser().getUsername())
                // 유효시간
                .withClaim("expireDate", new Date(System.currentTimeMillis() + JwtProperties.tokenValidTime))
                // username
                .withClaim("username", userDetails.getUser().getUsername())
                // HMAC256 복호화
                .sign(Algorithm.HMAC256(JwtProperties.secretKey));
        log.info("jwtToken : " + jwtToken);
        response.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken);
    }
}
