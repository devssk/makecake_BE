package com.project.makecake.service.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.project.makecake.domain.product.Cake;
import com.project.makecake.domain.product.CakeMenu;
import com.project.makecake.domain.review.Review;
import com.project.makecake.domain.review.ReviewImg;
import com.project.makecake.domain.store.*;
import com.project.makecake.domain.user.User;
import com.project.makecake.dto.home.HomeStoreDto;
import com.project.makecake.dto.home.SearchResponseDto;
import com.project.makecake.dto.like.LikeResponseDto;
import com.project.makecake.dto.review.ReviewResponseDto;
import com.project.makecake.dto.store.*;
import com.project.makecake.exceptionhandler.CustomException;
import com.project.makecake.exceptionhandler.ErrorCode;
import com.project.makecake.repository.*;
import com.project.makecake.security.UserDetailsImpl;
import com.project.makecake.service.OpenApiService;
import com.project.makecake.service.order.OrderFormService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreLikeRepository storeLikeRepository;
    private final StoreUrlRepository storeUrlRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImgRepository reviewImgRepository;
    private final CakeRepository cakeRepository;
    private final CakeLikeRepository cakeLikeRepository;
    private final OpenTimeRepository openTimeRepository;
    private final CakeMenuRepository cakeMenuRepository;
    private final OpenApiService openApiService;
    private final OrderFormService orderFormService;
    private final SearchKeywordRepository searchKeywordRepository;

    // (홈탭) 인기 매장 리스트 조회 메소드
    public List<HomeStoreDto> getStoreListAtHome() {

        List<HomeStoreDto> responseDtoList = new ArrayList<>();

        List<Store> foundStoreList = storeRepository.findTop5ByOrderByLikeCntDesc();

        for (Store eachStore : foundStoreList) {
            Long storeId = eachStore.getStoreId();
            String name = eachStore.getName();
            String thumbnailMainImg = eachStore.getThumbnailMainImg();
            int likeCnt = eachStore.getLikeCnt();

            HomeStoreDto responseDto = HomeStoreDto.builder()
                    .storeId(storeId)
                    .name(name)
                    .mainImg(thumbnailMainImg)
                    .likeCnt(likeCnt)
                    .build();

            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }

    // 매장 좋아요 메소드
    @Transactional
    public LikeResponseDto likeStore(Boolean myLike, Long storeId, UserDetailsImpl userDetails) {

        User user = userDetails.getUser();

        //true 추가(좋아요 누르기), false 삭제(좋아요 취소)
        Store store = storeRepository.findById(storeId)
                .orElseThrow(()-> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (myLike) {
            StoreLike storeLike = new StoreLike();
            storeLike.setStore(store);
            storeLike.setUser(user);
            storeLikeRepository.save(storeLike);
            store.setLikeCnt(store.getLikeCnt() +1);
            storeRepository.save(store);
            return LikeResponseDto.builder().myLike(true).likeCnt(store.getLikeCnt()).build();
        } else {
            storeLikeRepository.deleteByStoreAndUser(store, user);
            store.setLikeCnt(store.getLikeCnt() -1);
            storeRepository.save(store);
            return LikeResponseDto.builder().myLike(false).likeCnt(store.getLikeCnt()).build();
        }
    }

    // 매장 상세 조회 메소드
    public StoreDetailResponseDto getStoreDetails(Long storeId, UserDetailsImpl userDetails) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(()-> new CustomException(ErrorCode.STORE_NOT_FOUND));

        OpenTimeResponseDto openTimeToday = getOpenTime(storeId);

        List<StoreDetailUrlDto> urlList = getUrlList(storeId);

        boolean myLike = false;

        if(userDetails != null){
            if(storeLikeRepository.findByStoreAndUser(store, userDetails.getUser()) != null){
                myLike = true;
            }
        }

        List<StoreDetailMenuDto> menuList = new ArrayList<>();
        List<CakeMenu> foundMenuList = cakeMenuRepository.findAllByStore_StoreId(storeId);
        for(CakeMenu menu : foundMenuList){
            StoreDetailMenuDto menuDto = new StoreDetailMenuDto(menu);
            menuList.add(menuDto);
        }

        StoreMoreDetailsDto moreDetails = orderFormService.getMoreDetails(storeId);

        List<Cake> foundCakeList = cakeRepository.findTop9ByStoreOrderByCreatedAtDesc(store);

        return StoreDetailResponseDto.builder()
                .store(store)
                .openTimeToday(openTimeToday)
                .urlList(urlList)
                .myLike(myLike)
                .likeCnt(store.getLikeCnt())
                .menuList(menuList)
                .moreDetails(moreDetails)
                .cakeImgList(getCakeList(foundCakeList, userDetails))
                .build();
    }

    // (매장 상세) 케이크 조회 메소드
    public List<StoreDetailCakeResponseDto> getCakeListAtStore(long storeId, UserDetailsImpl userDetails) {

        List<Cake> foundCakeList = cakeRepository.findAllByStore_StoreId(storeId);
        return getCakeList(foundCakeList, userDetails);
    }

    // (매장 상세) 리뷰 조회 메소드
    public List<ReviewResponseDto> getReviewListAtStore(Long storeId, int page){

        Sort sort = Sort.by(
                new Sort.Order(Sort.Direction.DESC, "createdAt"),
                new Sort.Order(Sort.Direction.DESC,"reviewId")
        );
        Pageable pageable = PageRequest.of(page, 5, sort);
        Page<Review> foundReviewList = reviewRepository.findAllByStore_StoreId(storeId, pageable);

        List<ReviewResponseDto> responseDtoList = new ArrayList<>();

        for(Review review : foundReviewList){

            // 리뷰 이미지 리스트 반환
            List<String> reviewImgList = new ArrayList<>();
            List<ReviewImg> rawReviewImgList = reviewImgRepository.findAllByReview_ReviewId(review.getReviewId());
            for (ReviewImg rawReviewImg : rawReviewImgList) {
                reviewImgList.add(rawReviewImg.getImgUrl());
            }

            ReviewResponseDto responseDto = ReviewResponseDto.builder()
                    .review(review)
                    .reviewImgList(reviewImgList)
                    .build();

            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }

    // 매장 검색 결과 반환 메소드
    public List<SearchResponseDto> getStoreList(String searchType, String searchText) throws IOException {
        List<Store> foundStoreList = new ArrayList<>();

        if (searchType.equals("store")) {
                foundStoreList = storeRepository.findAllByNameContainingOrderByLikeCntDesc(searchText);
        } else if (searchType.equals("address")) {
                foundStoreList = storeRepository.findByFullAddressContainingOrderByLikeCntDesc(searchText);
        } else {
            //플레이스로 검색하기
            float minX = 0;
            float maxX = 0;
            float minY = 0;
            float maxY = 0;
            String urlString = "";

            // 네이버 지도 검색 api url
            if (searchText.startsWith("서울")){
                urlString = "https://map.naver.com/v5/api/search?caller=pcweb&query="
                        + URLEncoder.encode(searchText, "UTF-8")
                        + "&type=all&searchCoord=127.0234346;37.4979517&page=1&displayCount=20&isPlaceRecommendationReplace=true&lang=ko";
            } else {
                urlString = "https://map.naver.com/v5/api/search?caller=pcweb&query="
                        + URLEncoder.encode("서울" + searchText, "UTF-8")
                        + "&type=all&searchCoord=127.0234346;37.4979517&page=1&displayCount=20&isPlaceRecommendationReplace=true&lang=ko";
            }

            JsonElement element = openApiService.getOpenApiResult(urlString);

            JsonArray rawJsonArray = element.getAsJsonObject()
                    .get("result").getAsJsonObject()
                    .get("place").getAsJsonObject()
                    .get("boundary").getAsJsonArray();

            List<Float> foundBoundaryList = new ArrayList<>();

            for (int i=0; i< rawJsonArray.size(); i++) {
                String measureString = rawJsonArray.get(i).getAsString();
                float measure = Float.parseFloat(measureString);
                foundBoundaryList.add(measure);
            }

            Collections.sort(foundBoundaryList);

            minY = foundBoundaryList.get(0) - 0.01f;
            maxY = foundBoundaryList.get(1) + 0.01f;
            minX = foundBoundaryList.get(2)- 0.01f;
            maxX = foundBoundaryList.get(3) + 0.01f;

            foundStoreList = storeRepository.findByXBetweenAndYBetweenOrderByLikeCntDesc(minX, maxX, minY, maxY);
        }

        // Dto에 담아 반환
        List<SearchResponseDto> responseDtoList = new ArrayList<>();
        for (Store store : foundStoreList){
            responseDtoList.add(StoreDetailsAtSearch(store));
        }
        return responseDtoList;
    }

    // 매장 검색 결과 반환 메소드 2
    @Transactional
    public List<SearchResponseDto> getStoreListRenewal(String searchType, String searchText) throws IOException {

        List<Store> foundStoreList = new ArrayList<>();

        // 1. 매장명을 검색한 경우
        if(searchType.equals("store")) {
            foundStoreList = storeRepository.findAllByNameContainingOrderByLikeCntDesc(searchText);
        } else {

        // 2. 주소 또는 플레이스로 검색한 경우
            // 2-(1). store DB의 fullAddress에서 해당 검색어를 포함할 경우 결과 반환
            foundStoreList = storeRepository.findByFullAddressContainingOrderByLikeCntDesc(searchText);

            // 2-(2). 검색어가 fullAddress에 없을 경우 SearchKeyword DB 검색
            if(foundStoreList.size()==0){
                Optional<SearchKeyword> foundKeyWordOpt = searchKeywordRepository.findBySearchInput(searchText);

                if(foundKeyWordOpt.isPresent()){
                    SearchKeyword foundKeyWord = foundKeyWordOpt.get();
                    foundStoreList = storeRepository.findByXBetweenAndYBetweenOrderByLikeCntDesc(
                                    foundKeyWord.getMinX(),
                                    foundKeyWord.getMaxX(),
                                    foundKeyWord.getMinY(),
                                    foundKeyWord.getMaxY()
                                    );

                    foundKeyWord.addSearchCnt();

                } else {

                // 2-(3) 검색어가 searchKeyword에 없을 경우엔 api 검색 결과 반환 & searchKeyword에 데이터 추가

                    float minX = 0;
                    float maxX = 0;
                    float minY = 0;
                    float maxY = 0;

                    String urlString = "";

                    // 네이버 지도로 검색하기
                    // 검색어가 '서울'로 시작할 경우 그대로 검색
                    if (searchText.startsWith("서울")){
                        urlString = "https://map.naver.com/v5/api/search?caller=pcweb&query="
                                + URLEncoder.encode(searchText, "UTF-8")
                                + "&type=all&searchCoord=127.0234346;37.4979517&page=1&displayCount=20&isPlaceRecommendationReplace=true&lang=ko";

                    // 검색어가 '서울' 시작하지 않는 경우 '서울'을 붙여서 검색
                    } else {
                        urlString = "https://map.naver.com/v5/api/search?caller=pcweb&query="
                                + URLEncoder.encode("서울" + searchText, "UTF-8")
                                + "&type=all&searchCoord=127.0234346;37.4979517&page=1&displayCount=20&isPlaceRecommendationReplace=true&lang=ko";
                    }

                    JsonElement element = openApiService.getOpenApiResult(urlString);

                    JsonArray rawJsonArray = element.getAsJsonObject()
                            .get("result").getAsJsonObject()
                            .get("place").getAsJsonObject()
                            .get("boundary").getAsJsonArray();

                    List<Float> foundBoundaryList = new ArrayList<>();

                    // 검색결과에서 위경도 값 추출 후 가공
                    for (int i=0; i< rawJsonArray.size(); i++) {
                        String measureString = rawJsonArray.get(i).getAsString();
                        float measure = Float.parseFloat(measureString);
                        foundBoundaryList.add(measure);
                    }

                    Collections.sort(foundBoundaryList);

                    minY = foundBoundaryList.get(0) - 0.01f;
                    maxY = foundBoundaryList.get(1) + 0.01f;
                    minX = foundBoundaryList.get(2)- 0.01f;
                    maxX = foundBoundaryList.get(3) + 0.01f;

                    // 위경도 값으로 db 검색 결과 반환
                    foundStoreList = storeRepository.findByXBetweenAndYBetweenOrderByLikeCntDesc(minX, maxX, minY, maxY);


                    // 사용자 검색어 db에 신규 등록
                    SearchKeyword searchKeyword = new SearchKeyword().builder()
                            .searchInput(searchText)
                            .minX(minX)
                            .maxX(maxX)
                            .minY(minY)
                            .maxY(maxY)
                            .build();

                    searchKeywordRepository.save(searchKeyword);
                }
            }
        }

        // foundStoreList를 Dto에 담아 반환
        List<SearchResponseDto> responseDtoList = new ArrayList<>();
        for(Store store : foundStoreList){
            responseDtoList.add(StoreDetailsAtSearch(store));
        }
        return responseDtoList;
    }

    // 밤12시마다 오타 검색 키워드 삭제 메소드
    @Transactional
    @Scheduled(cron = "0 0 3 * * ?", zone = "Asia/Seoul")
    public void deleteSearchKeyword() {

        System.out.println("안녕");

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DATE,-7);

        String standard = formatter.format(calendar.getTime());

        List<SearchKeyword> searchKeywordList = searchKeywordRepository.findWrongKeyword(standard);
        for (SearchKeyword searchKeyword : searchKeywordList) {
            log.info("검색어 삭제 : " + searchKeyword.getSearchInput());
            searchKeywordRepository.delete(searchKeyword);
        }
    }

    // (매장 상세 페이지 조회 메소드) 케이크 이미지 반환 메소드
    public List<StoreDetailCakeResponseDto> getCakeList(List<Cake> foundCakeList, UserDetailsImpl userDetails){

        List<StoreDetailCakeResponseDto> responseDtoList = new ArrayList<>();

        for(Cake cake : foundCakeList){
            boolean myCakeLike = false;
            if(userDetails != null){
                User user = userDetails.getUser();
                if(cakeLikeRepository.findByUserAndCake(user, cake).isPresent()){
                    myCakeLike = true;
                }
            }
            //dto 값 담고, 리스트에 추가하기
            StoreDetailCakeResponseDto cakeDto = StoreDetailCakeResponseDto
                    .builder()
                    .cake(cake)
                    .myLike(myCakeLike)
                    .build();

            responseDtoList.add(cakeDto);
        }
        return responseDtoList;
    }

    // (매장 상세 페이지 조회 메소드) 매장 홈페이지 url 2개 반환 메소드
    public List<StoreDetailUrlDto> getUrlList(long storeId){

        List<StoreDetailUrlDto> urlList = new ArrayList<>();
        List<StoreUrl> foundUrlList = storeUrlRepository.findAllByStore_StoreId(storeId);

        if (foundUrlList.size() > 2) {
            List<StoreUrl> chosenList = foundUrlList.stream()
                    .filter(eachUrl -> eachUrl.getType().equals("normal") || eachUrl.getType().equals("instagram"))
                    .collect(Collectors.toList());

            while (chosenList.size() < 2){
                for(int i=0; i < foundUrlList.size(); i++){
                    StoreUrl eachUrl = foundUrlList.get(i);
                    if(!eachUrl.getType().equals("normal") && !eachUrl.getType().equals("instagram")){
                        chosenList.add(eachUrl);
                    }
                }
            }
            for (StoreUrl url : chosenList) {
                StoreDetailUrlDto urlDto = new StoreDetailUrlDto();
                urlDto.setUrl(url.getUrl());
                urlDto.setType(url.getType());
                urlList.add(urlDto);
            }
        }
        else {
            for (StoreUrl url : foundUrlList) {
                StoreDetailUrlDto urlDto = new StoreDetailUrlDto();
                urlDto.setUrl(url.getUrl());
                urlDto.setType(url.getType());
                urlList.add(urlDto);
            }
        }
        return urlList;
    }

    // (매장 상세 페이지 조회 메소드) 오늘의 영업 시간 조회 메소드
    public OpenTimeResponseDto getOpenTime(long storeId){

        //요일 반환
        LocalDate now = LocalDate.now();
        String dayOfWeek = "";
        String dayOfWeek2 = "";
        switch (now.getDayOfWeek().getValue()) {
            case 1:
                dayOfWeek = "월요일";
                dayOfWeek2 = "평일";
                break;
            case 2:
                dayOfWeek = "화요일";
                dayOfWeek2 = "평일";
                break;
            case 3:
                dayOfWeek = "수요일";
                dayOfWeek2 = "평일";
                break;
            case 4:
                dayOfWeek = "목요일";
                dayOfWeek2 = "평일";
                break;
            case 5:
                dayOfWeek = "금요일";
                dayOfWeek2 = "평일";
                break;
            case 6:
                dayOfWeek = "토요일";
                dayOfWeek2 = "주말";
                break;
            case 7:
                dayOfWeek = "일요일";
                dayOfWeek2 = "주말";
                break;
            default:
                dayOfWeek = "확인되지 않음";
                dayOfWeek2 = "확인되지 않음";
                break;
        }

        //storeId 로 openTime 가져오기
        List<OpenTime> openTimeList = openTimeRepository.findAllByStore_StoreId(storeId);
        if (openTimeList.size() == 0) {
            return new OpenTimeResponseDto("영업 시간은 매장 홈페이지를 확인해주세요");
        } else {
            //"화요일"이 리스트에 있는지 확인
            for (int i=0; i<openTimeList.size(); i++) {
                if (openTimeList.get(i).getType().equals(dayOfWeek)) {
                    return new OpenTimeResponseDto(openTimeList.get(i));
                }
            }
            //"매일"이 리스트에 있는지 확인
            for (int i=0; i<openTimeList.size(); i++) {
                if (openTimeList.get(i).getType().equals("매일")) {
                    return new OpenTimeResponseDto(openTimeList.get(i));
                }
            }
            //"평일" 또는 주말 이 리스트에 있는지 확인
            for (int i=0; i<openTimeList.size(); i++) {
                if (openTimeList.get(i).getType().equals(dayOfWeek2)) {
                    return new OpenTimeResponseDto(openTimeList.get(i));
                }
            }
            return new OpenTimeResponseDto("오늘은 휴일입니다");
        }
    }

    public SearchResponseDto getStoreDetailsAtSearch(Long storeId) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(()-> new CustomException(ErrorCode.STORE_NOT_FOUND));
        return StoreDetailsAtSearch(store);
    }

    // 내부 메소드
    public SearchResponseDto StoreDetailsAtSearch(Store store){

        String addressSimple = "";

        //"서울 OO구 OO동"
        if(!store.getFullAddress().equals(null)){
            String[] arr = store.getFullAddress().split(" ");
            addressSimple = arr[0].substring(0,2) + " "  + arr[1] + " " + arr[2];
        }

        SearchResponseDto responseDto = SearchResponseDto.builder()
                .store(store)
                .addressSimple(addressSimple)
                .build();

        return responseDto;
    }
}
