package com.onedrinktoday.backend.domain.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

  @NotNull(message = "게시글 ID를 입력해주세요.")
  private Long postId;
  private String content;
  private Boolean anonymous;
}