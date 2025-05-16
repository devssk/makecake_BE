package com.project.makecake.service.post;

import com.project.makecake.domain.noti.Noti;
import com.project.makecake.domain.noti.PersonalNoti;
import com.project.makecake.domain.order.Design;
import com.project.makecake.domain.post.Post;
import com.project.makecake.domain.post.PostLike;
import com.project.makecake.domain.user.User;
import com.project.makecake.dto.like.LikeRequestDto;
import com.project.makecake.dto.like.LikeResponseDto;
import com.project.makecake.dto.post.PostDetailResponseDto;
import com.project.makecake.dto.post.PostRequestDto;
import com.project.makecake.dto.post.PostSimpleResponseDto;
import com.project.makecake.enums.NotiType;
import com.project.makecake.exceptionhandler.CustomException;
import com.project.makecake.exceptionhandler.ErrorCode;
import com.project.makecake.repository.*;
import com.project.makecake.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final DesignRepository designRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final NotiRepository notiRepository;
    private final PersonalNotiRepository personalNotiRepository;

    // 도안 게시글 리스트 조회 메소드
    public List<PostSimpleResponseDto> getPostList(UserDetailsImpl userDetails, int page, String sortType) {

        User user = null;
        if (userDetails!=null) {
            user = userDetails.getUser();
        }

        Page<Post> foundPostList;
        if (sortType==null || sortType.equals("createdDate")) {
            Sort sort = Sort.by(Sort.Direction.DESC,"postId");
            Pageable pageable = PageRequest.of(page,54,sort);
            foundPostList = postRepository.findAll(pageable);
        } else {
            Sort sort = Sort.by(
                    new Sort.Order(Sort.Direction.DESC,sortType),
                    new Sort.Order(Sort.Direction.DESC,"postId")
            );
            Pageable pageable = PageRequest.of(page,54,sort);
            foundPostList = postRepository.findAll(pageable);
        }

        // 좋아요 반영해서 반환 DTO에 담기
        List<PostSimpleResponseDto> responseDtoList = new ArrayList<>();
        for (Post post : foundPostList) {

            boolean myLike = false;
            if(user!=null) {
                Optional<PostLike> foundPostLike = postLikeRepository.findByUserAndPost(user, post);
                if (foundPostLike.isPresent()) {
                    myLike = true;
                }
            }

            PostSimpleResponseDto responseDto = PostSimpleResponseDto.builder()
                    .post(post)
                    .myLike(myLike)
                    .build();
            responseDtoList.add(responseDto);
        }
        return responseDtoList;
    }

    // 도안 게시글 상세 조회 메소드
    @Transactional
    public PostDetailResponseDto getPostDetails(long postId, UserDetailsImpl userDetails) {

        User user = null;
        if (userDetails!=null) {
            user = userDetails.getUser();
        }

        Post foundPost = postRepository.findById(postId)
                .orElseThrow(()->new CustomException(ErrorCode.POST_NOT_FOUND));
        foundPost.addViewCnt();

        // 좋아요 반영
        boolean myLike = false;
        if(user!=null) {
            Optional<PostLike> foundPostLike = postLikeRepository.findByUserAndPost(user,foundPost);
            if (foundPostLike.isPresent()) {
                myLike = true;
            }
        }

        int commentCnt = commentRepository.countByPost(foundPost).intValue();

        return PostDetailResponseDto.builder()
                .post(foundPost)
                .myLike(myLike)
                .commentCnt(commentCnt)
                .build();
    }

    // 도안 게시글 작성 메소드
    @Transactional
    public HashMap<String,Long> addPost(long designId, UserDetailsImpl userDetails, PostRequestDto requestDto) {

        User user = userDetails.getUser();

        Design foundDesign = designRepository.findById(designId)
                .orElseThrow(()->new CustomException(ErrorCode.DESIGN_NOT_FOUND));

        if (foundDesign.isPost()) {
            throw new CustomException(ErrorCode.DESIGN_ALREADY_POST);
        }

        if (requestDto.getTitle().length() > 20) {
            throw new CustomException(ErrorCode.TITLE_LENGTH_WRONG);
        }

        if (requestDto.getContent().length() > 250) {
            throw new CustomException(ErrorCode.CONTENT_LENGTH_WRONG);
        }

        // 도안 게시글 저장
        Post post = Post.builder()
                .requestDto(requestDto)
                .user(user)
                .design(foundDesign)
                .build();
        Post savedPost = postRepository.save(post);
        foundDesign.editPostState(true);

        HashMap<String,Long> responseDto = new HashMap<>();
        responseDto.put("postId", savedPost.getPostId());
        return responseDto;
    }

    // 도안 게시글 수정 메소드
    @Transactional
    public void editPost(long postId, UserDetailsImpl userDetails, PostRequestDto requestDto) {

        if (requestDto.getTitle().length() > 20) {
            throw new CustomException(ErrorCode.TITLE_LENGTH_WRONG);
        }

        if (requestDto.getContent().length() > 250) {
            throw new CustomException(ErrorCode.CONTENT_LENGTH_WRONG);
        }

        User user = userDetails.getUser();

        Post foundPost = postRepository.findById(postId)
                .orElseThrow(()->new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!foundPost.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.NOT_POST_OWNER);
        }

        foundPost.edit(requestDto);
        postRepository.save(foundPost);
    }

    // 도안 게시글 삭제 메소드
    @Transactional
    public void deletePost(long postId, UserDetailsImpl userDetails) {

        User user = userDetails.getUser();

        Post foundPost = postRepository.findById(postId)
                .orElseThrow(()->new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!foundPost.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.NOT_POST_OWNER);
        }

        // 도안 연결 끊기
        Design connectDesign = foundPost.getDesign();
        foundPost.deleteRelation();
        connectDesign.editPostState(false);

        // 삭제
        postLikeRepository.deleteAllByPost(foundPost);
        commentRepository.deleteAllByPost(foundPost);
        postRepository.delete(foundPost);
    }

    // 도안 게시글 좋아요 등록 및 삭제 메소드
    @Transactional
    public LikeResponseDto savePostLike(long postId, LikeRequestDto requestDto, UserDetailsImpl userDetails) {

        User user = userDetails.getUser();

        Post foundPost = postRepository.findById(postId)
                .orElseThrow(()->new CustomException(ErrorCode.POST_NOT_FOUND));

        boolean existsPostLike = postLikeRepository.existsByUserAndPost(user,foundPost);

        // (1) 좋아요를 누른 경우
        if (requestDto.isMyLike()) {

            if (existsPostLike) {
                throw new CustomException(ErrorCode.LIKE_ALREADY_EXIST);
            }

            PostLike postLike = PostLike.builder()
                    .post(foundPost)
                    .user(user)
                    .build();
            postLikeRepository.save(postLike);

            // 좋아요 누른 유저와 게시글 작성자가 다를 경우에만 좋아요 알림 발송
            if (!user.getUserId().equals(foundPost.getUser().getUserId())
                    && foundPost.getUser().getRole()!=null) {
                addLikeNoti(foundPost, user);
            }

        // (2) 좋아요를 취소한 경우
        } else {

            if (!existsPostLike) {
                throw new CustomException(ErrorCode.LIKE_NOT_EXIST);
            }
            postLikeRepository.deleteByUserAndPost(user,foundPost);
        }

        foundPost.addLikeCnt(requestDto.isMyLike());

        return LikeResponseDto.builder()
                .myLike(requestDto.isMyLike())
                .likeCnt(foundPost.getLikeCnt())
                .build();
    }

    // 좋아요 알림 발송 메소드
    public void addLikeNoti(Post foundPost, User createUser) {

        String redirectUrl = "https://make-cake.com/post/" + foundPost.getPostId();

        Noti foundNoti = notiRepository.findByType(NotiType.LIKE);

        PersonalNoti personalNoti = PersonalNoti.builder()
                .recieveUser(foundPost.getUser())
                .createUser(createUser)
                .noti(foundNoti)
                .redirectUrl(redirectUrl)
                .build();

        personalNotiRepository.save(personalNoti);
    }
}
