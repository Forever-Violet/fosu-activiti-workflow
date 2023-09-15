package org.fosu.workflow.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("流程定义请求类")
public class ProcDefiREQ extends BaseRequest {
    @ApiModelProperty("流程名称")
    private String name;

    @ApiModelProperty("标识key")
    private String key;
}
