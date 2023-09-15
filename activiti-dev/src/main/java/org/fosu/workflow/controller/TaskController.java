package org.fosu.workflow.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.fosu.workflow.req.TaskCompleteREQ;
import org.fosu.workflow.req.TaskREQ;
import org.fosu.workflow.service.WaitTaskService;
import org.fosu.workflow.utils.Result;
import org.fosu.workflow.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api("任务相关接口")
@Slf4j
@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private WaitTaskService waitTaskService;

    @ApiOperation("查询当前用户是办理人或候选人的待办任务")
    @PostMapping("/list/wait")
    public Result findWaitTask(@RequestBody TaskREQ req) {
        try {
            return Result.ok("查询成功",
                    waitTaskService.findWaitTask(req));
        } catch (Exception e) {
            log.error("根据流程assignee查询当前人的个人任务,异常:{}", e);
            return Result.error("查询失败"+ e.getMessage());
        }
    }

    @ApiOperation("获取目标节点（下一节点）")
    @GetMapping("/next/node")
    public Result getNextNodeInfo(@RequestParam("taskId") String taskId)
    {
        return waitTaskService.getNextNodeInfo(taskId);
    }

    @ApiOperation("完成任务")
    @PostMapping("/complete")
    public Result completeTask(@RequestBody TaskCompleteREQ req) {
        return waitTaskService.completeTask(req);
    }

    @ApiOperation("查询当前用户是办理人的已办任务")
    @PostMapping("/list/complete")
    public Result findCompletedTask(@RequestBody TaskREQ req) {
        try {
            return Result.ok("查询成功", waitTaskService.findCompletedTask(req));
        } catch (Exception e) {
            log.error("根据流程assignee查询当前人的已办任务,异常:{}", e);
            return Result.error("查询失败"+ e.getMessage());
        }
    }

    @ApiOperation("转办当前代办任务给其他人")
    @PostMapping("/turn")
    public Result turnTask(@RequestParam String assigneeUserKey, @RequestParam String taskId) {
        return waitTaskService.turnTaskToOthers(assigneeUserKey, taskId);
    }

    @ApiOperation("根据任务id获取已执行的任务结点信息")
    @GetMapping("/back/nodes")
    public Result getRunNodes(@RequestParam String taskId) {
        try {
            return waitTaskService.getRunNodes(taskId);
        } catch (Exception e) {
            return Result.error("查询失败" + e.getMessage());
        }

    }

    @ApiOperation("驳回任务到目标节点")
    @PostMapping("/back")
    public Result backTaskToTarget(@RequestParam String taskId, @RequestParam String targetActivityId) {
        try {
            return waitTaskService.backToTargetNode(taskId, targetActivityId);
        } catch (Exception e) {
            log.error("完成任务,异常:{}", e.getMessage());
            return Result.error("驳回任务失败: " + e.getMessage());
        }
    }



}
