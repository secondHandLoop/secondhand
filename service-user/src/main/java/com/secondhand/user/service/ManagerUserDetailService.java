package com.secondhand.user.service;

import com.secondhand.common.output.ApiResponseHelper;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.user.entity.Address;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.AddressRepository;
import com.secondhand.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ManagerUserDetailService extends ApiResponseHelper {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public ManagerUserDetailService(UserRepository userRepository,
                                    AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    public Map<String, Object> service(Long userId) {
        AssertUtil.notNull(userId, "用户ID不能为空");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", user.getId());
        body.put("email", user.getEmail());
        body.put("username", user.getUsername());
        body.put("phone", user.getPhone());
        body.put("profilePhoto", user.getProfilePhoto());
        body.put("score", user.getScore());
        body.put("description", user.getDescription());
        body.put("role", user.getRole());
        body.put("status", user.getStatus());
        body.put("emailVerifiedAt", user.getEmailVerifiedAt());
        body.put("lastLoginAt", user.getLastLoginAt());
        body.put("createAt", user.getCreateAt());
        body.put("updateAt", user.getUpdateAt());

        List<Map<String, Object>> addresses = new ArrayList<>();
        for (Address address : addressRepository.findByUserId(user.getId())) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", address.getId());
            item.put("username", address.getUsername());
            item.put("state", address.getState());
            item.put("city", address.getCity());
            item.put("address", address.getAddress());
            item.put("zipcode", address.getZipcode());
            item.put("phone", address.getPhone());
            item.put("isDefault", address.getIsDefault());
            item.put("status", address.getStatus());
            item.put("createAt", address.getCreateAt());
            item.put("updateAt", address.getUpdateAt());
            addresses.add(item);
        }
        body.put("addresses", addresses);

        return success(body);
    }
}
