import request from '@/utils/request'

/**
 * 获取科室列表
 * params: { keyword?, page?, size? }
 */
export function getDepartments(params) {
  return request({
    url: '/sms/registration/departments',
    method: 'get',
    params
  })
}

/**
 * 获取科室下医生列表
 * deptId: 科室 ID
 * params: { keyword?, page?, size? }
 */
export function getDoctors(deptId, params) {
  return request({
    url: `/sms/registration/departments/${deptId}/doctors`,
    method: 'get',
    params
  })
}

// 按排班规则查询可挂号医生
export function getAvailableDoctors(params) {
  return request({
    url: '/sms/registration/doctors/available',
    method: 'get',
    params
  })
}

/**
 * 获取医生某日排班 / 可挂号时段
 * doctorId: 医生 ID
 * date: YYYY-MM-DD
 */
export function getDoctorSchedule(doctorId, date) {
  return request({
    url: `/sms/registration/doctors/${doctorId}/schedule`,
    method: 'get',
    params: { date }
  })
}

/**
 * 查询患者的挂号记录（按患者 ID）
 * patientId: 患者 ID
 * params: { page?, size?, status? }
 */
export function getRegistrationsByPatient(patientId, params) {
  return request({
    url: `/sms/registration/patients/${patientId}/registrations`,
    method: 'get',
    params
  })
}

/**
 * 获取单条挂号详情
 * registrationId: 挂号记录 ID
 */
export function getRegistrationDetails(registrationId) {
  return request({
    url: `/sms/registration/registrations/${registrationId}`,
    method: 'get'
  })
}

/**
 * 创建挂号（提交挂号申请）
 * data: {
 *   patientId, doctorId, deptId, date(YYYY-MM-DD), slotId, visitType?, remarks?
 * }
 */
export function createRegistration(data) {
  return request({
    url: '/sms/registration/registrations',
    method: 'post',
    data
  })
}

/**
 * 根据身份证号查询患者信息
 * idCard: 身份证号
 */
export function getPatientByIdCard(idCard) {
  return request({
    url: '/sms/registration/patient',
    method: 'get',
    params: { identificationNo: idCard }
  })
}

/**
 * 取消挂号
 * registrationId: 挂号记录 ID
 * data (可选): { reason? }
 */
export function cancelRegistration(registrationId, data) {
  return request({
    url: `/sms/registration/registrations/${registrationId}/cancel`,
    method: 'post',
    data
  })
}

/**
 * 检查医生某时段是否可挂号（可用于创建前校验）
 * params: { doctorId, date, slotId }
 */
export function checkAvailability(params) {
  return request({
    url: '/registration/availability',
    method: 'get',
    params
  })
}

/**
 * 修改挂号（例如更改时段/备注）
 * registrationId: 挂号记录 ID
 * data: { date?, slotId?, remarks? }
 */
export function updateRegistration(registrationId, data) {
  return request({
    // use the project's SMS registration prefix for consistency
    url: `/sms/registration/registrations/${registrationId}`,
    method: 'put',
    data
  })
}

/**
 * 删除挂号记录（前端会优先尝试软删/退号，如果后端支持物理删除可使用此接口）
 * registrationId: 挂号记录 ID
 */
export function deleteRegistration(registrationId) {
  return request({
    url: `/sms/registration/registrations/${registrationId}`,
    method: 'delete'
  })
}

/**
 *
 */
export function getAllRegistrations(data) {
  return request({
    url: '/sms/registration/getAllByPage',
    method: 'post',
    data
  })
}

/**
 * 新增排班规则（草稿）
 */
export function createSkdRule(data) {
  return request({
    url: '/sms/registration/skd/rules',
    method: 'post',
    data
  })
}

export function listSkdRules(deptId) {
  return request({
    url: '/sms/registration/skd/rules',
    method: 'get',
    params: { deptId }
  })
}

export function getSkdRuleDetail(id) {
  return request({
    url: `/sms/registration/skd/rules/${id}`,
    method: 'get'
  })
}

export function publishSkdRule(id) {
  return request({
    url: `/sms/registration/skd/rules/${id}/publish`,
    method: 'post'
  })
}

export function generateSkdByRule(id, data) {
  return request({
    url: `/sms/registration/skd/rules/${id}/generate`,
    method: 'post',
    data
  })
}
