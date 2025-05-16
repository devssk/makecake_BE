package com.project.makecake.repository;

import com.project.makecake.domain.review.Review;
import com.project.makecake.domain.review.ReviewImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewImgRepository extends JpaRepository<ReviewImg, Long> {

    List<ReviewImg> findAllByReview_ReviewId(long reviewId);
    ReviewImg findTop1ByReview(Review review);
    Optional<ReviewImg> findByReview(Review review);
    void deleteAllByReview_ReviewId(long reviewId);
    void deleteByImgUrl(String imgUrl);
    ReviewImg findByImgUrl(String imgUrl);

}
