package com.secondhand.user.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.user.dto.AddressResponse;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.user.dto.UserResponseMapper;
import com.secondhand.user.entity.Address;
import com.secondhand.user.repository.AddressRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Lists active addresses for the current user with the default address first.
 */
@Service
public class AddressListService {

    @Resource
    private AddressRepository addressRepository;

    /**
     * Returns the caller's saved addresses ordered for checkout/profile display.
     */
    public ApiResponse<List<AddressResponse>> listAddresses(Long currentUserId) {

        AssertUtil.isTrue(currentUserId != null, "用户未登录");

        List<Address> addresses =
                addressRepository.findByUserIdAndStatus(currentUserId, Status.ACTIVE);

        addresses.sort((a, b) -> {
            if (Boolean.TRUE.equals(a.getIsDefault()) && !Boolean.TRUE.equals(b.getIsDefault())) {
                return -1;
            }
            if (!Boolean.TRUE.equals(a.getIsDefault()) && Boolean.TRUE.equals(b.getIsDefault())) {
                return 1;
            }
            return Long.compare(b.getUpdateAt(), a.getUpdateAt());
        });

        List<AddressResponse> body = new ArrayList<>(addresses.size());
        for (Address address : addresses) {
            body.add(UserResponseMapper.toAddress(address));
        }
        return ApiResponse.success(body);
    }
}
