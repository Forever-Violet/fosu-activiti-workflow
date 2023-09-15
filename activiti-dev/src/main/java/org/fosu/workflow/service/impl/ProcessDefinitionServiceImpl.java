package org.fosu.workflow.service.impl;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.commons.lang3.StringUtils;
import org.fosu.workflow.entities.ProcessConfig;
import org.fosu.workflow.req.ProcDefiREQ;
import org.fosu.workflow.service.ProcessConfigService;
import org.fosu.workflow.service.ProcessDefinitionService;
import org.fosu.workflow.utils.DateUtils;
import org.fosu.workflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProcessDefinitionServiceImpl extends ActivitiService implements ProcessDefinitionService {
    @Autowired
    private ProcessConfigService processConfigService;

    /** 条件分页查询已部署的流程定义数据 */
    @Override
    public Result getProcDefiList(ProcDefiREQ req) {
        // 1. 获取ProcessDefinitionQuery
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
        // 条件查询
        if (StringUtils.isNotEmpty(req.getName())) {
            query.processDefinitionNameLike("%" + req.getName() + "%");
        }

        if (StringUtils.isNotEmpty(req.getKey())) {
            query.processDefinitionKeyLike("%" + req.getKey() + "%");
        }

        // 有多个相同标识key的流程时，只查询其最新版本
        List<ProcessDefinition> definitionList = query.latestVersion().listPage(req.getFirstResult(), req.getSize());

        // 用于前端显示页面，总记录数
        long total = query.count();

        // 封闭响应结果
        List<Map<String, Object>> records = definitionList.stream().map(processDefinition -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", processDefinition.getId());
            map.put("name", processDefinition.getName());
            map.put("key", processDefinition.getKey());
            map.put("version", processDefinition.getVersion());
            // 流程定义状态
            map.put("state", processDefinition.isSuspended() ? "已暂停" : "已启动");
            // xml文件名
            map.put("xmlName", processDefinition.getResourceName());
            // png文件名
            map.put("pngName", processDefinition.getDiagramResourceName());

            // 查询部署时间
            String deploymentId = processDefinition.getDeploymentId();
            map.put("deploymentId", deploymentId);
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
            map.put("deploymentTime", DateUtils.format(deployment.getDeploymentTime()));

            // 查询流程定义与业务关系配置信息
            ProcessConfig processConfig = processConfigService.getByProcessKey(processDefinition.getKey());
            if (processConfig != null) {
                // 业务路由名
                map.put("businessRoute", processConfig.getBusinessRoute());
                // 表单组件名
                map.put("formName", processConfig.getFormName());
            }

            return map;
        }).collect(Collectors.toList());

        // 返回数据封装
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("records", records);
        return Result.ok(result);
    }

    /** 通过流程定义id，挂起或激活流程定义 */
    @Override
    public Result updateProcDefiState(String procDefiId) {
        // 流程定义对象
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(procDefiId)
                .singleResult();

        // 获取当前状态是否为：挂起
        boolean suspended = processDefinition.isSuspended();
        if (suspended) {
            // 如果状态是：挂起，将状态更新为：激活
            repositoryService.activateProcessDefinitionById(procDefiId, true, null);
        } else {
            // 如果状态是：激活，将状态更信息为：挂起
            repositoryService.suspendProcessDefinitionById(procDefiId, true, null);
        }
        return Result.ok();
    }

    /** 根据部署ID删除流程部署和删除流程配置信息 */
    @Override
    public Result deleteDeployment(String deploymentId, String key) {
        // 不带级联的删除：如果有正在执行的流程，则删除失败抛出异常；不会删除ACT_HI_和历史表数据
        repositoryService.deleteDeployment(deploymentId);

        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).list();

        // 没有流程定义了，删除流程配置
        if (CollectionUtils.isEmpty(list)) {
            processConfigService.deleteByProcessKey(key);
        }

        return Result.ok();
    }
}
