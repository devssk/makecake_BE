package com.project.makecake.service.backoffice;

import com.project.makecake.dto.RedirectUrlRequestDto;
import com.project.makecake.dto.noti.NotiContentRequestDto;
import com.project.makecake.dto.noti.NotiRequestDto;
import com.project.makecake.enums.NotiType;
import com.project.makecake.enums.UserRoleEnum;
import com.project.makecake.exceptionhandler.CustomException;
import com.project.makecake.exceptionhandler.ErrorCode;
import com.project.makecake.domain.noti.FixNoti;
import com.project.makecake.domain.noti.Noti;
import com.project.makecake.domain.noti.PersonalNoti;
import com.project.makecake.domain.user.User;
import com.project.makecake.repository.FixNotiRepository;
import com.project.makecake.repository.NotiRepository;
import com.project.makecake.repository.PersonalNotiRepository;
import com.project.makecake.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BONotiService {

    private final NotiRepository notiRepository;
    private final FixNotiRepository fixNotiRepository;
    private final UserRepository userRepository;
    private final PersonalNotiRepository personalNotiRepository;

    // 알림 등록 메소드
    @Transactional
    public void addNoti(NotiRequestDto requestDto) {

        NotiType type;

        try {
            type = NotiType.valueOf(requestDto.getType().toUpperCase());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.BAD_NOTITYPE);
        }

        // LIKE, COMMENT 알림은 새로 생성 불가
        if(!type.isAdminManage()) {
            throw new CustomException(ErrorCode.NOT_ADD_NOTITYPE);
        }

        Noti noti = new Noti(requestDto);
        notiRepository.save(noti);
    }

    // 알림 내용 수정 메소드
    @Transactional
    public void editNoti(long notiId, NotiContentRequestDto requestDto) {

        if (requestDto.getMainContent()==null || requestDto.getMainContent().equals("")) {
            throw new CustomException(ErrorCode.NOTI_MAIN_CONTENT_NULL);
        }

        Noti foundNoti = notiRepository.findById(notiId)
                .orElseThrow(()->new CustomException(ErrorCode.NOTI_NOT_FOUND));

        // 메세지에 {닉네임}이 포함되어야 하는 알림 타입은 {닉네임}이 있는지 확인
        if (foundNoti.getType().isNeedMessageEdit() && !requestDto.getMainContent().contains("{닉네임}")) {
            throw new CustomException(ErrorCode.NOTI_NICKNAME_NULL);
        }

        foundNoti.editContent(requestDto);
    }

    // 고정 알림 띄우는 메소드
    @Transactional
    public void addFixNoti(long notiId, RedirectUrlRequestDto requestDto) {

        Noti foundNoti = notiRepository.findById(notiId)
                .orElseThrow(()->new CustomException(ErrorCode.NOTI_NOT_FOUND));

        if(!foundNoti.getType().isAdminManage()) {
            throw new CustomException(ErrorCode.NOT_FIX_NOTITYPE);
        }

        FixNoti fixNoti = FixNoti.builder()
                .noti(foundNoti)
                .redirectUrl(requestDto.getRedirectUrl())
                .build();

        fixNotiRepository.save(fixNoti);
    }

    // 고정 알림 내리는 메소드
    @Transactional
    public void editFixNoti(long fixNotiId) {

        FixNoti foundFixNoti = fixNotiRepository.findById(fixNotiId)
                .orElseThrow(()->new CustomException(ErrorCode.FIXNOTI_NOT_FOUND));

        foundFixNoti.editReveal();
    }

    //개인 알림 발송 메소드
    @Transactional
    public void addPersonalNoti(long notiId, RedirectUrlRequestDto requestDto) {

        Noti foundNoti = notiRepository.findById(notiId)
                .orElseThrow(()->new CustomException(ErrorCode.NOTI_NOT_FOUND));

        if(!foundNoti.getType().isAdminManage()) {
            throw new CustomException(ErrorCode.NOT_SEND_NOTITYPE);
        }

        List<User> foundUserList = userRepository.findAllByRole(UserRoleEnum.USER);

        for (User user : foundUserList) {
            PersonalNoti personalNoti = PersonalNoti.builder()
                    .recieveUser(user)
                    .createUser(null)
                    .noti(foundNoti)
                    .redirectUrl(requestDto.getRedirectUrl())
                    .build();
            personalNotiRepository.save(personalNoti);
        }
    }
}
