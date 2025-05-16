package com.project.makecake.dto.home;

import com.project.makecake.domain.store.Store;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchResponseDto {

    private long storeId;
    private String name;
    private float x;
    private float y;
    private String addressSimple;
    private String roadAddress;
    private String fullAddress;
    private String mainImg;
    private int likeCnt;
    private int reviewCnt;

    @Builder
    public SearchResponseDto(Store store, String addressSimple){
        this.storeId = store.getStoreId();
        this.name = store.getName();
        this.x = store.getX();
        this.y = store.getY();
        this.addressSimple = addressSimple;
        this.roadAddress = store.getRoadAddress();
        this.fullAddress = store.getFullAddress();
        this.mainImg = store.getThumbnailMainImg();
        this.likeCnt = store.getLikeCnt();
        this.reviewCnt = store.getReviewCnt();
    }

}
