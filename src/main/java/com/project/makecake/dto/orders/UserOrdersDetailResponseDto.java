package com.project.makecake.dto.orders;

import com.project.makecake.domain.order.UserOrders;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class UserOrdersDetailResponseDto {

    private String name;
    private String img;
    private List<String> formList;
    private List<String> instructionList;
    private List<String> userInput;
    private String copyText;
    private String storeUrl;

    @Builder
    public UserOrdersDetailResponseDto (
            UserOrders userOrders,
            List<String> formList,
            List<String>instructionList,
            List<String>userInput,
            String copyText,
            String storeUrl
    ) {
        this.name = userOrders.getOrderForm().getName();
        this.img = userOrders.getDesign().getImgUrl();
        this.formList = formList;
        this.instructionList = instructionList;
        this.userInput = userInput;
        this.copyText = copyText;
        this.storeUrl = storeUrl;
    }

}
