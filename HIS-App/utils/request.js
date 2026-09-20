function request(url, method = "GET", data = {}) {
  return new Promise((resolve, reject) => {
    const app = getApp ? getApp() : null;
    const baseUrl = (app && app.globalData && app.globalData.baseUrl) || "";
    const token = (app && app.globalData && app.globalData.token) || "";
    wx.request({
      url: `${baseUrl}${url}`,
      method,
      data,
      timeout: 10000,
      header: {
        "Content-Type": "application/json",
        "X-Token": token,
        Authorization: token ? `Bearer ${token}` : ""
      },
      success: (res) => resolve(res.data),
      fail: reject
    });
  });
}

module.exports = {
  request
};
