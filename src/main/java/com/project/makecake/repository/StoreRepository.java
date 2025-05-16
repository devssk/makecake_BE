package com.project.makecake.repository;

import com.project.makecake.domain.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findTop5ByOrderByLikeCntDesc();
    List<Store> findAllByNameContainingOrderByLikeCntDesc(String searchText);
    List<Store> findByFullAddressContainingOrderByLikeCntDesc(String searchText);
    List<Store> findByXBetweenAndYBetweenOrderByLikeCntDesc(float minX, float maxX, float minY, float maxY);
    List<Store> findByNameStartingWith(String searchText);

}
