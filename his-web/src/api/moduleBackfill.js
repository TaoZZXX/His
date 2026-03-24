import request from '@/utils/request'
import { getToken } from '@/utils/auth'

export function listBackfillModules() {
  return request({
    url: '/sms/module-backfill/modules',
    method: 'get',
    params: { token: getToken() }
  })
}

export function listBackfillColumns(table) {
  return request({
    url: `/sms/module-backfill/tables/${table}/columns`,
    method: 'get',
    params: { token: getToken() }
  })
}

export function pageBackfillRows(table, page, size) {
  return request({
    url: `/sms/module-backfill/tables/${table}/rows`,
    method: 'get',
    params: { token: getToken(), page, size }
  })
}

export function saveBackfillRow(table, data) {
  return request({
    url: `/sms/module-backfill/tables/${table}/save`,
    method: 'post',
    params: { token: getToken() },
    data
  })
}

export function deleteBackfillRow(table, id) {
  return request({
    url: `/sms/module-backfill/tables/${table}/rows/${id}`,
    method: 'delete',
    params: { token: getToken() }
  })
}
