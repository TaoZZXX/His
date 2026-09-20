const { request } = require("./request");

function login(username, passwordMd5) {
  return request("/sms/staff/login", "POST", { username, password: passwordMd5 });
}

function getInfo(token) {
  return request("/sms/staff/info", "GET", { token });
}

function getDepartments() {
  return request("/sms/registration/departments");
}

function getDoctorsByDept(deptId) {
  return request(`/sms/registration/departments/${deptId}/doctors`);
}

function getAvailableDoctors(deptId, date, session) {
  return request("/sms/registration/doctors/available", "GET", { deptId, date, session });
}

function listRegistrationRanks() {
  return request("/sms/registration/registration-ranks");
}

function createRegistration(payload) {
  return request("/sms/registration/registrations", "POST", payload);
}

function getPayables(registrationId) {
  return request(`/sms/registration/registrations/${registrationId}/payables`);
}

function payRegistrationItems(registrationId, payableItemIds) {
  return request(`/sms/registration/registrations/${registrationId}/pay-items`, "POST", {
    payableItemIds: payableItemIds && payableItemIds.length ? payableItemIds : []
  });
}

function getPatientByIdCard(idCard) {
  return request("/sms/registration/patient", "GET", { identificationNo: idCard });
}

function getAllRegistrations(page = 1, size = 200, keyword = "") {
  return request("/sms/registration/getAllByPage", "POST", { page, size, keyword });
}

function getDoctorPatientContext(registrationId, token) {
  return request(`/sms/doctor/desk/patients/${registrationId}/context`, "GET", { token });
}

module.exports = {
  login,
  getInfo,
  getDepartments,
  getDoctorsByDept,
  getAvailableDoctors,
  listRegistrationRanks,
  createRegistration,
  getPayables,
  payRegistrationItems,
  getPatientByIdCard,
  getAllRegistrations,
  getDoctorPatientContext
};
