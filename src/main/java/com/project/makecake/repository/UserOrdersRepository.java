package com.project.makecake.repository;

import com.project.makecake.domain.order.Design;
import com.project.makecake.domain.user.User;
import com.project.makecake.domain.order.UserOrders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOrdersRepository extends JpaRepository<UserOrders, Long> {

    Page<UserOrders> findByUserOrderByCreatedAtDesc(User foundUser, Pageable pageable);
    void deleteByDesign(Design design);

}
