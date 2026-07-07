package com.secondhand.user.controller;

import com.secondhand.common.controller.RequestUserIdResolver;
import com.secondhand.user.dto.AddressResponse;
import com.secondhand.user.dto.ApiResponse;
import com.secondhand.user.dto.ApiErrorResponse;
import com.secondhand.user.dto.FollowUserResponse;
import com.secondhand.user.dto.AuthSuccessResponse;
import com.secondhand.user.dto.ManagerCreateResponse;
import com.secondhand.user.dto.ManagerUserDetailResponse;
import com.secondhand.user.dto.ManagerUserListItemResponse;
import com.secondhand.user.dto.PageInfoResponse;
import com.secondhand.user.dto.PublicUserProfileResponse;
import com.secondhand.user.dto.UploadPhotoResponse;
import com.secondhand.user.dto.UserProfileResponse;
import com.secondhand.user.dto.VerificationChallengeResponse;
import com.secondhand.user.dto.VerificationStatusResponse;
import com.secondhand.user.entity.User;
import com.secondhand.user.service.AddressAddService;
import com.secondhand.user.service.AddressDeleteService;
import com.secondhand.user.service.AddressEditService;
import com.secondhand.user.service.AddressListService;
import com.secondhand.user.service.FollowService;
import com.secondhand.user.service.ManagerUserDetailService;
import com.secondhand.user.service.ManagerUserListService;
import com.secondhand.user.service.ManagerAddService;
import com.secondhand.user.service.UserEditService;
import com.secondhand.user.service.UserLoginService;
import com.secondhand.user.service.UserPasswordResetService;
import com.secondhand.user.service.UserPhotoUploadService;
import com.secondhand.user.service.UserProfileService;
import com.secondhand.user.service.UserRegisterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Tag(name = "Users", description = "Authentication, profile, manager, and address APIs.")
public class UserController {

    private final RequestUserIdResolver userIdResolver;
    private final UserRegisterService userRegisterService;
    private final UserLoginService userLoginService;
    private final UserProfileService userProfileService;
    private final UserEditService userEditService;
    private final UserPhotoUploadService userPhotoUploadService;
    private final UserPasswordResetService userPasswordResetService;
    private final ManagerUserListService managerUserListService;
    private final ManagerUserDetailService managerUserDetailService;
    private final ManagerAddService managerAddService;
    private final AddressListService addressListService;
    private final AddressAddService addressAddService;
    private final AddressEditService addressEditService;
    private final AddressDeleteService addressDeleteService;
    private final FollowService followService;

    public UserController(RequestUserIdResolver userIdResolver,
                          UserRegisterService userRegisterService,
                          UserLoginService userLoginService,
                          UserProfileService userProfileService,
                          UserEditService userEditService,
                          UserPhotoUploadService userPhotoUploadService,
                          UserPasswordResetService userPasswordResetService,
                          ManagerUserListService managerUserListService,
                          ManagerUserDetailService managerUserDetailService,
                          ManagerAddService managerAddService,
                          AddressListService addressListService,
                          AddressAddService addressAddService,
                          AddressEditService addressEditService,
                          AddressDeleteService addressDeleteService,
                          FollowService followService) {
        this.userIdResolver = userIdResolver;
        this.userRegisterService = userRegisterService;
        this.userLoginService = userLoginService;
        this.userProfileService = userProfileService;
        this.userEditService = userEditService;
        this.userPhotoUploadService = userPhotoUploadService;
        this.userPasswordResetService = userPasswordResetService;
        this.managerUserListService = managerUserListService;
        this.managerUserDetailService = managerUserDetailService;
        this.managerAddService = managerAddService;
        this.addressListService = addressListService;
        this.addressAddService = addressAddService;
        this.addressEditService = addressEditService;
        this.addressDeleteService = addressDeleteService;
        this.followService = followService;
    }

    @Operation(summary = "Request a registration code", description = "Sends an email verification code to an email address that is not yet registered.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Verification challenge created successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The registration request is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/register/request-code")
    public ApiResponse<VerificationChallengeResponse> requestRegisterCode(@RequestBody RegisterCodeRequest request) {
        return userRegisterService.requestEmailCode(request.getEmail());
    }

