package com.project.makecake.dto.cake;

import com.project.makecake.domain.product.Cake;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CakeResponseDto {

    private Long cakeId, storeId;
    private String img, storeName;
    private int likeCnt;
    private boolean myLike;

    @Builder
    public CakeResponseDto(Cake cake, boolean myLike, String imgUrl) {
        this.cakeId = cake.getCakeId();
        this.img = imgUrl;
        this.storeId = cake.getStore().getStoreId();
        this.storeName = cake.getStore().getName();
        this.likeCnt = cake.getLikeCnt();
        this.myLike = myLike;
    }

}
