package com.project.makecake.repository;

import com.project.makecake.domain.product.Cake;
import com.project.makecake.domain.product.CakeLike;
import com.project.makecake.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CakeLikeRepository extends JpaRepository<CakeLike, Long> {

    Optional<CakeLike> findByUserAndCake(User user, Cake cake);
    void deleteByUserAndCake(User user, Cake foundCake);
    Page<CakeLike> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    void deleteAllByCake(Cake cake);
    boolean existsByUserAndCake(User user, Cake foundCake);

}
