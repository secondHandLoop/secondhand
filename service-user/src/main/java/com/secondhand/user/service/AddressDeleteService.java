package com.secondhand.user.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.user.entity.Address;
import com.secondhand.user.repository.AddressRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * Soft-deletes address records owned by the current user.
 */
@Service
public class AddressDeleteService {

    @Resource
    private AddressRepository addressRepository;

    /**
     * Marks an address as deleted after verifying ownership.
     */
    public ApiResponse<Void> deleteAddress(Long currentUserId, Long id) {

        Address address = addressRepository.findById(id).orElse(null);

        AssertUtil.isTrue(address != null, "要删除的地址不存在");
        AssertUtil.isTrue(currentUserId.equals(address.getUserId()), "无权限删除该地址");

        address.setStatus(Status.DEL);
        address.setUpdateAt(System.currentTimeMillis() / 1000);
        addressRepository.save(address);

        return ApiResponse.success();
    }
}
