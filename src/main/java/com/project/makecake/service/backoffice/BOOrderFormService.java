package com.project.makecake.service.backoffice;

import com.project.makecake.dto.backoffice.OrderFormPeekResponseDto;
import com.project.makecake.dto.orders.OrderFormRequestDto;
import com.project.makecake.exceptionhandler.CustomException;
import com.project.makecake.exceptionhandler.ErrorCode;
import com.project.makecake.domain.order.OrderForm;
import com.project.makecake.domain.store.Store;
import com.project.makecake.repository.OrderFormRepository;
import com.project.makecake.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BOOrderFormService {

    private final StoreRepository storeRepository;
    private final OrderFormRepository orderFormRepository;

    // 주문서 등록 전 데이터 미리보기 메소드
    public OrderFormPeekResponseDto peekOrderForm(OrderFormRequestDto requestDto) {

        long storeId = requestDto.getStoreId();
        Store store = storeRepository.findById(storeId)
                .orElseThrow(()->new CustomException(ErrorCode.STORE_NOT_FOUND));
        String storeName = store.getName();

        // 1. 주문서 입력란 (formList)
        List<String> formList = new ArrayList<>();

        // ':' 기준으로 분리해서 배열에 넣어 반환
        String[] rawFormList = requestDto.getForm().split(":");

        for(String rawForm : rawFormList){
            formList.add(rawForm.trim());
        }

        // 2. 주문 전 필독사항 (instructionList)
        List<String> instructionList = new ArrayList<>();

        // '*' 기준으로 분리해서 배열에 넣어 반환
        String[] rawInstructionList = requestDto.getInstruction().trim().split("\\*");
        for(String rawInstruction : rawInstructionList) {
            if(!rawInstruction.trim().equals("")){
                instructionList.add(rawInstruction.trim());
            }
        }

        return OrderFormPeekResponseDto.builder()
                .storeId(storeId)
                .storeName(storeName)
                .name(requestDto.getName())
                .peekFormList(formList)
                .peekInstructionList(instructionList)
                .build();
    }

    // 주문서 등록 메소드
    @Transactional
    public String addOrderForm(OrderFormRequestDto requestDto) {

        Store store = storeRepository.findById(requestDto.getStoreId())
                .orElseThrow(()-> new CustomException(ErrorCode.STORE_NOT_FOUND));

        orderFormRepository.save(
                OrderForm.builder()
                .requestDto(requestDto)
                .store(store)
                .build()
        );

        return "주문서 등록 완료";
    }

    // 주문서 삭제 메소드
    @Transactional
    public String deleteOrderForm(long orderFormId) {

        OrderForm orderForm = orderFormRepository.findById(orderFormId)
                .orElseThrow(()-> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        orderFormRepository.delete(orderForm);

        return "주문서 삭제 완료";
    }
}
