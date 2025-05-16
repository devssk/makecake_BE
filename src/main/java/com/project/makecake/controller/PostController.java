package com.project.makecake.controller;

import com.project.makecake.dto.like.LikeRequestDto;
import com.project.makecake.dto.like.LikeResponseDto;
import com.project.makecake.dto.post.PostDetailResponseDto;
import com.project.makecake.dto.post.PostRequestDto;
import com.project.makecake.dto.post.PostSimpleResponseDto;
import com.project.makecake.security.UserDetailsImpl;
import com.project.makecake.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class PostController {

    private final PostService postService;

    // 도안 게시글 리스트 조회 API (54개씩)
    @GetMapping("/posts")
    public List<PostSimpleResponseDto> getPostList(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam int page,
            @RequestParam(required = false) String sortType
    ) {
        return postService.getPostList(userDetails, page, sortType);
    }

    // 도안 게시글 상세 조회 API
    @GetMapping("/posts/{postId}")
    public PostDetailResponseDto getPostDetails(
            @PathVariable long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return postService.getPostDetails(postId, userDetails);
    }

    // 도안 게시글 작성 API
    @PostMapping("/posts/{designId}")
    public HashMap<String,Long> addPost(
            @PathVariable long designId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody PostRequestDto requestDto
    ) {
        return postService.addPost(designId, userDetails, requestDto);
    }

    // 도안 게시글 수정 API
    @PutMapping("/posts/{postId}")
    public void editPost(
            @PathVariable long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody PostRequestDto requestDto
    ) {
        postService.editPost(postId, userDetails, requestDto);
    }

    // 도안 게시글 삭제 API
    @DeleteMapping("/posts/{postId}")
    public void deletePost(
            @PathVariable long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        postService.deletePost(postId, userDetails);
    }

    // 도안 게시글 좋아요 등록 및 삭제 API
    @PostMapping("/posts/{postId}/like")
    public LikeResponseDto savePostLike(
            @PathVariable long postId,
            @RequestBody LikeRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return postService.savePostLike(postId, requestDto, userDetails);
    }

}
