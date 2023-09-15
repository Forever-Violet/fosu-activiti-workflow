package org.fosu.workflow.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;

public class ActivitiService {
    /** 各种数据类型与json类型的互换 */
    @Autowired
    public ObjectMapper objectMapper;

    /** 任务 Service，用于管理和查询任务，例如：签收、办理等 */
    @Autowired
    public TaskService taskService;

    /** 运行时 Service，可以处理所有正在运行状态的流程实例和任务等 */
    @Autowired
    public RuntimeService runtimeService;

    /** 历史 Service，可以查询所有历史数据，例如：流程实例信息、参与者信 息、完成时间... */
    @Autowired
    public HistoryService historyService;

    /** 流程仓库 Service，主要用于管理流程仓库，比如流程定义的控制管理（部 署、删除、挂起、激活....） */
    @Autowired
    public RepositoryService repositoryService;

    /**
     * 内部最终调用repositoryService和runtimeService相关API
     * 需要ACTIVITI_USER权限
     */
    @Autowired
    public ProcessRuntime processRuntime;

    /**
     * 类内部调用taskService
     * 需要ACTIVITI_USER权限
     */
    @Autowired
    public TaskRuntime taskRuntime;
}
