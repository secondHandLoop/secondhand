package com.secondhand.user.api;

import com.secondhand.common.support.ReflectApiService;
import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.user.entity.Address;
import com.secondhand.user.repository.AddressRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import java.util.Map;

@Document(
        desc = "添加新地址",
        module = "user",
        name = "添加新地址",
        author = "Hongyu",
        platform = {Platform.APP, Platform.MANAGE},
        creatAt = "2025-11-01",
        request = {
                @ApiParam(name = "userId", value = "用户ID", dataType = "long"),
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


    public Map<String, Object> service(Long userId, String username, String state, String city,
                                       String address, Integer zipcode, Boolean isDefault, String phone) {
        Address addr = new Address();
        addr.setUserId(userId);
        addr.setUsername(username);
        addr.setState(state);
        addr.setCity(city);
        addr.setAddress(address);
        addr.setZipcode(zipcode);
        addr.setIsDefault(isDefault);
        addr.setPhone(phone);

        addressRepository.save(addr);
        return success();
    }
}