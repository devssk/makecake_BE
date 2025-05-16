package com.project.makecake.domain.store;

import com.project.makecake.domain.Timestamped;
import com.project.makecake.dto.backoffice.CakeOptionRowDto;
import com.project.makecake.enums.CakePriceState;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class StoreOption extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long storeOptionId;

    @Column
    private String mainCat;

    @Column
    private String subCat;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "CakePriceState default FIXED")
    private CakePriceState priceState;

    @Column(columnDefinition = "integer default 0")
    private String addedPrice;

    @Column
    private String moreInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORE_ID")
    private Store store;

    public StoreOption(CakeOptionRowDto optionRowDto, Store store, CakePriceState priceState){
        this.mainCat = optionRowDto.getMainCat();
        this.subCat = optionRowDto.getSubCat();
        this.addedPrice = optionRowDto.getAddedPrice();
        this.moreInfo = optionRowDto.getMoreInfo();
        this.priceState = priceState;
        this.store = store;
    }

}
