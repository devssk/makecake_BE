package com.project.makecake.dto.orders;

import com.project.makecake.domain.store.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderReadyStoreResponseDto {

    private long storeId;
    private String name, simpleAddress;

    @Builder
    public OrderReadyStoreResponseDto(Store store, String simpleAddress){
        this.storeId = store.getStoreId();
        this.name = store.getName();
        this.simpleAddress = simpleAddress;
    }

}
