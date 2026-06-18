package co.edu.escuelaing.alphaeci.identity_service.entrypoints.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.ChangePasswordRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.CompleteRegistrationRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.LoginRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.RefreshTokenRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.infrastructure.adapters.persistence.mapper.RegistrationProfileMapper;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.ResetPasswordRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.ValidateOtpRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.request.VerificationRequestDto;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.response.LoginResponseDto;
import co.edu.escuelaing.alphaeci.identity_service.application.dto.response.RegisterResponseDto;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.in.LoginPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.in.OtpPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.in.PasswordPort;
import co.edu.escuelaing.alphaeci.identity_service.domain.ports.in.VerificationPort;
import co.edu.escuelaing.alphaeci.identity_service.entrypoints.advice.ErrorResponse;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class IdentityController {

    private final LoginPort loginPort;
    private final OtpPort otpPort;
    private final VerificationPort verificationPort;
    private final PasswordPort passwordPort;
    private final RegistrationProfileMapper registrationProfileMapper;

    // ─── Verification ────────────────────────────────────────────────────────

    @Operation(tags = {"Verification"}, summary = "Register and send OTP",
            description = "Creates a new account (status PENDING_VERIFICATION) and sends a 6-digit OTP to the "
                    + "institutional email. The OTP is NOT returned — call POST /verify-otp to activate the account. "
                    + "Returns 409 if the email is already registered.")
    @ApiResponse(responseCode = "201", description = "OTP generated and dispatched to the provided email.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RegisterResponseDto.class)))
    @ApiResponse(responseCode = "400",
            description = "Validation failed: missing fields or non-institutional email domain.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected server-side error.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/init-verification")
    public ResponseEntity<RegisterResponseDto> initVerification(
            @Valid @RequestBody VerificationRequestDto request) {
        verificationPort.initVerification(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResponseDto("OTP sent to email"));
    }

    @Operation(tags = {"Verification"}, summary = "Complete registration",
            description = "Finalises account setup after OTP verification. Accepts the student's profile data "
                    + "and publishes a UserVerifiedEvent to the Profiles service. The account must already be "
                    + "verified (POST /verify-otp called successfully). Returns 403 if email is not yet verified.")
    @ApiResponse(responseCode = "200", description = "Registration complete. Profile event published.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RegisterResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed: missing or invalid fields.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Email not yet verified — call POST /verify-otp first.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected server-side error.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/complete-registration")
    public ResponseEntity<RegisterResponseDto> completeRegistration(
            @Valid @RequestBody CompleteRegistrationRequestDto request) {
        verificationPort.completeRegistration(
                request.getEmail(), registrationProfileMapper.toRegistrationProfile(request));
        return ResponseEntity.ok(new RegisterResponseDto("Registration complete"));
    }

    // ─── OTP ─────────────────────────────────────────────────────────────────

    @Operation(tags = {"OTP"}, summary = "Validate OTP and activate account",
            description = "Validates the 6-digit OTP sent during registration. On success: account is marked "
                    + "verified and ACTIVE, and a token pair (access 15 min + refresh 7 days) is returned. "
                    + "After 3 consecutive wrong attempts the OTP is deleted — call POST /resend-otp first.")
    @ApiResponse(responseCode = "200",
            description = "OTP valid. Account activated. Access and refresh tokens returned.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LoginResponseDto.class)))
    @ApiResponse(responseCode = "400",
            description = "Validation failed: missing fields or OTP not in 6-digit numeric format.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "OTP expired or already used.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "429",
            description = "Maximum attempts (3) exceeded. OTP invalidated. Call POST /resend-otp.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected server-side error.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/verify-otp")
    public ResponseEntity<LoginResponseDto> verifyOtp(@Valid @RequestBody ValidateOtpRequestDto request) {
        return ResponseEntity.ok(otpPort.validateOtp(request.getEmail(), request.getCode()));
    }

    @Operation(tags = {"OTP"}, summary = "Resend OTP",
            description = "Generates and sends a new 6-digit OTP to the given email. Use when the previous "
                    + "OTP expired (10-minute TTL) or the 3-attempt limit was exhausted. Overwrites any "
                    + "existing pending code in Redis.")
    @ApiResponse(responseCode = "200", description = "New OTP sent to the provided email.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RegisterResponseDto.class)))
    @ApiResponse(responseCode = "400",
            description = "Validation failed, account not found, or email already verified.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected server-side error.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/resend-otp")
    public ResponseEntity<RegisterResponseDto> resendOtp(@Valid @RequestBody VerificationRequestDto request) {
        otpPort.resendOtp(request.getEmail());
        return ResponseEntity.ok(new RegisterResponseDto("New OTP sent to email"));
    }

    // ─── Login / Session ─────────────────────────────────────────────────────

    @Operation(tags = {"Auth"}, summary = "Login",
            description = "Authenticates a verified user and returns a JWT access token (15 min) and refresh "
                    + "token (7 days). After 5 consecutive failed attempts the account is temporarily locked.")
    @ApiResponse(responseCode = "200", description = "Authentication successful. Token pair returned.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LoginResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed: missing email or password.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403",
            description = "Email not verified — complete OTP verification first.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422",
            description = "Account temporarily locked after 5 failed login attempts.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected server-side error.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(loginPort.login(request.getEmail(), request.getPassword()));
    }

    @Operation(tags = {"Auth"}, summary = "Refresh access token",
            description = "Exchanges a valid refresh token for a new token pair. Implements token rotation: "
                    + "the submitted refresh token is immediately invalidated and replaced. Clients must store "
                    + "the new refresh token and discard the old one.")
    @ApiResponse(responseCode = "200", description = "New token pair returned.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LoginResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed: refreshToken field missing or blank.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Refresh token invalid, already used, or expired.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected server-side error.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        return ResponseEntity.ok(loginPort.refresh(request.getRefreshToken()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(tags = {"Auth"}, summary = "Logout",
            description = "Revokes the current session's refresh token. After logout the refresh token can no "
                    + "longer generate new access tokens. The access token remains valid until its 15-minute "
                    + "natural expiry — clients must discard both tokens immediately.")
    @ApiResponse(responseCode = "200", description = "Session closed. Refresh token revoked.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RegisterResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed: refreshToken field missing or blank.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Refresh token not found or already revoked.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected server-side error.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/logout")
    public ResponseEntity<RegisterResponseDto> logout(@Valid @RequestBody RefreshTokenRequestDto request) {
        loginPort.logout(request.getRefreshToken());
        return ResponseEntity.ok(new RegisterResponseDto("Session closed successfully"));
    }

    // ─── Password ────────────────────────────────────────────────────────────

    @Operation(tags = {"Password"}, summary = "Forgot password",
            description = "Initiates password recovery. A 6-digit numeric code is generated, stored in Redis "
                    + "with a 15-minute TTL, and sent to the institutional email. Call POST /reset-password "
                    + "with the code and the new password to complete recovery.")
    @ApiResponse(responseCode = "200", description = "Recovery code sent to the provided email.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RegisterResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed: missing or malformed email.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected server-side error.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/forgot-password")
    public ResponseEntity<RegisterResponseDto> forgotPassword(
            @Valid @RequestBody VerificationRequestDto request) {
        passwordPort.forgotPassword(request.getEmail());
        return ResponseEntity.ok(new RegisterResponseDto("Recovery code sent to email"));
    }

    @Operation(tags = {"Password"}, summary = "Reset password",
            description = "Completes the password recovery started by POST /forgot-password. Validates the "
                    + "6-digit recovery code and updates the password if valid. The code is single-use and "
                    + "deleted from Redis immediately upon success.")
    @ApiResponse(responseCode = "200", description = "Password updated successfully.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RegisterResponseDto.class)))
    @ApiResponse(responseCode = "400",
            description = "Validation failed: missing fields or password does not meet requirements.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Recovery code is invalid.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Recovery code expired or already used.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "429", description = "Maximum code attempts exceeded.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected server-side error.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/reset-password")
    public ResponseEntity<RegisterResponseDto> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request) {
        passwordPort.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
        return ResponseEntity.ok(new RegisterResponseDto("Password updated successfully"));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(tags = {"Password"}, summary = "Change password",
            description = "Allows an authenticated user to change their password. Requires a valid Bearer token "
                    + "in the Authorization header. The current password is verified before applying the update. "
                    + "Use POST /forgot-password for forgotten passwords.")
    @ApiResponse(responseCode = "200", description = "Password changed successfully.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RegisterResponseDto.class)))
    @ApiResponse(responseCode = "400",
            description = "Validation failed: missing fields or new password does not meet requirements.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401",
            description = "Authorization header missing/invalid, or current password is wrong.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Unexpected server-side error.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/change-password")
    public ResponseEntity<RegisterResponseDto> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ChangePasswordRequestDto request) {
        String token = authHeader.replace("Bearer ", "").trim();
        passwordPort.changePassword(token, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(new RegisterResponseDto("Password changed successfully"));
    }
}
