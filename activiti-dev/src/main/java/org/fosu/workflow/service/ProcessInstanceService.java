package org.fosu.workflow.service;

import org.fosu.workflow.req.ProcInstREQ;
import org.fosu.workflow.req.StartREQ;
import org.fosu.workflow.utils.Result;

import javax.servlet.http.HttpServletResponse;

public interface ProcessInstanceService {

    /**
     * 提交申请启动流程实例
     * @param req 启动流程请求类
     * @return 数据封装实体类
     */
    Result startProcess(StartREQ req);

    /**
     * 通过流程实例id获取申请表单
     * @param procInstId 流程实例id
     * @return 数据封装实体类
     */
    Result getFormNameByProcInstId(String procInstId);

    /**
     * 获取流程实例审批历史图
     * @param procInstId 流程实例id
     * @param response 浏览器相应类
     */
    void getHistoryProcessImage(String procInstId, HttpServletResponse response);

    /**
     * 撤回申请
     * @param businessKey 业务id
     * @param procInstId 流程实例id
     * @param message 撤回原因
     * @return 数据封装实体类
     */
    Result cancel(String businessKey, String procInstId, String message);

    /**
     * 通过流程实例Id查询审批历史信息
     * @param procInstId 流程实例id
     * @return 数据封装实体类
     */
    Result getHistoryInfoList(String procInstId);


    /**
     * 查询正在运行中的流程实例
     * @param req 流程实例条件请求类
     * @return 数据封装实体类
     */
    Result getProcInstListRunning(ProcInstREQ req);

    /**
     * 挂起或激活单个流程实例
     * @param procInstId 流程id
     * @return 数据封装实体类
     */
    Result updateProcInstState(String procInstId);

    /**
     * 作废（删除）流程实例，不会删除历史记录
     * @param procInstId 流程id
     * @return 数据封装实体类
     */
    Result deleteProcInst(String procInstId);

    /**
     * 查询已结束的流程实例
     * @param req 查询条件实体类
     * @return 数据封装实体类
     */
    Result getProcInstFinish(ProcInstREQ req);

    /**
     * 删除流程实例与历史记录
     * @param procInstId 流程id
     * @return 数据封装实体类
     */
    Result deleteProcInstAndHistory(String procInstId);
}
