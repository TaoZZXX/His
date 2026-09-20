App({
  globalData: {
    baseUrl: "http://localhost:9999",
    token: "",
    wxProfile: {
      nickName: "微信用户",
      avatarUrl: ""
    },
    patientProfile: {
      name: "",
      idCard: "",
      gender: "男",
      contact: "",
      address: ""
    }
  },
  onLaunch() {
    const wxProfile = wx.getStorageSync("wxProfile");
    const patientProfile = wx.getStorageSync("patientProfile");
    const baseUrl = wx.getStorageSync("hisBaseUrl");
    const token = wx.getStorageSync("hisToken");
    if (wxProfile) this.globalData.wxProfile = wxProfile;
    if (patientProfile) this.globalData.patientProfile = patientProfile;
    if (baseUrl) this.globalData.baseUrl = baseUrl;
    if (token) this.globalData.token = token;
  },
  setWxProfile(profile) {
    this.globalData.wxProfile = { ...this.globalData.wxProfile, ...profile };
    wx.setStorageSync("wxProfile", this.globalData.wxProfile);
  },
  setPatientProfile(profile) {
    this.globalData.patientProfile = { ...this.globalData.patientProfile, ...profile };
    wx.setStorageSync("patientProfile", this.globalData.patientProfile);
  }
});
