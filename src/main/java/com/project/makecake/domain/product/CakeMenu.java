package com.project.makecake.domain.product;

import com.project.makecake.domain.store.Store;
import com.project.makecake.domain.Timestamped;
import com.project.makecake.dto.backoffice.CakeMenuRowDto;
import com.project.makecake.enums.CakePriceState;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
public class CakeMenu extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cakeMenuId;

    @Column
    private String type;

    @Column
    private String size;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "CakePriceState default FIXED")
    private CakePriceState priceState;

    @Column(columnDefinition = "integer default 0")
    private String price;

    @Column
    private String moreInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="STORE_ID")
    private Store store;

    //생성자
    @Builder
    public CakeMenu(CakeMenuRowDto menuRowDto, Store store, CakePriceState priceState){
        this.type = menuRowDto.getType();
        this.size = menuRowDto.getSize();
        this.price = menuRowDto.getPrice();
        this.moreInfo = menuRowDto.getMoreInfo();
        this.priceState = priceState;
        this.store = store;
    }

    // (백오피스) 수정 메소드
    @Builder
    public void edit(CakeMenuRowDto menuRowDto, CakePriceState priceState) {
        this.type = menuRowDto.getType();
        this.size = menuRowDto.getSize();
        this.price = menuRowDto.getPrice();
        this.priceState = priceState;
        this.moreInfo = menuRowDto.getMoreInfo();
    }
}
