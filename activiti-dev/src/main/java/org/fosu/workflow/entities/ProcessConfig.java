package org.fosu.workflow.entities;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.fosu.workflow.utils.DateUtils;

import java.util.Date;

@Data
@ApiModel("流程定义配置实体类")
@TableName("mxg_process_config")
public class ProcessConfig {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @ApiModelProperty("流程KEY")
    private String processKey;

    @ApiModelProperty("业务申请路由名")
    private String businessRoute;

    @ApiModelProperty("关联表单组件名")
    private String formName;

    @ApiModelProperty("创建时间")
    private Date createDate;

    @ApiModelProperty("更新时间")
    private Date updateDate;

    public String getCreateDateStr() {
        if (createDate == null) {
            return "";
        }

        return DateUtils.format(createDate);
    }

    public String getUpdateDateStr() {
        if (updateDate == null) {
            return "";
        }
        return DateUtils.format(updateDate);
    }
}
