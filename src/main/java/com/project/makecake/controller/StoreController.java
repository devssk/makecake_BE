package com.project.makecake.controller;

import com.project.makecake.dto.home.*;
import com.project.makecake.dto.like.LikeRequestDto;
import com.project.makecake.dto.like.LikeResponseDto;
import com.project.makecake.dto.review.ReviewResponseDto;
import com.project.makecake.dto.store.StoreDetailCakeResponseDto;
import com.project.makecake.dto.store.StoreDetailResponseDto;
import com.project.makecake.security.UserDetailsImpl;
import com.project.makecake.service.product.CakeService;
import com.project.makecake.service.review.ReviewService;
import com.project.makecake.service.store.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class StoreController {

    private final StoreService storeService;
    private final CakeService cakeService;
    private final ReviewService reviewService;

    // (홈탭) 인기 매장, 인기 케이크 5개 조회 API
    @GetMapping("/home")
    public HomeResponseDto getStoreAndCakeAtHome() {
        List<HomeStoreDto> storeResponseDtoList = storeService.getStoreListAtHome();
        List<HomeCakeDto> cakeResponseDtoList = cakeService.getCakeListAtHome();
        HomeResponseDto responseDto = new HomeResponseDto(storeResponseDtoList, cakeResponseDtoList);

        return responseDto;
    }

    // (홈탭) 최신 리뷰 조회 API
    @GetMapping("/home/reviews")
    public List<HomeReviewDto> getReviewListAtHome(){
        return reviewService.getReviewListAtHome();
    }

    // 매장 검색 결과 반환 API (original)
    @GetMapping("/search-before-renewal")
    public List<SearchResponseDto> getStoreList(
            @RequestParam String searchType,
            @RequestParam String searchText
    ) throws IOException {
        return storeService.getStoreList(searchType, searchText);
    }

    // 매장 검색 결과 반환 API (성능 개선용)
    @GetMapping("/search")
    public List<SearchResponseDto> getStoreListRenewal(
            @RequestParam String searchType,
            @RequestParam String searchText
    ) throws IOException {
        return storeService.getStoreListRenewal(searchType, searchText);
    }

    // 지도에서 매장 정보 반환 API
    @GetMapping("search/{storeId}")
    public SearchResponseDto getStoreDetailsAtSearch(@PathVariable Long storeId){
        return storeService.getStoreDetailsAtSearch(storeId);
    }

    // 매장 상세페이지 조회 API
    @GetMapping("/stores/{storeId}")
    public StoreDetailResponseDto getStoreDetails(
            @PathVariable Long storeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return storeService.getStoreDetails(storeId, userDetails);
    }

    // (매장 상세페이지) 매장 케이크 조회 API (9개씩)
    @GetMapping("/stores/{storeId}/cakes")
    public List<StoreDetailCakeResponseDto> getCakeListAtStore(
            @PathVariable long storeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return  storeService.getCakeListAtStore(storeId, userDetails);
    }

    // (매장 상세페이지) 매장 리뷰 조회 API (3개씩)
    @GetMapping("/stores/{storeId}/reviews")
    public List<ReviewResponseDto> getReviewListAtStore(
            @PathVariable long storeId,
            @RequestParam int page
    ) {
        return  storeService.getReviewListAtStore(storeId, page);
    }

    // 매장 좋아요 API
    @PostMapping("/stores/{storeId}/likes")
    public LikeResponseDto likeStore(
            @RequestBody LikeRequestDto requestDto,
            @PathVariable Long storeId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return storeService.likeStore(requestDto.isMyLike(), storeId, userDetails);
    }

}