package com.dzp.clevergarlic.enums;

/**
 * 公共状态枚举
 * @Auther ck
 * @Date 2020/7/13 14:38
 * @Desc
 */
public enum CommonStatusEnum implements CodeEnum {

    /* 计划，参数 */
    REVIEW_XJ(1,"新建"),
    REVIEW_WQR(2,"填写完成"),
    REVIEW_YQR(3,"已确认"),
    REVIEW_YQX(4,"已取消"),
    REVIEW_YJS(5,"已计算"),

    /* 参数列表 */
    CSLB_WSZ(1,"未设置"),
    CSLB_YSZ(2,"已设置"),
    ;

    private CommonStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private Integer code;
    private String msg;

    @Override
    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public static String getMsgByCode(Integer code) {
        for (CommonStatusEnum value : CommonStatusEnum.values()) {
            if (code == null) {
                return null;
            }
            if (value.getCode().equals(code)) {
                return value.getMsg();
            }
        }
        return null;
    }
}
