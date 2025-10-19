package com.secondhand.message.entity;

import com.secondhand.common.entity.Basic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class MsgInfo  extends Basic {
    private String phone;
    private String code;
}
