package com.project.makecake.domain.post;

import com.project.makecake.domain.Timestamped;
import com.project.makecake.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Entity
public class Comment extends Timestamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long commentId;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="postId")
    private Post post;

    @ManyToOne
    @JoinColumn(name="userId")
    private User user;

    @Builder
    public Comment(String content, Post post, User user){
        this.content = content;
        this.post = post;
        this.user = user;
    }

    // 수정 메소드
    public void editContent(String content) {
        this.content = content;
    }

}
