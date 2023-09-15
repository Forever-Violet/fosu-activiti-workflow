package org.fosu.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.fosu.workflow.entities.ProcessConfig;
import org.fosu.workflow.utils.Result;

public interface ProcessConfigService extends IService<ProcessConfig> {
    /**
     * 通过流程key查询配置数据
     * @param processKey 流程key
     * @return 流程定义配置实体类
     */
    ProcessConfig getByProcessKey(String processKey);

    /**
     * 通过流程key删除配置数据
     * @param processKey 流程key
     * @return 数据结果封装类
     */
    Result deleteByProcessKey(String processKey);

    /**
     * 通过业务路由名查询流程定义配置信息（目的查询获取流程定义key)
     * @param businessRoute
     * @return
     */
    ProcessConfig getByBusinessRoute(String businessRoute);
}
