package com.secondhand.user.api;

import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.common.support.ReflectApiService;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.UserRepository;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.AssertTrue;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;

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
    public Map<String, Object> service(Long uid, User user) {
        User dbUser = userRepository.getOne(user.getId());
        AssertUtil.isTrue(uid.equals(user.getId()), "当前用户无权访问");
        AssertUtil.isFalse(dbUser.getEmail().equals(user.getEmail()), "邮箱已注册");
        AssertUtil.isFalse(dbUser.getPhone().equals(user.getPhone()), "手机号已注册");
        String hashed = passwordEncoder != null ? passwordEncoder.encode(user.getPassword()) : user.getPassword();

        dbUser.setEmail(user.getEmail());
        //TODO: Check old email password or verify the user
        dbUser.setPassword(hashed);
        dbUser.setPhone(user.getPhone());
        dbUser.setProfilePhoto(user.getProfilePhoto());
        dbUser.setDescription(user.getDescription());
        userRepository.save(dbUser);

        return success();
    }
}
