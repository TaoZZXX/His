import request from '@/utils/request'
import { getToken } from '@/utils/auth'

export function getDoctorDeskPatients(params) {
  return request({
    url: '/sms/doctor/desk/patients',
    method: 'get',
    params: {
      token: getToken(),
      scope: params && params.scope ? params.scope : 'self',
      keyword: params && params.keyword ? params.keyword : undefined,
      date: params && params.date ? params.date : undefined,
      session: params && params.session ? params.session : undefined
    }
  })
}

/**
 * @param {string} scope self | dept — 与队列 Tab 一致；dept=同科室可操作他人排班挂号
 */
export function startDoctorDeskVisit(registrationId, scope) {
  return request({
    url: `/sms/doctor/desk/patients/${registrationId}/start`,
    method: 'post',
    params: {
      token: getToken(),
      scope: scope || 'self'
    }
  })
}

export function finishDoctorDeskVisit(registrationId, scope) {
  return request({
    url: `/sms/doctor/desk/patients/${registrationId}/finish`,
    method: 'post',
    params: {
      token: getToken(),
      scope: scope || 'self'
    }
  })
}

export function getDoctorDeskContext(registrationId) {
  return request({
    url: `/sms/doctor/desk/patients/${registrationId}/context`,
    method: 'get',
    params: {
      token: getToken()
    }
  })
}

export function getDoctorDeskMedicines(keyword) {
  return request({
    url: '/sms/doctor/desk/dict/medicines',
    method: 'get',
    params: {
      token: getToken(),
      keyword: keyword || undefined
    }
  })
}

export function getDoctorDeskNonDrugDict(type, keyword) {
  return request({
    url: '/sms/doctor/desk/dict/non-drug',
    method: 'get',
    params: {
      token: getToken(),
      type,
      keyword: keyword || undefined
    }
  })
}

export function saveDoctorDeskCaseHistory(registrationId, data) {
  return request({
    url: `/sms/doctor/desk/patients/${registrationId}/case-history`,
    method: 'post',
    params: { token: getToken() },
    data
  })
}

export function saveDoctorDeskDiagnosis(registrationId, data) {
  return request({
    url: `/sms/doctor/desk/patients/${registrationId}/diagnosis`,
    method: 'post',
    params: { token: getToken() },
    data
  })
}

export function saveDoctorDeskNonDrugItem(registrationId, data) {
  return request({
    url: `/sms/doctor/desk/patients/${registrationId}/non-drug-items`,
    method: 'post',
    params: { token: getToken() },
    data
  })
}

export function saveDoctorDeskMedicinePrescription(registrationId, data) {
  return request({
    url: `/sms/doctor/desk/patients/${registrationId}/prescriptions/medicine`,
    method: 'post',
    params: { token: getToken() },
    data
  })
}

export function saveDoctorDeskHerbalPrescription(registrationId, data) {
  return request({
    url: `/sms/doctor/desk/patients/${registrationId}/prescriptions/herbal`,
    method: 'post',
    params: { token: getToken() },
    data
  })
}

