package com.project.makecake.repository;

import com.project.makecake.domain.post.Comment;
import com.project.makecake.domain.post.Post;
import com.project.makecake.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment,Long> {

    void deleteAllByPost(Post foundPost);
    Page<Comment> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    Long countByPost(Post foundPost);
    Page<Comment> findAllByPost(Post foundPost, Pageable pageable);

}
