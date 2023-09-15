package org.fosu.workflow.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.fosu.workflow.service.SysUserService;
import org.fosu.workflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api("用户信息控制层")
@Slf4j
@RestController
@RequestMapping("/user")
public class SysUserController {
    @Autowired
    private SysUserService sysUserService;

    @ApiOperation("随机查询10条用户信息")
    @GetMapping("/all")
    public Result all() {
        return Result.ok(sysUserService.getTenUsers());
    }
}