    @Operation(summary = "Verify a registration code", description = "Validates the email verification code sent during registration.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Verification code accepted.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The verification code is invalid or expired.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/register/verify-code")
    public ApiResponse<VerificationStatusResponse> verifyRegisterCode(@RequestBody VerifyCodeRequest request) {
        return userRegisterService.verifyEmailCode(request.getVerificationToken(), request.getCode());
    }

    @Operation(summary = "Complete registration", description = "Creates a new customer account after the email verification challenge has been verified.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account created successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The registration payload is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/register")
    public ApiResponse<AuthSuccessResponse> register(@RequestBody RegisterCompleteRequest request) {
        return userRegisterService.completeRegistration(
                request.getVerificationToken(),
                request.getPassword(),
                request.getUsername()
        );
    }

    @Operation(summary = "Login", description = "Authenticates a user by email and password. Depending on account inactivity, the response may require email re-verification before issuing a token.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login handled successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The login credentials are invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody LoginRequest request) {
        return userLoginService.login(request.getEmail(), request.getPassword());
    }

    @Operation(summary = "Verify login email", description = "Completes a login flow that was paused for email re-verification.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login verification completed successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The verification code is invalid or expired.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/login/verify-email")
    public ApiResponse<AuthSuccessResponse> verifyLoginEmail(@RequestBody VerifyCodeRequest request) {
        return userLoginService.verifyLoginEmail(request.getVerificationToken(), request.getCode());
    }

    @Operation(summary = "Create a manager account", description = "Allows an authenticated manager to create another manager account.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Manager account created successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The manager request is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Only managers can create manager accounts.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/managers")
    public ApiResponse<ManagerCreateResponse> createManager(@RequestHeader("Authorization") String authorizationHeader,
                                                            @RequestBody ManagerCreateRequest request) {
        Long currentUserId = userIdResolver.requireManagerUserId(authorizationHeader);
        return managerAddService.createManager(
                currentUserId,
                request.getEmail(),
                request.getPassword(),
                request.getUsername()
        );
    }

    @Operation(summary = "List users for managers", description = "Returns a filtered, paginated list of users for manager dashboards.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users returned successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Only managers can access this endpoint.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/manage/users")
    public ApiResponse<List<ManagerUserListItemResponse>> listUsers(@RequestHeader("Authorization") String authorizationHeader,
                                                                    @Parameter(description = "Page number starting from 1.") @RequestParam(required = false) Integer page,
                                                                    @Parameter(description = "Number of users per page.") @RequestParam(required = false) Integer limit,
                                                                    @Parameter(description = "Keyword matched against email, username, and phone.") @RequestParam(required = false) String keyword,
                                                                    @Parameter(description = "Filter by user role.") @RequestParam(required = false) String role,
                                                                    @Parameter(description = "Filter by account status.") @RequestParam(required = false) String status) {
        userIdResolver.requireManagerUserId(authorizationHeader);
        return managerUserListService.listUsers(page, limit, keyword, role, status);
    }

    @Operation(summary = "Get user detail for managers", description = "Returns a full user profile and address list for manager review.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User detail returned successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Only managers can access this endpoint.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/manage/users/{id}")
    public ApiResponse<ManagerUserDetailResponse> userDetail(@RequestHeader("Authorization") String authorizationHeader,
                                                             @Parameter(description = "User id to inspect.") @PathVariable Long id) {
        userIdResolver.requireManagerUserId(authorizationHeader);
        return managerUserDetailService.getUserDetail(id);
    }

    @Operation(summary = "Get a public user profile", description = "Returns the public profile fields visible to other users.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Public profile returned successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The user id is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/users/{id}")
    public ApiResponse<PublicUserProfileResponse> publicProfile(@Parameter(description = "Public user id.") @PathVariable Long id) {
        return userProfileService.getPublicProfile(id);
    }

