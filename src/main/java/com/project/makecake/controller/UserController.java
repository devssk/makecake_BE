package com.project.makecake.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.makecake.dto.ImageInfoDto;
import com.project.makecake.dto.mypage.MypageResponseDto;
import com.project.makecake.dto.user.LoginCheckResponseDto;
import com.project.makecake.dto.user.SignupRequestDto;
import com.project.makecake.enums.FolderName;
import com.project.makecake.security.UserDetailsImpl;
import com.project.makecake.service.*;
import com.project.makecake.service.user.GoogleLoginService;
import com.project.makecake.service.user.KakaoLoginService;
import com.project.makecake.service.user.NaverLoginService;
import com.project.makecake.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;


@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;
    private final KakaoLoginService kakaoLoginService;
    private final NaverLoginService naverLoginService;
    private final GoogleLoginService googleLoginService;
    private final S3Service s3Service;

    // 회원가입 API
    @PostMapping("/users/signup")
    public HashMap<String, Boolean> addUser(
            @RequestBody SignupRequestDto requestDto
    ) {
        return userService.addUser(requestDto);
    }

    // username 중복검사 API
    @PostMapping("/users/username-check")
    public HashMap<String, Boolean> checkUsername(
            @RequestBody SignupRequestDto requestDto
    ) {
        return userService.checkUsername(requestDto);
    }

    // nickname 중복검사 API
    @PostMapping("/users/nickname-check")
    public HashMap<String, Boolean> checkNickname(
            @RequestBody SignupRequestDto requestDto
    ) {
        return userService.checkNickname(requestDto);
    }

    // 로그인 체크 API
    @GetMapping("/users/login-check")
    public LoginCheckResponseDto checkLogin(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return userService.checkLogin(userDetails);
    }

    // 프로필 수정 API
    @PutMapping("/users/profile")
    public MypageResponseDto editProfile(
            @RequestParam(value = "imgFile", required = false) MultipartFile imgFile,
            @RequestParam String nickname,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) throws IOException {
        return userService.editProfile(imgFile, nickname, userDetails);
    }

    // 회원탈퇴 API
    @PutMapping("/users/resign")
    public MypageResponseDto resignUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return userService.resignUser(userDetails);
    }

    // (임시) 이미지 업로드 API
    @PostMapping("/users/image")
    public ImageInfoDto userImage(
            @RequestParam(value = "imgFile", required = false) MultipartFile imgFile
    ) throws IOException {
        ImageInfoDto imageInfoDto = s3Service.uploadImg(imgFile, FolderName.PROFILE.name());
        return imageInfoDto;
    }

    // 카카오 로그인 API
    @GetMapping("/user/kakao/callback")
    public void kakaoLogin(
            @RequestParam String code,
            HttpServletResponse response
    ) throws JsonProcessingException {
        kakaoLoginService.kakaoLogin(code, response);
    }

    // 네이버 로그인 API
    @GetMapping("/user/naver/callback")
    public void naverLogin(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletResponse response
    ) throws JsonProcessingException {
        naverLoginService.naverLogin(code, state, response);
    }

    // 구글 로그인 API
    @GetMapping("/user/google/callback")
    public void googleLogin(
            @RequestParam String code,
            HttpServletResponse response
    ) throws JsonProcessingException {
        googleLoginService.googleLogin(code, response);
    }

}
