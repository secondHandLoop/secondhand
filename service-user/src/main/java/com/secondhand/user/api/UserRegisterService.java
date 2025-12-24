package com.secondhand.user.api;


import com.secondhand.common.support.ReflectApiService;
import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.UserRepository;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Document(desc = "用户注册", module = "user", name = "用户注册", author = "Jeffrey", platform = {Platform.MANAGE, Platform.APP, Platform.APPLETS}, creatAt = "2025-10-17",
        request = {
                @ApiParam(name = "email", value = "邮箱", dataType = "String"),
                @ApiParam(name = "password", value = "密码", dataType = "String"),
                @ApiParam(name = "username", value = "姓名", dataType = "String", required = false),
        },
        response = {
                @ApiParam(name = "id", value = "用户ID", dataType = "String"),
                @ApiParam(name = "email", value = "邮箱", dataType = "String"),
                @ApiParam(name = "name", value = "姓名", dataType = "String")
        }
)

@Component("userRegisterService1.0")
public class UserRegisterService extends ReflectApiService {
        @Resource
        private UserRepository userRepository;

        @Resource
        private BCryptPasswordEncoder passwordEncoder;

        public Map<String, Object> service(String email, String password, String username) {
                if (userRepository.findByEmail(email).isPresent()) {
                        throw new IllegalArgumentException("邮箱已存在");
                }

                String hashed = passwordEncoder != null ? passwordEncoder.encode(password) : password;

                User user = new User();
                user.setEmail(email);
                user.setPassword(hashed);

                userRepository.save(user);

                return success();
        }
}