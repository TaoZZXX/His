<template>
  <div class="registration-container">
    <!-- 表单区域 -->
    <el-form :model="form" label-width="100px" class="form-area">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="身份证号">
            <el-input v-model="form.idCard" placeholder="身份证号" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="姓名">
            <el-input v-model="form.name" placeholder="姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="出生日期">
            <el-date-picker v-model="form.birthDate" placeholder="选择日期" style="width: 100%;" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="性别">
            <el-select v-model="form.gender" placeholder="性别">
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="家庭住址">
            <el-input v-model="form.address" placeholder="家庭住址" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="联系方式">
            <el-input v-model="form.contact" placeholder="联系方式" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="挂号科室">
            <el-select v-model="form.department" placeholder="挂号科室">
              <el-option label="心血管内科" value="心血管内科" />
              <el-option label="神经内科" value="神经内科" />
              <!-- 可根据实际科室补充 -->
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="挂号级别">
            <el-select v-model="form.level" placeholder="挂号级别">
              <el-option label="普通" value="普通" />
              <el-option label="专家" value="专家" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="挂号日期">
            <el-date-picker v-model="form.registrationDate" placeholder="选择日期" style="width: 100%;" />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="支付方式">
            <el-select v-model="form.payment" placeholder="请选择支付方式">
              <el-option label="现金" value="现金" />
              <el-option label="支付宝" value="支付宝" />
              <el-option label="微信" value="微信" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="午别">
            <el-select v-model="form.session" placeholder="默认">
              <el-option label="上午" value="上午" />
              <el-option label="下午" value="下午" />
              <el-option label="默认" value="默认" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="看诊医生">
            <el-select v-model="form.doctor" placeholder="默认">
              <el-option label="默认" value="默认" />
              <!-- 可根据实际医生补充 -->
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-form-item label="应收金额">
            <el-input v-model="form.amount" placeholder="0" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="病历本">
            <el-radio-group v-model="form.medicalRecord">
              <el-radio label="是">是</el-radio>
              <el-radio label="否">否</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24" style="text-align: right; margin-top: 20px;">
          <el-button type="primary" @click="handleRegisterAndPrint" :loading="submitting">
            挂号&打印发票
          </el-button>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script>
import { createRegistration } from '@/api/registService'

export default {
  name: 'Registration',
  data() {
    return {
      invoiceNo: '',
      submitting: false,
      form: {
        idCard: '',
        name: '',
        birthDate: '',
        gender: '',
        address: '',
        contact: '',
        department: '',
        level: '',
        registrationDate: '',
        payment: '',
        session: '默认',
        doctor: '默认',
        amount: 0,
        medicalRecord: ''
      }
    };
  },
  methods: {
    // 简单日期格式化 YYYY-MM-DD
    formatDate(d) {
      if (!d) return ''
      if (typeof d === 'string') return d
      const dt = new Date(d)
      const y = dt.getFullYear()
      const m = String(dt.getMonth() + 1).padStart(2, '0')
      const day = String(dt.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    },

    // 打开新窗口写入发票并触发打印
    openPrintWindow(regInfo = {}) {
      const win = window.open('', '_blank')
      if (!win) {
        this.$message.error('浏览器阻止了弹窗，请允许弹窗后重试')
        return
      }
      const invoiceNo = regInfo.invoiceNo || regInfo.invoice || this.invoiceNo || '—'
      const patientName = regInfo.name || this.form.name || '—'
      const idCard = regInfo.idCard || this.form.idCard || '—'
      const dept = regInfo.department || this.form.department || '—'
      const doctor = regInfo.doctor || this.form.doctor || '—'
      const date = regInfo.registrationDate || this.formatDate(this.form.registrationDate) || '—'
      const amount = regInfo.amount != null ? regInfo.amount : this.form.amount
      const html = `
        <!doctype html>
        <html>
        <head>
          <meta charset="utf-8">
          <title>挂号发票</title>
          <style>
            body { font-family: Arial, "Microsoft Yahei"; padding:20px; }
            .invoice { width:600px; margin:0 auto; }
            .header { text-align:center; margin-bottom:20px; }
            .row { display:flex; justify-content:space-between; margin:6px 0; }
            .label { color:#666; }
            .value { font-weight:600; }
            .foot { margin-top:30px; text-align:center; color:#999; font-size:12px; }
          </style>
        </head>
        <body>
          <div class="invoice">
            <div class="header"><h2>医院挂号发票</h2></div>
            <div class="row"><span class="label">发票号：</span><span class="value">${invoiceNo}</span></div>
            <div class="row"><span class="label">姓名：</span><span class="value">${patientName}</span></div>
            <div class="row"><span class="label">身份证号：</span><span class="value">${idCard}</span></div>
            <div class="row"><span class="label">科室：</span><span class="value">${dept}</span></div>
            <div class="row"><span class="label">医生：</span><span class="value">${doctor}</span></div>
            <div class="row"><span class="label">挂号日期：</span><span class="value">${date}</span></div>
            <div class="row"><span class="label">金额：</span><span class="value">¥ ${amount}</span></div>
            <div class="foot">此发票仅供挂号使用，请妥善保存</div>
          </div>
        </body>
        </html>
      `
      win.document.open()
      win.document.write(html)
      win.document.close()
    },

    // 按钮主方法：校验 -> 创建挂号 -> 打印发票
    async handleRegisterAndPrint() {
      // 必填校验（可根据需要扩展）
      if (!this.form.name || !this.form.idCard || !this.form.department || !this.form.registrationDate) {
        this.$message.warning('请填写姓名、身份证号、科室和挂号日期')
        return
      }

      const payload = {
        id: this.form.idCard, // 若后端使用患者 ID，请替换
        identificationNo: this.form.idCard,
        name: this.form.name,
        birthDate: this.formatDate(this.form.birthDate),
        gender: this.form.gender,
        address: this.form.address,
        contact: this.form.contact,
        department: this.form.department,
        level: this.form.level,
        registrationDate: this.formatDate(this.form.registrationDate),
        payment: this.form.payment,
        session: this.form.session,
        doctorId: this.form.doctor,
        doctor: this.form.doctor,
        amount: this.form.amount,
        medicalRecord: this.form.medicalRecord
      }

      this.submitting = true
      try {
        const res = await createRegistration(payload)

        if (res.code !== 20000) {
          this.$message.error(res.message || '挂号失败')
          return
        }

        if (res.invoiceNo) this.invoiceNo = regInfo.invoiceNo

        this.$message.success('挂号成功，即将打印发票')
        // 前端生成并打印发票（简单 HTML），也可以替换为后端打印 URL
        this.openPrintWindow(Object.assign({}, this.form, regInfo))
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.registration-container {
  padding: 24px;
}
.tabs {
  margin: 16px 0;
}
.form-area {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
}

/* 新增样式：顶部一行控件 */
.header-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 16px 0;
}
.header-left {
  font-size: 16px;
  font-weight: 500;
}
.invoice-input {
  width: 260px;
}
</style>
