import request from '@/utils/request'

export default {
    
    // 查询个人待办任务列表
    getWaitTaskList(data, current, size) {
        return request({
                url: '/task/list/wait',
                method: 'post',
                data: {...data, current, size}
        })
    },

    // 查询个人已处理任务列表
    getCompleteTaskList(data, current, size) {
      return request({
              url: '/task/list/complete',
              method: 'post',
              data: {...data, current, size}
      })
    },


    // 完成任务
    completeTask(data) {
        return request({
            url: '/task/complete',
            method: 'post',
            data
        })
    },

    // 签收任务
    claimTask(params) {
        return request({
            url: '/task/claim',
            method: 'post',
            params
        })
    },

    // 转办任务
    turnTask(params) {
        return request({
            url: '/task/turn',
            method: 'post',
            params
        })
    },
    
    // 获取下一节点信息
    getNextNodeInfo(taskId) {
        return request({
          url: '/task/next/node',
          method: 'get',
          params: {taskId}
        })
    },

    // 查询可驳回节点
    getBackNodes(taskId) {
      return request({
        url: '/task/back/nodes',
        method: 'get',
        params: {taskId}
      })
    },

    // 任务驳回指定节点
    backTask(params) {
        return request({
            url: '/task/back',
            method: 'post',
            params,
        })
    }

}