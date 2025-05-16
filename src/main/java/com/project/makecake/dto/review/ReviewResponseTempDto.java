package com.project.makecake.dto.review;

import com.project.makecake.domain.review.Review;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewResponseTempDto {

    private Long reviewId;
    private Long storeId;
    private String writerNickname;
    private String createdDate;
    private String content;
    private String reviewImage;

    public ReviewResponseTempDto(Review review, String reviewImage){
        this.reviewId = review.getReviewId();
        this.storeId = review.getStore().getStoreId();
        this.writerNickname = review.getUser().getNickname();
        this.createdDate = review.getCreatedAt();
        this.content = review.getContent();
        this.reviewImage = reviewImage;
    }

}
