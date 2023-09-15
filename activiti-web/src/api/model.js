import request from '@/utils/request'

export default {

  // 查询列表
  getList(data, current, size) {
    return request({
          url: '/model/list',
          method: 'post',
          data: {...data, current, size}
    })
  },

  // 新增模型
  add(data) {
      return request({
          url: '/model',
          method: 'post',
          data
      })
  },

  // 删除模型
  deleteById(id) {
      return request({
          url: `/model/${id}`,
          method: 'delete'
      })
  },
  

  // 部署发布流程
  deployById(id) {
      return request({
          url: `/model/deploy/${id}`,
          method: 'post'
      })
  },

}

