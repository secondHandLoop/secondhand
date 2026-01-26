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

import java.util.Map;

@Document(
        desc = "删除地址",
        module = "user",
        name = "删除地址",
        author = "Hongyu",
        platform = {Platform.MANAGE, Platform.APP, Platform.APPLETS},
        creatAt = "2025-11-01",
        request = {
                @ApiParam(name = "id", value = "地址ID", dataType = "Long")
        },
        response = {
                @ApiParam(name = "message", value = "提示信息", dataType = "String")
        }
)
@Component("addressDeleteService1.0")
public class AddressDeleteService extends ReflectApiService {

    @Resource
    private AddressRepository addressRepository;

    public Map<String, Object> service(Long uid, Long id) {

        Address address = addressRepository.findById(id).orElse(null);

        AssertUtil.isTrue(address != null, "要删除的地址不存在");
        AssertUtil.isTrue(uid.equals(address.getUserId()), "无权限删除该地址");

        address.setStatus(Status.DEL);
        address.setUpdateAt(System.currentTimeMillis() / 1000);
        addressRepository.save(address);

        return success();
    }
}
