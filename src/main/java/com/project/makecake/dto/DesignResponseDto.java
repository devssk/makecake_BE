package com.project.makecake.dto;

import com.project.makecake.domain.order.Design;
import lombok.Getter;

@Getter
public class DesignResponseDto {

    private Long designId;
    private String img;
    private boolean orders;

    public DesignResponseDto(Design design) {
        this.designId = design.getDesignId();
        this.img = design.getImgUrl();
        this.orders = design.isOrders();
    }
}
