package com.project.makecake.dto.post;

import com.project.makecake.domain.post.Post;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostDetailResponseDto {

    private Long postId, designId;
    private String title, img, profileImg, nickname, createdDate, content, size, shape, purpose;
    private int likeCnt, viewCnt, commentCnt;
    private boolean myLike, orders;

    @Builder
    public PostDetailResponseDto(Post post, boolean myLike, int commentCnt) {
        this.postId = post.getPostId();
        this.title = post.getTitle();
        this.designId = post.getDesign().getDesignId();
        this.img = post.getDesign().getImgUrl();
        this.profileImg = post.getUser().getProfileImgUrl();
        this.nickname = post.getUser().getNickname();
        this.likeCnt = post.getLikeCnt();
        this.myLike = myLike;
        this.viewCnt = post.getViewCnt();
        this.commentCnt = commentCnt;
        this.createdDate = post.getCreatedAt();
        this.content = post.getContent();
        this.size = post.getSize();
        this.shape = post.getShape();
        this.purpose = post.getPurpose();
        if (post.getDesign().isOrders()) {
            this.orders = post.getDesign().isOrders();
        }
    }

}
