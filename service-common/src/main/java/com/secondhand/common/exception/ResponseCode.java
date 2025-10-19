package com.secondhand.common.exception;

import lombok.Getter;

@Getter
public enum ResponseCode {
    SUCCESS_CODE("success", 200),

    NO_DATA_CODE("No data", 404),

    PARAMETER_ERR("请求参数有误", -1),

    NO_CACHE_PROVIDER("未发现缓存提供者", -205),
    ILLEGAL_TOKEN("非法登录", -105),

    UMC_PASSWORD_ERROR("密码错误", -301),
    UMC_CURRENT_PASSWORD_ERROR("当前密码错误", -302),
    UMC_NEW_PASSWORD_LENGTH_ERROR("新密码长度不能少于6位", -303),
    UMC_NEW_PASSWORD_PATTERN_ERROR("密码必须包含大小写字母和数字", -304),
    UMC_PHONE_NOT_FOUND("用户不存在", -307),
    UMC_PHONE_ERROR("手机号不存在", -308),
    UMC_WECHAT_LOGIN_ERROR("微信登录错误", -309),
    UMC_WECHAT_PHONE_ERROR("获取手机号失败", -310),
    UMC_COUPON_ERROR("优惠券作废了", -311),
    UMC_COUPON_REPEAT("已经兑换过该优惠券了", -312),
    UMC_ROLE_REPEAT("该角色已经存在", -313),
    UMC_COUPON_EXPIRED("过期啦", -314),
    UMC_COUPON_UP("已经兑换满了", -315),
    UMC_REGION_NOT_SUPPORT("该区域还未开放", -316),
    UMC_PHONE_REPEAT("手机号已经存在", -317),
    UMC_CUSTOMER_COUPON_ILLEGAL("无效的优惠券", -318),
    UMC_PUSH_RID_EMPTY("未绑定pushRid", -319),
    UMC_WITHDRAW_ERROR("非法金额", -320),
    UMC_DY_LOGIN_ERROR("抖音登录错误", -321),
    UMC_TRANSPORTER_AUTH_REPEAT("身份证号码已存在", -324),
    UMC_TRANSPORTER_DEPOSIT_REPEAT("已经支付过押金", -325),
    UMC_WITHDRAW_PROFILE_ERROR("请补全提现账户信息", -326),
    UMC_DY_PHONE_ERROR("获取手机号失败", -327),
    UMC_PAYMENT_PASSWORD_ERROR("支付密码错误", -328),
    UMC_PAYMENT_BALANCE_ERROR("余额不足", -329),
    UMC_COUPON_CHECK("您已经领取过本优惠券了，不可重复领取哟", -330),
    UMC_SYSTEM_CONFIG_REPEAT("信息已存在", -331),
    UMC_POI_SYN_FAIL("门店同步失败", -332),

    UMC_PAYEE_ACCOUNT_ENABLE_FAIL("已经存在激活账户，请先关闭再激活", -333),
    UMC_FEE_RULE_ENABLE_FAIL("已经存在激活佣金规则，请先关闭再激活", -334),
    UMC_POI_SYN_VALIDATE_FAIL("只有顶级商户才能同步门店", -335),
    UMC_HOUSEKEEPER_TRANSACTION_BILL_REPEAT_ERROR("该预约单已经入账", -336),
    UMC_DY_PHONE_DECRYPT_ERROR("获取手机号失败", -337),

