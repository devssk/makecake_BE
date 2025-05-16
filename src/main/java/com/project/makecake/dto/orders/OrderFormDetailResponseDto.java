package com.project.makecake.dto.orders;

import com.project.makecake.dto.store.StoreMoreDetailsDto;
import com.project.makecake.domain.order.OrderForm;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderFormDetailResponseDto {

    private long orderFormId, storeId;
    private String name;
    private List<String> formList;
    private List<String> instructionList;
    private StoreMoreDetailsDto moreDetails;

    @Builder
    OrderFormDetailResponseDto (
            OrderForm orderForm,
            List<String> formList,
            List<String> instructionList,
            StoreMoreDetailsDto moreDetails
    ){
        this.orderFormId = orderForm.getOrderFormId();
        this.storeId = orderForm.getStore().getStoreId();
        this.name = orderForm.getName();
        this.formList = formList;
        this.instructionList = instructionList;
        this.moreDetails = moreDetails;
    }

}
