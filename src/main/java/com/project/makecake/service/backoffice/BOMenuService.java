package com.project.makecake.service.backoffice;

import com.project.makecake.dto.backoffice.*;
import com.project.makecake.enums.CakePriceState;
import com.project.makecake.exceptionhandler.CustomException;
import com.project.makecake.exceptionhandler.ErrorCode;
import com.project.makecake.domain.product.CakeMenu;
import com.project.makecake.domain.store.Store;
import com.project.makecake.domain.store.StoreOption;
import com.project.makecake.repository.CakeMenuRepository;
import com.project.makecake.repository.StoreOptionRepository;
import com.project.makecake.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BOMenuService {

    private final StoreRepository storeRepository;
    private final CakeMenuRepository cakeMenuRepository;
    private final StoreOptionRepository storeOptionRepository;

    // 케이크 메뉴와 옵션 데이터를 잘 입력했는지 확인 메소드
    public MenuAndOptionResponseDto peekMenuAndOption(MenuAndOptionRequestDto requestDto) {

        long storeId = requestDto.getStoreId();
        Store store = storeRepository.findById(storeId)
                .orElseThrow(()->new CustomException(ErrorCode.STORE_NOT_FOUND));
        String storeName = store.getName();

        // 1. 케이크 메뉴
        List<CakeMenuRowDto> CakeMenuList = new ArrayList<>();
        String rawCakeMenuString = requestDto.getAboutCake();

        // 반환한 값을 '/' 기준으로 나눠서 배열에 담음
        List<String> rawCakeMenuList = Arrays.asList(rawCakeMenuString.split("/"));

        // 케이크 메뉴의 필드는 5개이므로 5개 정보를 한 행에 담음
        for(int j=0; j < rawCakeMenuList.size()/5; j++){
            List<String> rawRow = rawCakeMenuList.subList(j*5, (j+1)*5);
            CakeMenuRowDto menuRow = new CakeMenuRowDto(
                    rawRow.get(0).trim(),
                    rawRow.get(1).trim(),
                    rawRow.get(2).trim(),
                    rawRow.get(3).trim(),
                    rawRow.get(4).trim()
            );
            CakeMenuList.add(menuRow);
        }

        // 2. 케이크 옵션
        List<CakeOptionRowDto> CakeOptionList = new ArrayList<>();

        String rawCakeOptionString = requestDto.getAboutOption();

        // 반환한 값을 '/' 기준으로 나눠서 배열에 담음
        List<String> rawCakeOptionList = Arrays.asList(rawCakeOptionString.split("/"));

        // 케이크 옵션의 필드는 5개이므로 5개 정보를 한 행에 담음
        for(int j=0; j < rawCakeOptionList.size()/5; j++){
            List<String> rawRow = rawCakeOptionList.subList(j*5, (j+1)*5);
            CakeOptionRowDto optionRow = new CakeOptionRowDto(
                    rawRow.get(0).trim(),
                    rawRow.get(1).trim(),
                    rawRow.get(2).trim(),
                    rawRow.get(3).trim(),
                    rawRow.get(4).trim()
            );
            CakeOptionList.add(optionRow);
        }

        return new MenuAndOptionResponseDto(storeId, storeName, CakeMenuList, CakeOptionList);
    }


    // 케이크 메뉴와 옵션 데이터 저장 메소드
    @Transactional
    public String addMenuAndOption(MenuAndOptionResponseDto requestDto) {

        Store store = storeRepository.findById(requestDto.getStoreId())
                .orElseThrow(()->new CustomException(ErrorCode.STORE_NOT_FOUND));

        // 1. 케이크 메뉴 저장
        List<CakeMenuRowDto> menuList = requestDto.getPeekMenuList();
        for (CakeMenuRowDto menuRowDto : menuList) {
            String stateValue = menuRowDto.getPriceState();
            cakeMenuRepository.save(new CakeMenu(menuRowDto, store, CakePriceState.valueOf(stateValue)));
        }

        // 2. 케이크 옵션 저장
        List<CakeOptionRowDto> optionList = requestDto.getPeekOptionList();
        for (CakeOptionRowDto optionRowDto : optionList) {
            String stateValue = optionRowDto.getPriceState();
            storeOptionRepository.save(new StoreOption(optionRowDto, store, CakePriceState.valueOf(stateValue)));
        }

        return "데이터가 저장되었습니다.";
    }

    // 케이크 메뉴 삭제 메소드
    @Transactional
    public void deleteCakeMenu(long storeId){
        cakeMenuRepository.deleteAllByStore_StoreId(storeId);
    }

    // 케이크 옵션 삭제 메소드
    @Transactional
    public void deleteCakeOption(long storeId){
        storeOptionRepository.deleteAllByStore_StoreId(storeId);
    }

    // 매장 이름으로 매장 id 검색하기 메소드
    public FindStoreIdResponseDto findStoreId(FindStoreIdRequestDto requestDto) {
        String searchText = requestDto.getSearchText();
        Long storeId = 0L;
        String storeName = "";
        List<Store> searchResult = storeRepository.findByNameStartingWith(searchText);
        if(searchResult.size()>0){
            storeName = searchResult.get(0).getName();
            storeId = searchResult.get(0).getStoreId();
        }
        FindStoreIdResponseDto responseDto = new FindStoreIdResponseDto();
        responseDto.setStoreId(storeId);
        responseDto.setStoreName(storeName);

        return responseDto;
    }
}
