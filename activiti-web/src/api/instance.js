import request from '@/utils/request'

export default {


    // 提交申请 ，启动流程申请
    startProcessApply(data) {
        return request({
            url: '/instance/start',
            method: 'post',
            data
        })
    },

    // 撤回申请
    cancelApply(params) {
        return request({
            url: '/instance/cancel/apply',
            method: 'delete',
            params
        })
    },

    // 获取表单组件名
    getFormNameByProcInstId(procInstId) {
        return request({
            url: `/instance/form/name/${procInstId}`,
            method: 'get'
        })
    },

   // 根据路由名查询流程配置信息
   getProcessConfigyBusinessRoute(routeName) {
        return request({
            url: `/processConfig/route/${routeName}`,
            method: 'get'
        })
    },

     // 根据流程实例ID查询审批历史记录 
     getHistoryInfoList(procInstId) {
        return request({
          url: '/instance/history/list',
          method: 'get',
          params: {procInstId}
        })
    },

    // 获取流程审批历史图
    getHistoryProcessImage(procInstId) {
        return process.env.VUE_APP_BASE_URL + `/instance/history/image?procInstId=${procInstId}&t=`+Math.random()
    },

    // 查询正在运行的流程实例列表
    getProcInstListRunning(data, current, size) {
        return request({
            url: '/instance/list/running',
            method: 'post',
            data: {...data, current, size}
        })
    },

    

    // 挂起或激活单个流程实例 (流程实例id)
    updateProcInstState(procInstId) {
        return request({
            url: `/instance/state/${procInstId}`,
            method: 'put'
        })
    },

    // 删除流程实例 (流程实例id)
    deleteProcInst(procInstId) {
        return request({
            url: `/instance/${procInstId}`,
            method: 'delete'
        })
    },

    // 查询已结束的流程实例列表
    getProcInstListFinish(data, current, size) {
        return request({
            url: '/instance/list/finish',
            method: 'post',
            data: {...data, current, size}
        })
    },

    // 删除流程实例和审批历史记录 (流程实例id)
    deleteProcInstAndHistory(procInstId) {
        return request({
            url: `/instance/history/${procInstId}`,
            method: 'delete'
        })
    },

    
}