    OMC_WAREHOUSING_REPEAT("此订单已经入库", -801),
    OMC_ROUTE_ERROR("无法获取距离", -802),
    OMC_TRANSPORTER_ASSIGN_ERROR("该账户已被冻结或休息！", -803),
    OMC_MEMBER_ORDER_PAID_REPEAT("订单已经支付啦", -804),
    OMC_PICKUP_POSTER_EMPTY("请拍照相关图片", -805),
    OMC_TRANSPORTER_ASSIGN_REPEAT("已分配给该配送员啦", -806),
    OMC_PRODUCT_EMPTY("未选择产品", -807),
    OMC_TRANSPORTER_ORDER_GRABBING("该订单已被别人抢走啦", -808),
    OMC_EX_WAREHOUSE_REPEAT("此订单已经出库", -809),
    OMC_SN_ERROR("订单编号不存在", -810),
    OMC_TAKE_SHOES_ERROR("还未清洗或已取走", -811),
    OMC_ORDER_CANCEL_ERROR("完成的订单不能取消", -812),
    OMC_ORDER_BOOK_ERROR("订单状态异常", -813),
    OMC_ORDER_EXPRESS_CANCEL_ERROR("未发现运单号", -814),
    OMC_ORDER_EXPRESS_TIME_ERROR("未指定取货时间", -815),
    OMC_ORDER_EXPRESS_REPEAT_ERROR("重复下单", -816),
    OMC_ORDER_EXPRESS_OUTPUT_ERROR("订单还未清洗完成", -817),
    OMC_ORDER_EXPRESS_TYPE_ERROR("到店自提不支持快递下单", -818),
    OMC_ORDER_FINISH_ERROR("只有清洗完成或者出库的订单可以结束", -819),
    OMC_ORDER_CANCEL_EXPRESS_ERROR("请先取消物流订单", -820),
    OMC_ORDER_YC_UN_HANDLE_ERROR("请先处理异常", -821),
    OMC_WAREHOUSING_POSTER("请至少上传3张图片", -822),
    OMC_BOOK_CHANGE_ERROR("只能在取货一小时前更改", -823),
    OMC_OUTBOUND_EMPTY_ERROR("订单为空", -825),
    OMC_ORDER_ITEM_EMPTY_ERROR("未找到物品", -826),
    OMC_KS_TOKEN_ERROR("快手token过期", -827),
    OMC_WAREHOUSING_ATTACH_EMPTY("未上传附件", -828),
    OMC_SHELF_ADD_ERROR("货架编号已经存在", -829),
    OMC_SHELF_EMPTY_ERROR("没有可用的货架", -830),
    OMC_QA_STATUS_ERROR("只有入库|返洗|清洗完成状态才能质检，请完善流程", -831),
    OMC_DY_BOOK_ERROR("预约失败", -832),
    OMC_ORDER_OUTBOUND_ERROR("清洗完成｜质检完成 才能出库", -833),
    OMC_ORDER_EXPRESS_OUT_ERROR("物流单出错，请到列表重新点送件下单", -834),
    OMC_BOOK_NOT_FOUND_ERROR("未找到预约信息", -835),
    OMC_BOOK_CANCEL_TIME_ERROR("即将上门，不能取消预约", -836),
    OMC_BOOK_REPEAT_ERROR("请不要重复预约", -837),
    OMC_WAREHOUSE_REPEAT_ERROR("此订单已经入库完成", -838),
    OMC_QA_COMPLETE_ERROR("存在物品未完成质检", -839),
    OMC_ORDER_NOT_FOUND_ERROR("未找到相关订单", -850),
    OMC_ORDER_CLEAN_END_ERROR("订单还未入库", -851),
    OMC_ORDER_OUTBOUND_REPEAT_ERROR("此订单已经出库", -852),
    OMC_WAREHOUSING_MERCHANT_ERROR("该订单不属于当前工厂或店铺，请联系管理员进行分配", -853),
    OMC_WAREHOUSE_COMPLETE_ERROR("此订单还有物品未完成入库", -855),
    OMC_ITEM_STOP_ERROR("该物品不支持当前操作", -856),
    OMC_ORDER_BILL_TYPE_ERROR("当前订单状态不允许发起此类型工单", -857),
    OMC_ORDER_ITEM_DELETE_ERROR("当前物品状态不允许被删除", -858),
    OMC_ORDER_BACK_ITEM_EMPTY("请选择反洗物品", -859),
    OMC_WAREHOUSE_PAUSE_COMPLETE_ERROR("本单物品有工单待处理，请先处理工单", -860),
    OMC_EXPRESS_PAUSE("系统检测异常数据过多，已暂停非鞋伯伯系统订单下物流单", -861),
    OMC_EXPRESS_PRINT_ERROR("请选择物流单号", -862),
    OMC_KS_HX_ERROR("快手核销失败", -863),
    OMC_ORDER_ITEM_QA_ERROR("订单还未入库", -865),
    OMC_ORDER_ADDRESS_MODIFY_ERROR("请先取消物流，再进行修改", -866),
    OMC_ORDER_BOOK_AGREE_USER_ILLEGAL("只能同意自己的预约单", -867),
    OMC_ORDER_BOOK_AGREE_STATUS_ILLEGAL("订单已被别人抢走啦！", -868),
    OMC_ORDER_BOOK_NOT_FOUND("预约单不存在", -869),
    OMC_ORDER_BOOK_REPEAT_ERROR("您已经预约过啦", -870),
    OMC_ORDER_BOOK_STATUS_INVALID("预约状态非法", -871),
    OMC_ORDER_BOOK_START_INVALID("您还未同意接受订单", -872),
    OMC_WORK_BILL_STATUS_ERROR("工单状态有误", -873),

    PMC_ADD_TEST_PRODUCT_ERROR("商品ID不能为空", -701),

    MMS_SEND_ERROR("消息发送失败", -601),
    MMS_CODE_ERROR("验证码错误", -602),
    PAY_TRANSACTION_ERROR("已经支付过了", -901),
    PAY_QR_CODE_ERROR("获取支付码失败", -902),
    PAY_QR_CODE_CREATE_ERROR("生成二维码出错", -903),
    PAY_ORDER_CREATE_ERROR("支付错误", -904),
    PAY_DY_ORDER_CREATE_ERROR("支付错误", -905),
    LEVEL_NAME_RENAME("百科分类同级不能重名", -707),

    PMC_COMMENT_ORDER_EXIST("该订单已评价", -1001);
    private final String message;
    private final int code;

    ResponseCode(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() { return message; }
    public int getCode() { return code; }
}
