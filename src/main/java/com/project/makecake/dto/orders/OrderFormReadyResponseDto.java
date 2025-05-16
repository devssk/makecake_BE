package com.project.makecake.dto.orders;

import com.project.makecake.domain.order.OrderForm;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderFormReadyResponseDto {

    private Long orderFormId;
    private String name;

    @Builder
    public OrderFormReadyResponseDto(OrderForm orderForm){
        this.orderFormId = orderForm.getOrderFormId();
        this.name = orderForm.getName();
    }

}
