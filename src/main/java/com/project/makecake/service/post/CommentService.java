package com.project.makecake.service.post;

import com.project.makecake.domain.noti.Noti;
import com.project.makecake.domain.noti.PersonalNoti;
import com.project.makecake.domain.post.Comment;
import com.project.makecake.domain.post.Post;
import com.project.makecake.domain.user.User;
import com.project.makecake.dto.comment.CommentRequestDto;
import com.project.makecake.dto.comment.CommentResponseDto;
import com.project.makecake.enums.NotiType;
import com.project.makecake.exceptionhandler.CustomException;
import com.project.makecake.exceptionhandler.ErrorCode;
import com.project.makecake.repository.CommentRepository;
import com.project.makecake.repository.NotiRepository;
import com.project.makecake.repository.PersonalNotiRepository;
import com.project.makecake.repository.PostRepository;
import com.project.makecake.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final NotiRepository notiRepository;
    private final PersonalNotiRepository personalNotiRepository;

    // 도안 댓글 리스트 조회 메소드
    public List<CommentResponseDto> getCommentList(long postId, int page) {

        Post foundPost = postRepository.findById(postId)
                .orElseThrow(()->new CustomException(ErrorCode.POST_NOT_FOUND));

        Sort sort = Sort.by(Sort.Direction.DESC,"commentId");
        Pageable pageable = PageRequest.of(page,15,sort);
        Page<Comment> foundCommentList = commentRepository.findAllByPost(foundPost, pageable);

        // 반환 DTO에 담기
        List<CommentResponseDto> responseDtoList = new ArrayList<>();
        for (Comment comment : foundCommentList) {
            CommentResponseDto responseDto = new CommentResponseDto(comment);
            responseDtoList.add(responseDto);
        }

        return responseDtoList;
    }

    // 도안 댓글 작성 메소드
    @Transactional
    public void addComment(long postId, UserDetailsImpl userDetails, CommentRequestDto requestDto) {

        User user = userDetails.getUser();

        if (requestDto.getContent().length() > 100) {
            throw new CustomException(ErrorCode.CONTENT_LENGTH_WRONG);
        }

        Post foundPost = postRepository.findById(postId)
                .orElseThrow(()->new CustomException(ErrorCode.POST_NOT_FOUND));

        if (requestDto.getContent() == null || requestDto.getContent().equals("")) {
            throw new CustomException(ErrorCode.COMMENT_CONTENT_NULL);
        }

        // 댓글 저장
        Comment comment = Comment.builder()
                .content(requestDto.getContent())
                .post(foundPost)
                .user(user)
                .build();
        commentRepository.save(comment);

        // 댓글 작성자와 게시글 작성자가 다를 경우에만 댓글 알림 발송
        if (!user.getUserId().equals(foundPost.getUser().getUserId())
                && foundPost.getUser().getRole()!=null) {
            addCommentNoti(foundPost,user);
        }

        foundPost.editCommentCnt(true);
    }

    // 도안 댓글 수정 메소드
    @Transactional
    public void editComment(long commentId, UserDetailsImpl userDetails, CommentRequestDto requestDto) {

        if (requestDto.getContent().length() > 100) {
            throw new CustomException(ErrorCode.CONTENT_LENGTH_WRONG);
        }

        User user = userDetails.getUser();

        Comment foundComment = commentRepository.findById(commentId)
                .orElseThrow(()->new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!user.getUserId().equals(foundComment.getUser().getUserId())) {
            throw new CustomException(ErrorCode.NOT_COMMENT_OWNER);
        }

        foundComment.editContent(requestDto.getContent());
    }

    // 도안 댓글 삭제 메소드
    @Transactional
    public void deleteComment(long commentId, UserDetailsImpl userDetails) {

        User user = userDetails.getUser();

        Comment foundComment = commentRepository.findById(commentId)
                .orElseThrow(()->new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!user.getUserId().equals(foundComment.getUser().getUserId())) {
            throw new CustomException(ErrorCode.NOT_COMMENT_OWNER);
        }

        // 도안 게시글 댓글수 감소
        foundComment.getPost().editCommentCnt(false);

        // 댓글 삭제
        commentRepository.delete(foundComment);
    }

    // 댓글 알림 발송 메소드
    public void addCommentNoti(Post foundPost, User createUser) {

        String redirectUrl = "https://make-cake.com/post/" + foundPost.getPostId();

        Noti foundNoti = notiRepository.findByType(NotiType.COMMENT);

        // personalNoti 생성
        PersonalNoti personalNoti = PersonalNoti.builder()
                .recieveUser(foundPost.getUser())
                .createUser(createUser)
                .noti(foundNoti)
                .redirectUrl(redirectUrl)
                .build();

        personalNotiRepository.save(personalNoti);
    }
}
