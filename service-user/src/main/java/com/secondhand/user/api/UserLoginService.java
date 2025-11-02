package com.secondhand.user.api;

import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.common.support.ReflectApiService;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.user.repository.UserRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Document(desc = "用户登录", module = "user", name = "用户登录", author = "Jeffrey", platform = {Platform.MANAGE, Platform.APP, Platform.APPLETS}, creatAt = "2025-10-31",
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

@Component("userLoginService")
public class UserLoginService extends ReflectApiService {

    @Resource
    private UserRepository userRepository;

    public Map<String, Object> service(Long uid, String email, String username, String phone, String password) {
        AssertUtil.isTrue(userRepository.findByEmail(email).isPresent() ||
                        userRepository.findByUsername(username).isPresent() || userRepository.findByPhone(phone).isPresent(),
                "Current email/username/phone number and password is incorrect");

        return success();
    }
}
