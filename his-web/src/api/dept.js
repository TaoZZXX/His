import request from '@/utils/request'

/** 科室管理分页 */
export function pageDepts(params) {
  return request({
    url: '/sms/dept/page',
    method: 'get',
    params
  })
}

export function createDept(data) {
  return request({
    url: '/sms/dept',
    method: 'post',
    data
  })
}

export function updateDept(id, data) {
  return request({
    url: `/sms/dept/${id}`,
    method: 'put',
    data
  })
}

export function deleteDept(id) {
  return request({
    url: `/sms/dept/${id}`,
    method: 'delete'
  })
}

export function batchDeleteDepts(ids) {
  return request({
    url: '/sms/dept/batch-delete',
    method: 'post',
    data: { ids: ids || [] }
  })
}
