package com.project.makecake.dto.store;

import com.project.makecake.domain.store.Store;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class StoreDetailResponseDto {

    private long storeId;
    private String mainImg, name, roadAddress, fullAddress, phone;
    private OpenTimeResponseDto openTimeToday;
    private List<StoreDetailUrlDto> urlList;
    private Boolean myLike;
    private int likeCnt;
    private List<StoreDetailMenuDto> menuList;
    private StoreMoreDetailsDto moreDetails;
    private List<StoreDetailCakeResponseDto> cakeImgList;

    @Builder
    public StoreDetailResponseDto(
            Store store,
            OpenTimeResponseDto openTimeToday,
            List<StoreDetailUrlDto> urlList,
            Boolean myLike,int likeCnt,
            List<StoreDetailMenuDto> menuList,
            StoreMoreDetailsDto moreDetails,
            List<StoreDetailCakeResponseDto> cakeImgList
    ){
        this.storeId = store.getStoreId();
        this.mainImg = store.getMainImg();
        this.name = store.getName();
        this.roadAddress = store.getRoadAddress();
        this.fullAddress = store.getFullAddress();
        this.openTimeToday = openTimeToday;
        this.phone = store.getPhone();
        this.urlList = urlList;
        this.myLike = myLike;
        this.likeCnt = likeCnt;
        this.menuList = menuList;
        this.moreDetails = moreDetails;
        this.cakeImgList = cakeImgList;
    }

}