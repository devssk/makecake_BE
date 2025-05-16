package com.project.makecake.dto.noti;

import com.project.makecake.domain.noti.FixNoti;
import com.project.makecake.domain.noti.PersonalNoti;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class NotiResponseDto {

    private List<Fix> fixNotiList;
    private List<Personal> personalNotiList;

    @Getter
    public static class Fix {
        private String type, mainContent, subContent, redirectUrl;

        // 생성자
        public Fix(FixNoti fixNoti) {
            this.type = fixNoti.getNoti().getType().toString();
            this.mainContent = fixNoti.getNoti().getMainContent();
            this.subContent = fixNoti.getNoti().getSubContent();
            this.redirectUrl = fixNoti.getRedirectUrl();
        }
    }

    @Getter
    public static class Personal {
        private String type, mainContent, subContent, timeDiff, redirectUrl;
        private boolean checked;

        // 광고, 공지 알림인 경우 생성자
        @Builder(builderMethodName = "nonEditBuilder", buildMethodName = "nonEditBuild")
        public Personal(PersonalNoti personalNoti, String timeDiff) {
            this.type = personalNoti.getNoti().getType().toString();
            this.mainContent = personalNoti.getNoti().getMainContent();
            this.subContent = personalNoti.getNoti().getSubContent();
            this.checked = personalNoti.isChecked();
            this.timeDiff = timeDiff;
            this.redirectUrl = personalNoti.getRedirectUrl();
        }

        // 좋아요, 댓글 알림인 경우 생성자
        @Builder(builderMethodName = "editBuilder", buildMethodName = "editBuild")
        public Personal(PersonalNoti personalNoti, String editedMainContent, String timeDiff) {
            this.type = personalNoti.getNoti().getType().toString();
            this.mainContent = editedMainContent;
            this.subContent = personalNoti.getNoti().getSubContent();
            this.checked = personalNoti.isChecked();
            this.timeDiff = timeDiff;
            this.redirectUrl = personalNoti.getRedirectUrl();
        }
    }
}
