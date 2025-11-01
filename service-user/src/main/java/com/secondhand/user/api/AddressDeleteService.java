package com.secondhand.user.api;

import com.secondhand.common.support.ReflectApiService;
import com.secondhand.common.doc.ApiParam;
import com.secondhand.common.doc.Document;
import com.secondhand.common.doc.Platform;
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

    public Map<String, Object> service(Long id) {
        if (!addressRepository.existsById(id)) {
            return fail("要删除的地址不存在", 404);
        }


        addressRepository.deleteById(id);

        return success();
    }
}
