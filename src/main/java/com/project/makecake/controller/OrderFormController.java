package com.project.makecake.controller;

import com.project.makecake.dto.orders.OrderFormDetailResponseDto;
import com.project.makecake.dto.orders.OrderFormReadyResponseDto;
import com.project.makecake.service.order.OrderFormService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderFormController {

    private final OrderFormService orderFormService;

    // (주문하기) 주문 가능 매장 리스트 조회 API
    @GetMapping("/orders/stores")
    public List<OrderFormReadyResponseDto> getOrderFormList(){
        return orderFormService.getOrderFormList();
    }

    // (주문하기) 케이크 주문서 작성 페이지 조회 API
    @GetMapping("/orders/order-forms/{orderFormId}")
    public OrderFormDetailResponseDto getOrderFormDetails(
            @PathVariable Long orderFormId
    ) {
        return orderFormService.getOrderFormDetails(orderFormId);
    }

}
