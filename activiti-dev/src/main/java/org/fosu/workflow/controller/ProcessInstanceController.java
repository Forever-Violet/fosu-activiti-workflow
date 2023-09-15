package org.fosu.workflow.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.fosu.workflow.req.ProcInstREQ;
import org.fosu.workflow.req.StartREQ;
import org.fosu.workflow.service.ProcessInstanceService;
import org.fosu.workflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @author 12508
 */
@Api("流程实例管理")
@Slf4j
@RestController
@RequestMapping("/instance")
public class ProcessInstanceController {

    @Autowired
    private ProcessInstanceService processInstanceService;

    @ApiOperation("通过实例ID获取申请表单组件名")
    @GetMapping("/form/name/{procInstId}")
    public Result getFormName(@PathVariable String procInstId) {
        return processInstanceService.getFormNameByProcInstId(procInstId);
    }

    @ApiOperation("获取流程示例图审批历史图")
    @GetMapping("/history/image")
    public void getHistoryProcessImage(@RequestParam String procInstId, HttpServletResponse response) {
        processInstanceService.getHistoryProcessImage(procInstId, response);
    }


    @ApiOperation(value = "提交申请，启动流程实例")
    @PostMapping("/start")
    public Result start(@RequestBody StartREQ req) {
        return processInstanceService.startProcess(req);
    }

    @ApiOperation("撤回申请")
    @DeleteMapping("/cancel/apply")
    public Result cancelApply(@RequestParam String businessKey,
                              @RequestParam String procInstId,
                              @RequestParam(defaultValue = "撤回成功")
                              String message) {
        return processInstanceService.cancel(businessKey, procInstId, message);
    }

    @ApiOperation("根据流程实例ID查询审批历史记录")
    @GetMapping("/history/list")
    public Result historyInfoList(@RequestParam String procInstId) {
        return processInstanceService.getHistoryInfoList(procInstId);
    }

    @ApiOperation("查询正在运行中的流程实例")
    @PostMapping("/list/running")
    public Result getProcInstListRunning(@RequestBody ProcInstREQ req) {
        return processInstanceService.getProcInstListRunning(req);
    }

    @ApiOperation("挂起或激活单个流程实例")
    @PutMapping("/state/{procInstId}")
    public Result updateProcInstState(@PathVariable("procInstId") String procInstId) {
        return processInstanceService.updateProcInstState(procInstId);
    }

    @ApiOperation("作废（删除）流程实例，不会删除历史记录")
    @DeleteMapping("/{procInstId}")
    public Result deleteProcInst(@PathVariable("procInstId") String procInstId) {
        return processInstanceService.deleteProcInst(procInstId);
    }

    @ApiOperation("查询已结束的流程实例")
    @PostMapping("/list/finish")
    public Result getProcInstFinish(@RequestBody ProcInstREQ req) {
        return processInstanceService.getProcInstFinish(req);
    }

    @ApiOperation("删除已结束流程实例和历史记录")
    @DeleteMapping("/history/{procInstId}")
    public Result deleteProcInstAndHistory(@PathVariable String procInstId) {
        return processInstanceService.deleteProcInstAndHistory(procInstId);
    }

}
