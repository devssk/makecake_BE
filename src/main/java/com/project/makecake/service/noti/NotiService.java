package com.project.makecake.service.noti;

import com.project.makecake.dto.noti.NewNotiResponseDto;
import com.project.makecake.dto.noti.NotiResponseDto;
import com.project.makecake.domain.noti.FixNoti;
import com.project.makecake.domain.noti.PersonalNoti;
import com.project.makecake.domain.user.User;
import com.project.makecake.repository.FixNotiRepository;
import com.project.makecake.repository.PersonalNotiRepository;
import com.project.makecake.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class NotiService {

    private final PersonalNotiRepository personalNotiRepository;
    private final FixNotiRepository fixNotiRepository;

    // 새로운 알림 여부 조회 메소드
    public NewNotiResponseDto getNewNoti(UserDetailsImpl userDetails) {

        // 비회원이면 new값을 false로 return
        if (userDetails == null) {
            return new NewNotiResponseDto(true);
        }

        User user = userDetails.getUser();

        boolean existsNewNoti = personalNotiRepository.existsByRecieveUserAndChecked(user,false);
        return new NewNotiResponseDto(!existsNewNoti);

    }

    // 알림 조회 메소드
    @Transactional
    public NotiResponseDto getNotiList(UserDetailsImpl userDetails) throws ParseException {

        User user = userDetails.getUser();

        List<NotiResponseDto.Fix> fixNotiResponseDtoList = getFixNotiList();

        List<NotiResponseDto.Personal> personalNotiResponseDtoList = getPersonalNotiList(user);

        return NotiResponseDto.builder()
                .fixNotiList(fixNotiResponseDtoList)
                .personalNotiList(personalNotiResponseDtoList)
                .build();

    }

    // FixNoti 찾기 메소드
    public List<NotiResponseDto.Fix> getFixNotiList() {

        List<FixNoti> foundFixNotiList = fixNotiRepository.findAllByReveal(true);

        List<NotiResponseDto.Fix> fixNotiResponseDtoList = new ArrayList<>();
        for (FixNoti fixNoti : foundFixNotiList) {
            NotiResponseDto.Fix responseDto = new NotiResponseDto.Fix(fixNoti);
            fixNotiResponseDtoList.add(responseDto);
        }

        return fixNotiResponseDtoList;
    }

    // PersonalNoti 찾기 메소드
    public List<NotiResponseDto.Personal> getPersonalNotiList(User user) throws ParseException {

        List<PersonalNoti> foundPersonalNotiList = personalNotiRepository.findTop30ByRecieveUserOrderByCreatedAtDesc(user);

        List<NotiResponseDto.Personal> personalNotiResponseDtoList = new ArrayList<>();

        for (PersonalNoti personalNoti : foundPersonalNotiList) {

            String timeDiff = calculateTimeDiff(personalNoti);

            NotiResponseDto.Personal responseDto = addPersonalNotiResponseDto(personalNoti,timeDiff);

            personalNotiResponseDtoList.add(responseDto);

            personalNoti.editChecked();
        }

        // 최신 30개에 포함되지 않은 알림은 자동 읽음 처리
        List<PersonalNoti> foundUncheckedPersonalNotiList = personalNotiRepository
                .findAllByRecieveUserAndChecked(user,false);

        for (PersonalNoti personalNoti : foundUncheckedPersonalNotiList) {
            personalNoti.editChecked();
        }

        return personalNotiResponseDtoList;
    }

    // 시간 차이 계산 메소드
    public String calculateTimeDiff(PersonalNoti personalNoti) throws ParseException {
        Date nowDateTime = new Date();

        String createdAt = personalNoti.getCreatedAt();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date createdDateTime = formatter.parse(createdAt);

        long diff = (nowDateTime.getTime()-createdDateTime.getTime())/60000;

        String timeDiff;
        if (diff<60) {
            timeDiff = diff+"분전";
        } else if (diff<60*24) {
            timeDiff = diff/60+"시간전";
        } else {
            timeDiff = diff/(60*24)+"일전";
        }

        return timeDiff;
    }

    // PersonalNoti DTO 생성
    public NotiResponseDto.Personal addPersonalNotiResponseDto(PersonalNoti personalNoti, String timeDiff) {

        // mainContent의 수정이 필요한 경우
        if (personalNoti.getNoti().getType().isNeedMessageEdit()) {

            String editedMainContent = editMainContent(personalNoti);

            return NotiResponseDto.Personal.editBuilder()
                    .personalNoti(personalNoti)
                    .editedMainContent(editedMainContent)
                    .timeDiff(timeDiff)
                    .editBuild();

        // mainContent을 수정하지 않는 경우
        } else {
            return NotiResponseDto.Personal.nonEditBuilder()
                    .personalNoti(personalNoti)
                    .timeDiff(timeDiff)
                    .nonEditBuild();
        }
    }

    // 알림 메인 내용 수정 메소드
    public String editMainContent(PersonalNoti personalNoti) {

        String mainContent = personalNoti.getNoti().getMainContent();

        // createUser의 닉네임 넣어서 반환
        mainContent = mainContent.replace("{닉네임}",personalNoti.getCreateUser().getNickname());

        return mainContent;
    }
}
