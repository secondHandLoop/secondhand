package com.secondhand.user.api;

import com.secondhand.common.entity.Status;
import com.secondhand.common.output.IDataToMap;
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
@Component("addressListService1.0")
public class AddressListService extends ReflectApiService {

    @Resource
    private AddressRepository addressRepository;

    public Map<String, Object> service(Long userId) {

        AssertUtil.isTrue(userId != null, "用户未登录");

        List<Address> addresses =
                addressRepository.findByUserIdAndStatus(userId, Status.ACTIVE);

        addresses.sort((a, b) -> {
            if (Boolean.TRUE.equals(a.getIsDefault()) && !Boolean.TRUE.equals(b.getIsDefault())) {
                return -1;
            }
            if (!Boolean.TRUE.equals(a.getIsDefault()) && Boolean.TRUE.equals(b.getIsDefault())) {
                return 1;
            }
            return Long.compare(b.getUpdateAt(), a.getUpdateAt());
        });

        return success(addresses, (IDataToMap<Address>) (map, source) -> {
            map.put("id", source.getId());
            map.put("isDefault", source.getIsDefault());
            map.put("userId", source.getUserId());
            map.put("username", source.getUsername());
            map.put("state", source.getState());
            map.put("city", source.getCity());
            map.put("address", source.getAddress());
            map.put("zipcode", source.getZipcode());
            map.put("phone", source.getPhone());
            map.put("createAt", source.getCreateAt());
            map.put("updateAt", source.getUpdateAt());
        });
    }
}

