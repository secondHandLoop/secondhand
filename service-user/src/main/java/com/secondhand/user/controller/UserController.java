package com.secondhand.user.controller;

import com.secondhand.common.controller.RequestUserIdResolver;
import com.secondhand.user.entity.User;
import com.secondhand.user.service.AddressAddService;
import com.secondhand.user.service.AddressDeleteService;
import com.secondhand.user.service.AddressEditService;
import com.secondhand.user.service.AddressListService;
import com.secondhand.user.service.ManagerUserDetailService;
import com.secondhand.user.service.ManagerUserListService;
import com.secondhand.user.service.ManagerAddService;
import com.secondhand.user.service.UserEditService;
import com.secondhand.user.service.UserLoginService;
import com.secondhand.user.service.UserPasswordResetService;
import com.secondhand.user.service.UserPhotoUploadService;
import com.secondhand.user.service.UserProfileService;
import com.secondhand.user.service.UserRegisterService;
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

import java.util.Map;

@RestController
@RequestMapping("/user")
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
                          AddressDeleteService addressDeleteService) {
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
    }

    @PostMapping("/register/request-code")
    public Map<String, Object> requestRegisterCode(@RequestBody RegisterCodeRequest request) {
        return userRegisterService.requestEmailCode(request.getEmail());
    }

    @PostMapping("/register/verify-code")
    public Map<String, Object> verifyRegisterCode(@RequestBody VerifyCodeRequest request) {
        return userRegisterService.verifyEmailCode(request.getVerificationToken(), request.getCode());
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterCompleteRequest request) {
        return userRegisterService.completeRegistration(
                request.getVerificationToken(),
                request.getPassword(),
                request.getUsername()
        );
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        return userLoginService.login(request.getEmail(), request.getPassword());
    }

    @PostMapping("/login/verify-email")
    public Map<String, Object> verifyLoginEmail(@RequestBody VerifyCodeRequest request) {
        return userLoginService.verifyLoginEmail(request.getVerificationToken(), request.getCode());
    }

    @PostMapping("/managers")
    public Map<String, Object> createManager(@RequestHeader("Authorization") String authorizationHeader,
                                             @RequestBody ManagerCreateRequest request) {
        Long currentUserId = userIdResolver.requireManagerUserId(authorizationHeader);
        return managerAddService.createManager(
                currentUserId,
                request.getEmail(),
                request.getPassword(),
                request.getUsername()
        );
    }

    @GetMapping("/manage/users")
    public Map<String, Object> listUsers(@RequestHeader("Authorization") String authorizationHeader,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false) Integer limit,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String role,
                                         @RequestParam(required = false) String status) {
        userIdResolver.requireManagerUserId(authorizationHeader);
        return managerUserListService.service(page, limit, keyword, role, status);
    }

    @GetMapping("/manage/users/{id}")
    public Map<String, Object> userDetail(@RequestHeader("Authorization") String authorizationHeader,
                                          @PathVariable Long id) {
        userIdResolver.requireManagerUserId(authorizationHeader);
        return managerUserDetailService.service(id);
    }

    @GetMapping("/users/{id}")
    public Map<String, Object> publicProfile(@PathVariable Long id) {
        return userProfileService.publicProfile(id);
    }

    @PostMapping("/profile/photo")
    public Map<String, Object> uploadProfilePhoto(@RequestHeader("Authorization") String authorizationHeader,
                                                  @RequestParam("photo") MultipartFile photo) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        String url = userPhotoUploadService.uploadAvatar(photo, currentUserId);
        // persist the new photo URL on the user record
        User user = new User();
        user.setId(currentUserId);
        user.setProfilePhoto(url);
        userEditService.service(currentUserId, user, null);
        java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("url", url);
        return new com.secondhand.common.output.ApiResponseHelper().success(body);
    }

    @PostMapping("/password/reset/request")
    public Map<String, Object> requestPasswordReset(@RequestHeader("Authorization") String authorizationHeader) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return userPasswordResetService.requestCode(currentUserId);
    }

    @PostMapping("/password/reset/verify")
    public Map<String, Object> verifyPasswordResetCode(@RequestBody PasswordResetVerifyRequest request) {
        return userPasswordResetService.verifyCode(request.getVerificationToken(), request.getCode());
    }

    @PostMapping("/password/reset/complete")
    public Map<String, Object> completePasswordReset(@RequestBody PasswordResetCompleteRequest request) {
        return userPasswordResetService.completeReset(request.getVerificationToken(), request.getNewPassword());
    }

    public static class PasswordResetVerifyRequest {
        private String verificationToken;
        private String code;
        public String getVerificationToken() { return verificationToken; }
        public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    public static class PasswordResetCompleteRequest {
        private String verificationToken;
        private String newPassword;
        public String getVerificationToken() { return verificationToken; }
        public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    @GetMapping("/profile")
    public Map<String, Object> profile(@RequestHeader("Authorization") String authorizationHeader) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return userProfileService.service(currentUserId);
    }

    @PutMapping("/profile")
    public Map<String, Object> editProfile(@RequestHeader("Authorization") String authorizationHeader,
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

        return userEditService.service(currentUserId, user, request.getCurrentPassword());
    }

    @GetMapping("/addresses")
    public Map<String, Object> listAddresses(@RequestHeader("Authorization") String authorizationHeader) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return addressListService.service(currentUserId);
    }

    @PostMapping("/addresses")
    public Map<String, Object> addAddress(@RequestHeader("Authorization") String authorizationHeader,
                                          @RequestBody AddressCreateRequest request) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return addressAddService.service(currentUserId, request.getUsername(), request.getState(), request.getCity(),
                request.getAddress(), request.getZipcode(), request.getIsDefault(), request.getPhone());
    }

    @PutMapping("/addresses/{id}")
    public Map<String, Object> editAddress(@RequestHeader("Authorization") String authorizationHeader,
                                           @PathVariable Long id,
                                           @RequestBody AddressEditRequest request) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return addressEditService.service(currentUserId, id, request.getState(), request.getCity(), request.getAddress(),
                request.getZipcode(), request.getIsDefault(), request.getPhone());
    }

    @DeleteMapping("/addresses/{id}")
    public Map<String, Object> deleteAddress(@RequestHeader("Authorization") String authorizationHeader,
                                             @PathVariable Long id) {
        Long currentUserId = userIdResolver.requireUserId(authorizationHeader);
        return addressDeleteService.service(currentUserId, id);
    }

    public static class RegisterCodeRequest {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class VerifyCodeRequest {
        private String verificationToken;
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

    public static class RegisterCompleteRequest {
        private String verificationToken;
        private String password;
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

    public static class LoginRequest {
        private String email;
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

    public static class ManagerCreateRequest {
        private String email;
        private String password;
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

    public static class EditUserRequest {
        private Long id;
        private String email;
        private String password;
        private String currentPassword;
        private String phone;
        private String username;
        private String profilePhoto;
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

    public static class AddressCreateRequest {
        private String username;
        private String state;
        private String city;
        private String address;
        private Integer zipcode;
        private Boolean isDefault;
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

    public static class AddressEditRequest {
        private String state;
        private String city;
        private String address;
        private Integer zipcode;
        private Boolean isDefault;
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
