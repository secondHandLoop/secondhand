package com.secondhand.user.api;

import com.secondhand.common.entity.Status;
import com.secondhand.common.support.ReflectApiService;
import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.user.entity.Address;
import com.secondhand.user.repository.AddressRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Document(
        desc = "添加新地址",
        module = "user",
        name = "添加新地址",
        author = "Hongyu",
        platform = {Platform.APP, Platform.MANAGE},
        creatAt = "2025-11-01",
        request = {
                @ApiParam(name = "username", value = "收件人姓名", dataType = "String"),
                @ApiParam(name = "state", value = "州/省份", dataType = "String"),
                @ApiParam(name = "city", value = "城市", dataType = "String"),
                @ApiParam(name = "address", value = "详细地址", dataType = "String"),
                @ApiParam(name = "zipcode", value = "邮政编码", dataType = "int"),
                @ApiParam(name = "isDefault", value = "是否为默认地址", dataType = "boolean"),
                @ApiParam(name = "phone", value = "手机号", dataType = "String")
        },
        response = {
                @ApiParam(name = "id", value = "新地址ID", dataType = "long"),
                @ApiParam(name = "message", value = "操作结果", dataType = "String")
        }
)

@Component("addressAddService1.0")
public class AddressAddService extends ReflectApiService {

    @Resource
    private AddressRepository addressRepository;

    public Map<String, Object> service(Long uid, String username, String state, String city,
                                       String address, Integer zipcode, Boolean isDefault, String phone) {

        AssertUtil.isTrue(uid != null, "用户未登录");

        boolean hasAny = addressRepository.existsByUserIdAndStatus(uid, Status.ACTIVE);

        boolean makeDefault;
        if (!hasAny) {
            makeDefault = true;
        } else {
            makeDefault = Boolean.TRUE.equals(isDefault);
        }

        if (makeDefault) {
            List<Address> defaults =
                    addressRepository.findByUserIdAndStatusAndIsDefault(uid, Status.ACTIVE, true);

            long now = System.currentTimeMillis() / 1000;
            for (Address a : defaults) {
                a.setIsDefault(false);
                a.setUpdateAt(now);
            }
            if (!defaults.isEmpty()) {
                addressRepository.saveAll(defaults);
            }
        }

        Address addr = new Address();
        addr.setUserId(uid);
        addr.setUsername(username);
        addr.setState(state);
        addr.setCity(city);
        addr.setAddress(address);
        addr.setZipcode(zipcode);
        addr.setIsDefault(makeDefault);
        addr.setPhone(phone);

        addressRepository.save(addr);
        return success();
    }
}
