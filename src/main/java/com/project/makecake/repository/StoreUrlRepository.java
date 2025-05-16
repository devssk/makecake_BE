package com.project.makecake.repository;

import com.project.makecake.domain.store.StoreUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreUrlRepository extends JpaRepository<StoreUrl, Long> {

    List<StoreUrl> findAllByStore_StoreId(long storeId);
    StoreUrl findByTypeAndStore_StoreId(String type, long storeId);
    void deleteAllByStore_StoreId(Long storeId);

}
