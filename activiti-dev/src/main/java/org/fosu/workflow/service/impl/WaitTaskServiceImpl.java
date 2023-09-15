package org.fosu.workflow.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.Lists;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.bpmn.model.*;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.fosu.workflow.cmd.DeleteExecutionCommand;
import org.fosu.workflow.cmd.DeleteTaskCommand;
import org.fosu.workflow.enums.BusinessStatusEnum;
import org.fosu.workflow.req.TaskCompleteREQ;
import org.fosu.workflow.req.TaskREQ;
import org.fosu.workflow.service.BusinessStatusService;
import org.fosu.workflow.service.WaitTaskService;
import org.fosu.workflow.utils.DateUtils;
import org.fosu.workflow.utils.Result;
import org.fosu.workflow.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class WaitTaskServiceImpl implements WaitTaskService {

    @Resource
    TaskService taskService;

    @Resource
    RuntimeService runtimeService;

    @Resource
    RepositoryService repositoryService;

    @Autowired
    private BusinessStatusService businessStatusService;

    @Resource
    HistoryService historyService;

    @Resource
    TaskRuntime taskRuntime;

    @Resource
    ManagementService managementService;


    /**
     * 查询当前用户是办理人或候选人的待办任务
     */
    @Override
    public Map<String, Object> findWaitTask(TaskREQ req) {
        // 办理人（当前用户）
        String assignee = UserUtils.getUsername();
        TaskQuery query = taskService.createTaskQuery()
                .taskCandidateOrAssigned(assignee) // 作为办理人或候选人
                .orderByTaskCreateTime().desc();
        if (StringUtils.isNotEmpty(req.getTaskName())) {
            query.taskNameLike("%" + req.getTaskName() + "%");
        }
        List<Task> taskList = query.listPage(req.getFirstResult(), req.getSize());
        // 用于前端显示页面，总记录数
        long total = query.count();
        List<Map<String, Object>> records = taskList.stream().map(task ->
        {
            Map<String, Object> map = new HashMap<>();
            map.put("taskId", task.getId()); // 任务ID
            map.put("taskName", task.getName()); // 任务名称
            map.put("processStatus", task.isSuspended() ? "已暂停" : "已启动");
            map.put("taskCreateTime", DateUtils.format(task.getCreateTime())); // 任务的创建时间
            map.put("taskAssignee", task.getAssignee()); // 任务的办理人：为null，说明当前是候选人，不是办理人，要先签收
            map.put("processInstanceId", task.getProcessInstanceId());
            // 流程实例ID
            map.put("executionId", task.getExecutionId()); // 执行对象ID
            map.put("processDefinitionId", task.getProcessDefinitionId()); // 流程定义ID
            // 查询流程实例
            ProcessInstance processInstance =
                    runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            map.put("businessKey", processInstance.getBusinessKey()); //业务唯一标识
            map.put("proposer", processInstance.getStartUserId()); // 获取发起人
            map.put("processName", processInstance.getProcessDefinitionName()); // 流程名称
            map.put("version", processInstance.getProcessDefinitionVersion()); // 版本号
            return map;
        }).collect(Collectors.toList());
        // 返回数据封装
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("records", records);
        return result;
    }

    /**
     * 查询当前用户是办理人的已办任务
     */
    @Override
    public Map<String, Object> findCompletedTask(TaskREQ req) {
        String assignee = UserUtils.getUsername(); // 当前用户 (办理人

        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(assignee) // 作为办理人
                .orderByTaskCreateTime().desc()
                .finished();
        if (StringUtils.isNotEmpty(req.getTaskName())) {
            query.taskNameLike("%" + req.getTaskName() + "%");
        }
        List<HistoricTaskInstance> taskList = query.listPage(req.getFirstResult(), req.getSize());
        // 用于前端显示页面，总记录数
        long total = query.count();
        List<Map<String, Object>> records = taskList.stream().map(task -> {
            Map<String, Object> map = new HashMap<>();
            map.put("taskId", task.getId()); // 任务ID
            map.put("taskName", task.getName()); // 任务名称
            map.put("taskStartTime", DateUtils.format(task.getStartTime())); // 任务的开始时间
            map.put("taskEndTime", DateUtils.format(task.getEndTime())); //任务的结束时间
            map.put("taskAssignee", task.getAssignee()); // 任务的办理人
            map.put("processInstanceId", task.getProcessInstanceId());// 流程实例ID
            map.put("executionId", task.getExecutionId()); // 执行对象ID
            map.put("processDefinitionId", task.getProcessDefinitionId()); // 流程定义ID
            // 查询流程实例
            HistoricProcessInstance historicProcessInstance =
                    historyService.createHistoricProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            map.put("businessKey", historicProcessInstance.getBusinessKey()); //业务唯一标识
            map.put("proposer", historicProcessInstance.getStartUserId()); // 获取发起人
            map.put("processName", historicProcessInstance.getProcessDefinitionName()); // 流程名称
            map.put("version", historicProcessInstance.getProcessDefinitionVersion()); // 版本号
            return map;
        }).collect(Collectors.toList());
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", total);
        resultMap.put("records", records);
        return resultMap;
    }

    /**
     * 获取目标节点
     */
    @Override
    public Result getNextNodeInfo(String taskId) {
        Task task =
                taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return Result.error("任务不存在");
        }
        // 获取当前模型
        BpmnModel bpmnModel =
                repositoryService.getBpmnModel(task.getProcessDefinitionId());
        // 根据任务节点id获取当前节点
        FlowElement flowElement =
                bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        // 封装下一个节点信息
        List<Map<String, Object>> nextNodes = new ArrayList<>();
        getNextNodes(flowElement, nextNodes);
        return Result.ok(nextNodes);
    }

    /**
     * 判断当前节点的下一节点是人工任务的集合
     *
     * @param flowElement 当前节点
     * @param nextNodes   下节点名称集合
     */
    private void getNextNodes(FlowElement flowElement, List<Map<String, Object>> nextNodes) {
        // 获取当前节点的连线信息
        List<SequenceFlow> outgoingFlows = ((FlowNode)
                flowElement).getOutgoingFlows();
        outgoingFlows.stream().forEach(outgoingFlow -> {
            // 下一节点
            FlowElement nextFlowElement =
                    outgoingFlow.getTargetFlowElement();
            if (nextFlowElement instanceof EndEvent) {
                return;
            } else if (nextFlowElement instanceof UserTask) {
                Map<String, Object> node = new HashMap<>();
                // 用户任务，获取节点id和名称
                node.put("id", nextFlowElement.getId());
                node.put("name", nextFlowElement.getName());
                nextNodes.add(node);
            } else if (nextFlowElement instanceof ParallelGateway // 并行网关
                    || nextFlowElement instanceof ExclusiveGateway) {
                // 互斥网关
                getNextNodes(nextFlowElement, nextNodes);
            }
        });
    }

    /**
     * 完成任务
     */
    @Override
    public Result completeTask(TaskCompleteREQ req) {
        // 任务id
        String taskId = req.getTaskId();
        // 1.查询任务信息
        org.activiti.api.task.model.Task task = taskRuntime.task(taskId);
        if (task == null) {
            return Result.error("任务不存在或不是您办理的任务");
        }
        String procInstId = task.getProcessInstanceId();
        // 2. 指定任务审批意见
        taskService.addComment(taskId, procInstId, req.getMessage());
        // 3.完成任务
        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(taskId).build());
        // 4.查询下一个任务
        List<Task> taskList =
                taskService.createTaskQuery().processInstanceId(procInstId).list();
        // 5. 指定办理人
        if (CollectionUtils.isEmpty(taskList)) {
            // 1.获取流程实例拿到业务id
            // 注意新api的Task对象有businessKey属性，但是没有值
            // 并且是整个流程结束，所以查询历史实例获取businessKey
            HistoricProcessInstance historicProcessInstance =
                    historyService.createHistoricProcessInstanceQuery().processInstanceId(procInstId).singleResult();
            // 2.更新业务状态
            return businessStatusService.updateState(historicProcessInstance.getBusinessKey(), BusinessStatusEnum.FINISH);
        } else {
            // 有下一个人工任务
            Map<String, String> assigneeMap = req.getAssigneeMap();
            if (assigneeMap == null) {
                // 没有分配处理人，直接结束删除流程实例
                return deleteProcessInstance(procInstId);
            }
            // 针对每个任务分配审批人
            for (Task t : taskList) {
                // 当前任务有审批人，则不设置新的审批人
                if (StringUtils.isNotEmpty(t.getAssignee())) {
                    continue;
                }
                // 任务的节点id，获取对应审批人
                String[] assignees =
                        req.getAssignees(t.getTaskDefinitionKey());
                if (ArrayUtils.isEmpty(assignees)) {
                    // 如果下个节点未分配审批人：结束删除流程实例
                    return deleteProcessInstance(procInstId);
                }
                // 分配第一个任务办理人
                if (assignees.length == 1) {
                    // 一个审批人，直接作为办理人
                    taskService.setAssignee(t.getId(), assignees[0]);
                } else {
                    // 多个审批人，作为候选人
                    for (String assignee : assignees) {
                        taskService.addCandidateUser(t.getId(), assignee);
                    }
                }
            }
        }
        return Result.ok();
    }

    private Result deleteProcessInstance(String procInstId) {
        // 1.删除流程实例
        runtimeService.deleteProcessInstance(procInstId, "审批节点未分配审批人，流程自动中断取消");
        // 2.通过任务对象获取流程实例

        HistoricProcessInstance historicProcessInstance =
                historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(procInstId).singleResult();
        // 3.更新业务状态
        businessStatusService.updateState(historicProcessInstance.getBusinessKey(), BusinessStatusEnum.CANCEL);
        return Result.ok("审批节点未分配审批人，流程自动中断取消");
    }

    @Override
    public Result turnTaskToOthers(String assigneeUserKey, String taskId) {
        // 当前办理人
        String assignee = UserUtils.getUsername();
        // 查询当前办理人是否为当前任务的负责人, 如果是才能转办
        Task task = taskService.createTaskQuery()
                .taskId(taskId).taskAssignee(assignee).singleResult();
        if (task != null) {
            // 执行任务转办操作,  任务id , 被转办人id
            taskService.setAssignee(taskId, assigneeUserKey);
            String message = String.format("%s 转办任务【%s】给 %s 办理", UserUtils.getUsername(), task.getName(), assigneeUserKey);
            taskService.addComment(taskId, task.getProcessInstanceId(), message);
            return Result.ok(taskId);
        }
        return Result.error("您不是当前任务的办理人，无法转办该任务");
    }

    @Override  //
    public Result getRunNodes(String taskId) {
        // 根据任务id 获取 流程实例id
        // 先获取任务
        Task task = taskService.createTaskQuery().taskId(taskId).taskAssignee(UserUtils.getUsername()).singleResult();
        if (task != null) {
/*            // 从任务 获取流程实例id
            String processInstanceId = task.getProcessInstanceId();

            List<HistoricActivityInstance> historicActivityInstanceList =
                    historyService.createHistoricActivityInstanceQuery()
                            .processInstanceId(processInstanceId) // 流程id
                            .finished() //已经执行的任务结点
                            .orderByHistoricActivityInstanceEndTime()
                            .asc()
                            .list();
            //  // 已经执行的节点Id集合
            List<Map<String, String>> mapList = historicActivityInstanceList.stream().map(item -> {
                // 遍历 添加到map  list中
                Map<String, String> map = new HashMap<>();
                // 节点id
                map.put("activityId", item.getActivityId());
                // 节点名称
                map.put("activityName", item.getActivityName());
                return map;
            }).collect(Collectors.toList());
            return Result.ok(mapList);*/
            // 查询已完成历史任务节点，且去重，
            // 注意：必须带任务id_值，不然查询出来的全部相同数据。而且原id每条数据都是不相同，没法去重，因此使用rand()随机数作为id
            String sql = "select rand() AS ID_, t2.* from ( select distinct t1.TASK_DEF_KEY_, t1.NAME_ from " +
                    " ( select ID_, RES.TASK_DEF_KEY_, RES.NAME_, RES.START_TIME_, RES.END_TIME_ from " +
                    " ACT_HI_TASKINST RES " +
                    " WHERE RES.PROC_INST_ID_ = #{processInstanceId} and TASK_DEF_KEY_ != #{taskDefKey}" +
                    " and RES.END_TIME_ is not null order by RES.START_TIME_ asc) t1 ) t2";
            List<HistoricTaskInstance> historicTaskInstanceList =
                    historyService.createNativeHistoricTaskInstanceQuery()
                            .sql(sql)
                            .parameter("processInstanceId", task.getProcessInstanceId())
                            .parameter("taskDefKey", task.getTaskDefinitionKey())
                            .list();

            List<Map<String, Object>> records =
                    historicTaskInstanceList.stream().map(historicTaskInstance -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("activityId", historicTaskInstance.getTaskDefinitionKey()); // 节点id
                        map.put("activityName", historicTaskInstance.getName()); //节点名称
                        return map;
                    }).collect(Collectors.toList());
            return Result.ok(records);

        }
        return Result.error("任务不存在或您不是该任务办理人");
    }

    @Override
    public Result backToTargetNode(String taskId, String targetActivityId) {
        Task task = taskService.createTaskQuery().taskId(taskId)
                .taskAssignee(UserUtils.getUsername())
                .singleResult();
        // 判断当前用户是否为该节点处理人
        if (task == null) {
            return Result.error("当前用户不是该节点办理人");
        }
        String procInstId = task.getProcessInstanceId();
        // 1.获取流程模型实例
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        // 2.获取当前节点信息(强类型转换FlowNode获取连线)
        FlowNode curFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());

        // 3.获取当前节点的原出口连线
        List<SequenceFlow> sequenceFlowList = curFlowNode.getOutgoingFlows();
        // 4.临时存储原出口连线（用于驳回后还原回来）
        ArrayList<SequenceFlow> oriSequenceFlows = new ArrayList<>(sequenceFlowList);
        // 5.清空原出口连心
        sequenceFlowList.clear();
        // 6.获取驳回的目标节点信息
        FlowNode targetFlowNode = (FlowNode) bpmnModel.getFlowElement(targetActivityId);
        // 7. 获取驳回的新流向节点
        // 通过获取目标节点的入口连线， 通过入口连线获取目标节点的父节点，看父节点有几个出口连线，
        // 如果父节点是并行或包含网关，出口连线就会有多条，否则一般是一条出口连线，此时出口连线的所有节点才是驳回的真实节点，
        // 目的：解决并行网关多个出口情况，不使用驳回父节点找子节点，当驳回到并行网关下的任务节点时会汇聚不了直接结束。
        List<SequenceFlow> incomingFlows =
                targetFlowNode.getIncomingFlows();
        // 存储获取驳回的新的流向
        List<SequenceFlow> allSequenceFlow = new ArrayList<>();
        incomingFlows.stream().forEach(incomingFlow -> {
            // 找到入口连线的源头（获取目标节点的父节点）
            FlowNode source = (FlowNode)
                    incomingFlow.getSourceFlowElement();
            // 获取目标节点的父组件的所有出口，
            List<SequenceFlow> sequenceFlows;
            if (source instanceof ParallelGateway) { // 并行网关
                // 并行网关的出口有多条连线:根据目标入口连线的父节点的出口连线，其所有出口连线才是驳回的真实节点
                sequenceFlows = source.getOutgoingFlows();
            } else {
                // 其他类型，将目标入口作为当前节点的出口
                sequenceFlows = targetFlowNode.getIncomingFlows();
            }
            // 找到后把它添加到集合作为新方向
            allSequenceFlow.addAll(sequenceFlows);
        });
        // 8.将新方向设置为当前节点出口
        curFlowNode.setOutgoingFlows(allSequenceFlow);
        // 9.查询当前流程实例的所有任务，进行完成和删除任务，删除执行数据表is_active_=0的数据
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(procInstId).list();
        taskList.stream().forEach(t -> {
            // 如果是当前节点任务(并行网关后面多个任务)，完成任务让流程流向新的驳回节点

            if (taskId.equals(t.getId())) {
                // 保存记录『admin 驳回任务【行政审批】→【部门领导审批】』
                String message = String.format("『%s 驳回任务 【%s】→【%s】』",
                        task.getAssignee(),
                        curFlowNode.getName(),
                        targetFlowNode.getName());
                taskService.addComment(t.getId(), procInstId, message);
                // 完成任务（驳回完成）
                taskService.complete(t.getId());
                // 完成的这条任务在act_ru_execution表中对应的执行数据，is_active_ 会更新为0，
                // 更新后，会影响并行任务节点操作驳回，然后又流向回来时并行任务完成一个就会往后走，不会等其他并行任务完成，
                // 因为并行汇聚是通过判断并行任务的执行数据 is_active_=0 ，所以要把当前is_active_=0 的数据删除
                DeleteExecutionCommand deleteExecutionCMD = new DeleteExecutionCommand(task.getExecutionId());
                managementService.executeCommand(deleteExecutionCMD);
            } else {
                // 删除非当前节点任务
                DeleteTaskCommand deleteTaskCMD = new DeleteTaskCommand(t.getId());
                managementService.executeCommand(deleteTaskCMD);
            }
        });
        // 10.实现驳回功能后，将当前节点方向恢复为原来正常的方向
        curFlowNode.setOutgoingFlows(oriSequenceFlows);
        // 11.查询驳回后的新任务
        List<Task> newTaskList = taskService.createTaskQuery()
                .processInstanceId(task.getProcessInstanceId())
                .list();
        // 12.设置新任务执行人
        newTaskList.stream().forEach(newTask -> {
            // 取得之前目标节点的原执行人
            HistoricTaskInstance oldTargetTask =
                    historyService.createHistoricTaskInstanceQuery()
                            .taskDefinitionKey(newTask.getTaskDefinitionKey()) // 同一节点
                            .processInstanceId(task.getProcessInstanceId())
                            .finished() // 已完成任务
                            .orderByTaskCreateTime().desc() // 最新办理的前面
                            .list().get(0);
            taskService.setAssignee(newTask.getId(), oldTargetTask.getAssignee());
        });
        return Result.ok();
    }

    /*    @Override// 
    public Result backToTargetNode(String taskId, String targetActivityId) {
        String assignee = UserUtils.getUsername();
        // 获取当前任务实例
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (assignee.equals(task.getAssignee())) { //如果办理人匹配
            // 获取当前结点
            String currentActivityId = task.getTaskDefinitionKey();
            String processDefinitionId = task.getProcessDefinitionId();
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
            FlowNode currentFlow = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currentActivityId);

            if (null == currentFlow) {
                List<SubProcess> subProcessList = bpmnModel.getMainProcess().findFlowElementsOfType(SubProcess.class, true);
                for (SubProcess subProcess : subProcessList) {
                    FlowElement flowElement = subProcess.getFlowElement(currentActivityId);
                    if (flowElement != null) {
                        currentFlow = (FlowNode) flowElement;
                        break;
                    }
                }
            }

            // 获取目标节点
            FlowNode targetFlow = (FlowNode) bpmnModel.getFlowElement(targetActivityId);

            // 如果不是同一个流程 不能驳回
            if (!(currentFlow.getParentContainer().equals(targetFlow.getParentContainer()))) {
                return Result.error("驳回失败");
            }

            // 记录原活动方向
            List<SequenceFlow> originSequenceFlows = Lists.newArrayList();
            originSequenceFlows.addAll(currentFlow.getOutgoingFlows());

            // 清理活动方向
            currentFlow.getOutgoingFlows().clear();

            // 建立新的方向
            List<SequenceFlow> newSequenceFlows = Lists.newArrayList();
            SequenceFlow newSequenceFlow = new SequenceFlow();
            String uuid = UUID.randomUUID().toString().replace("-", "");
            newSequenceFlow.setId(uuid);
            newSequenceFlow.setSourceFlowElement(currentFlow);  //原节点
            newSequenceFlow.setTargetFlowElement(targetFlow);  //目标节点
            newSequenceFlows.add(newSequenceFlow);
            currentFlow.setOutgoingFlows(newSequenceFlows);

            //完成节点任务
            taskService.complete(task.getId());
            //恢复原方向
            currentFlow.setOutgoingFlows(originSequenceFlows);
            return Result.ok("驳回成功");
        }
        return Result.error("您不是当前任务的办理人，无权驳回该任务");
    }*/
}
