package com.onedrinktoday.backend.domain.notification.dto;

import com.onedrinktoday.backend.domain.notification.entity.Notification;
import com.onedrinktoday.backend.global.type.NotificationType;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

  private Long id;
  private Long postId;
  private NotificationType type;
  private String content;
  private Timestamp createdAt;

  public static NotificationResponse from(Notification notification) {
    return NotificationResponse.builder()
        .id(notification.getId())
        .postId(notification.getPostId())
        .type(notification.getType())
        .content(notification.getContent())
        .createdAt(notification.getCreatedAt())
        .build();
  }
}