    @Operation(summary = "Upload profile photo", description = "Uploads a new profile photo for the authenticated user and saves the returned URL to the user record.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile photo uploaded successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The uploaded file is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/profile/photo")
    public ApiResponse<UploadPhotoResponse> uploadProfilePhoto(@RequestHeader("Authorization") String authorizationHeader,
                                                               @Parameter(description = "Image file used as the new profile photo.") @RequestParam("photo") MultipartFile photo) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        String url = userPhotoUploadService.uploadAvatar(photo, currentUserId);
        // persist the new photo URL on the user record
        User user = new User();
        user.setId(currentUserId);
        user.setProfilePhoto(url);
        userEditService.editUser(currentUserId, user, null);
        UploadPhotoResponse body = new UploadPhotoResponse();
        body.setUrl(url);
        return ApiResponse.success(body);
    }

    @Operation(summary = "Request a password reset code", description = "Sends a password-reset verification code to the authenticated user's email address.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset challenge created successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/password/reset/request")
    public ApiResponse<VerificationChallengeResponse> requestPasswordReset(@RequestHeader("Authorization") String authorizationHeader) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return userPasswordResetService.requestCode(currentUserId);
    }

    @Operation(summary = "Verify a password reset code", description = "Validates the password-reset verification code before the new password is submitted.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset code accepted.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The verification code is invalid or expired.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/password/reset/verify")
    public ApiResponse<VerificationStatusResponse> verifyPasswordResetCode(@RequestBody PasswordResetVerifyRequest request) {
        return userPasswordResetService.verifyCode(request.getVerificationToken(), request.getCode());
    }

    @Operation(summary = "Complete password reset", description = "Replaces the user's password after a password-reset verification challenge has been verified.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset completed successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The reset payload is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/password/reset/complete")
    public ApiResponse<Void> completePasswordReset(@RequestBody PasswordResetCompleteRequest request) {
        return userPasswordResetService.completeReset(request.getVerificationToken(), request.getNewPassword());
    }

    public static class PasswordResetVerifyRequest {
        @Schema(description = "Verification token returned by the password reset request endpoint.", example = "5ac0b2d4-1f58-4f11-88d4-0a7d5b48d2b9")
        private String verificationToken;
        @Schema(description = "Six-digit email verification code.", example = "482913")
        private String code;
        public String getVerificationToken() { return verificationToken; }
        public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    @Schema(name = "PasswordResetCompleteRequest", description = "Request payload used to submit a new password after verification.")
    public static class PasswordResetCompleteRequest {
        @Schema(description = "Previously verified password reset token.", example = "5ac0b2d4-1f58-4f11-88d4-0a7d5b48d2b9")
        private String verificationToken;
        @Schema(description = "New account password.", example = "newPassword123")
        private String newPassword;
        public String getVerificationToken() { return verificationToken; }
        public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    @Operation(summary = "Get current user's profile", description = "Returns the full profile of the authenticated user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile returned successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> profile(@RequestHeader("Authorization") String authorizationHeader) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return userProfileService.getProfile(currentUserId);
    }

    @Operation(summary = "Edit current user's profile", description = "Updates the authenticated user's profile. Changing email or password requires the current password.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The profile update is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/profile")
    public ApiResponse<Void> editProfile(@RequestHeader("Authorization") String authorizationHeader,
                                         @RequestBody EditUserRequest request) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);

        User user = new User();
        user.setId(request.getId());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setPhone(request.getPhone());
        user.setUsername(request.getUsername());
        user.setProfilePhoto(request.getProfilePhoto());
        user.setDescription(request.getDescription());

        return userEditService.editUser(currentUserId, user, request.getCurrentPassword());
    }

    @Operation(summary = "List addresses", description = "Returns the authenticated user's saved addresses, with the default address first.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Addresses returned successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/addresses")
    public ApiResponse<List<AddressResponse>> listAddresses(@RequestHeader("Authorization") String authorizationHeader) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return addressListService.listAddresses(currentUserId);
    }

    @Operation(summary = "Add an address", description = "Creates a new saved address for the authenticated user and can mark it as the default address.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address created successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The address payload is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/addresses")
    public ApiResponse<Void> addAddress(@RequestHeader("Authorization") String authorizationHeader,
                                        @RequestBody AddressCreateRequest request) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return addressAddService.addAddress(currentUserId, request.getUsername(), request.getState(), request.getCity(),
                request.getAddress(), request.getZipcode(), request.getIsDefault(), request.getPhone());
    }

    @Operation(summary = "Edit an address", description = "Updates an existing saved address owned by the authenticated user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address updated successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The address update is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/addresses/{id}")
    public ApiResponse<Void> editAddress(@RequestHeader("Authorization") String authorizationHeader,
                                         @Parameter(description = "Address id to update.") @PathVariable Long id,
                                         @RequestBody AddressEditRequest request) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return addressEditService.editAddress(currentUserId, id, request.getState(), request.getCity(), request.getAddress(),
                request.getZipcode(), request.getIsDefault(), request.getPhone());
    }

    @Operation(summary = "Delete an address", description = "Soft-deletes an address owned by the authenticated user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Address deleted successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The address id is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/addresses/{id}")
    public ApiResponse<Void> deleteAddress(@RequestHeader("Authorization") String authorizationHeader,
                                           @Parameter(description = "Address id to delete.") @PathVariable Long id) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return addressDeleteService.deleteAddress(currentUserId, id);
    }

    @Operation(summary = "Follow a user", description = "Makes the authenticated user follow another user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Followed successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "The follow request is invalid.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/users/{id}/follow")
    public ApiResponse<Void> followUser(@RequestHeader("Authorization") String authorizationHeader,
                                        @Parameter(description = "User id to follow.") @PathVariable Long id) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return followService.follow(currentUserId, id);
    }

    @Operation(summary = "Unfollow a user", description = "Makes the authenticated user stop following another user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unfollowed successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/users/{id}/follow")
    public ApiResponse<Void> unfollowUser(@RequestHeader("Authorization") String authorizationHeader,
                                          @Parameter(description = "User id to unfollow.") @PathVariable Long id) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return followService.unfollow(currentUserId, id);
    }

    @Operation(summary = "Get follow status", description = "Reports whether the authenticated user follows the given user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Follow status returned successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/users/{id}/follow-status")
    public ApiResponse<Boolean> followStatus(@RequestHeader("Authorization") String authorizationHeader,
                                             @Parameter(description = "User id to check.") @PathVariable Long id) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return followService.isFollowing(currentUserId, id);
    }

    @Operation(summary = "List following", description = "Returns the users the authenticated user is following.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Following list returned successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/following")
    public ApiResponse<List<FollowUserResponse>> listFollowing(@RequestHeader("Authorization") String authorizationHeader) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return followService.listFollowing(currentUserId);
    }

    @Operation(summary = "List followers", description = "Returns the users who follow the authenticated user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Followers list returned successfully.", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/followers")
    public ApiResponse<List<FollowUserResponse>> listFollowers(@RequestHeader("Authorization") String authorizationHeader) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return followService.listFollowers(currentUserId);
    }

    @Schema(name = "RegisterCodeRequest", description = "Request payload used to start email verification during registration.")
    public static class RegisterCodeRequest {
        @Schema(description = "Email address to register.", example = "user@example.com")
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    @Schema(name = "VerifyCodeRequest", description = "Request payload containing a verification token and the code sent by email.")
    public static class VerifyCodeRequest {
        @Schema(description = "Verification token returned by the challenge endpoint.", example = "5ac0b2d4-1f58-4f11-88d4-0a7d5b48d2b9")
        private String verificationToken;
        @Schema(description = "Six-digit email verification code.", example = "482913")
        private String code;

        public String getVerificationToken() {
            return verificationToken;
        }

        public void setVerificationToken(String verificationToken) {
            this.verificationToken = verificationToken;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    @Schema(name = "RegisterCompleteRequest", description = "Request payload used to create a new account after email verification.")
    public static class RegisterCompleteRequest {
        @Schema(description = "Previously verified registration token.", example = "5ac0b2d4-1f58-4f11-88d4-0a7d5b48d2b9")
        private String verificationToken;
        @Schema(description = "New account password.", example = "password123")
        private String password;
        @Schema(description = "Display name for the new account.", example = "Jeffrey")
        private String username;

        public String getVerificationToken() {
            return verificationToken;
        }

        public void setVerificationToken(String verificationToken) {
            this.verificationToken = verificationToken;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    @Schema(name = "LoginRequest", description = "Request payload used to authenticate with email and password.")
    public static class LoginRequest {
        @Schema(description = "Registered email address.", example = "user@example.com")
        private String email;
        @Schema(description = "Account password.", example = "password123")
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @Schema(name = "ManagerCreateRequest", description = "Request payload used by a manager to create another manager account.")
    public static class ManagerCreateRequest {
        @Schema(description = "Manager email address.", example = "manager@example.com")
        private String email;
        @Schema(description = "Initial password for the new manager.", example = "managerPassword123")
        private String password;
        @Schema(description = "Displayed username for the new manager.", example = "AdminUser")
        private String username;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    @Schema(name = "EditUserRequest", description = "Request payload used to update the authenticated user's profile.")
    public static class EditUserRequest {
        @Schema(description = "Current user's id. Must match the authenticated user.", example = "1")
        private Long id;
        @Schema(description = "New email address.", example = "new-email@example.com")
        private String email;
        @Schema(description = "New password if the user is changing it.", example = "newPassword123")
        private String password;
        @Schema(description = "Current password, required when changing email or password.", example = "oldPassword123")
        private String currentPassword;
        @Schema(description = "Phone number.", example = "1234567890")
        private String phone;
        @Schema(description = "Display name.", example = "Jeffrey")
        private String username;
        @Schema(description = "Existing profile photo URL.", example = "https://cdn.example.com/avatar.jpg")
        private String profilePhoto;
        @Schema(description = "User bio or profile description.", example = "Secondhand marketplace seller")
        private String description;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getProfilePhoto() {
            return profilePhoto;
        }

        public void setProfilePhoto(String profilePhoto) {
            this.profilePhoto = profilePhoto;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    @Schema(name = "AddressCreateRequest", description = "Request payload used to create a saved address.")
    public static class AddressCreateRequest {
        @Schema(description = "Recipient name for the address.", example = "Jeffrey Ye")
        private String username;
        @Schema(description = "State or province.", example = "Texas")
        private String state;
        @Schema(description = "City.", example = "Austin")
        private String city;
        @Schema(description = "Street address.", example = "123 Main St")
        private String address;
        @Schema(description = "Zip code.", example = "78701")
        private Integer zipcode;
        @Schema(description = "Whether this address should become the default address.", example = "true")
        private Boolean isDefault;
        @Schema(description = "Phone number for delivery contact.", example = "1234567890")
        private String phone;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Integer getZipcode() {
            return zipcode;
        }

        public void setZipcode(Integer zipcode) {
            this.zipcode = zipcode;
        }

        public Boolean getIsDefault() {
            return isDefault;
        }

        public void setIsDefault(Boolean isDefault) {
            this.isDefault = isDefault;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    @Schema(name = "AddressEditRequest", description = "Request payload used to update an existing saved address.")
    public static class AddressEditRequest {
        @Schema(description = "State or province.", example = "Texas")
        private String state;
        @Schema(description = "City.", example = "Austin")
        private String city;
        @Schema(description = "Street address.", example = "456 Market St")
        private String address;
        @Schema(description = "Zip code.", example = "78702")
        private Integer zipcode;
        @Schema(description = "Whether this address should become the default address.", example = "false")
        private Boolean isDefault;
        @Schema(description = "Phone number for delivery contact.", example = "1234567890")
        private String phone;

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Integer getZipcode() {
            return zipcode;
        }

        public void setZipcode(Integer zipcode) {
            this.zipcode = zipcode;
        }

        public Boolean getIsDefault() {
            return isDefault;
        }

        public void setIsDefault(Boolean isDefault) {
            this.isDefault = isDefault;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
