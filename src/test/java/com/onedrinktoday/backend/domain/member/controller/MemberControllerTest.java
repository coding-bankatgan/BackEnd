package com.onedrinktoday.backend.domain.member.controller;

import static com.onedrinktoday.backend.global.exception.ErrorCode.*;
import static com.onedrinktoday.backend.global.exception.ErrorCode.EMAIL_NOT_FOUND;
import static com.onedrinktoday.backend.global.exception.ErrorCode.LOGIN_FAIL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onedrinktoday.backend.domain.member.dto.ChangePasswordRequestDTO;
import com.onedrinktoday.backend.domain.member.dto.MemberRequest;
import com.onedrinktoday.backend.domain.member.dto.MemberResponse;
import com.onedrinktoday.backend.domain.member.dto.PasswordResetDTO;
import com.onedrinktoday.backend.domain.member.dto.PasswordResetRequest;
import com.onedrinktoday.backend.domain.member.service.MemberService;
import com.onedrinktoday.backend.global.exception.CustomException;
import com.onedrinktoday.backend.global.security.JwtProvider;
import com.onedrinktoday.backend.global.security.TokenDto;
import com.onedrinktoday.backend.global.type.Drink;
import com.onedrinktoday.backend.global.type.Role;
import java.sql.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

@WithMockUser
@WebMvcTest(MemberController.class)
public class MemberControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private MemberService memberService;

  @MockBean
  private JwtProvider jwtProvider;

  private MemberRequest.SignUp signUpRequest;
  private MemberResponse memberResponse;
  private MemberRequest.UpdateInfo updateInfo;
  private PasswordResetRequest passwordResetRequest;
  private ChangePasswordRequestDTO changePasswordRequestDTO;
  private TokenDto tokenDto;

  static final String token = "token";
  static final String invalidToken = "invalidToken";
  static final String newPassword = "newPassword123!";
  static final String wrongPassword = "wrongPassword!";

  @BeforeEach
  public void setUp() {
    signUpRequest = MemberRequest.SignUp.builder()
        .regionId(1L)
        .name("JohnDoe")
        .email("john.doe@examples.com")
        .password("Password123!")
        .birthDate(new Date())
        .favorDrink(List.of(Drink.SOJU, Drink.DISTILLED_SPIRITS))
        .alarmEnabled(true)
        .build();

    memberResponse = MemberResponse.builder()
        .id(1L)
        .placeName("서울특별시")
        .name("JohnDoe")
        .email("john.doe@examples.com")
        .birthDate(new Date())
        .favorDrink(List.of(Drink.SOJU, Drink.DISTILLED_SPIRITS))
        .role(Role.USER)
        .alarmEnabled(true)
        .createdAt(new Timestamp(System.currentTimeMillis()))
        .build();

    updateInfo = new MemberRequest.UpdateInfo(
        2L, "JohnDoeBa", List.of(Drink.BEER), false, "newImageUrl");

    tokenDto = TokenDto.builder()
        .accessToken("accessToken")
        .refreshToken("refreshToken")
        .build();

    passwordResetRequest = new PasswordResetRequest("john.doe@examples.com");
    changePasswordRequestDTO = new ChangePasswordRequestDTO("Password123!!", "newPassword123!");
  }

  @Test
  @DisplayName("회원가입 성공")
  public void successSignUp() throws Exception {
    //then
    given(memberService.signUp(any(MemberRequest.SignUp.class))).willReturn(memberResponse);

    //when
    //then
    mockMvc.perform(post("/api/members/signup")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(signUpRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(memberResponse.getName()))
        .andExpect(jsonPath("$.email").value(memberResponse.getEmail()))
        .andExpect(
            jsonPath("$.favorDrink[0]").value(memberResponse.getFavorDrink().get(0).toString()))
        .andExpect(jsonPath("$.role").value(memberResponse.getRole().toString()))
        .andExpect(jsonPath("$.alarmEnabled").value(memberResponse.isAlarmEnabled()))
        .andDo(print());
  }

  @Test
  @DisplayName("회원가입 실패 - 회원 정보 미입력")
  public void failSignUp() throws Exception {
    //given
    MemberRequest.SignUp invalidRequest = MemberRequest.SignUp.builder()
        .regionId(null)
        .name(null)
        .email(null)
        .password(null)
        .birthDate(null)
        .favorDrink(List.of(Drink.SOJU, Drink.DISTILLED_SPIRITS))
        .alarmEnabled(true)
        .build();

    //when
    //then
    mockMvc.perform(post("/api/members/signup")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @Test
  @DisplayName("로그인 성공")
  public void successSignIn() throws Exception {
    //given
    MemberRequest.SignIn request = new MemberRequest.SignIn("john.doe@examples.com",
        "Password123!");

    given(memberService.signIn(any(MemberRequest.SignIn.class))).willReturn(tokenDto);

    //when
    //then
    mockMvc.perform(post("/api/members/signin")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  @DisplayName("로그인 실패 - 이메일 or 비밀번호 잘못된 입력")
  public void failSignIn() throws Exception {
    //given
    MemberRequest.SignIn request = new MemberRequest.SignIn("wrongEmail", wrongPassword);

    given(memberService.signIn(any(MemberRequest.SignIn.class)))
        .willThrow(new CustomException(LOGIN_FAIL));

    //when
    //then
    mockMvc.perform(post("/api/members/signin")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(LOGIN_FAIL.getMessage()))
        .andDo(print());
  }

  @Test
  @DisplayName("비밀번호 재설정 요청 성공")
  public void successRequestPasswordReset() throws Exception {
    //when
    //then
    mockMvc.perform(post("/api/members/request-password-reset")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(passwordResetRequest)))
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  @DisplayName("비밀번호 재설정 요청 실패 - 이메일 미등록")
  public void failRequestPasswordReset() throws Exception {
    //given
    String wrongEmail = "wrong.email@examples.com";

    willThrow(new CustomException(EMAIL_NOT_FOUND)).given(memberService)
        .requestPasswordReset(wrongEmail);

    //when
    //then
    mockMvc.perform(post("/api/members/request-password-reset")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(new PasswordResetRequest(wrongEmail))))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(EMAIL_NOT_FOUND.getMessage()))
        .andDo(print());
  }

  @Test
  @DisplayName("비밀번호 재설정 페이지 리디렉션 성공")
  public void successShowResetPasswordPage() throws Exception {
    //given
    //when
    //then
    mockMvc.perform(get("/api/members/password-reset")
            .with(csrf())
            .param("token", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value("비밀번호 재설정 페이지. 토큰: " + token))
        .andDo(print());
  }

  @Test
  @DisplayName("비밀번호 재설정 페이지 - 토큰 누락")
  public void failShowResetPasswordPage() throws Exception {
    //given
    //when
    //then
    mockMvc.perform(get("/api/members/password-reset"))
        .andExpect(status().isBadRequest())
        .andDo(print());
  }

  @Test
  @DisplayName("비밀번호 재설정 성공 (비밀번호 모를 경우)")
  public void successResetPassword() throws Exception {
    //given
    PasswordResetDTO passwordResetDTO = new PasswordResetDTO(token, newPassword);

    //when
    //then
    mockMvc.perform(post("/api/members/password-reset")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(passwordResetDTO)))
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  @DisplayName("비밀번호 재설정 실패 (비밀번호 모를 경우) - 유효하지 않은 토큰 입력")
  public void failResetPassword() throws Exception {
    //given
    PasswordResetDTO passwordResetDTO = new PasswordResetDTO(invalidToken, newPassword);

    willThrow(new CustomException(INVALID_REFRESH_TOKEN)).given(memberService)
        .resetPassword(passwordResetDTO.getToken(), passwordResetDTO.getNewPassword());

    //when
    //then
    mockMvc.perform(post("/api/members/password-reset")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(passwordResetDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(INVALID_REFRESH_TOKEN.getMessage()))
        .andDo(print());
  }

  @Test
  @DisplayName("비밀번호 변경 성공 (비밀번호 알고 있을 경우)")
  public void successChangePassword() throws Exception {
    //given
    given(jwtProvider.getEmail(token)).willReturn("john.doe@examples.com");

    //when
    //then
    mockMvc.perform(post("/api/members/password-change")
            .with(csrf())
            .header("Access-Token", token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(changePasswordRequestDTO)))
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  @DisplayName("비밀번호 변경 실패 (비밀번호 알고 있을 경우) - 현재 비밀번호 틀림")
  public void failChangePassword() throws Exception {
    //given
    String email = "john.doe@examples.com";
    ChangePasswordRequestDTO request = new ChangePasswordRequestDTO(wrongPassword,
        newPassword);

    given(jwtProvider.getEmail(token)).willReturn(email);
    willThrow(new CustomException(LOGIN_FAIL)).given(memberService)
        .changePassword(email, request.getCurrentPassword(), request.getNewPassword());

    //when
    //then
    mockMvc.perform(post("/api/members/password-change")
            .with(csrf())
            .header("Access-Token", token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(request)))
        .andExpect(status().isBadRequest()) // 400 Bad Request
        .andExpect(content().string(LOGIN_FAIL.getMessage())) // 예외 메시지 검증
        .andDo(print());
  }

  @Test
  @DisplayName("리프레시 토큰 갱신 성공")
  public void successRefreshAccessToken() throws Exception {
    //given
    String refreshToken = "refreshToken";
    given(memberService.refreshAccessToken(refreshToken)).willReturn(tokenDto);

    //when
    //then
    mockMvc.perform(post("/api/members/refresh")
            .with(csrf())
            .header("Refresh-Token", refreshToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value(tokenDto.getAccessToken()))
        .andExpect(jsonPath("$.refreshToken").value(tokenDto.getRefreshToken()))
        .andDo(print());
  }

  @Test
  @DisplayName("리프레시 토큰 갱신 실패 - 토큰이 유효하지 않음")
  public void failRefreshAccessToken() throws Exception {
    //given
    given(memberService.refreshAccessToken(invalidToken))
        .willThrow(new CustomException(INVALID_REFRESH_TOKEN));

    //when
    //then
    mockMvc.perform(post("/api/members/refresh")
            .with(csrf())
            .header("Refresh-Token", invalidToken))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(INVALID_REFRESH_TOKEN.getMessage()))
        .andDo(print());
  }

  @Test
  @DisplayName("회원 정보 조회 성공")
  public void successGetMemberInfo() throws Exception {
    //given
    given(memberService.getMemberInfo()).willReturn(memberResponse);

    //when
    //then
    mockMvc.perform(get("/api/members")
            .with(csrf()))
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  @DisplayName("회원 정보 조회 실패 - 회원 정보가 없음")
  public void failGetMemberInfo() throws Exception {
    //given
    given(memberService.getMemberInfo())
        .willThrow(new CustomException(MEMBER_NOT_FOUND));

    //when
    //then
    mockMvc.perform(get("/api/members")
            .with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(content().string(MEMBER_NOT_FOUND.getMessage()))
        .andDo(print());
  }

  @Test
  @DisplayName("회원 정보 수정 성공")
  public void successUpdateMemberInfo() throws Exception {
    //given
    MemberResponse updatedMemberResponse = MemberResponse.builder()
        .id(1L)
        .placeName("서울특별시")
        .name(updateInfo.getName())
        .email("john.doe@examples.com")
        .birthDate(new Date())
        .favorDrink(updateInfo.getFavorDrink())
        .role(Role.USER)
        .alarmEnabled(updateInfo.isAlarmEnabled())
        .createdAt(new Timestamp(System.currentTimeMillis()))
        .imageUrl(updateInfo.getImageUrl())
        .build();

    given(memberService.updateMemberInfo(any(MemberRequest.UpdateInfo.class)))
        .willReturn(updatedMemberResponse);

    //when
    //then
    mockMvc.perform(post("/api/members")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(updateInfo)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(updateInfo.getName()))
        .andExpect(jsonPath("$.favorDrink[0]").value(updateInfo.getFavorDrink().get(0).toString()))
        .andExpect(jsonPath("$.alarmEnabled").value(updateInfo.isAlarmEnabled()))
        .andExpect(jsonPath("$.imageUrl").value(updateInfo.getImageUrl()))
        .andDo(print());
  }

  @Test
  @DisplayName("회원 정보 수정 실패 - 회원 정보 없음")
  public void failUpdateMemberInfo() throws Exception {
    // Given
    given(memberService.updateMemberInfo(any(MemberRequest.UpdateInfo.class)))
        .willThrow(new CustomException(MEMBER_NOT_FOUND));

    //when
    //then
    mockMvc.perform(post("/api/members")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(updateInfo)))
        .andExpect(status().isNotFound())
        .andExpect(content().string(MEMBER_NOT_FOUND.getMessage()))
        .andDo(print());
  }
}