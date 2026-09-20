const { getDepartments, getAvailableDoctors } = require("../../utils/api");

Page({
  data: {
    keyword: "",
    departments: [],
    activeDeptId: null,
    doctors: []
  },
  onLoad() {
    this.loadData();
  },
  async loadData() {
    try {
      const deptRes = await getDepartments();
      const departments = (deptRes && deptRes.data) || [];
      const first = departments[0] || null;
      this.setData({
        departments,
        activeDeptId: first ? first.id : null
      });
      if (first) {
        await this.loadDoctors(first.id);
      }
    } catch (e) {
      const departments = [
        { id: 1, name: "心血管内科" },
        { id: 2, name: "神经内科" },
        { id: 3, name: "普通内科" }
      ];
      this.setData({ departments, activeDeptId: 1 });
      this.loadDoctors(1);
    }
  },
  async loadDoctors(deptId) {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, "0");
    const d = String(now.getDate()).padStart(2, "0");
    const date = `${y}-${m}-${d}`;
    const session = now.getHours() < 12 ? "上午" : "下午";
    try {
      const res = await getAvailableDoctors(deptId, date, session);
      this.setData({ doctors: (res && res.data) || [] });
    } catch (e) {
      this.setData({
        doctors: [
          { id: 101, name: "郭秀晶", title: "专家", avatar: "" },
          { id: 102, name: "丁聪华", title: "主治", avatar: "" }
        ]
      });
    }
  },
  onSelectDept(e) {
    const deptId = Number(e.currentTarget.dataset.id);
    this.setData({ activeDeptId: deptId });
    this.loadDoctors(deptId);
  },
  onSearchInput(e) {
    this.setData({ keyword: e.detail.value || "" });
  },
  goDoctorDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/doctor-detail/index?doctorId=${id}&deptId=${this.data.activeDeptId}` });
  }
});
