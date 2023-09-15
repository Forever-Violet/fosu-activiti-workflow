package org.fosu.workflow.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "查询模型请求类")
public class ModelREQ extends BaseRequest{
    @ApiModelProperty("模型名称")
    private String name;

    @ApiModelProperty("标识Key")
    private String key;
}
