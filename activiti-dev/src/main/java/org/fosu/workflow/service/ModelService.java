package org.fosu.workflow.service;

import org.fosu.workflow.req.ModelAddREQ;
import org.fosu.workflow.req.ModelREQ;
import org.fosu.workflow.utils.Result;

import javax.servlet.http.HttpServletResponse;

/**
 * 流程定义模型管理
 */
public interface ModelService {
    /**
     * 新增模型基本信息
     * @param req 模型基本信息实体类
     * @return 结果封装类
     * @throws Exception 抛出异常
     */
    Result add(ModelAddREQ req) throws Exception;

    /**
     * 条件分页查询流程定义模型列表数据
     * @param req 查询条件实体类
     * @return 结果封装类
     */
    Result getModelList(ModelREQ req);

    /**
     * 通过流程定义模型ID部署流程定义
     * @param modelId 流程定义模型ID
     * @return 结果封装类
     * @throws Exception
     */
    Result deploy(String modelId) throws Exception;

    /**
     * 导出模型图zip压缩包(.bpmn20.xml和.png图片资源)
     * @param modelId 流程定义模型ID
     * @param response 响应浏览器
     */
    void exportZip(String modelId, HttpServletResponse response);

    /**
     * 删除流程定义模型
     * @param modelId 流程定义模型id
     */
    void deleteModel(String modelId);
}
