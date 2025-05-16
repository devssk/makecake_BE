package com.project.makecake.dto.store;

import com.project.makecake.domain.product.CakeMenu;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreDetailMenuDto {

    private String type;
    private String size;
    private String price;

    public StoreDetailMenuDto(CakeMenu cakeMenu){
        this.type = cakeMenu.getType();
        this.size = cakeMenu.getSize();
        this.price = cakeMenu.getPrice();
    }

}
