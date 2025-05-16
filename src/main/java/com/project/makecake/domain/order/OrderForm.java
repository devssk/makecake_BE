package com.project.makecake.domain.order;

import com.project.makecake.domain.store.Store;
import com.project.makecake.domain.Timestamped;
import com.project.makecake.dto.orders.OrderFormRequestDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class OrderForm extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long orderFormId;

    @Column
    private String form;

    @Column
    private String name;

    @Column
    private String instruction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="STORE_ID")
    private Store store;

    @Builder
    public OrderForm(OrderFormRequestDto requestDto, Store store){
        this.form = requestDto.getForm();
        this.name = requestDto.getName();
        this.instruction = requestDto.getInstruction();
        this.store = store;
    }

}
