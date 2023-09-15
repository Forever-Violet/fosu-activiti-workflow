package org.fosu.workflow.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.fosu.workflow.entities.Leave;
import org.fosu.workflow.req.LeaveREQ;
import org.fosu.workflow.service.LeaveService;
import org.fosu.workflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api("请假申请控制层")
@ResponseBody
@RestController
@RequestMapping("/leave")
public class LeaveController {
    @Autowired
    private LeaveService leaveService;
    @ApiOperation("新增请假申请")
    @PostMapping
    public Result add(@RequestBody Leave leave) {
        return leaveService.add(leave);
    }


    @ApiOperation("查询请假申请列表")
    @PostMapping("/list")
    public Result listPage(@RequestBody LeaveREQ req) {
        return leaveService.listPage(req);
    }

    @ApiOperation("查询请假详情信息")
    @GetMapping("/{id}")
    public Result view(@PathVariable String id) {
        Leave leave = leaveService.getById(id);
        return Result.ok(leave);
    }

    @ApiOperation("修改详情信息")
    @PutMapping
    public Result edit(@RequestBody Leave leave) {
        return leaveService.update(leave);
    }

}
