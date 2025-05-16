package com.project.makecake.dto.cake;

import com.project.makecake.domain.product.Cake;
import lombok.Getter;

@Getter
public class CakeSimpleResponseDto {

    Long cakeId;
    String img;

    public CakeSimpleResponseDto(Cake cake) {
        this.cakeId = cake.getCakeId();
        this.img = cake.getThumbnailUrl();
    }
}
