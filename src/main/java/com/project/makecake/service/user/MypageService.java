package com.project.makecake.service.user;

import com.project.makecake.domain.order.Design;
import com.project.makecake.domain.post.Comment;
import com.project.makecake.domain.post.Post;
import com.project.makecake.domain.post.PostLike;
import com.project.makecake.domain.product.CakeLike;
import com.project.makecake.domain.review.Review;
import com.project.makecake.domain.review.ReviewImg;
import com.project.makecake.domain.store.StoreLike;
import com.project.makecake.domain.user.User;
import com.project.makecake.dto.DesignResponseDto;
import com.project.makecake.dto.mypage.*;
import com.project.makecake.exceptionhandler.CustomException;
import com.project.makecake.exceptionhandler.ErrorCode;
import com.project.makecake.repository.*;
import com.project.makecake.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MypageService {

    private final UserRepository userRepository;
    private final DesignRepository designRepository;
    private final CakeLikeRepository cakeLikeRepository;
    private final CommentRepository commentRepository;
    private final StoreLikeRepository storeLikeRepository;
    private final ReviewRepository reviewRepository;
    private final PostRepository postRepository;
    private final ReviewImgRepository reviewImgRepository;
    private final PostLikeRepository postLikeRepository;

    // 마이프로필 조회 메소드
    public MypageResponseDto getMyProfile(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MAMBER);
        }

        User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        String email = foundUser.getUsername();
        if (foundUser.getProviderEmail() != null){
            email = foundUser.getProviderEmail();
        }
        MypageResponseDto responseDto = MypageResponseDto.builder()
                .nickname(foundUser.getNickname())
                .profileImg(foundUser.getProfileImgUrl())
                .email(email)
                .build();
        return responseDto;
    }

    // 내가 그린 도안 조회 메소드
    public List<MyDesignResponseDto> getMyDesignList(UserDetailsImpl userDetails, String option, int page) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MAMBER);
        }
        User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        List<MyDesignResponseDto> responseDtoList = new ArrayList<>();
        if (option.equals("nonpost")){
            Pageable pageable = PageRequest.of(page, 54);
            Page<Design> foundDesignList = designRepository
                    .findByUserAndPostOrderByCreatedAtDesc(foundUser, false, pageable);

            for (Design design : foundDesignList){
                MyDesignResponseDto responseDto = MyDesignResponseDto.builder()
                        .designId(design.getDesignId())
                        .img(design.getImgUrl())
                        .build();
                responseDtoList.add(responseDto);
            }
        } else if (option.equals("post")){
            Pageable pageable = PageRequest.of(page, 54);
            Page<Post> foundPostList = postRepository.findByUserOrderByCreatedAtDesc(foundUser, pageable);
            for (Post post : foundPostList){
                MyDesignResponseDto responseDto = MyDesignResponseDto.builder()
                        .postId(post.getPostId())
                        .designId(post.getDesign().getDesignId())
                        .img(post.getDesign().getImgUrl())
                        .build();
                responseDtoList.add(responseDto);
            }
        }
        return responseDtoList;
    }

    // 내가 게시 안 한 도안 상세 조회 메소드
    public DesignResponseDto getDesignDetails(Long designId, UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MAMBER);
        }

        Design foundDesign = designRepository.findById(designId)
                .orElseThrow(()->new CustomException(ErrorCode.DESIGN_NOT_FOUND));

        DesignResponseDto responseDto = new DesignResponseDto(foundDesign);

        return responseDto;
    }

    // 내가 좋아요 한 게시글 조회 메소드
    public List<MyReactPostResponceDto> getMyLikePostList(UserDetailsImpl userDetails, int page) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MAMBER);
        }
        User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        Pageable pageable = PageRequest.of(page, 8);
        Page<PostLike> foundPostList = postLikeRepository.findByUserOrderByCreatedAtDesc(foundUser, pageable);
        List<MyReactPostResponceDto> responseDtoList = new ArrayList<>();
        for (PostLike postLike : foundPostList){
            MyReactPostResponceDto responseDto = MyReactPostResponceDto.builder()
                    .postId(postLike.getPost().getPostId())
                    .img(postLike.getPost().getDesign().getImgUrl())
                    .nickname(postLike.getPost().getUser().getNickname())
                    .profileImg(postLike.getPost().getUser().getProfileImgUrl())
                    .content(postLike.getPost().getContent())
                    .createdDate(postLike.getPost().getCreatedAt())
                    .build();
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }

    // 내가 남긴 댓글 조회 메소드
    public List<MyCommentResponseDto> getMyCommentList(UserDetailsImpl userDetails, int page) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MAMBER);
        }
        User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        Pageable pageable = PageRequest.of(page, 15);
        Page<Comment> foundCommentList = commentRepository.findByUserOrderByCreatedAtDesc(foundUser, pageable);
        List<MyCommentResponseDto> responseDtoList = new ArrayList<>();
        for (Comment comment : foundCommentList){
            MyCommentResponseDto responseDto = MyCommentResponseDto.builder()
                    .commentId(comment.getCommentId())
                    .content(comment.getContent())
                    .createdDate(comment.getCreatedAt())
                    .postId(comment.getPost().getPostId())
                    .postTitle(comment.getPost().getTitle())
                    .build();
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }

    // 내가 좋아요 한 매장 조회 메소드
    public List<MyReactStoreResponseDto> getMyLikeStoreList(UserDetailsImpl userDetails, int page) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MAMBER);
        }
        User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        Pageable pageable = PageRequest.of(page, 20);
        Page<StoreLike> foundStoreLikeList = storeLikeRepository.findByUserOrderByCreatedAtDesc(foundUser, pageable);
        List<MyReactStoreResponseDto> responseDtoList = new ArrayList<>();
        for (StoreLike storeLike : foundStoreLikeList){
            String address = storeLike.getStore().getFullAddress();
            String[] custom = address.split(" ");
            String addressSimple = custom[0] + " " + custom[1] + " " + custom[2];
            MyReactStoreResponseDto responseDto = MyReactStoreResponseDto.builder()
                    .storeId(storeLike.getStore().getStoreId())
                    .name(storeLike.getStore().getName())
                    .addressSimple(addressSimple)
                    .mainImg(storeLike.getStore().getThumbnailMainImg())
                    .build();
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }

    // 내가 남긴 후기 조회 메소드
    public List<MyReviewResponseDto> getMyReviewList(UserDetailsImpl userDetails, int page) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MAMBER);
        }
        User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        Pageable pageable = PageRequest.of(page, 15);
        Page<Review> foundReviewList = reviewRepository.findByUserOrderByCreatedAtDesc(foundUser, pageable);
        List<MyReviewResponseDto> responseDtoList = new ArrayList<>();
        for (Review review : foundReviewList){
            ReviewImg reviewImg = reviewImgRepository.findTop1ByReview(review);
            String reviewImgUrl = "";
            if (reviewImg != null) {
                reviewImgUrl = reviewImg.getImgUrl();
            }

            MyReviewResponseDto responseDto = MyReviewResponseDto.builder()
                    .reviewId(review.getReviewId())
                    .storeId(review.getStore().getStoreId())
                    .name(review.getStore().getName())
                    .content(review.getContent())
                    .createdDate(review.getCreatedAt())
                    .reviewImages(reviewImgUrl)
                    .build();
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }

    // 내가 좋아요 한 케이크 조회 메소드
    public List<MyReactCakeResponseDto> getMyLikeCakeList(UserDetailsImpl userDetails, int page) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_MAMBER);
        }
        User foundUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
        Pageable pageable = PageRequest.of(page, 20);
        Page<CakeLike> foundCakeList = cakeLikeRepository.findByUserOrderByCreatedAtDesc(foundUser, pageable);
        List<MyReactCakeResponseDto> responseDtoList = new ArrayList<>();
        for (CakeLike cakeLike : foundCakeList) {
            MyReactCakeResponseDto responseDto = MyReactCakeResponseDto.builder()
                    .cakeId(cakeLike.getCake().getCakeId())
                    .img(cakeLike.getCake().getThumbnailUrl())
                    .storeName(cakeLike.getCake().getStore().getName())
                    .storeId(cakeLike.getCake().getStore().getStoreId())
                    .likeCnt(cakeLike.getCake().getLikeCnt())
                    .myLike(true)
                    .build();
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }


}
