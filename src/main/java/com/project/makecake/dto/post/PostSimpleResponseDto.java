package com.project.makecake.dto.post;

import com.project.makecake.domain.post.Post;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostSimpleResponseDto {

    private Long postId;
    private String img;
    private boolean myLike;

    @Builder
    public PostSimpleResponseDto(Post post, boolean myLike) {
        this.postId = post.getPostId();
        this.img = post.getDesign().getImgUrl();
        this.myLike = myLike;
    }

}
