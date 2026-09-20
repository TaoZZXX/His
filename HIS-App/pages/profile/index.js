Page({
  data: {
    wxProfile: {},
    patientProfile: {},
    menus: [
      { key: "record", name: "历史病历" },
      { key: "help", name: "帮助中心" },
      { key: "about", name: "关于我们" },
      { key: "feedback", name: "意见反馈" },
      { key: "setting", name: "系统设置" }
    ]
  },
  onLoad() {
    const app = getApp();
    this.setData({
      wxProfile: app.globalData.wxProfile,
      patientProfile: app.globalData.patientProfile
    });
  },
  onShow() {
    const app = getApp();
    this.setData({
      wxProfile: app.globalData.wxProfile,
      patientProfile: app.globalData.patientProfile
    });
  },
  getWxProfile() {
    wx.getUserProfile({
      desc: "用于展示头像昵称",
      success: (res) => {
        const profile = res.userInfo || {};
        const app = getApp();
        app.setWxProfile({
          nickName: profile.nickName || "微信用户",
          avatarUrl: profile.avatarUrl || ""
        });
        this.setData({ wxProfile: app.globalData.wxProfile });
      }
    });
  },
  onMenuTap(e) {
    const key = e.currentTarget.dataset.key;
    const map = {
      record: "/pages/profile/history/index",
      help: "/pages/profile/help/index",
      about: "/pages/profile/about/index",
      feedback: "/pages/profile/feedback/index",
      setting: "/pages/profile/settings/index"
    };
    const url = map[key];
    if (!url) return;
    wx.navigateTo({ url });
  }
});
