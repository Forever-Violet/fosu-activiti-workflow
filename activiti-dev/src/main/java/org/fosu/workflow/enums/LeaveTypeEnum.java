package org.fosu.workflow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 1病假，2事假，3年假，4婚假，5产假，6丧假，7探亲，8调休，9其他
 */
@Getter
@AllArgsConstructor
public enum LeaveTypeEnum {
    BJ(1, "病假"), SJ(2, "事假"), NJ(3, "年假"),
    HJ(4, "婚假"), CJ(5, "产假"), SANG_J(6, "丧假"),
    TQ(7, "探亲"), TX(8, "调休"), OTHOR(9, "其他"), ;

    private Integer code;
    private String desc;

    /**
     * 根据code返回枚举值
     * @return
     */
    public static LeaveTypeEnum getEumByCode(Integer code){
        if(code == null) return null;

        for(LeaveTypeEnum typeEnum: LeaveTypeEnum.values()) {
            if(typeEnum.getCode() == code) {
                return typeEnum;
            }
        }
        return null;
    }
}
