Page({
  data: {
    content: "",
    history: []
  },
  onShow() {
    const list = wx.getStorageSync("feedbackList") || [];
    this.setData({ history: list.slice(0, 20) });
  },
  onInput(e) {
    this.setData({ content: e.detail.value || "" });
  },
  onSubmit() {
    const content = (this.data.content || "").trim();
    if (!content) {
      wx.showToast({ title: "请输入反馈内容", icon: "none" });
      return;
    }
    const list = wx.getStorageSync("feedbackList") || [];
    list.unshift({
      content,
      createTime: new Date().toLocaleString()
    });
    wx.setStorageSync("feedbackList", list.slice(0, 100));
    this.setData({ content: "", history: list.slice(0, 20) });
    wx.showToast({ title: "反馈已提交", icon: "success" });
  }
});
