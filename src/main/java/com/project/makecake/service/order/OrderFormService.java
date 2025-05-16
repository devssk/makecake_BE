package com.project.makecake.service.order;

import com.project.makecake.dto.orders.OrderFormDetailResponseDto;
import com.project.makecake.dto.orders.OrderFormReadyResponseDto;
import com.project.makecake.dto.orders.OrderReadyStoreResponseDto;
import com.project.makecake.dto.store.StoreMoreCakeMenuDto;
import com.project.makecake.dto.store.StoreMoreCakeOptionDto;
import com.project.makecake.dto.store.StoreMoreCakeTasteDto;
import com.project.makecake.dto.store.StoreMoreDetailsDto;
import com.project.makecake.exceptionhandler.CustomException;
import com.project.makecake.exceptionhandler.ErrorCode;
import com.project.makecake.domain.product.CakeMenu;
import com.project.makecake.domain.order.OrderForm;
import com.project.makecake.domain.store.Store;
import com.project.makecake.domain.store.StoreOption;
import com.project.makecake.repository.CakeMenuRepository;
import com.project.makecake.repository.OrderFormRepository;
import com.project.makecake.repository.StoreOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor

public class OrderFormService {

    private final OrderFormRepository orderFormRepository;
    private final CakeMenuRepository cakeMenuRepository;
    private final StoreOptionRepository storeOptionRepository;

    // 주문 가능한 주문서 조회 메소드
    public List<OrderFormReadyResponseDto> getOrderFormList() {

        List<OrderFormReadyResponseDto> responseDtoList = new ArrayList<>();

        List<OrderForm> foundOrderFormList = orderFormRepository.findAllByOrderByNameAsc();

        // 주문서를 Dto에 담아 반환
        for(OrderForm orderForm : foundOrderFormList){
            responseDtoList.add(OrderFormReadyResponseDto.builder()
                    .orderForm(orderForm)
                    .build());
        }
        return responseDtoList;
    }

    // 주문 가능한 매장 조회 메소드
    public List<OrderReadyStoreResponseDto> getOrderReadyStoreList() {

        List<OrderReadyStoreResponseDto> responseDtoList = new ArrayList<>();

        // 매장 리스트 반환
        List<Store> foundStoreList = orderFormRepository.findDistinctStore();
        for(Store store : foundStoreList){
            String addressSimple = "";

            // 간편 주소 형태로 가공("서울 OO구 OO동")
            if(store.getFullAddress() != null){
                String[] arr = store.getFullAddress().split(" ");
                addressSimple = arr[0].substring(0,2) + " "  + arr[1] + " " + arr[2];
            }

            responseDtoList.add(
                    OrderReadyStoreResponseDto.builder()
                    .store(store)
                    .simpleAddress(addressSimple)
                    .build());

            // 간편주소 가나다 순으로 정렬
            Comparator<OrderReadyStoreResponseDto> compareByAddress = (OrderReadyStoreResponseDto r1, OrderReadyStoreResponseDto r2) -> r1.getSimpleAddress().compareTo( r2.getSimpleAddress() );
            responseDtoList.sort(compareByAddress);
        }
        return responseDtoList;
    }

    // 케이크 주문서 작성 페이지 조회 메소드
    public OrderFormDetailResponseDto getOrderFormDetails(Long orderFormId) {

        OrderForm orderForm = orderFormRepository.findById(orderFormId)
                .orElseThrow(()-> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // 1. 주문서 입력란 (formList)
        List<String> formList = new ArrayList<>();

        // ':' 기준으로 분리해서 배열에 넣어 반환
        String[] rawFormList = orderForm.getForm().split(":");

        for(String rawForm : rawFormList){
            formList.add(rawForm.trim());
        }

        // 2. 주문 전 필독사항 (instructionList)
        List<String> instructionList = new ArrayList<>();

        // '*' 기준으로 분리해서 배열에 넣어 반환
        String[] rawInstructionList = orderForm.getInstruction().trim().split("\\*");
        for(String rawInstruction : rawInstructionList) {
            if(!rawInstruction.trim().equals("")){
                instructionList.add(rawInstruction.trim());
            }
        }

        // 3. 케이크 메뉴 & 옵션 정보 반환
        StoreMoreDetailsDto moreDetails = getMoreDetails(orderForm.getStore().getStoreId());

        return OrderFormDetailResponseDto.builder()
                .orderForm(orderForm)
                .formList(formList)
                .instructionList(instructionList)
                .moreDetails(moreDetails)
                .build();
    }

    // (내부 메소드) 케이크 메뉴, 꾸미기 옵션 조회 메소드
    public StoreMoreDetailsDto getMoreDetails(long storeId){

        // 1. 케이크 메뉴
        List<StoreMoreCakeMenuDto> cakeMenuList = new ArrayList<>();

        List<CakeMenu> foundMenuList = cakeMenuRepository.findAllByStore_StoreId(storeId);
        for(CakeMenu menu : foundMenuList){
            cakeMenuList.add(
                    StoreMoreCakeMenuDto.builder()
                            .cakeMenu(menu)
                            .build()
            );
        }

        // 2. 케이크 맛, 3. 케이크 꾸미기 옵션
        List<StoreMoreCakeTasteDto> cakeTasteList = new ArrayList<>();
        List<StoreMoreCakeOptionDto> cakeOptionList = new ArrayList<>();

        List<StoreOption> foundOptionList = storeOptionRepository.findAllByStore_StoreId(storeId);

        for(StoreOption storeOption : foundOptionList){

            // 2. 케이크 맛(케이크 옵션 중 대분류가 '맛'인 경우)
            if(storeOption.getMainCat().equals("맛")) {

                cakeTasteList.add(
                        StoreMoreCakeTasteDto.builder()
                                .storeOption(storeOption)
                                .build()
                );

            // 3. 케이크 꾸미기 옵션(이 외 경우)
            } else {
                cakeOptionList.add(StoreMoreCakeOptionDto.builder()
                        .storeOption(storeOption)
                        .build());
            }
        }

        return StoreMoreDetailsDto.builder()
                .cakeMenuList(cakeMenuList)
                .cakeTasteList(cakeTasteList)
                .cakeOptionList(cakeOptionList)
                .build();
    }
}
