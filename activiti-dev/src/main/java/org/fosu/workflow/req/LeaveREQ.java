package org.fosu.workflow.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "查询请假列表条件")
public class LeaveREQ extends BaseRequest {
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("流程状态")
    private Integer status;
    @ApiModelProperty("所属用户名")
    private String username;

}
