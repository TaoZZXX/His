const { createRegistration, listRegistrationRanks } = require("../../utils/api");

Page({
  data: {
    doctor: {
      id: null,
      name: "郭秀晶",
      title: "专家",
      deptName: "心血管内科",
      intro: "主治：擅长于普通内科疾病的诊治，尤其是一体多病、多系统疾病的诊治。"
    },
    schedules: [
      { id: 1, text: "2026-03-25 上午", quota: 20, left: 20 },
      { id: 2, text: "2026-03-25 下午", quota: 20, left: 18 }
    ],
    selectedScheduleId: 1,
    creating: false,
    rankId: null,
    deptId: 1,
    patientForm: {
      name: "",
      idCard: "",
      gender: "男",
      contact: "",
      address: ""
    }
  },
  async onLoad(options) {
    const app = getApp();
    this.setData({
      patientForm: { ...this.data.patientForm, ...(app.globalData.patientProfile || {}) }
    });
    if (options && options.doctorId) {
      this.setData({
        "doctor.id": Number(options.doctorId)
      });
    }
    if (options && options.deptId) {
      this.setData({ deptId: Number(options.deptId) });
    }
    await this.loadRanks();
  },
  async loadRanks() {
    try {
      const res = await listRegistrationRanks();
      const list = (res && res.data) || [];
      if (list.length) {
        this.setData({ rankId: list[0].id });
      }
    } catch (e) {}
  },
  onSelectSchedule(e) {
    this.setData({ selectedScheduleId: Number(e.currentTarget.dataset.id) });
  },
  onFormInput(e) {
    const key = e.currentTarget.dataset.key;
    const value = e.detail.value || "";
    this.setData({ [`patientForm.${key}`]: value });
  },
  onGenderChange(e) {
    this.setData({ "patientForm.gender": e.detail.value });
  },
  async onRegister() {
    if (this.data.creating) return;
    const f = this.data.patientForm;
    if (!f.name || !f.idCard) {
      wx.showToast({ title: "请填写姓名和身份证号", icon: "none" });
      return;
    }
    const app = getApp();
    app.setPatientProfile(f);
    this.setData({ creating: true });
    try {
      const payload = {
        identificationNo: Number(f.idCard),
        name: f.name,
        gender: f.gender,
        contact: f.contact,
        address: f.address,
        departmentId: this.data.deptId,
        rankId: this.data.rankId,
        doctorId: this.data.doctor.id,
        session: this.data.selectedScheduleId === 1 ? "上午" : "下午",
        registrationDate: "2026-03-25",
        payment: "微信",
        medicalRecord: "否"
      };
      const res = await createRegistration(payload);
      const registrationId =
        (res && res.data && res.data.registrationId) || Date.now();
      wx.navigateTo({ url: `/pages/payment/index?registrationId=${registrationId}` });
    } catch (e) {
      wx.navigateTo({ url: `/pages/payment/index?registrationId=${Date.now()}` });
    } finally {
      this.setData({ creating: false });
    }
  }
});
