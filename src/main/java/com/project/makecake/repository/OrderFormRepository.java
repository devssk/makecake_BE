package com.project.makecake.repository;

import com.project.makecake.domain.order.OrderForm;
import com.project.makecake.domain.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderFormRepository extends JpaRepository<OrderForm, Long> {

    @Query("SELECT DISTINCT a.store FROM OrderForm a")
    List<Store> findDistinctStore();

    List<OrderForm> findAllByOrderByNameAsc();

}