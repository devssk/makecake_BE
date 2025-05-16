package com.project.makecake.controller;

import com.project.makecake.dto.review.ReviewResponseTempDto;
import com.project.makecake.security.UserDetailsImpl;
import com.project.makecake.service.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 매장 후기 작성 API
    @PostMapping("/stores/{storeId}/reviews")
    public void addReview(
            @PathVariable long storeId,
            @RequestPart(value="content") String content,
            @RequestParam(required = false) List<MultipartFile> imgFileList,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) throws IOException {
        reviewService.addReview(storeId, content, imgFileList, userDetails);
    }

    // 매장 후기 상세 조회 API
    @GetMapping("/reviews/{reviewId}")
    public ReviewResponseTempDto getReviewDetails(@PathVariable long reviewId){
        return reviewService.getReviewDetails(reviewId);
    }

    // 매장 후기 수정 API
    @PutMapping(path = "/reviews/{reviewId}", consumes = {"multipart/form-data"})
    public void editReview(@PathVariable long reviewId,
                           @RequestParam(value="content") String content,
                           @RequestParam(required = false) List<MultipartFile> imgFileList,
                           @RequestParam(value= "imgUrlList") List<String> imgUrlList,
                           @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException{
        reviewService.editReview(reviewId, content, imgFileList, imgUrlList, userDetails);
    }

    // 매장 후기 삭제 API
    @DeleteMapping("/reviews/{reviewId}")
    public void deleteReview(@PathVariable long reviewId){
        reviewService.deleteReview(reviewId);
    }

}
