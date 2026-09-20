const api = require("../../utils/api");

function splitImages(raw) {
  if (!raw) return [];
  return String(raw)
    .split(",")
    .map((v) => v.trim())
    .filter(Boolean);
}

function absUrl(u, baseUrl) {
  if (!u) return "";
  if (/^https?:\/\//i.test(u)) return u;
  const b = (baseUrl || "").replace(/\/$/, "");
  return `${b}${u.startsWith("/") ? "" : "/"}${u}`;
}

Page({
  data: {
    registrationId: "",
    loading: false,
    resultRows: [],
    errorText: ""
  },
  onLoad(options) {
    const id = options && options.registrationId ? String(options.registrationId) : "";
    this.setData({ registrationId: id });
    if (id) this.loadRealResult(id);
  },
  async loadRealResult(registrationId) {
    const app = getApp();
    const token = app.globalData.token || "";
    if (!token) {
      this.setData({
        errorText: "当前未配置医生 token，无法读取检查检验结果。可在“我的-系统设置”中配置。"
      });
      return;
    }
    this.setData({ loading: true, errorText: "" });
    try {
      const res = await api.getDoctorPatientContext(registrationId, token);
      if (!res || res.code !== 20000 || !res.data) {
        throw new Error((res && res.message) || "加载结果失败");
      }
      const baseUrl = app.globalData.baseUrl || "";
      const rows = []
        .concat((res.data.examItems || []).map((it) => ({ ...it, kind: "检查" })))
        .concat((res.data.labItems || []).map((it) => ({ ...it, kind: "检验" })))
        .filter((it) => it && (it.checkResult || it.resultImgUrlList))
        .map((it) => ({
          id: it.id,
          kind: it.kind,
          checkParts: it.checkParts || "-",
          checkResult: it.checkResult || "",
          clinicalImpression: it.clinicalImpression || "",
          clinicalDiagnosis: it.clinicalDiagnosis || "",
          logDatetime: it.logDatetime || "",
          images: splitImages(it.resultImgUrlList).map((img) => absUrl(img, baseUrl))
        }));
      this.setData({ resultRows: rows });
      if (!rows.length) {
        this.setData({ errorText: "当前挂号暂无已回填的检查检验结果。" });
      }
    } catch (e) {
      this.setData({ errorText: e.message || "加载结果失败" });
    } finally {
      this.setData({ loading: false });
    }
  },
  onConfirm() {
    wx.navigateBack({ delta: 1 });
  },
  onPreviewImage(e) {
    const src = e.currentTarget.dataset.src;
    let list = e.currentTarget.dataset.list || [];
    if (typeof list === "string") {
      list = list.split(",").map((v) => v.trim()).filter(Boolean);
    }
    if (!src) return;
    wx.previewImage({ current: src, urls: list });
  }
});
