package com.project.makecake.dto.store;

import com.project.makecake.domain.product.CakeMenu;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StoreMoreCakeMenuDto {

    private String type;
    private String size;
    private String price;
    private String moreInfo;

    @Builder
    public StoreMoreCakeMenuDto(CakeMenu cakeMenu){
        this.type = cakeMenu.getType();
        this.size = cakeMenu.getSize();
        this.price = cakeMenu.getPrice();
        this.moreInfo = cakeMenu.getMoreInfo();
    }

}

