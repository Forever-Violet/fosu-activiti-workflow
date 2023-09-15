import request from '@/utils/request'

export default {
    
    // 查询流程列表
    getList(data, current, size) {
        return request({
                url: '/process/list',
                method: 'post',
                data: {...data, current, size}
        })
    },

    // 通过部署id删除流程部署信息
    deleteByDeploymentId(deploymentId, key) {
        return request({
            url: `/process/${deploymentId}`,
            method: 'delete',
            params: {key}
        })
    },

    // 激活或挂起流程定义
    updateProcessDefinitionState(definitionId) {
        return request({
            url: `/process/state/${definitionId}`,
            method: 'put'
        })
    },

    // 部署流程文件
    deployProcessFile(data) {
        return request({
            url: `/process/file/deploy`,
            method: 'post',
            data
        })
    },

    // 查询流程定义配置信息
    getProcessConfigByProcessKey(processKey) {
        return request({
            url: `/processConfig/${processKey}`,
            method: 'get'
        })
    },

    // 流程定义配置信息
    updateProcessConfig(data) {
        return request({
            url: `/processConfig`,
            method: 'put',
            data
        })
    },
    

    /**
     * 获取流程图文件
     * @param {*} type xml 或 png
     * @param {*} definitionId 流程定义id
     */
    getProcessFileURL(type, definitionId) {
        return process.env.VUE_APP_BASE_URL + `/process/export/${type}/${definitionId}`
    },

    


    
}