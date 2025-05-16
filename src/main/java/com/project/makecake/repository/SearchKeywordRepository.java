package com.project.makecake.repository;

import com.project.makecake.domain.store.SearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {

    Optional<SearchKeyword> findBySearchInput(String searchText);

    @Query(value = "select s from SearchKeyword as s where s.createdAt<:standard and s.searchCnt=1")
    List<SearchKeyword> findWrongKeyword(String standard);

}
