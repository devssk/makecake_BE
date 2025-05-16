package com.project.makecake.domain.review;

import com.project.makecake.domain.Timestamped;
import com.project.makecake.domain.user.User;
import com.project.makecake.domain.store.Store;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class Review extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORE_ID")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="USER_ID")
    private User user;

    // 생성자
    @Builder
    public Review(String content, Store store, User user){
        this.content = content;
        this.store = store;
        this.user = user;
    }

    // 수정 메소드
    public void edit(String content){
        this.content = content;
    }

}
