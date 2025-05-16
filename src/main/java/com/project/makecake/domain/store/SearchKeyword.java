package com.project.makecake.domain.store;

import com.project.makecake.domain.Timestamped;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class SearchKeyword extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long searchKeywordId;

    @Column
    private String searchInput;

    @Column
    private float minX;

    @Column
    private float maxX;

    @Column
    private float minY;

    @Column
    private float maxY;

    @Column(columnDefinition = "integer default 0")
    private int searchCnt;

    //생성자
    @Builder
    public SearchKeyword (String searchInput, float minX, float maxX, float minY, float maxY) {
        this.searchInput = searchInput;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.searchCnt = 0;
    }

    public void addSearchCnt() {
        this.searchCnt += 1;
    }

}
