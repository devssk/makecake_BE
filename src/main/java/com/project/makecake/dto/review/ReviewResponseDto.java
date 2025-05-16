package com.project.makecake.dto.review;

import com.project.makecake.domain.review.Review;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter

public class ReviewResponseDto {

    private Long reviewId;
    private String writerImg, writerNickname, createdDate,content;
    private List<String> reviewImgList;

    @Builder
    public ReviewResponseDto(Review review, List<String> reviewImgList){
        this.reviewId = review.getReviewId();
        this.writerImg = review.getUser().getProfileImgUrl();
        this.writerNickname = review.getUser().getNickname();
        this.createdDate = review.getCreatedAt();
        this.content = review.getContent();
        this.reviewImgList = reviewImgList;
    }

}
