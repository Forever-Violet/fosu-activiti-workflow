package org.fosu.workflow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BusinessStatusEnum {

    CANCEL(0, "已撤回"), WAIT(1, "待提交"), PROCESS(2, "处理中"),
    FINISH(3, "已完成"), INVALID(4, "已作废"), DELETE(5, "已删除");
    private Integer code;
    private String desc;

    public static BusinessStatusEnum getEumByCode(Integer code){
        if(code == null) return null;

        for(BusinessStatusEnum statusEnum: BusinessStatusEnum.values()) {
            if(statusEnum.getCode() == code) {
                return statusEnum;
            }
        }
        return null;
    }

}
