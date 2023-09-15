package org.fosu.workflow.service.impl;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.fosu.workflow.activiti.image.CustomProcessDiagramGenerator;
import org.fosu.workflow.entities.BusinessStatus;
import org.fosu.workflow.entities.ProcessConfig;
import org.fosu.workflow.enums.BusinessStatusEnum;
import org.fosu.workflow.req.ProcInstREQ;
import org.fosu.workflow.req.StartREQ;
import org.fosu.workflow.service.BusinessStatusService;
import org.fosu.workflow.service.ProcessConfigService;
import org.fosu.workflow.service.ProcessInstanceService;
import org.fosu.workflow.utils.DateUtils;
import org.fosu.workflow.utils.Result;
import org.fosu.workflow.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ProcessInstanceServiceImpl extends ActivitiService implements ProcessInstanceService {
    @Autowired
    private ProcessConfigService processConfigService;
    @Autowired
    private BusinessStatusService businessStatusService;


    /**
     * 通过流程实例id获取申请表单
     */
    @Override
    public Result getFormNameByProcInstId(String procInstId) {
        // 通过历史流程实例查询，因为如果流程实例全部审批完后，正在运行的流程实例数据会被删除，只有历史中有
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .includeProcessVariables()  // 查询流程变量
                .processInstanceId(procInstId).singleResult();

        // 再获取流程实例中的流程变量(启动流程实例时设置了)
        return Result.ok(historicProcessInstance.getProcessVariables().get("formName"));
    }

    /**
     * 获取流程实例审批历史图
     */
    @Override
    public void getHistoryProcessImage(String procInstId, HttpServletResponse response) {
        InputStream inputStream = null;
        try {
            // 1.查询流程实例历史数据
            HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(procInstId).singleResult();

            // 2. 查询流程中已执行的节点，按时开始时间降序排列
            List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(procInstId)
                    .orderByHistoricActivityInstanceStartTime().desc()
                    .list();

            // 3. 单独的提取高亮节点id ( 绿色）
            List<String> highLightedActivityIdList =
                    historicActivityInstanceList.stream()
                            .map(HistoricActivityInstance::getActivityId).collect(Collectors.toList());

            // 4. 正在执行的节点 （红色）
            List<Execution> runningActivityInstanceList = runtimeService.createExecutionQuery()
                    .processInstanceId(procInstId).list();

            List<String> runningActivityIdList = new ArrayList<>();
            for (Execution execution : runningActivityInstanceList) {
                if (StringUtils.isNotEmpty(execution.getActivityId())) {
                    runningActivityIdList.add(execution.getActivityId());
                }
            }

            // 获取流程定义Model对象
            BpmnModel bpmnModel = repositoryService.getBpmnModel(instance.getProcessDefinitionId());

            // 实例化流程图生成器
            CustomProcessDiagramGenerator generator = new CustomProcessDiagramGenerator();
            // 获取高亮连线id
            List<String> highLightedFlows = generator.getHighLightedFlows(bpmnModel, historicActivityInstanceList);
            // 生成历史流程图
            inputStream = generator.generateDiagramCustom(bpmnModel, highLightedActivityIdList,
                    runningActivityIdList, highLightedFlows,
                    "宋体", "微软雅黑", "黑体");

            // 响应相关图片
            response.setContentType("image/svg+xml");
            byte[] bytes = IOUtils.toByteArray(inputStream);
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Result startProcess(StartREQ req) {
        // 通过业务申请路由名，获取流程配置
        ProcessConfig processConfig =
                processConfigService.getByBusinessRoute(req.getBusinessRoute());
        if (processConfig == null ||
                StringUtils.isEmpty(processConfig.getProcessKey())) {
            return Result.error("未找到对应流程，请先配置流程");
        }
        // 接收前端传递的流程变量map，其中有申请表单的{entity: {业务表单实体数据}}
        Map<String, Object> variables = req.getVariables();
        // 将‘表单组件名’，添加到流程变量中
        variables.put("formName", processConfig.getFormName());
        // 任务办理人
        List<String> assignees = req.getAssignees();
        if (CollectionUtils.isEmpty(assignees)) {
            return Result.error("请指定审批人");
        }
        // 启动流程用户(保存在act_hi_procinst的start_user_id字段)
        Authentication.setAuthenticatedUserId(UserUtils.getUsername());
        // 开启流程实例(流程设计图唯一标识key，businessKey业务ID，流程变量)
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey(processConfig.getProcessKey(), req.getBusinessKey(), variables);
        // 设置流程实例名称
        runtimeService.setProcessInstanceName(processInstance.getProcessInstanceId(), processInstance.getProcessDefinitionName());
        // 查询当前流程实例的正在运行任务
        List<Task> taskList =
                taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
        for (Task task : taskList) {
            // 分配第一个任务办理人
            if (assignees.size() == 1) {
                // 一位办理人，直接作为办理人
                taskService.setAssignee(task.getId(), assignees.get(0));
            } else {
                // 多位办理人，作为候选人
                for (String assignee : assignees) {
                    taskService.addCandidateUser(task.getId(), assignee);
                }
            }
        }
        // 更新业务状态表数据(业务key，业务状态，流程实例id)
        return businessStatusService.updateState(req.getBusinessKey(),
                BusinessStatusEnum.PROCESS, processInstance.getProcessInstanceId());
    }


    /**
     * 撤回申请
     */
    @Override
    public Result cancel(String businessKey, String procInstId, String message) {
        // 撤回，删除当前流程实例
        runtimeService.deleteProcessInstance(procInstId,
                UserUtils.getUsername() + " 主动撤回了当前申请：" + message);
        // 删除历史记录
        historyService.deleteHistoricProcessInstance(procInstId);
        historyService.deleteHistoricTaskInstance(procInstId);
        // 更新业务状态：已撤回
        return businessStatusService.updateState(businessKey,
                BusinessStatusEnum.CANCEL, "");
    }

    /**
     * 通过流程实例Id查询审批历史信息
     */
    @Override
    public Result getHistoryInfoList(String procInstId) {
        // 查询流程人工任务历史数据
        List<HistoricTaskInstance> list =
                historyService.createHistoricTaskInstanceQuery()
                        .processInstanceId(procInstId)
                        .orderByTaskCreateTime().asc()
                        .list();
        List<Map<String, Object>> records =
                list.stream().map(historicTaskInstance -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("taskId", historicTaskInstance.getId()); // 任务ID
                    map.put("taskName", historicTaskInstance.getName()); // 任务名称
                    map.put("processInstanceId",
                            historicTaskInstance.getProcessInstanceId()); // 流程实例ID
                    map.put("startTime",
                            DateUtils.format(historicTaskInstance.getStartTime())); // 开始时间
                    map.put("endTime",
                            DateUtils.format(historicTaskInstance.getEndTime())); // 结束时间
                    map.put("status", historicTaskInstance.getEndTime() == null ?
                            "待处理" : "已处理"); // 状态
                    map.put("assignee", historicTaskInstance.getAssignee()); //办理人
                    // 审批意见：撤回原因为空，查询审批意见
                    String message = historicTaskInstance.getDeleteReason();
                    if (StringUtils.isEmpty(message)) {
                        List<Comment> taskComments =
                                taskService.getTaskComments(historicTaskInstance.getId());
                        message = taskComments.stream()
                                .map(comment ->
                                        comment.getFullMessage()).collect(Collectors.joining("。 "));
                    }
                    map.put("message", message);
                    return map;
                }).collect(Collectors.toList());
        return Result.ok(records);
    }

    /**
     * 查询正在运行中的流程实例
     */
    @Override
    public Result getProcInstListRunning(ProcInstREQ req) {
        ProcessInstanceQuery query =
                runtimeService.createProcessInstanceQuery();
        if (StringUtils.isNotEmpty(req.getProcessName())) {
            query.processInstanceNameLikeIgnoreCase("%" +
                    req.getProcessName() + "%");
        }
        if (StringUtils.isNotEmpty(req.getProposer())) {
            query.startedBy(req.getProposer());
        }
        List<ProcessInstance> instanceList =
                query.listPage(req.getFirstResult(), req.getSize());
        // 用于前端显示页面，总记录数
        long total = query.count();
        List<Map<String, Object>> records =
                instanceList.stream().map(instance -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("processInstanceId",
                            instance.getProcessInstanceId()); // 流程实例id
                    map.put("processInstanceName", instance.getName()); // 流程实例名称
                    map.put("processKey", instance.getProcessDefinitionKey());
                    map.put("version", instance.getProcessDefinitionVersion());
                    // 流程定义版本号
                    map.put("proposer", instance.getStartUserId()); // 流程发起人
                    map.put("processStatus", instance.isSuspended() ? "已暂停" :
                            "已启动"); // 流程状态
                    map.put("businessKey", instance.getBusinessKey()); // 业务唯一标识
                    // 查询当前实例任务
                    List<Task> taskList = taskService.createTaskQuery()
                            .processInstanceId(instance.getProcessInstanceId()).list();
                    String currTaskInfo = ""; // 当前任务
                    for (Task task : taskList) {
                        currTaskInfo += "任务名【" + task.getName() + "】，办理人【" + task.getAssignee()
                                + "】<br>";
                    }
                    map.put("currTaskInfo", currTaskInfo);
                    map.put("startTime",
                            DateUtils.format(instance.getStartTime()));
                    return map;
                }).collect(Collectors.toList());
        // 排序
        Collections.sort(records, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> m1, Map<String,
                    Object> m2) {
                String date1 = String.valueOf(m1.get("startTime"));
                String date2 = String.valueOf(m2.get("startTime"));
                return date2.compareTo(date1);
            }
        });
        // 返回数据封装
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("records", records);
        return Result.ok(result);
    }


    /**
     * 挂起或激活单个流程实例
     */
    @Override
    public Result updateProcInstState(String procInstId) {
        // 查询流程实例对象
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(procInstId).singleResult();
        // 获取当前流程实例状态是否为：挂起（暂停）
        boolean suspended = processInstance.isSuspended();
        // 判断
        if (suspended) {
            // 如果状态是：挂起，将状态更新为：激活 (启动）
            runtimeService.activateProcessInstanceById(procInstId);
        } else {
            // 如果状态是：激活，将状态更新为：挂起（暂停）
            runtimeService.suspendProcessInstanceById(procInstId);
        }
        return Result.ok();
    }

    /**
     * 作废（删除）流程实例，不会删除历史记录
     */
    @Override
    public Result deleteProcInst(String procInstId) {
        // 查询流程实例
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                        .processInstanceId(procInstId).singleResult();
        if (instance == null) {
            return Result.error("流程实例不存在");
        }
        // 删除流程实例
        runtimeService.deleteProcessInstance(procInstId, UserUtils.getUsername() + " 作废了当前流程申请");
        // 更新流程业务状态
        return businessStatusService.updateState(instance.getBusinessKey(), BusinessStatusEnum.INVALID);
    }


    /**
     * 查询已结束的流程实例
     */
    @Override
    public Result getProcInstFinish(ProcInstREQ req) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
                        .finished()
                        .orderByProcessInstanceEndTime().desc();
        if (StringUtils.isNotEmpty(req.getProcessName())) {
            query.processInstanceNameLikeIgnoreCase(req.getProcessName());
        }
        if (StringUtils.isNotEmpty(req.getProposer())) {
            query.startedBy(req.getProposer());
        }
        List<HistoricProcessInstance> instanceList =
                query.listPage(req.getFirstResult(), req.getSize());
        // 用于前端显示页面，总记录数
        long total = query.count();
        List<Map<String, Object>> records =
                instanceList.stream().map(instance -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("processInstanceId", instance.getId());
                    map.put("processInstanceName", instance.getName());
                    map.put("processKey", instance.getProcessDefinitionKey());
                    map.put("version", instance.getProcessDefinitionVersion());
                    map.put("proposer", instance.getStartUserId());
                    map.put("businessKey", instance.getBusinessKey());
                    map.put("startTime",
                            DateUtils.format(instance.getStartTime()));
                    map.put("endTime", DateUtils.format(instance.getEndTime()));
                    // 原因
                    map.put("deleteReason", instance.getDeleteReason());
                    // 业务状态
                    BusinessStatus businessStatus =
                            businessStatusService.getById(instance.getBusinessKey());
                    if (businessStatus != null) {
                        map.put("status",
                                BusinessStatusEnum.getEumByCode(businessStatus.getStatus()).getDesc()
                        );
                    }
                    return map;
                }).collect(Collectors.toList());

        // 返回数据封装
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("records", records);
        return Result.ok(result);
    }

    /**
     * 删除流程实例与历史记录
     */
    @Override
    public Result deleteProcInstAndHistory(String procInstId) {
        // 1. 查询历史流程实例
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(procInstId).singleResult();
        // 2. 删除历史流程实例
        historyService.deleteHistoricProcessInstance(procInstId);
        historyService.deleteHistoricTaskInstance(procInstId);
        // 3. 更新流程业务状态, 注意：流程实例id传递一个空字符串""，不要是null,不然无法更新到
        return businessStatusService.updateState(instance.getBusinessKey(), BusinessStatusEnum.DELETE, "");
    }

}
