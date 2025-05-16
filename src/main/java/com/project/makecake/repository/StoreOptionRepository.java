package com.project.makecake.repository;

import com.project.makecake.domain.store.StoreOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreOptionRepository extends JpaRepository<StoreOption, Long> {

    List<StoreOption> findAllByStore_StoreId(long storeId);
    void deleteAllByStore_StoreId(long storeId);

}
