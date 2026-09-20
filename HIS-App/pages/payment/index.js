const { getPayables, payRegistrationItems } = require("../../utils/api");

Page({
  data: {
    registrationId: null,
    total: "0.00",
    items: [],
    paying: false,
    invoiceNo: ""
  },
  onLoad(options) {
    const registrationId = options && options.registrationId ? Number(options.registrationId) : null;
    this.setData({ registrationId });
    this.loadItems(registrationId);
  },
  async loadItems(registrationId) {
    try {
      const res = await getPayables(registrationId);
      const list = (res && res.data) || [];
      const normalized = list.map((it) => ({
        id: it.id,
        itemName: it.itemName || it.item_name,
        qty: 1,
        unitPrice: Number(it.amount || 0),
        status: Number(it.status) === 1 ? "已缴费" : "未登记",
        typeName: "检验"
      }));
      const total = normalized.reduce((sum, it) => sum + Number(it.unitPrice * it.qty), 0).toFixed(2);
      this.setData({ items: normalized, total });
    } catch (e) {
      const list = [
        { id: 1, itemName: "低流量吸氧", qty: 1, unitPrice: 20.05, status: "未登记", typeName: "检验" },
        { id: 2, itemName: "洗胃", qty: 1, unitPrice: 40.0, status: "未登记", typeName: "检验" }
      ];
      const total = list.reduce((sum, it) => sum + Number(it.unitPrice * it.qty), 0).toFixed(2);
      this.setData({ items: list, total });
    }
  },
  async onPayAll() {
    if (this.data.paying || !this.data.registrationId) return;
    this.setData({ paying: true });
    try {
      const res = await payRegistrationItems(this.data.registrationId, []);
      const invoiceNo = (res && res.data && res.data.invoiceNo) || "";
      this.setData({ invoiceNo: invoiceNo ? String(invoiceNo) : "" });
      wx.showToast({ title: "缴费成功", icon: "success" });
      await this.loadItems(this.data.registrationId);
    } catch (e) {
      wx.showToast({ title: "缴费失败", icon: "none" });
    } finally {
      this.setData({ paying: false });
    }
  },
  goCheckResult() {
    const id = this.data.registrationId;
    wx.navigateTo({ url: `/pages/check-result/index?registrationId=${id || ""}` });
  }
});
