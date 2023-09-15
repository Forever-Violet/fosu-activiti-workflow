package org.fosu.workflow.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ApiModel(value = "启动流程请求类")
public class StartREQ implements Serializable {

    @ApiModelProperty("业务列表页面的前端路由名")
    private String businessRoute;
    @ApiModelProperty("业务唯一值id")
    private String businessKey;
    @ApiModelProperty("节点任务办理人一位或多位")
    private List<String> assignees;
    @ApiModelProperty("流程变量")
    private Map<String, Object> variables;

    public Map<String, Object> getVariables() {
        // 为空时，创建空实例
        return variables == null ? new HashMap<>() : variables;
    }
}
