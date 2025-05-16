package com.project.makecake.dto.store;

import com.project.makecake.domain.product.Cake;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StoreDetailCakeResponseDto {

    private Long cakeId;
    private String img;
    private int likeCnt;
    private Boolean myLike;

    @Builder
    public StoreDetailCakeResponseDto(Cake cake, boolean myLike){
        this.cakeId = cake.getCakeId();
        this.img = cake.getThumbnailUrl();
        this.likeCnt = cake.getLikeCnt();
        this.myLike = myLike;
    }

}
