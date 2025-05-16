package com.project.makecake.controller;

import com.project.makecake.dto.orders.OrderReadyStoreResponseDto;
import com.project.makecake.service.order.OrderFormService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 노티로 보낼 만한 공지사항, 안내 사항 웹페이지 생성 및 조회 컨트롤러
@RequiredArgsConstructor
@RestController
public class GuideController {

    private final OrderFormService orderFormService;

    // 주문 가능 매장 안내 페이지 조회 API
    @GetMapping("/order-guide")
    public List<OrderReadyStoreResponseDto> getOrderReadyStoreList(){
        return orderFormService.getOrderReadyStoreList();
    }

}