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
import java.util.Optional;

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
@Component("addressUpdateService1.0")
public class AddressUpdateService extends ReflectApiService {

    @Resource
    private AddressRepository addressRepository;

    public Map<String, Object> service(Long id,
                                       String state,
                                       String city,
                                       String address,
                                       Integer zipcode,
                                       Boolean isDefault,
                                       String phone) {


        Optional<Address> optional = addressRepository.findById(id);
        if (optional.isEmpty()) {
            return fail("地址不存在", 404);
        }


        Address addr = optional.get();
        if (state != null) addr.setState(state);
        if (city != null) addr.setCity(city);
        if (address != null) addr.setAddress(address);
        if (zipcode != null) addr.setZipcode(zipcode);
        if (isDefault != null) addr.setIsDefault(isDefault);
        if (phone != null) addr.setPhone(phone);

        addressRepository.save(addr);

        return success();
    }
}