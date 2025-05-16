package com.project.makecake.repository;

import com.project.makecake.domain.noti.PersonalNoti;
import com.project.makecake.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonalNotiRepository extends JpaRepository<PersonalNoti,Long> {

    Optional<PersonalNoti> findByRecieveUserAndChecked(User user, boolean b);
    List<PersonalNoti> findTop30ByRecieveUserOrderByCreatedAtDesc(User user);
    List<PersonalNoti> findAllByRecieveUserAndChecked(User user, boolean b);
    boolean existsByRecieveUserAndChecked(User user, boolean b);

}
