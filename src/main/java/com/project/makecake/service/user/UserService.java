package com.project.makecake.service.user;

import com.project.makecake.dto.ImageInfoDto;
import com.project.makecake.dto.mypage.MypageResponseDto;
import com.project.makecake.dto.user.LoginCheckResponseDto;
import com.project.makecake.dto.user.SignupRequestDto;
import com.project.makecake.enums.FolderName;
import com.project.makecake.enums.UserRoleEnum;
import com.project.makecake.exceptionhandler.CustomException;
import com.project.makecake.exceptionhandler.ErrorCode;
import com.project.makecake.domain.user.User;
import com.project.makecake.repository.UserRepository;
import com.project.makecake.security.UserDetailsImpl;
import com.project.makecake.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class UserService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    // 회원가입
    public HashMap<String, Boolean> addUser(SignupRequestDto requestDto) {

        validate(requestDto);

        String username = requestDto.getUsername();
        String nickname = requestDto.getNickname();

        String password = passwordEncoder.encode(requestDto.getPassword());

        // 기본 프로필 이미지
        String profileImgUrl = "https://makecake.s3.ap-northeast-2.amazonaws.com/PROFILE/ef771589-abc6-4ddd-951c-73cc2420aa2fKakaoTalk_20220329_214148108.png";

        UserRoleEnum role = UserRoleEnum.USER;

        User user = User.builder()
                .username(username)
                .nickname(nickname)
                .password(password)
                .profileImgUrl(profileImgUrl)
                .profileImgName(null)
                .role(role)
                .build();

        User savedUser = userRepository.save(user);

        // 회원가입 확인
        Optional<User> foundUser = userRepository.findById(savedUser.getUserId());
        HashMap<String, Boolean> responseDto = new HashMap<>();
        responseDto.put("signup", foundUser.isPresent());
        return responseDto;
    }

    // 로그인체크 메소드
    public LoginCheckResponseDto checkLogin(UserDetailsImpl userDetails) {

        User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        LoginCheckResponseDto responseDto = LoginCheckResponseDto.builder()
                .userId(foundUser.getUserId())
                .nickname(foundUser.getNickname())
                .build();
        return responseDto;
    }

    // 회원 탈퇴 메소드 (회원삭제가 아니라 회원정보를 삭제)
    public MypageResponseDto resignUser(UserDetailsImpl userDetails) {

        User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElse(null);

        if (foundUser != null) {
            String username = "resignUser_"+foundUser.getUserId();
            String password = passwordEncoder.encode(UUID.randomUUID().toString());
            String nickname = "알수없음";
            foundUser.setUsername(username);
            foundUser.setNickname(nickname);
            foundUser.setPassword(password);
            foundUser.setProfileImgUrl("https://makecake.s3.ap-northeast-2.amazonaws.com/PROFILE/18d2090b-1b98-4c34-b92b-a9f50d03bd53makecake_default.png");
            foundUser.setProfileImgName(null);
            foundUser.setRole(null);
            foundUser.setProvider(null);
            foundUser.setProviderEmail(null);
            foundUser.setProviderId(null);
            userRepository.save(foundUser);
        }

        String email = foundUser.getUsername();
        if (foundUser.getProviderEmail() != null){
            email = foundUser.getProviderEmail();
        }

        MypageResponseDto responseDto = MypageResponseDto.builder()
                .nickname(foundUser.getNickname())
                .profileImg(foundUser.getProfileImgUrl())
                .email(email)
                .build();
        return responseDto;
    }

    // 프로필 수정 메소드
    public MypageResponseDto editProfile(
            MultipartFile imgFile,
            String editNickname,
            UserDetailsImpl userDetails
    ) throws IOException {

        User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        String profile = foundUser.getProfileImgUrl();
        String profileName = foundUser.getProfileImgName();

        if (imgFile != null){
            if (foundUser.getProfileImgName() != null){
                s3Service.deleteFile(foundUser.getProfileImgName());
            }
            ImageInfoDto imageInfoDto = s3Service.uploadImg(imgFile, FolderName.PROFILE.name());
            profile = imageInfoDto.getUrl();
            profileName = imageInfoDto.getName();
        }

        String nickname = foundUser.getNickname();
        if (!nickname.equals(editNickname)){
            // 닉네임 중복체크
            Optional<User> checkNickname = userRepository.findByNickname(editNickname);
            if (checkNickname.isPresent()){
                throw new CustomException(ErrorCode.NICKNAME_DUPLICATE);
            }
            nickname = editNickname;
        }

        foundUser.setNickname(nickname);
        foundUser.setProfileImgUrl(profile);
        foundUser.setProfileImgName(profileName);
        User savedUser = userRepository.save(foundUser);
        MypageResponseDto responseDto = MypageResponseDto.builder()
                .nickname(savedUser.getNickname())
                .profileImg(savedUser.getProfileImgUrl())
                .build();
        return responseDto;
    }

    // 유효성 검사 메소드
    private void validate(SignupRequestDto requestDto) {

        Pattern usernamePattern = Pattern
                .compile("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$");
        Matcher usernameMatcher = usernamePattern.matcher(requestDto.getUsername());
        if (!usernameMatcher.matches()) {
            throw new CustomException(ErrorCode.USERNAME_WRONG);
        }

        // username 중복체크
        Optional<User> checkUsername = userRepository.findByUsername(requestDto.getUsername());
        if (checkUsername.isPresent()) {
            throw new CustomException(ErrorCode.EMAIL_DUPLICATE);
        }

        // nickname 길이체크 (2~8)
        String nickname = requestDto.getNickname();
        if (nickname.length() < 2 || nickname.length() > 8) {
            throw new CustomException(ErrorCode.NICKNAME_LENGTH_WRONG);
        }

        // nickname 중복체크
        Optional<User> checkNickname = userRepository.findByNickname(requestDto.getNickname());
        if (checkNickname.isPresent()) {
            throw new CustomException(ErrorCode.NICKNAME_DUPLICATE);
        }

        // 패스워드 유효성체크 (영문, 숫자, 특수문자 1개씩 필수, 10~20)
        Pattern passwordPattern = Pattern.compile("^.*(?=^.{10,20}$)(?=.*\\d)(?=.*[a-zA-Z])(?=.*[~`!@#$%/^&*()-+=])[A-Za-z\\d~`!@#$%/^&*()-+=]*$");
        Matcher passwordMatcher = passwordPattern.matcher(requestDto.getPassword());
        if (!passwordMatcher.matches()) {
            throw new CustomException(ErrorCode.PASSWORD_WRONG);
        }

        // 패스워드 일치 확인
        if (!requestDto.getPassword().equals(requestDto.getPasswordCheck())) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCH);
        }
    }

    // username 중복검사 메소드
    public HashMap<String, Boolean> checkUsername(SignupRequestDto requestDto) {

        String username = requestDto.getUsername();
        Optional<User> sameUser = userRepository.findByUsername(username);
        HashMap<String, Boolean> responseDto = new HashMap<>();
        responseDto.put("isTrue", sameUser.isPresent());
        return responseDto;
    }

    // nickname 중복검사 메소드
    public HashMap<String, Boolean> checkNickname(SignupRequestDto requestDto) {

        String nickname = requestDto.getNickname();
        Optional<User> sameNickname = userRepository.findByNickname(nickname);
        HashMap<String, Boolean> responseDto = new HashMap<>();
        responseDto.put("isTrue", sameNickname.isPresent());
        return responseDto;
    }

}
