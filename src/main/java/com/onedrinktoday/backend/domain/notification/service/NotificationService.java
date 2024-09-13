package com.onedrinktoday.backend.domain.notification.service;

import static com.onedrinktoday.backend.global.exception.ErrorCode.*;
import static com.onedrinktoday.backend.global.type.NotificationType.*;

import com.onedrinktoday.backend.domain.declaration.entity.Declaration;
import com.onedrinktoday.backend.domain.member.entity.Member;
import com.onedrinktoday.backend.domain.member.service.MemberService;
import com.onedrinktoday.backend.domain.notification.entity.Notification;
import com.onedrinktoday.backend.domain.notification.repository.NotificationRepository;
import com.onedrinktoday.backend.domain.post.entity.Post;
import com.onedrinktoday.backend.domain.post.repository.PostRepository;
import com.onedrinktoday.backend.domain.postTag.repository.PostTagRepository;
import com.onedrinktoday.backend.domain.registration.entity.Registration;
import com.onedrinktoday.backend.domain.tag.entity.Tag;
import com.onedrinktoday.backend.domain.tagFollow.entity.TagFollow;
import com.onedrinktoday.backend.domain.tagFollow.repository.TagFollowRepository;
import com.onedrinktoday.backend.global.exception.CustomException;
import com.onedrinktoday.backend.global.type.NotificationType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final MemberService memberService;
  private final PostRepository postRepository;
  private final TagFollowRepository tagFollowRepository;
  private final PostTagRepository postTagRepository;

  public void createNotification(Member member, Long postId, NotificationType type,
      String content) {
    Notification notification = Notification.builder()
        .member(member)
        .postId(postId)
        .type(type)
        .content(content)
        .build();

    notificationRepository.save(notification);
  }

  public List<Notification> getRecentNotifications() {
    Long member = memberService.getMember().getId();
    return notificationRepository.findTop20ByMemberIdOrderByCreatedAtDesc(member);
  }

  public void postCommentNotification(Long postId, String memberName, boolean isAnonymous) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

    String content = isAnonymous ? "게시글에 댓글이 달렸습니다." : memberName + "님이 댓글을 달았습니다.";

    createNotification(post.getMember(), postId, COMMENT, content);
  }

  public void tagFollowPostNotification(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));

    List<Tag> tags = postTagRepository.findTagsByPostId(postId);

    for (Tag tag : tags) {
      List<TagFollow> tagFollows = tagFollowRepository.findByTag(tag);
      for (TagFollow tagFollow : tagFollows) {
        if (!tagFollow.getMember().equals(post.getMember())) {
          createNotification(tagFollow.getMember(), postId, FOLLOW,
              "새로운 게시글이 " + tag.getTagName() + " 태그와 작성되었습니다.");
        }
      }
    }
  }

  public void postDeclarationNotification(Post post, Declaration declaration) {
    createNotification(
        post.getMember(),
        post.getId(),
        NotificationType.REMOVED,
        "게시글이 신고되어 삭제되었습니다."
    );

    createNotification(
        declaration.getMember(),
        null,
        NotificationType.DECLARATION,
        "신고된 게시글이 승인되어 삭제되었습니다."
    );
  }

  public void approveRegistrationNotification(Member member, Registration registration) {
    String message = "신청된 " + registration.getDrinkName() + " 특산주가 승인되었습니다.";

    createNotification(
        member,
        registration.getId(),
        NotificationType.REGISTRATION,
        message
    );
  }
}