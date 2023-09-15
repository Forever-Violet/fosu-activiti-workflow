package org.fosu.workflow.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.fosu.workflow.req.ProcDefiREQ;
import org.fosu.workflow.service.ProcessDefinitionService;
import org.fosu.workflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.zip.ZipInputStream;

@Api("流程定义管理")
@Slf4j
@RestController
@RequestMapping("/process")
public class ProcessDefinitionController {
    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Autowired
    private RepositoryService repositoryService;

    @ApiOperation("查询部署的流程定义数据列表，如果有多个相同标识key的流程时，只查询其最新版本")
    @PostMapping("/list")
    public Result getProcDefiList(@RequestBody ProcDefiREQ procDefiREQ) {
        return processDefinitionService.getProcDefiList(procDefiREQ);
    }

    @ApiOperation("通过流程定义id，挂起或激活流程定义")
    @PutMapping("/state/{procDefiId}")
    public Result updateProcessDefinitionState(@PathVariable String procDefiId) {
        return processDefinitionService.updateProcDefiState(procDefiId);
    }

    @ApiOperation("根据部署ID删除流程部署信息")
    @DeleteMapping("/{deploymentId}")
    public Result deleteDeployment(@PathVariable String deploymentId,
                                   @RequestParam String key) {
        try {
            return processDefinitionService.deleteDeployment(deploymentId, key);
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();
            log.error("根据部署ID删除流程，异常：{}", message);
            if (StringUtils.contains(message, "a foreign key constraint fails")) {
                return Result.error("有正在执行的流程实例，不允许删除");
            } else {
                return Result.error("删除失败，原因：" + e.getMessage());
            }
        }
    }

    @ApiOperation("导出下载流程文件(.bpmn20.xml流程图或.png图片资源)")
    @GetMapping("/export/{type}/{definitionId}")
    public void exportFile(@PathVariable("type") String type,
                           @PathVariable("definitionId") String definitionId,
                           HttpServletResponse response) {
        try {
            // 查询流程定义数据
            ProcessDefinition processDefinition = repositoryService.getProcessDefinition(definitionId);
            // 文件输入流
            String filename = "文件不存在";
            if ("xml".equals(type)) {
                // xml类型
                response.setContentType("application/xml");
                // xml文件名
                filename = processDefinition.getResourceName();
            } else if ("png".equals(type)) {
                // png类型
                response.setContentType("image/png");
                // png图片名
                filename = processDefinition.getDiagramResourceName();
            }

            // 获取对应文件输入流
            InputStream inputStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), filename);

            // 防止文件名中文乱码，要进行编码
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));

            // 这句必须放到setHeader下面，否则10k以上的无法导出
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            log.error("导出失败：{}", e.getMessage());
        }
    }

    @ApiOperation("上传.zip或.bpmn或.bpmn20.xml流程文件并完成部署")
    @PostMapping("/file/deploy")
    public Result deployByFile(@RequestParam("file") MultipartFile file) {
        try {
            // 文件名+后缀名
            String filename = file.getOriginalFilename();
            // 文件后缀名
            String suffix = filename.substring(filename.lastIndexOf(".") + 1).toUpperCase();

            InputStream inputStream = file.getInputStream();
            DeploymentBuilder deployment = repositoryService.createDeployment();
            if ("ZIP".equals(suffix)) {
                // zip
                deployment.addZipInputStream(new ZipInputStream(inputStream));
            } else {
                // xml或bpm
                deployment.addInputStream(filename, inputStream);
            }
            // 部署名称
            deployment.name(filename.substring(0, filename.lastIndexOf(".")));

            // 开始部署
            deployment.deploy();
            return Result.ok();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("部署失败：" + e.getMessage());
            return Result.error("部署失败");
        }
    }
}
