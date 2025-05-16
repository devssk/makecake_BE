package com.project.makecake.controller;

import com.project.makecake.dto.DesignResponseDto;
import com.project.makecake.security.UserDetailsImpl;
import com.project.makecake.service.order.DesignService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
public class DesignController {

    private final DesignService designService;

    // 도안 저장 API
    @PostMapping("/designs")
    public DesignResponseDto addDesign(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam MultipartFile imgFile
    ) throws IOException {
        return designService.addDesign(userDetails, imgFile);
    }

    // 도안 삭제 API
    @DeleteMapping("/designs/{designId}")
    public void removeDesign(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable long designId
    ) {
        designService.removeDesign(userDetails, designId);
    }

}
