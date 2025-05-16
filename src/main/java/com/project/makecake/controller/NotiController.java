package com.project.makecake.controller;

import com.project.makecake.dto.noti.NewNotiResponseDto;
import com.project.makecake.dto.noti.NotiResponseDto;
import com.project.makecake.security.UserDetailsImpl;
import com.project.makecake.service.noti.NotiService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RequiredArgsConstructor
@RestController
public class NotiController {

    private final NotiService notiService;

    // 새로운 알림 여부 조회 API
    @GetMapping("/home/new-noti")
    public NewNotiResponseDto getNewNoti(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return notiService.getNewNoti(userDetails);
    }

    // 알림 조회 API
    @GetMapping("/noti")
    public NotiResponseDto getNotiList(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) throws ParseException {
        return notiService.getNotiList(userDetails);
    }

}
