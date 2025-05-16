package com.project.makecake.domain.order;

import com.project.makecake.domain.Timestamped;
import com.project.makecake.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class UserOrders extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userOrdersId;

    @Column
    private String formFilled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_FORM_ID")
    private OrderForm orderForm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="DESIGN_ID")
    private Design design;

    @Builder
    public UserOrders(String formFilled, OrderForm orderForm, User user, Design design){
        this.formFilled = formFilled;
        this.orderForm = orderForm;
        this.user = user;
        this.design = design;
    }

}
