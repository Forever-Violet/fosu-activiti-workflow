package org.fosu.workflow.service;

import org.fosu.workflow.req.ProcDefiREQ;
import org.fosu.workflow.utils.Result;

/**
 * 流程定义管理
 */
public interface ProcessDefinitionService {
    /**
     * 条件分页查询已部署的流程定义数据
     * @param req 流程定义请求实体类
     * @return 数据封装实体类
     */
    Result getProcDefiList(ProcDefiREQ req);

    /**
     * 通过流程定义id，挂起或激活流程定义
     * @param procDefiId 流程实例id
     * @return 数据封装实体类
     */
    Result updateProcDefiState(String procDefiId);

    /**
     * 根据部署ID删除流程部署和删除流程配置信息
     * @param deploymentId 部署Id
     * @param key 流程key
     * @return 数据封装实体类
     */
    Result deleteDeployment(String deploymentId, String key);
}
