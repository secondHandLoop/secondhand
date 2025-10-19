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
                @ApiParam(name = "name", value = "姓名", dataType = "String", required = false),
                @ApiParam(name = "wechatId", value = "微信号", dataType = "String", required = false),
                @ApiParam(name = "campus", value = "学校/校区", dataType = "String", required = false)
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

        // Optional: inject if you want to hash passwords (recommended)
        @Resource
        private BCryptPasswordEncoder passwordEncoder;

        /**
         * 注册入口
         * 框架会按 @ApiParam 名称把请求参数注入到同名方法参数中（如果你的 ReflectApiService 如此实现）
         */
        public Map<String, Object> service(String email, String password, String name, String wechatId, String campus) {
                // 1) 校验唯一性
                if (userRepository.findByEmail(email).isPresent()) {
                        // 你的基类里如果有错误返回方法可用：return fail("邮箱已存在");
                        throw new IllegalArgumentException("邮箱已存在");
                }

                // 2) 构造实体 & 加密密码
                String hashed = passwordEncoder != null ? passwordEncoder.encode(password) : password;

                User user = new User();
                user.setEmail(email);
                user.setPasswordHash(hashed);

                userRepository.save(user);

                // 3) 成功返回（如果 success() 支持数据，带上必要字段）
                // 下面两种根据你的 ReflectApiService 实现二选一：
                // return success(); // 仅状态
                return success();
        }
}