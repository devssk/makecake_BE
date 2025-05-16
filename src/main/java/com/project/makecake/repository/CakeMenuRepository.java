package com.project.makecake.repository;

import com.project.makecake.domain.product.CakeMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CakeMenuRepository extends JpaRepository<CakeMenu, Long> {

    List<CakeMenu> findAllByStore_StoreId(long storeId);
    void deleteAllByStore_StoreId(long storeId);

}
