package com.project.makecake.repository;

import com.project.makecake.model.Design;
import com.project.makecake.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignRepository extends JpaRepository<Design,Long> {
    Page<Design> findByUserAndPost(User user, boolean post , Pageable pageable);
}
