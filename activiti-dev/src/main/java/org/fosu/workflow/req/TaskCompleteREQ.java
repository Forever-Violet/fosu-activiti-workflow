package org.fosu.workflow.req;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
@ApiModel("完成任务请求类")
public class TaskCompleteREQ implements Serializable {
    @ApiModelProperty("任务Id")
    private String taskId;
    @ApiModelProperty("审批意见")
    private String message;
    @ApiModelProperty("下个节点审批人，key：节点Id，value：审批人集合")
    private Map<String, String> assigneeMap;

    public String getMessage() {
        // 默认：审批通过
        return StringUtils.isEmpty(message) ? "审批通过" : message;
    }/**
     * 根据节点id获取审批人集合
     * @param key 节点id
     * @return 字符串数组
     */
    public String[] getAssignees(String key) {
        if (assigneeMap == null) {
            return null;
        }
        return assigneeMap.get(key).split(",");
    }
}
