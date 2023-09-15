package org.fosu.workflow.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.fosu.workflow.entities.ProcessConfig;
import org.fosu.workflow.service.ProcessConfigService;
import org.fosu.workflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api("流程配置控制层")
@RequestMapping("/processConfig")
@RestController
public class ProcessConfigController {
    @Autowired
    private ProcessConfigService processConfigService;

    @ApiOperation("根据流程key查询流程配置")
    @GetMapping("/{processKey}")
    public Result view(@PathVariable String processKey) {
        return Result.ok(processConfigService.getByProcessKey(processKey));
    }

    @ApiOperation("新增或更新流程配置")
    @PutMapping
    public Result saveOrUpdate(@RequestBody ProcessConfig processConfig) {
        boolean flag = processConfigService.saveOrUpdate(processConfig);
        if (flag) {
            return Result.ok();
        } else {
            return Result.error("操作失败");
        }
    }
}
