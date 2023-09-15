package org.fosu.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.fosu.workflow.entities.Leave;
import org.fosu.workflow.req.TaskCompleteREQ;
import org.fosu.workflow.req.TaskREQ;
import org.fosu.workflow.utils.Result;

import java.util.Map;

public interface WaitTaskService {
    /**
     * 查询当前用户是办理人或候选人的待办任务
     * @param req 条件查询任务请求类
     * @return 结果集合
     */
    Map<String, Object> findWaitTask(TaskREQ req);

    /**
     * 查询当前用户是办理人或候选人的已办任务
     * @param req 条件查询任务请求类
     * @return 结果集合
     */
    Map<String, Object> findCompletedTask(TaskREQ req);


    /**
     * 获取目标节点
     * @param taskId 任务id
     * @return 结果数据封装类
     */
    Result getNextNodeInfo(String taskId);

    /**
     * 完成任务
     * @param req 完成任务请求类
     * @return 结果数据封装类
     */
    Result completeTask(TaskCompleteREQ req);

    /**
     * 转办任务
     * @param assigneeUserKey 被转办任务的 人的用户id
     * @param taskId 需要转办的任务id
     * @return 结果数据封装类
     */
    Result turnTaskToOthers(String assigneeUserKey, String taskId);

    /**
     * 根据任务id获取已经执行的任务节点信息
     * @param taskId 任务id
     * @return 结果数据封装类
     */
    Result getRunNodes(String taskId);

    /**
     * 驳回到目标节点
     * @param taskId 任务id
     * @param targetActivityId 目标及诶单id
     * @return Result
     */
    Result backToTargetNode(String taskId, String targetActivityId);




}
