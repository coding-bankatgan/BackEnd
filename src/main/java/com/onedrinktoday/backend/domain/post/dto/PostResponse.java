package com.onedrinktoday.backend.domain.post.dto;

import com.onedrinktoday.backend.domain.drink.dto.DrinkDTO;
import com.onedrinktoday.backend.domain.post.entity.Post;
import com.onedrinktoday.backend.domain.tag.dto.TagDTO;
import com.onedrinktoday.backend.domain.tag.entity.Tag;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
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
public class PostResponse {
  private Long id;
  private Long memberId;
  private String memberName;
  private DrinkDTO drink;
  private String type;
  private String title;
  private String content;
  private Float rating;
  private List<TagDTO> tags;
  private Integer viewCount;
  private Timestamp createdAt;
  private Timestamp updatedAt;

  public static PostResponse from(Post post, List<Tag> tags) {
    return PostResponse.builder()
        .id(post.getId())
        .memberId(post.getMember().getId())
        .memberName(post.getMember().getName())
        .drink(DrinkDTO.from(post.getDrink()))
        .type(post.getType().name())
        .title(post.getTitle())
        .content(post.getContent())
        .rating(post.getRating())
        .tags(tags.stream().map(TagDTO::from).collect(Collectors.toList()))
        .viewCount(post.getViewCount())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .build();
  }

  // post 엔티티 변환
  public static PostResponse from(Post post) {
    return PostResponse.builder()
        .id(post.getId())
        .title(post.getTitle())
        .content(post.getContent())
        .rating(post.getRating())
        .viewCount(post.getViewCount())
        .createdAt(post.getCreatedAt())
        .drink(DrinkDTO.from(post.getDrink()))
        .build();
  }
}
