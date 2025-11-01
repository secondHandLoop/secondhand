package com.secondhand.user.api;

import com.secondhand.common.support.ReflectApiService;
import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
import com.secondhand.user.entity.Address;
import com.secondhand.user.repository.AddressRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Document(
        desc = "查询用户地址",
        module = "user",
        name = "查询地址",
        author = "Hongyu",
        platform = {Platform.MANAGE, Platform.APP, Platform.APPLETS},
        creatAt = "2025-11-01",
        request = {
                @ApiParam(name = "userId", value = "用户ID", dataType = "Long")
        },
        response = {
                @ApiParam(name = "addresses", value = "地址列表", dataType = "List<Address>")
        }
)
@Component("addressQueryService1.0")
public class AddressQueryService extends ReflectApiService {

    @Resource
    private AddressRepository addressRepository;

    public Map<String, Object> service(Long userId) {
        List<Address> addresses = addressRepository.findByUserId(userId);

        return success(Map.of(
                "addresses", addresses
        ));
    }
}
