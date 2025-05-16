package com.project.makecake.repository;

import com.project.makecake.domain.review.Review;
import com.project.makecake.domain.store.Store;
import com.project.makecake.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<Review> findTop3ByStoreOrderByCreatedAtDesc(Store store);
    Page<Review> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    List<Review> findTop5ByOrderByCreatedAtDesc();
    Page<Review> findAllByStore_StoreId(Long storeId, Pageable pageable);

}
