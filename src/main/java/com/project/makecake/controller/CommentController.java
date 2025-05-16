package com.project.makecake.controller;

import com.project.makecake.dto.comment.CommentRequestDto;
import com.project.makecake.dto.comment.CommentResponseDto;
import com.project.makecake.security.UserDetailsImpl;
import com.project.makecake.service.post.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class CommentController {

    private final CommentService commentService;

    // 도안 게시글 댓글 리스트 조회 API (5개씩)
    @GetMapping("/posts/{postId}/comments")
    public List<CommentResponseDto> getCommentList(
            @PathVariable long postId,
            @RequestParam int page
    ) {
        return commentService.getCommentList(postId, page);
    }

    // 도안 댓글 작성 API
    @PostMapping("/posts/{postId}/comments")
    public void addComment(
            @PathVariable long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody CommentRequestDto requestDto
    ) {
        commentService.addComment(postId, userDetails, requestDto);
    }

    // 도안 댓글 수정 API
    @PutMapping("/comments/{commentId}")
    public void editComment(
            @PathVariable long commentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody CommentRequestDto requestDto
    ) {
        commentService.editComment(commentId, userDetails, requestDto);
    }

    // 도안 댓글 삭제 API
    @DeleteMapping("/comments/{commentId}")
    public void deleteComment(
            @PathVariable long commentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        commentService.deleteComment(commentId, userDetails);
    }

}
