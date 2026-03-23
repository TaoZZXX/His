import request from '@/utils/request'
import { getToken } from '@/utils/auth'

export function listDailySettlements(startDate, endDate) {
  return request({
    url: '/sms/finance/daily-settlement/list',
    method: 'get',
    params: {
      token: getToken(),
      startDate,
      endDate
    }
  })
}

export function getDailySettlement(id) {
  return request({
    url: `/sms/finance/daily-settlement/${id}`,
    method: 'get',
    params: { token: getToken() }
  })
}

/** rangeStart / rangeEnd: yyyy-MM-dd HH:mm:ss */
export function generateDailySettlement(rangeStart, rangeEnd) {
  return request({
    url: '/sms/finance/daily-settlement/generate',
    method: 'post',
    params: { token: getToken() },
    data: { rangeStart, rangeEnd }
  })
}

export function auditDailySettlement(id) {
  return request({
    url: `/sms/finance/daily-settlement/${id}/audit`,
    method: 'post',
    params: { token: getToken() }
  })
}
