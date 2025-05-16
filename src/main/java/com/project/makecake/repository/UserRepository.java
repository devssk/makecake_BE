package com.project.makecake.repository;

import com.project.makecake.enums.UserRoleEnum;
import com.project.makecake.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByNickname(String nickname);
    List<User> findAllByRole(UserRoleEnum user);

}
