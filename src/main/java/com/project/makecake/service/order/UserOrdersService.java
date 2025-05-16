package com.project.makecake.service.order;

import com.project.makecake.domain.order.Design;
import com.project.makecake.domain.order.OrderForm;
import com.project.makecake.domain.order.UserOrders;
import com.project.makecake.domain.store.StoreUrl;
import com.project.makecake.domain.user.User;
import com.project.makecake.dto.mypage.MyOrderListResponseDto;
import com.project.makecake.dto.orders.UserOrderRequestDto;
import com.project.makecake.dto.orders.UserOrdersDetailResponseDto;
import com.project.makecake.exceptionhandler.CustomException;
import com.project.makecake.exceptionhandler.ErrorCode;
import com.project.makecake.repository.*;
import com.project.makecake.security.UserDetailsImpl;
import com.project.makecake.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserOrdersService {

    private final UserRepository userRepository;
    private final DesignRepository designRepository;
    private final UserOrdersRepository userOrdersRepository;
    private final OrderFormRepository orderFormRepository;
    private final StoreUrlRepository storeUrlRepository;
    private final S3Service s3Service;


    // (마이페이지 > 케이크 주문) 주문 안 된/된 도안 리스트 조회 메소드
    public List<MyOrderListResponseDto> getMyOrderList(
            UserDetailsImpl userDetails,
            String option,
            int page
    ) {

        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MAMBER);
        }

        User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        List<MyOrderListResponseDto> responseDtoList = new ArrayList<>();
        if (option.equals("notOrdered")){
            Pageable pageable = PageRequest.of(page, 54);
            Page<Design> foundDesignList = designRepository
                    .findByUserAndOrdersOrderByCreatedAtDesc(foundUser, false, pageable);

            for (Design design : foundDesignList){
                MyOrderListResponseDto responseDto = MyOrderListResponseDto.builder()
                        .designId(design.getDesignId())
                        .img(design.getImgUrl())
                        .build();
                responseDtoList.add(responseDto);
            }
        } else if (option.equals("ordered")){
            Pageable pageable = PageRequest.of(page, 54);
            Page<UserOrders> foundUserOrdersList = userOrdersRepository
                    .findByUserOrderByCreatedAtDesc(foundUser, pageable);

            for (UserOrders userOrders : foundUserOrdersList){
                MyOrderListResponseDto responseDto = MyOrderListResponseDto.builder()
                        .userOrdersId(userOrders.getUserOrdersId())
                        .designId(userOrders.getDesign().getDesignId())
                        .img(userOrders.getDesign().getImgUrl())
                        .build();
                responseDtoList.add(responseDto);
            }
        }
        return responseDtoList;
    }

    // (주문하기) 케이크 주문서 작성 메소드
    @Transactional
    public HashMap<String, Long> addUserOrders(
            Long orderFormId,
            UserOrderRequestDto requestDto,
            UserDetailsImpl userDetails
    ) {

        String formFilled = "";
        for(String input : requestDto.getUserInput()){
            formFilled += input + "<br>";
        }

        OrderForm orderForm = orderFormRepository.findById(orderFormId)
                .orElseThrow(()-> new CustomException(ErrorCode.ORDERFORM_NOT_FOUND));

        Design foundDesign = designRepository.findById(requestDto.getDesignId())
                .orElseThrow(()-> new CustomException(ErrorCode.DESIGN_NOT_FOUND));

        UserOrders userOrders = UserOrders.builder()
                .user(userDetails.getUser())
                .design(foundDesign)
                .orderForm(orderForm)
                .formFilled(formFilled)
                .build();

        UserOrders savedUserOrders = userOrdersRepository.save(userOrders);

        foundDesign.editOrderState(true);

        // 반환 객체 생성
        HashMap<String,Long> responseDto = new HashMap<>();
        responseDto.put("userOrdersId", savedUserOrders.getUserOrdersId());
        return responseDto;
    }

    // 작성한 주문서 조회 메소드
    public UserOrdersDetailResponseDto getUserOrdersDetails(long userOrdersId) {
        UserOrders userOrders = userOrdersRepository.findById(userOrdersId)
                .orElseThrow(()-> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        OrderForm orderForm = userOrders.getOrderForm();

        // 주문서 양식
        List<String> formList = new ArrayList<>();
        String form = orderForm.getForm();
        List<String> rawFormList = Arrays.asList(form.split(":"));

        for(String rawForm : rawFormList){
            formList.add(rawForm.trim());
        }

        // 주문 전 필독사항
        List<String> instructionList = new ArrayList<>();
        String instruction = orderForm.getInstruction();

        List<String> rawInstructionList = Arrays.asList(instruction.trim().split("\\*"));
        for(String rawInstruction : rawInstructionList) {
            if(!rawInstruction.trim().equals("")){
                instructionList.add(rawInstruction.trim());
            }
        }

        // 사용자 입력 사항 반환
        List<String> userInputList = new ArrayList<>();
        String userInput = userOrders.getFormFilled();

        // 마지막 공백으로
        if(userInput.endsWith("<br><br>")){
            userInput = userInput.substring(0, userInput.length()-8) + "<br> <br>";
        }

        List<String> rawUserInputList = Arrays.asList(userInput.split("<br>"));
        for(String rawUserInput : rawUserInputList) {
            userInputList.add(rawUserInput.trim());
        }

        String copyText = "";

        // copyText
        for(int i=0; i<formList.size(); i++){
            copyText += formList.get(i) + " : " + userInputList.get(i)+"\n";
        }

        //storeUrl
        StoreUrl foundStoreUrl = storeUrlRepository
                .findByTypeAndStore_StoreId("normal", orderForm.getStore().getStoreId());

        UserOrdersDetailResponseDto responseDto = UserOrdersDetailResponseDto.builder()
                .userOrders(userOrders)
                .formList(formList)
                .instructionList(instructionList)
                .userInput(userInputList)
                .copyText(copyText)
                .storeUrl(foundStoreUrl.getUrl())
                .build();

        return responseDto;
    }


    public void deleteUserOrders(Long userOrdersId, UserDetailsImpl userDetails) {

        User user = userDetails.getUser();

        UserOrders userOrders = userOrdersRepository.findById(userOrdersId)
                .orElseThrow(()-> new CustomException(ErrorCode.ORDER_NOT_FOUND));


        if (!user.getUserId().equals(userOrders.getUser().getUserId())){
            throw new CustomException(ErrorCode.NOT_ORDER_OWNER);
        }

        Design design = designRepository.findById(userOrders.getDesign().getDesignId())
                        .orElseThrow(()-> new CustomException(ErrorCode.DESIGN_NOT_FOUND));

        design.editOrderState(false);

        userOrdersRepository.delete(userOrders);
    }

    // 주문서의 도안 전송 메소드
    public ResponseEntity<byte[]> getDesignAtOrders(long userOrdersId) throws IOException {

        UserOrders foundUserOrders = userOrdersRepository.findById(userOrdersId)
                .orElseThrow(()-> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        String foundFileName = foundUserOrders.getDesign().getImgName();

        return s3Service.downloadFile(foundFileName);
    }
}
