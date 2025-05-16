package com.project.makecake.repository;

import com.project.makecake.domain.post.Post;
import com.project.makecake.domain.post.PostLike;
import com.project.makecake.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike,Long> {

    void deleteByUserAndPost(User user, Post foundPost);
    void deleteAllByPost(Post foundPost);
    Optional<PostLike> findByUserAndPost(User user, Post foundPost);
    Page<PostLike> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    boolean existsByUserAndPost(User user, Post foundPost);

}
