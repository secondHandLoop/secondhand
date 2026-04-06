package com.secondhand.user.service;

import com.secondhand.common.tool.AssertUtil;
import com.secondhand.user.dto.AddressResponse;
import com.secondhand.user.dto.ApiResponse;
import com.secondhand.user.dto.ManagerUserDetailResponse;
import com.secondhand.user.dto.UserResponseMapper;
import com.secondhand.user.entity.Address;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.AddressRepository;
import com.secondhand.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds manager-facing user detail responses including address history.
 */
@Service
public class ManagerUserDetailService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public ManagerUserDetailService(UserRepository userRepository,
                                    AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    /**
     * Loads a user profile and all related addresses for administrative review.
     */
    public ApiResponse<ManagerUserDetailResponse> getUserDetail(Long userId) {
        AssertUtil.notNull(userId, "用户ID不能为空");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        ManagerUserDetailResponse body = new ManagerUserDetailResponse();
        body.setId(user.getId());
        body.setEmail(user.getEmail());
        body.setUsername(user.getUsername());
        body.setPhone(user.getPhone());
        body.setProfilePhoto(user.getProfilePhoto());
        body.setScore(user.getScore());
        body.setDescription(user.getDescription());
        body.setRole(user.getRole());
        body.setStatus(user.getStatus());
        body.setEmailVerifiedAt(user.getEmailVerifiedAt());
        body.setLastLoginAt(user.getLastLoginAt());
        body.setCreateAt(user.getCreateAt());
        body.setUpdateAt(user.getUpdateAt());

        List<AddressResponse> addresses = new ArrayList<>();
        for (Address address : addressRepository.findByUserId(user.getId())) {
            addresses.add(UserResponseMapper.toAddress(address));
        }
        body.setAddresses(addresses);

        return ApiResponse.success(body);
    }
}
