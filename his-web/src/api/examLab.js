import request from '@/utils/request'
import { getToken } from '@/utils/auth'

/**
 * @param {'execute'|'result'} mode
 * @param {0|1} filterByStaffDept 1=按登录人科室与项目字典 dept 过滤（易漏数据）；0=全院
 */
export function getExamLabQueue(mode, filterByStaffDept = 0) {
  return request({
    url: '/sms/exam-lab/queue',
    method: 'get',
    params: {
      token: getToken(),
      mode,
      filterByStaffDept
    }
  })
}

/** 门诊医技工作台：未登记 + 已登记 */
export function getMedTechWorkbench(keyword, filterByStaffDept = 0, limit) {
  return request({
    url: '/sms/exam-lab/workbench',
    method: 'get',
    params: {
      token: getToken(),
      keyword: keyword || undefined,
      filterByStaffDept,
      limit: limit != null && limit !== '' ? limit : undefined
    }
  })
}

export function executeExamLabItem(itemId) {
  return request({
    url: `/sms/exam-lab/items/${itemId}/execute`,
    method: 'post',
    params: { token: getToken() }
  })
}

export function saveExamLabResult(itemId, data) {
  return request({
    url: `/sms/exam-lab/items/${itemId}/result`,
    method: 'put',
    params: { token: getToken() },
    data: data || {}
  })
}

/** 上传检查结果图片，返回 data.url（如 /sms/exam-result-files/xxx.jpg）写入库 */
export function uploadExamResultImage(formData) {
  return request({
    url: '/sms/exam-lab/result-images/upload',
    method: 'post',
    params: { token: getToken() },
    data: formData
  })
}
