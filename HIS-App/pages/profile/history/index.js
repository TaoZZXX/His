const api = require("../../../utils/api");

Page({
  data: {
    loading: false,
    records: [],
    errorText: ""
  },
  onShow() {
    this.loadHistory();
  },
  async loadHistory() {
    const app = getApp();
    const profile = app.globalData.patientProfile || {};
    const idCard = (profile.idCard || "").trim();
    if (!idCard) {
      this.setData({
        records: [],
        errorText: "请先在挂号流程中填写就诊人身份证信息。"
      });
      return;
    }
    this.setData({ loading: true, errorText: "" });
    try {
      const pRes = await api.getPatientByIdCard(idCard);
      if (!pRes || pRes.code !== 20000 || !pRes.data) {
        throw new Error((pRes && pRes.message) || "未找到就诊人信息");
      }
      const medicalRecordNo = pRes.data.medicalRecordNo;
      const regRes = await api.getAllRegistrations(0, 300, "");
      if (!regRes || regRes.code !== 20000 || !regRes.data) {
        throw new Error((regRes && regRes.message) || "历史病历查询失败");
      }
      const rows = (regRes.data.records || [])
        .filter((r) => String(r.medicalRecordNo || "") === String(medicalRecordNo || ""))
        .map((r) => ({
          id: r.id,
          medicalRecordNo: r.medicalRecordNo || "-",
          deptName: r.deptName || "-",
          doctorName: r.staffName || "-",
          visitDate: r.createTime || r.visitDate || "-",
          statusText: Number(r.status) === 2 ? "已就诊" : Number(r.status) === 3 ? "已退号" : "进行中"
        }));
      this.setData({ records: rows });
      if (!rows.length) {
        this.setData({ errorText: "暂无历史病历记录。" });
      }
    } catch (e) {
      this.setData({ errorText: e.message || "历史病历查询失败" });
    } finally {
      this.setData({ loading: false });
    }
  },
  toCheckResult(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) return;
    wx.navigateTo({
      url: `/pages/check-result/index?registrationId=${id}`
    });
  }
});
