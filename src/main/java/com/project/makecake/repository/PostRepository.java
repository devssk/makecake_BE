package com.project.makecake.repository;

import com.project.makecake.domain.post.Post;
import com.project.makecake.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post,Long> {

    Page<Post> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    @EntityGraph(attributePaths = "design")
    Page<Post> findAll(Pageable pageable);

//    @Query(value = "select p from Post p join fetch p.design",
//            countQuery = "SELECT COUNT(DISTINCT p) FROM Post p INNER JOIN p.design")
//    Page<Post> findAll(Pageable pageable);
}
