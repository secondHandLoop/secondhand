package com.secondhand.user.service;

import com.secondhand.common.entity.Status;
import com.secondhand.common.support.ReflectApiService;
import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.user.entity.Address;
import com.secondhand.user.repository.AddressRepository;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Document(
        desc = "修改地址",
        module = "user",
        name = "修改地址",
        author = "Hongyu",
        platform = {Platform.MANAGE, Platform.APP, Platform.APPLETS},
        creatAt = "2025-11-01",
        request = {
                @ApiParam(name = "id", value = "地址ID", dataType = "Long"),
                @ApiParam(name = "state", value = "省份/州", dataType = "String", required = false),
                @ApiParam(name = "city", value = "城市", dataType = "String", required = false),
                @ApiParam(name = "address", value = "详细地址", dataType = "String", required = false),
                @ApiParam(name = "zipcode", value = "邮编", dataType = "int", required = false),
                @ApiParam(name = "isDefault", value = "是否默认地址", dataType = "boolean", required = false),
                @ApiParam(name = "phone", value = "电话", dataType = "String", required = false)
        },
        response = {
                @ApiParam(name = "id", value = "地址ID", dataType = "Long"),
                @ApiParam(name = "message", value = "提示信息", dataType = "String")
        }
)

@Component("addressEditService1.0")
public class AddressEditService extends ReflectApiService {

    @Resource
    private AddressRepository addressRepository;

    @Transactional
    public Map<String, Object> service(Long currentUserId,
                                       Long id,
                                       String state,
                                       String city,
                                       String address,
                                       Integer zipcode,
                                       Boolean isDefault,
                                       String phone) {

        Address addr = addressRepository.findById(id).orElse(null);
        AssertUtil.isTrue(addr != null, "地址不存在");

        AssertUtil.isTrue(addr.getStatus() == Status.ACTIVE, "地址已删除");
        AssertUtil.isTrue(currentUserId.equals(addr.getUserId()), "无权限修改该地址");

        if (Boolean.TRUE.equals(isDefault)) {
            List<Address> defaults =
                    addressRepository.findByUserIdAndStatusAndIsDefault(currentUserId, Status.ACTIVE, true);

            long now = System.currentTimeMillis() / 1000;
            for (Address a : defaults) {
                if (!a.getId().equals(id)) {
                    a.setIsDefault(false);
                    a.setUpdateAt(now);
                }
            }
            if (!defaults.isEmpty()) {
                addressRepository.saveAll(defaults);
            }

            addr.setIsDefault(true);
        } else if (isDefault != null) {
            addr.setIsDefault(false);
        }

        if (state != null) addr.setState(state);
        if (city != null) addr.setCity(city);
        if (address != null) addr.setAddress(address);
        if (zipcode != null) addr.setZipcode(zipcode);
        if (phone != null) addr.setPhone(phone);

        addr.setUpdateAt(System.currentTimeMillis() / 1000);

        addressRepository.save(addr);
        return success();
    }
}
