import request from '@/utils/request'
export function getall() {
    return request({
      url: '/user/all',
      method: 'get',
    })
  }
