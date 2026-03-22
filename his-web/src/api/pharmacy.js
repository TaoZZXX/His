import request from '@/utils/request'
import { getToken } from '@/utils/auth'

export function getPharmacyPatients(mode, keyword, limit) {
  return request({
    url: '/sms/pharmacy/patients',
    method: 'get',
    params: {
      token: getToken(),
      mode,
      keyword: keyword || undefined,
      limit: limit != null && limit !== '' ? limit : undefined
    }
  })
}

export function getPharmacyMedicineLines(registrationId) {
  return request({
    url: `/sms/pharmacy/registrations/${registrationId}/medicine-lines`,
    method: 'get',
    params: { token: getToken() }
  })
}

export function dispensePharmacyItems(itemIds) {
  return request({
    url: '/sms/pharmacy/items/dispense',
    method: 'post',
    params: { token: getToken() },
    data: { itemIds: itemIds || [] }
  })
}

export function refundPharmacyItems(itemIds) {
  return request({
    url: '/sms/pharmacy/items/refund',
    method: 'post',
    params: { token: getToken() },
    data: { itemIds: itemIds || [] }
  })
}

/** 药品维护分页：page 从 1 起；status 空=全部 */
export function getPharmacyDrugPage({ page = 1, size = 10, keyword, status } = {}) {
  return request({
    url: '/sms/pharmacy/drugs',
    method: 'get',
    params: {
      token: getToken(),
      page,
      size,
      keyword: keyword || undefined,
      status: status !== '' && status !== undefined ? status : undefined
    }
  })
}

export function createPharmacyDrug(data) {
  return request({
    url: '/sms/pharmacy/drugs',
    method: 'post',
    params: { token: getToken() },
    data: data || {}
  })
}

export function updatePharmacyDrug(id, data) {
  return request({
    url: `/sms/pharmacy/drugs/${id}`,
    method: 'put',
    params: { token: getToken() },
    data: data || {}
  })
}
