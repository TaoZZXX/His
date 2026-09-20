const { getDepartments } = require("../../utils/api");

Page({
  data: {
    banner:
      "https://images.unsplash.com/photo-1576091160550-2173dba999ef?auto=format&fit=crop&w=1200&q=80",
    departments: []
  },
  onLoad() {
    this.loadDepartments();
  },
  async loadDepartments() {
    try {
      const res = await getDepartments();
      const list = (res && res.data) || [];
      this.setData({
        departments: list.map((d) => ({
          id: d.id,
          name: d.name,
          intro: `${d.name}，提供门诊诊疗与随访服务`
        }))
      });
    } catch (e) {
      this.setData({
        departments: [
          { id: 1, name: "心血管内科", intro: "心内科、心外科常见疾病门诊与复诊" },
          { id: 2, name: "神经内科", intro: "头痛、脑血管疾病、慢病管理" },
          { id: 3, name: "消化内科", intro: "胃肠、肝胆胰常见疾病诊疗" }
        ]
      });
    }
  }
});
