package com.project.makecake.domain.product;

import com.project.makecake.domain.store.Store;
import com.project.makecake.domain.Timestamped;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Entity
public class Cake extends Timestamped {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long cakeId;

    @Column
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORE_ID")
    private Store store;

    @Column
    private int likeCnt;

    @Column
    private String thumbnailUrl;

    @Builder
    public Cake(String url, Store store, String thumbnailUrl) {
        this.url = url;
        this.store = store;
        this.thumbnailUrl = thumbnailUrl;
    }

    // 좋아요수 변경 메소드
    public void editLikeCnt(boolean myLike) {
        if (myLike) {
            this.likeCnt += 1;
        } else {
            this.likeCnt -= 1;
        }
    }

}
