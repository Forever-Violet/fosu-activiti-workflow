package org.fosu.workflow.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "新增模型请求类")
public class ModelAddREQ extends ModelREQ implements Serializable {
    @ApiModelProperty("描述")
    private String description;
}
