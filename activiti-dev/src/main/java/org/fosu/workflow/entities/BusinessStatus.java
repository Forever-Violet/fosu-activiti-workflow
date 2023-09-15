package org.fosu.workflow.entities;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("业务状态实体")
@TableName("mxg_business_status")
public class BusinessStatus {
    @TableId
    @ApiModelProperty("业务ID")
    private String businessKey;

    @ApiModelProperty("流程实例ID")
    private String processInstanceId;

    @ApiModelProperty("流程状态：0已撤回, 1待提交，2处理中,3已完成, 4已作废，5已删除")
    private Integer status;

    @ApiModelProperty("创建时间")
    private Date createDate;

    @ApiModelProperty("更新时间")
    private Date updateDate;
}
