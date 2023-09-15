package org.fosu.workflow.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.fosu.workflow.req.ModelAddREQ;
import org.fosu.workflow.req.ModelREQ;
import org.fosu.workflow.service.ModelService;
import org.fosu.workflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Api("流程定义模型管理")
@Slf4j
@RestController
@RequestMapping("/model")
public class ModelController {
    @Autowired
    private ModelService modelService;

    @ApiOperation("新增流程定义模型数据")
    @PostMapping
    public Result add(@RequestBody ModelAddREQ req) {
        try {
            return modelService.add(req);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("创建模型失败：" + e.getMessage());
            return Result.error("创建模型失败");
        }
    }

    @ApiOperation("条件分页查询流程定义模型列表数据")
    @PostMapping("/list")
    public Result modelList(@RequestBody ModelREQ req) {
        try {
            return modelService.getModelList(req);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询流程定义模型列表数据失败：" + e.getMessage());
            return Result.error("查询模型列表失败");
        }
    }

    @ApiOperation("通过流程定义模型ID部署流程定义")
    @PostMapping("/deploy/{modelId}")
    public Result deploy(@PathVariable("modelId") String modelId) {
        try {
            return modelService.deploy(modelId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("通过流程定义模型ID部署流程定义失败：" + e.getMessage());
            return Result.error("部署流程定义失败");
        }
    }

    @ApiOperation("导出流程定义模型zip压缩包")
    @GetMapping("/export/zip/{modelId}")
    public void exportZip(@PathVariable("modelId") String modelId, HttpServletResponse response) {
        modelService.exportZip(modelId, response);
    }

    @ApiOperation("删除流程定义模型")
    @DeleteMapping("{modelId}")
    public Result deleteModel(@PathVariable("modelId") String modelId) {
        modelService.deleteModel(modelId);
        return Result.ok();
    }
}
