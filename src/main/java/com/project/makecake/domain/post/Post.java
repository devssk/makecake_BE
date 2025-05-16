package com.project.makecake.domain.post;

import com.project.makecake.domain.Timestamped;
import com.project.makecake.domain.user.User;
import com.project.makecake.domain.order.Design;
import com.project.makecake.dto.post.PostRequestDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Entity
public class Post extends Timestamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long postId;

    @Column(nullable = false)
    private String title;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="designId")
    private Design design;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="userId")
    private User user;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String size;

    @Column(nullable = false)
    private String shape;

    @Column(nullable = false)
    private String purpose;

    @Column
    private int viewCnt;

    @Column
    private int likeCnt;

    @Column
    private int commentCnt;

    @Builder
    public Post(PostRequestDto requestDto, User user, Design design) {
        this.title = requestDto.getTitle();
        this.design = design;
        this.user = user;
        this.content = requestDto.getContent();
        this.size = requestDto.getSize();
        this.shape = requestDto.getShape();
        this.purpose = requestDto.getPurpose();
        this.viewCnt = 0;
        this.likeCnt = 0;
        this.commentCnt = 0;
    }

    // 수정 메소드
    public void edit(PostRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.content = requestDto.getContent();
        this.size = requestDto.getSize();
        this.shape = requestDto.getShape();
        this.purpose = requestDto.getPurpose();
    }

    // 연관관계 삭제 메소드
    public void deleteRelation() {
        this.design = null;
    }

    // likeCnt 변경 메소드
    public void addLikeCnt(boolean myLike) {
        if (myLike) {
            this.likeCnt += 1;
        } else {
            this.likeCnt -= 1;
        }
    }

    // 조회수 올리기 메소드
    public void addViewCnt() {
        this.viewCnt +=1;
    }

    // commentCnt 변경 메소드
    public void editCommentCnt(boolean comment) {
        if(comment) {
            this.commentCnt +=1;
        } else {
            this.commentCnt -=1;
        }
    }

}
