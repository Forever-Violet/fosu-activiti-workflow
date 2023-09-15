package org.fosu.workflow.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "分页请求基础类")
public class BaseRequest<T> implements Serializable {
    @ApiModelProperty(value = "页码", required = true)
    private int current;

    @ApiModelProperty(value = "每页显示多少条", required = true)
    private int size;

    /**
     * activiti分页
     */
    public Integer getFirstResult() {
        return (current - 1) * size;
    }

    /**
     * mybatis-plus分页
     */
    public Page<T> getPage() {
        return new Page<T>(current, size);
    }
}
