package com.secondhand.user.service;

import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.common.support.ReflectApiService;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.UserRepository;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

@Document(desc = "用户编辑", module = "user", name = "用户编辑", author = "Jeffrey", platform = {Platform.MANAGE, Platform.APP, Platform.APPLETS}, creatAt = "2025-10-25",
        request = {
                @ApiParam(name = "user", value = "user", dataType = "User", note = "packing using User"),
                @ApiParam(name = "email", value = "邮箱", dataType = "String"),
                @ApiParam(name = "password", value = "密码", dataType = "String"),
                @ApiParam(name = "name", value = "姓名", dataType = "String", required = false),
        },
        response = {
                @ApiParam(name = "id", value = "用户ID", dataType = "String"),
                @ApiParam(name = "email", value = "邮箱", dataType = "String"),
                @ApiParam(name = "name", value = "姓名", dataType = "String")
        })
@Component("userEditService1.0")
public class UserEditService extends ReflectApiService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    public Map<String, Object> service(Long currentUserId, User user, String currentPassword) {
        User dbUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        AssertUtil.isTrue(currentUserId.equals(user.getId()), "当前用户无权访问");

        boolean emailChanged = StringUtils.hasText(user.getEmail()) && !user.getEmail().equals(dbUser.getEmail());
        boolean passwordChanged = StringUtils.hasText(user.getPassword());

        if (emailChanged || passwordChanged) {
            AssertUtil.notNullOrBlank(currentPassword, "修改邮箱或密码需要输入当前密码");
            boolean passwordMatch = passwordEncoder != null
                    ? passwordEncoder.matches(currentPassword, dbUser.getPassword())
                    : currentPassword.equals(dbUser.getPassword());
            AssertUtil.isTrue(passwordMatch, "当前密码错误");
        }

        if (emailChanged) {
            Optional<User> emailOwner = userRepository.findByEmail(user.getEmail());
            AssertUtil.isFalse(emailOwner.isPresent() && !emailOwner.get().getId().equals(dbUser.getId()), "邮箱已注册");
            dbUser.setEmail(user.getEmail());
        }

        if (StringUtils.hasText(user.getPhone()) && !user.getPhone().equals(dbUser.getPhone())) {
            Optional<User> phoneOwner = userRepository.findByPhone(user.getPhone());
            AssertUtil.isFalse(phoneOwner.isPresent() && !phoneOwner.get().getId().equals(dbUser.getId()), "手机号已注册");
            dbUser.setPhone(user.getPhone());
        }

        if (passwordChanged) {
            String hashed = passwordEncoder != null ? passwordEncoder.encode(user.getPassword()) : user.getPassword();
            dbUser.setPassword(hashed);
        }

        if (user.getProfilePhoto() != null) {
            dbUser.setProfilePhoto(user.getProfilePhoto());
        }
        if (user.getDescription() != null) {
            dbUser.setDescription(user.getDescription());
        }
        if (user.getUsername() != null) {
            dbUser.setUsername(user.getUsername());
        }

        dbUser.setUpdateAt(System.currentTimeMillis() / 1000);
        userRepository.save(dbUser);

        return success();
    }
}
