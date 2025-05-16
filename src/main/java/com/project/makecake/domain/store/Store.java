package com.project.makecake.domain.store;

import com.project.makecake.domain.Timestamped;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Getter
@Setter
@DynamicUpdate
public class Store extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long storeId;

    @Column
    private Long naverId;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column
    private float x;

    @Column
    private float y;

    @Column
    private String fullAddress;

    @Column
    private String roadAddress;

    @Column
    private String mainImg;

    @Column
    private String openTimeString;

    @Column
    private String phone;

    @Column(columnDefinition = "integer default 0")
    private int likeCnt;

    @Column(columnDefinition = "integer default 0")
    private int reviewCnt;

    @Column
    private String thumbnailMainImg;

    // 매장 후기 수 Cnt
    public void countReview(boolean bool) {
        if(bool) {
            this.reviewCnt +=1;
        } else {
            this.reviewCnt -=1;
        }
    }

}