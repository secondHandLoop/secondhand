package com.secondhand.user.api;

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

import java.util.Map;
import java.util.Optional;

@Document(desc = "用户登录", module = "user", name = "用户登录", author = "Jeffrey", platform = {Platform.MANAGE, Platform.APP, Platform.APPLETS}, creatAt = "2025-10-31",
        request = {
                @ApiParam(name = "email", value = "邮箱", dataType = "String"),
                @ApiParam(name = "username", value = "用户名", dataType = "String", required = false),
                @ApiParam(name = "phone", value = "电话", dataType = "String", required = false),
                @ApiParam(name = "password", value = "密码", dataType = "String"),
        },
        response = {
                @ApiParam(name = "id", value = "用户ID", dataType = "String"),
                @ApiParam(name = "email", value = "邮箱", dataType = "String"),
                @ApiParam(name = "name", value = "姓名", dataType = "String")
        })

@Component("userLoginService1.0")
public class UserLoginService extends ReflectApiService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;


    public Map<String, Object> service(Long uid, String email, String username, String phone, String password) {
        Optional<User> optionalUser = Optional.empty();

        if (email != null && !email.isEmpty()) {
            optionalUser = userRepository.findByEmail(email);
        } else if (username != null && !username.isEmpty()) {
            optionalUser = userRepository.findByUsername(username);
        } else if (phone != null && !phone.isEmpty()) {
            optionalUser = userRepository.findByPhone(phone);
        }

        AssertUtil.isTrue(optionalUser.isPresent(), "账号不存在");

        User user = optionalUser.get();

        boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());
        AssertUtil.isTrue(passwordMatch, "密码错误");

        return success("登录成功");
    }
}
