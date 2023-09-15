import request from '@/utils/request'

// 借款表单管理
export default {
  // 新增申请
  add(data) {
    return request({
        url: '/loan',
        method: 'post',
        data
    })
  },

  // 申请列表
  getList(data, current, size) {
    return request({
        url: '/loan/list',
        method: 'post',
        data: {...data, current, size}
    })
  },

  // 查询详情
  getById(id) {
    return request({
        url: `/loan/${id}`,
        method: 'get'
    })
  },

  // 修改申请
  update(data) {
    return request({
        url: '/loan',
        method: 'put',
        data
    })
  },

}
