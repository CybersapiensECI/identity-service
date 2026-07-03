package co.edu.escuelaing.alphaeci.identity_service.entrypoints.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.ChangePasswordRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.CompleteRegistrationRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.LoginRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.RefreshTokenRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.ResetPasswordRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.ValidateOtpRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.VerificationRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.response.LoginResponseDto;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidCredentialsException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.InvalidInputException;
import co.edu.escuelaing.alphaeci.identity_service.domain.exceptions.UserAlreadyExistsException;
import co.edu.escuelaing.alphaeci.identity_service.domain.model.RegistrationProfile;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.in.LoginPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.in.OtpPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.in.PasswordPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.in.VerificationPort;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.persistence.mapper.RegistrationProfileMapper;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.config.DevSecurityConfig;

@WebMvcTest(IdentityController.class)
@Import(DevSecurityConfig.class)
@ActiveProfiles("dev")
class IdentityControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private LoginPort loginPort;
    @MockitoBean private OtpPort otpPort;
    @MockitoBean private VerificationPort verificationPort;
    @MockitoBean private PasswordPort passwordPort;
    @MockitoBean private RegistrationProfileMapper registrationProfileMapper;

    private static final String BASE = "/api/v1/auth";
    private static final String EMAIL = "user@mail.escuelaing.edu.co";
    private static final String PASSWORD = "SecurePass1!";
    private static final String NEW_PASSWORD = "NewSecure2@";

    private LoginResponseDto tokenPair() {
        return new LoginResponseDto("access-token", "refresh-token", "Bearer");
    }

    private String json(Object obj) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.writeValueAsString(obj);
    }

    // ── initVerification ─────────────────────────────────────────────────────

    @Test
    void initVerification_success_returns201() throws Exception {
        VerificationRequestDto req = new VerificationRequestDto();
        req.setEmail(EMAIL);
        req.setPassword(PASSWORD);

        mockMvc.perform(post(BASE + "/init-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("OTP sent to email"));

        verify(verificationPort).initVerification(EMAIL, PASSWORD);
    }

    @Test
    void initVerification_invalidEmail_returns400() throws Exception {
        VerificationRequestDto req = new VerificationRequestDto();
        req.setEmail("not-an-email");
        req.setPassword(PASSWORD);

        mockMvc.perform(post(BASE + "/init-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initVerification_blankPassword_returns400() throws Exception {
        VerificationRequestDto req = new VerificationRequestDto();
        req.setEmail(EMAIL);
        req.setPassword("   ");

        mockMvc.perform(post(BASE + "/init-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initVerification_emailAlreadyExists_returns409() throws Exception {
        VerificationRequestDto req = new VerificationRequestDto();
        req.setEmail(EMAIL);
        req.setPassword(PASSWORD);
        doThrow(new UserAlreadyExistsException("Email already registered"))
                .when(verificationPort).initVerification(anyString(), anyString());

        mockMvc.perform(post(BASE + "/init-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isConflict());
    }

    // ── completeRegistration ──────────────────────────────────────────────────

    @Test
    void completeRegistration_success_returns200() throws Exception {
        CompleteRegistrationRequestDto req = buildCompleteRequest();
        when(registrationProfileMapper.toRegistrationProfile(any())).thenReturn(mock(RegistrationProfile.class));

        mockMvc.perform(post(BASE + "/complete-registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registration complete"));
    }

    @Test
    void completeRegistration_blankName_returns400() throws Exception {
        CompleteRegistrationRequestDto req = buildCompleteRequest();
        req.setName("");

        mockMvc.perform(post(BASE + "/complete-registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeRegistration_nullDateOfBirth_returns400() throws Exception {
        CompleteRegistrationRequestDto req = buildCompleteRequest();
        req.setDateOfBirth(null);

        mockMvc.perform(post(BASE + "/complete-registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isBadRequest());
    }

    // ── verifyOtp ─────────────────────────────────────────────────────────────

    @Test
    void verifyOtp_success_returns200() throws Exception {
        ValidateOtpRequestDto req = new ValidateOtpRequestDto();
        req.setEmail(EMAIL);
        req.setCode("123456");
        when(otpPort.validateOtp(EMAIL, "123456")).thenReturn(tokenPair());

        mockMvc.perform(post(BASE + "/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void verifyOtp_invalidCodeFormat_returns400() throws Exception {
        ValidateOtpRequestDto req = new ValidateOtpRequestDto();
        req.setEmail(EMAIL);
        req.setCode("abc");

        mockMvc.perform(post(BASE + "/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isBadRequest());
    }

    // ── resendOtp ─────────────────────────────────────────────────────────────

    @Test
    void resendOtp_success_returns200() throws Exception {
        VerificationRequestDto req = new VerificationRequestDto();
        req.setEmail(EMAIL);
        req.setPassword(PASSWORD);

        mockMvc.perform(post(BASE + "/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("New OTP sent to email"));
    }

    @Test
    void resendOtp_businessError_returnsCorrectStatus() throws Exception {
        VerificationRequestDto req = new VerificationRequestDto();
        req.setEmail(EMAIL);
        req.setPassword(PASSWORD);
        doThrow(new InvalidInputException("User not found")).when(otpPort).resendOtp(anyString());

        mockMvc.perform(post(BASE + "/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    // ── login ────────────────────────────────────────────────────────────────

    @Test
    void login_success_returns200() throws Exception {
        LoginRequestDto req = new LoginRequestDto();
        req.setEmail(EMAIL);
        req.setPassword(PASSWORD);
        when(loginPort.login(EMAIL, PASSWORD)).thenReturn(tokenPair());

        mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        LoginRequestDto req = new LoginRequestDto();
        req.setEmail(EMAIL);
        req.setPassword(PASSWORD);
        when(loginPort.login(anyString(), anyString()))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_invalidEmailDomain_returns400() throws Exception {
        LoginRequestDto req = new LoginRequestDto();
        req.setEmail("user@gmail.com");
        req.setPassword(PASSWORD);

        mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isBadRequest());
    }

    // ── refresh ───────────────────────────────────────────────────────────────

    @Test
    void refresh_success_returns200() throws Exception {
        RefreshTokenRequestDto req = new RefreshTokenRequestDto();
        req.setRefreshToken("old-refresh-token");
        when(loginPort.refresh("old-refresh-token")).thenReturn(tokenPair());

        mockMvc.perform(post(BASE + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void refresh_blankToken_returns400() throws Exception {
        RefreshTokenRequestDto req = new RefreshTokenRequestDto();
        req.setRefreshToken("   ");

        mockMvc.perform(post(BASE + "/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isBadRequest());
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Test
    void logout_success_returns200() throws Exception {
        RefreshTokenRequestDto req = new RefreshTokenRequestDto();
        req.setRefreshToken("some-refresh-token");

        mockMvc.perform(post(BASE + "/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Session closed successfully"));

        verify(loginPort).logout("some-refresh-token");
    }

    // ── forgotPassword ────────────────────────────────────────────────────────

    @Test
    void forgotPassword_success_returns200() throws Exception {
        VerificationRequestDto req = new VerificationRequestDto();
        req.setEmail(EMAIL);
        req.setPassword(PASSWORD);

        mockMvc.perform(post(BASE + "/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Recovery code sent to email"));

        verify(passwordPort).forgotPassword(EMAIL);
    }

    // ── resetPassword ─────────────────────────────────────────────────────────

    @Test
    void resetPassword_success_returns200() throws Exception {
        ResetPasswordRequestDto req = new ResetPasswordRequestDto();
        req.setEmail(EMAIL);
        req.setCode("123456");
        req.setNewPassword(PASSWORD);

        mockMvc.perform(post(BASE + "/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully"));

        verify(passwordPort).resetPassword(EMAIL, "123456", PASSWORD);
    }

    @Test
    void resetPassword_invalidCodeFormat_returns400() throws Exception {
        ResetPasswordRequestDto req = new ResetPasswordRequestDto();
        req.setEmail(EMAIL);
        req.setCode("abc");
        req.setNewPassword(PASSWORD);

        mockMvc.perform(post(BASE + "/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isBadRequest());
    }

    // ── changePassword ────────────────────────────────────────────────────────

    @Test
    void changePassword_success_returns200() throws Exception {
        ChangePasswordRequestDto req = new ChangePasswordRequestDto();
        req.setCurrentPassword(PASSWORD);
        req.setNewPassword(NEW_PASSWORD);

        mockMvc.perform(post(BASE + "/change-password")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(passwordPort).changePassword("valid-token", PASSWORD, NEW_PASSWORD);
    }

    @Test
    void changePassword_wrongCurrentPassword_returns401() throws Exception {
        ChangePasswordRequestDto req = new ChangePasswordRequestDto();
        req.setCurrentPassword(PASSWORD);
        req.setNewPassword(NEW_PASSWORD);
        doThrow(new InvalidCredentialsException("Current password is incorrect"))
                .when(passwordPort).changePassword(anyString(), anyString(), anyString());

        mockMvc.perform(post(BASE + "/change-password")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isUnauthorized());
    }

    private CompleteRegistrationRequestDto buildCompleteRequest() {
        CompleteRegistrationRequestDto req = new CompleteRegistrationRequestDto();
        req.setEmail(EMAIL);
        req.setName("Juan Pérez");
        req.setGender("MALE");
        req.setCareer("SYSTEMS_ENGINEERING");
        req.setSemester(5);
        req.setStudentCarnet("2019050123");
        req.setPhotoUrl("https://photo.url/img.jpg");
        req.setBiography("Bio text");
        req.setPrivacyLevel("PUBLIC");
        req.setDateOfBirth(LocalDate.of(2001, Month.MAY, 15));
        req.setGeolocationEnabled(true);
        return req;
    }
}
