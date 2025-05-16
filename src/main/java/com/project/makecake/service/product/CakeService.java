package com.project.makecake.service.product;

import com.project.makecake.dto.cake.CakeResponseDto;
import com.project.makecake.dto.cake.CakeSimpleResponseDto;
import com.project.makecake.dto.home.HomeCakeDto;
import com.project.makecake.dto.like.LikeRequestDto;
import com.project.makecake.dto.like.LikeResponseDto;
import com.project.makecake.exceptionhandler.CustomException;
import com.project.makecake.exceptionhandler.ErrorCode;
import com.project.makecake.domain.product.Cake;
import com.project.makecake.domain.product.CakeLike;
import com.project.makecake.domain.user.User;
import com.project.makecake.repository.CakeLikeRepository;
import com.project.makecake.repository.CakeRepository;
import com.project.makecake.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CakeService {

    private final CakeRepository cakeRepository;
    private final CakeLikeRepository cakeLikeRepository;

    //홈탭 케이크 불러오기 메소드
    @Transactional
    public List<HomeCakeDto> getCakeListAtHome() {
        List<HomeCakeDto> homeCakeDtoList = new ArrayList<>();
        List<Cake> rawCakeList = cakeRepository.findTop5ByOrderByLikeCntDesc();

        for(Cake eachCake : rawCakeList){
            Long cakeId = eachCake.getCakeId();
            String thumbnailMainImg = eachCake.getThumbnailUrl();
            int likeCnt = eachCake.getLikeCnt();

            HomeCakeDto homeCakeDto = HomeCakeDto.builder()
                    .cakeId(cakeId)
                    .mainImg(thumbnailMainImg)
                    .likeCnt(likeCnt)
                    .build();
            homeCakeDtoList.add(homeCakeDto);
        }
        return homeCakeDtoList;
    }

    // 케이크 사진 리스트 조회 메소드
    public List<CakeSimpleResponseDto> getCakeList(int page, String sortType) {

        if (page>=60 && sortType.equals("random")) {
            return new ArrayList<>();
        }

        List<Cake> foundCakeList = findCakeListBySortType(page, sortType);

        List<CakeSimpleResponseDto> responseDtoList = new ArrayList<>();
        for (Cake cake : foundCakeList) {
            CakeSimpleResponseDto responseDto = new CakeSimpleResponseDto(cake);
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }

    // 케이크 사진 상세 조회 메소드
    public CakeResponseDto getCakeDetails(UserDetailsImpl userDetails, long cakeId) {

        User user = null;
        if (userDetails!=null) {
            user = userDetails.getUser();
        }

        Cake foundCake = cakeRepository.findById(cakeId)
                .orElseThrow(()->new CustomException(ErrorCode.CAKE_NOT_FOUND));

        boolean myLike = false;

        // 좋아요 여부 반영
        if(user!=null) {
            boolean foundCakeLike = cakeLikeRepository.existsByUserAndCake(user, foundCake);
            if (foundCakeLike) {
                myLike = true;
            }
        }

        return CakeResponseDto.builder()
                .cake(foundCake)
                .myLike(myLike)
                .imgUrl(foundCake.getUrl())
                .build();
    }

    // 케이크 좋아요 생성 및 삭제 메소드
    @Transactional
    public LikeResponseDto saveCakeLike(long cakeId, LikeRequestDto requestDto, UserDetailsImpl userDetails) {

        User user = userDetails.getUser();

        Cake foundCake = cakeRepository.findById(cakeId)
                .orElseThrow(()->new CustomException(ErrorCode.CAKE_NOT_FOUND));

        boolean existsCakeLike = cakeLikeRepository.existsByUserAndCake(user,foundCake);

        // 좋아요를 누른 경우
        if (requestDto.isMyLike()) {

            if (existsCakeLike) {
                throw new CustomException(ErrorCode.LIKE_ALREADY_EXIST);
            }

            CakeLike cakeLike = CakeLike.builder()
                    .cake(foundCake)
                    .user(user)
                    .build();
            cakeLikeRepository.save(cakeLike);

        // 좋아요를 취소한 경우
        } else {
            if (!existsCakeLike) {
                throw new CustomException(ErrorCode.LIKE_NOT_EXIST);
            }
            cakeLikeRepository.deleteByUserAndCake(user, foundCake);
        }

        foundCake.editLikeCnt(requestDto.isMyLike());

        return LikeResponseDto.builder()
                .myLike(requestDto.isMyLike())
                .likeCnt(foundCake.getLikeCnt())
                .build();
    }

    // 케이크 리스트 찾기 메소드
    public List<Cake> findCakeListBySortType(int page, String sortType) {

        List<Cake> foundCakeList = new ArrayList<>();

        if (sortType.equals("likeCnt")) {
            Sort sort = Sort.by(
                    new Sort.Order(Sort.Direction.DESC,"likeCnt"),
                    new Sort.Order(Sort.Direction.DESC,"cakeId")
            );
            Pageable pageable = PageRequest.of(page,54,sort);
            Page<Cake> foundCakePage = cakeRepository.findAll(pageable);

            for (Cake cake : foundCakePage) {
                foundCakeList.add(cake);
            }
        } else {
            foundCakeList = cakeRepository.findByRandom();
        }

        return foundCakeList;
    }
}