package org.fosu.workflow.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.apache.commons.lang3.StringUtils;
import org.fosu.workflow.req.ModelAddREQ;
import org.fosu.workflow.req.ModelREQ;
import org.fosu.workflow.service.ModelService;
import org.fosu.workflow.utils.DateUtils;
import org.fosu.workflow.utils.Result;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ModelServiceImpl extends ActivitiService implements ModelService {
    /** 新增模型基本信息 */
    @Override
    public Result add(ModelAddREQ req) throws Exception {
        // 初始化一个空模型
        Model model = repositoryService.newModel();
        model.setName(req.getName());
        model.setKey(req.getKey());
        model.setVersion(0);

        // 封装模型json对象
        ObjectNode modelObjectNode = objectMapper.createObjectNode();
        modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, req.getName());
        modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 0);
        modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, req.getDescription());
        model.setMetaInfo(modelObjectNode.toString());
        // 存储模型对象(表ACT_RE_MODEL)
        repositoryService.saveModel(model);

        // 封装模型对象基础数据
        // {"id":"canvas","resourceId":"canvas","stencilset":{"namespace":"http://b3mn.org/stencilset/bpmn2.0#"},"properties":{"process_id":"未定义"}}
        ObjectNode editorNode = objectMapper.createObjectNode();
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.replace("stencilset", stencilSetNode);
        // 标识key
        ObjectNode propertiesNode = objectMapper.createObjectNode();
        propertiesNode.put("process_id", req.getKey());
        editorNode.replace("properties", propertiesNode);

        // 存储模型对象基础数据(表ACT_GE_BYTEARRAY)
        repositoryService.addModelEditorSource(model.getId(), editorNode.toString().getBytes("utf-8"));
        return Result.ok(model.getId());
    }

    /** 条件分页查询流程定义模型列表数据 */
    @Override
    public Result getModelList(ModelREQ req) {
        ModelQuery query = repositoryService.createModelQuery();
        if (StringUtils.isNoneEmpty(req.getName())) {
            query.modelNameLike("%" + req.getName() + "%");
        }

        if (StringUtils.isNotEmpty(req.getKey())) {
            query.modelKey(req.getKey());
        }

        // 按创建时间降序排序
        query.orderByCreateTime().desc();
        // 开始查询(第几条开始，查询多少条)
        List<Model> modelList = query.listPage(req.getFirstResult(), req.getSize());

        // 用于前端显示页面，总记录数
        long total = query.count();

        // 转换结果给前端展示
        List<Map<String, Object>> records =  modelList.stream().map(model -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", model.getId());  // 模型id
            map.put("name", model.getName());  // 模型名称
            map.put("key", model.getKey());  // 模型key
            map.put("version", model.getVersion());  // 版本号
            // 模型描述
            String desc = JSONObject.parseObject(model.getMetaInfo()).getString(ModelDataJsonConstants.MODEL_DESCRIPTION);
            map.put("description", desc);
            // 创建时间
            map.put("createDate", DateUtils.format(model.getCreateTime()));
            // 更新时间
            map.put("updateDate", DateUtils.format(model.getLastUpdateTime()));
            return map;
        }).collect(Collectors.toList());

        // 封装响应结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("records", records);
        return Result.ok(result);
    }

    /** 通过流程定义模型ID部署流程定义 */
    @Override
    public Result deploy(String modelId) throws Exception {
        // 1.查询流程定义模型json字节码
        byte[] jsonBytes = repositoryService.getModelEditorSource(modelId);
        if (jsonBytes == null) {
            return Result.error("模型数据为空，请先设计流程定义模型，再进行部署");
        }
        // 将json字节码转为xml字节码
        byte[] xmlBytes = bpmnJsonXmlBytes(jsonBytes);
        if (xmlBytes == null) {
            return Result.error("数据模型不符合要求，请至少涉及一条主线流程");
        }
        // 2.查询流程定义模型的图片
        byte[] pngBytes = repositoryService.getModelEditorSourceExtra(modelId);

        // 查询模型的基本信息
        Model model = repositoryService.getModel(modelId);

        // xml资源的名称，对应act_ge_bytearray表中的name_字段
        String processName = model.getName() + ".bpmn20.xml";
        // 图片资源名称，对应act_ge_bytearray表中的name_字段
        String pngName = model.getName() + "." + model.getKey() + ".png";

        // 3.调用部署相关的api方法进行部署流程定义
        Deployment deployment = repositoryService.createDeployment()
                .name(model.getName())  // 部署名称
                .addString(processName, new String(xmlBytes, "UTF-8"))  // bpmn20.xml资源
                .addBytes(pngName, pngBytes)  // png资源
                .deploy();

        // 更新部署id到流程定义模型数据表中
        model.setDeploymentId(deployment.getId());
        repositoryService.saveModel(model);

        return Result.ok();
    }

    /** 导出模型图zip压缩包(.bpmn20.xml和.png图片资源) */
    @Override
    public void exportZip(String modelId, HttpServletResponse response) {
        ZipOutputStream zipos = null;
        try {
            // 实例化zip压缩对象输出流
            zipos = new ZipOutputStream(response.getOutputStream());
            // 压缩包文件名
            String zipName = "模型不存在";

            // 1.查询模型基本信息
            Model model = repositoryService.getModel(modelId);
            if (model != null) {
                // 2.查询流程定义模型的json字节码
                byte[] bpmnJsonBytes = repositoryService.getModelEditorSource(modelId);
                // 2.1 将json字节码转换为xml字节码
                byte[] xmlBytes = bpmnJsonXmlBytes(bpmnJsonBytes);
                if (xmlBytes == null) {
                    zipName = "模型数据为空-请先设计流程定义模型，在导出";
                } else {
                    // 压缩包文件名
                    zipName = model.getName() + "(" + model.getKey() + ")";
                    // 将xml添加到压缩包中(指定xml文件名：请假流程.bpmn20.xml)
                    zipos.putNextEntry(new ZipEntry(model.getName() + ".bpmn20.xml"));
                    // 将xml写入压缩流
                    zipos.write(xmlBytes);

                    // 3.查询流程定义模型的图片字节码
                    byte[] pngBytes = repositoryService.getModelEditorSourceExtra(modelId);
                    if (pngBytes != null) {
                        // 图片文件名(请假流程.leaveProcess.png)
                        zipos.putNextEntry(
                                new ZipEntry(model.getName() + "." + model.getKey() + ".png")
                        );
                        zipos.write(pngBytes);
                    }
                }
            }

            response.setContentType("application/octet-stream");
            // 防止中文名乱码，要编码
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(zipName, "UTF-8") + ".zip");
            // 刷出相应流
            response.flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zipos != null) {
                try {
                    zipos.closeEntry();
                    zipos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** 删除流程定义模型 */
    @Override
    public void deleteModel(String modelId) {
        repositoryService.deleteModel(modelId);
    }

    private byte[] bpmnJsonXmlBytes(byte[] jsonBytes) throws IOException {
        if (jsonBytes == null) {
            return null;
        }

        // 1.json字节码转成BpmnModel对象
        JsonNode jsonNode = objectMapper.readTree(jsonBytes);
        BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(jsonNode);

        if (bpmnModel.getProcesses().size() == 0) {
            return null;
        }

        // 2.BpmnModel对象转为xml字节码
        byte[] xmlBytes = new BpmnXMLConverter().convertToXML(bpmnModel);
        return xmlBytes;
    }
}
