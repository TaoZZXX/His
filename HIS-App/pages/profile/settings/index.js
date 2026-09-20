Page({
  data: {
    baseUrl: "",
    token: ""
  },
  onShow() {
    const app = getApp();
    this.setData({
      baseUrl: app.globalData.baseUrl || "",
      token: app.globalData.token || ""
    });
  },
  onBaseUrlInput(e) {
    this.setData({ baseUrl: e.detail.value || "" });
  },
  onTokenInput(e) {
    this.setData({ token: e.detail.value || "" });
  },
  onSave() {
    const app = getApp();
    app.globalData.baseUrl = (this.data.baseUrl || "").trim();
    app.globalData.token = (this.data.token || "").trim();
    wx.setStorageSync("hisBaseUrl", app.globalData.baseUrl);
    wx.setStorageSync("hisToken", app.globalData.token);
    wx.showToast({ title: "保存成功", icon: "success" });
  },
  onClearToken() {
    this.setData({ token: "" });
    const app = getApp();
    app.globalData.token = "";
    wx.setStorageSync("hisToken", "");
    wx.showToast({ title: "已清空 token", icon: "none" });
  }
});
