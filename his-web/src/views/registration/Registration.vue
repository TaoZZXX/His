<template>
  <div class="registration-workbench">
    <div class="workbench-toolbar">
      <span class="toolbar-title">门诊挂号</span>
      <div class="toolbar-actions">
        <el-button type="text" icon="el-icon-plus" class="toolbar-link" @click="openRegisterDialog">挂号</el-button>
        <el-button type="text" icon="el-icon-postcard" class="toolbar-link" @click="handleReadCard">读卡</el-button>
        <el-button type="text" icon="el-icon-printer" class="toolbar-link" @click="handleReprint">重打</el-button>
        <el-button type="text" icon="el-icon-document" class="toolbar-link" @click="handleComplementPrint">补打</el-button>
      </div>
    </div>

    <div class="list-panel">
      <div class="list-toolbar">
        <span class="list-title">挂号信息列表</span>
        <div class="list-right">
          <el-input
            v-model="keyword"
            clearable
            placeholder="病历号/姓名"
            class="search-input"
            @keyup.enter.native="handleSearch"
          />
          <el-button type="primary" icon="el-icon-search" @click="handleSearch" />
          <el-button type="text" icon="el-icon-refresh" class="refresh-link" @click="loadList">刷新</el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
        highlight-current-row
        class="reg-table"
        @current-change="handleCurrentRow"
      >
        <el-table-column label="操作" width="120" fixed="left" align="center">
          <template slot-scope="{ row }">
            <template v-if="canCancel(row)">
              <el-button type="text" size="small" class="op-danger" @click="handleCancel(row)">退号</el-button>
            </template>
            <template v-else-if="row.status === 2" />
            <span v-else class="op-muted">收费请至「收费与财务」</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="cashierTagType(row)" size="small" effect="dark">{{ cashierStatusLabel(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="medicalRecordNo" label="病历号" min-width="160" show-overflow-tooltip />
        <el-table-column prop="patientName" label="姓名" width="100" show-overflow-tooltip />
        <el-table-column prop="genderStr" label="性别" width="72" align="center" />
        <el-table-column label="出生日期" width="120" align="center">
          <template slot-scope="{ row }">{{ formatCellDate(row.dateOfBirth) }}</template>
        </el-table-column>
        <el-table-column label="看诊日期" width="120" align="center">
          <template slot-scope="{ row }">{{ formatCellDate(row.attendanceDate) }}</template>
        </el-table-column>
        <el-table-column prop="deptName" label="看诊科目" min-width="140" show-overflow-tooltip />
      </el-table>

      <div class="pager-wrap">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes"
          :current-page.sync="currentPage"
          :page-sizes="[10, 20, 50]"
          :page-size.sync="pageSize"
          :total="total"
          @current-change="loadList"
          @size-change="onSizeChange"
        />
      </div>
    </div>

    <el-dialog
      title="新建挂号"
      :visible.sync="registerDialogVisible"
      width="920px"
      append-to-body
      custom-class="register-dialog"
      @closed="resetRegisterForm"
    >
      <el-form :model="form" label-width="100px" class="form-area">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="身份证号">
              <el-input
                v-model="form.idCard"
                placeholder="身份证号"
                maxlength="18"
                @blur="handleIdCardInputFinish"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="姓名">
              <el-input v-model="form.name" placeholder="姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="出生日期">
              <el-date-picker v-model="form.birthDate" placeholder="选择日期" style="width: 100%;" value-format="yyyy-MM-dd" />
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
              <el-select v-model="form.department" placeholder="挂号科室" @change="handleDeptChange">
                <el-option v-for="dept in deptOptions" :key="dept.id" :label="dept.name" :value="dept.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="挂号号别">
              <el-select v-model="form.rankId" placeholder="请选择号别" @change="onRankChange">
                <el-option
                  v-for="r in rankOptions"
                  :key="r.id"
                  :label="rankOptionLabel(r)"
                  :value="r.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="挂号日期">
              <el-date-picker
                v-model="form.registrationDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%;"
                value-format="yyyy-MM-dd"
                @change="loadAvailableDoctors"
              />
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
              <el-select v-model="form.session" placeholder="默认" @change="loadAvailableDoctors">
                <el-option label="上午" value="上午" />
                <el-option label="下午" value="下午" />
                <el-option label="默认" value="默认" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="看诊医生">
              <el-select v-model="form.doctor" placeholder="请选择医生">
                <el-option v-for="doc in doctorOptions" :key="doc.id" :label="doc.name" :value="doc.id" />
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
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="registerDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleRegisterAndPrint">挂号并打印发票</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  createRegistration,
  getPatientByIdCard,
  getDepartments,
  getAvailableDoctors,
  getAllRegistrations,
  cancelRegistration,
  listRegistrationRanks,
  payRegistrationItems
} from '@/api/registService'

const ST = { CANCELED: 2, UNFINISHED: 0, FINISHED: 1 }
const END = { UNFINISHED: 0, FINISHED: 1 }

export default {
  name: 'Registration',
  data() {
    const now = new Date()
    const session = now.getHours() < 12 ? '上午' : '下午'
    const y = now.getFullYear()
    const m = String(now.getMonth() + 1).padStart(2, '0')
    const d = String(now.getDate()).padStart(2, '0')
    return {
      keyword: '',
      loading: false,
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      currentRow: null,
      registerDialogVisible: false,
      submitting: false,
      invoiceNo: '',
      deptOptions: [],
      doctorOptions: [],
      rankOptions: [],
      form: {
        idCard: '',
        name: '',
        birthDate: '',
        gender: '',
        address: '',
        contact: '',
        department: '',
        rankId: null,
        level: '',
        registrationDate: `${y}-${m}-${d}`,
        payment: '',
        session,
        doctor: '',
        amount: 0,
        medicalRecord: '否'
      },
    }
  },
  created() {
    this.loadDepartments()
    this.loadRegistrationRanks()
    this.loadList()
  },
  watch: {
    '$route.path'(p) {
      if (p === '/charge/registration') {
        this.loadList()
      }
    }
  },
  methods: {
    /** 将 status / endAttendance / bindStatus 规范为 number 或 null */
    normalizeRegistrationRow(row) {
      if (!row || typeof row !== 'object') return row
      const toInt = v => {
        if (v == null || v === '') return null
        const n = Number(v)
        return Number.isNaN(n) ? null : n
      }
      return {
        ...row,
        status: toInt(row.status),
        endAttendance: toInt(row.endAttendance),
        bindStatus: toInt(row.bindStatus)
      }
    },

    formatCellDate(val) {
      if (val == null || val === '') return '—'
      if (typeof val === 'string') {
        return val.length >= 10 ? val.slice(0, 10) : val
      }
      return this.formatDate(val)
    },

    cashierStatusLabel(row) {
      if (!row) return '—'
      if (row.status === ST.CANCELED) return '已取消'
      if (row.bindStatus === 1) {
        return row.endAttendance === END.FINISHED ? '已缴费' : '诊中·已缴'
      }
      if (row.endAttendance === END.FINISHED) return '诊毕·待缴'
      if (row.status === ST.FINISHED && row.endAttendance === END.UNFINISHED) return '诊中·待缴'
      return '未看诊'
    },

    cashierTagType(row) {
      if (!row) return 'info'
      if (row.status === ST.CANCELED) return 'info'
      if (row.bindStatus === 1) return 'success'
      if (row.endAttendance === END.FINISHED) return 'warning'
      if (row.status === ST.FINISHED && row.endAttendance === END.UNFINISHED) return 'warning'
      return 'primary'
    },

    canCancel(row) {
      if (!row || row.status === ST.CANCELED) return false
      return row.status === ST.UNFINISHED && row.endAttendance === END.UNFINISHED
    },

    handleCurrentRow(row) {
      this.currentRow = row
    },

    handleSearch() {
      this.currentPage = 1
      this.loadList()
    },

    onSizeChange() {
      this.currentPage = 1
      this.loadList()
    },

    async loadList() {
      this.loading = true
      try {
        const res = await getAllRegistrations({
          page: Math.max(0, this.currentPage - 1),
          size: this.pageSize,
          keyword: (this.keyword || '').trim() || undefined
        })
        if (res.code !== 20000) {
          this.$message.error(res.message || '查询失败')
          this.tableData = []
          this.total = 0
          return
        }
        const payload = res.data || {}
        const raw = Array.isArray(payload.records) ? payload.records : []
        // 统一为数字，避免接口偶发字符串导致 === 判断失败，一直显示「未看诊」
        this.tableData = raw.map(r => this.normalizeRegistrationRow(r))
        this.total = typeof payload.total === 'number' ? payload.total : 0
      } catch (e) {
        this.$message.error('查询挂号列表失败')
        this.tableData = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    handleCancel(row) {
      this.$confirm('确认退号？退号后不可恢复', '退号确认', { type: 'warning' })
        .then(async () => {
          try {
            const res = await cancelRegistration(row.id)
            if (res.code !== 20000) {
              this.$message.error(res.message || '退号失败')
              return
            }
            this.$message.success('退号成功')
            this.loadList()
          } catch (e) {
            this.$message.error('退号失败')
          }
        })
        .catch(() => {})
    },

    openRegisterDialog() {
      this.registerDialogVisible = true
    },

    resetRegisterForm() {
      const now = new Date()
      const session = now.getHours() < 12 ? '上午' : '下午'
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const d = String(now.getDate()).padStart(2, '0')
      this.form = {
        idCard: '',
        name: '',
        birthDate: '',
        gender: '',
        address: '',
        contact: '',
        department: '',
        rankId: null,
        level: '',
        registrationDate: `${y}-${m}-${d}`,
        payment: '',
        session,
        doctor: '',
        amount: 0,
        medicalRecord: '否'
      }
      this.doctorOptions = []
    },

    handleReadCard() {
      this.$message.info('请接入读卡器后读取证件号；演示模式下请在挂号弹窗中手动输入身份证号')
      this.openRegisterDialog()
    },

    handleReprint() {
      if (!this.currentRow) {
        this.$message.warning('请先在表格中选中一行记录')
        return
      }
      if (this.currentRow.status === ST.CANCELED) {
        this.$message.warning('已取消记录无法打印发票')
        return
      }
      this.openPrintWindowFromRow(this.currentRow)
    },

    handleComplementPrint() {
      this.handleReprint()
    },

    openPrintWindowFromRow(row) {
      const deptName = row.deptName || '—'
      const win = window.open('', '_blank')
      if (!win) {
        this.$message.error('浏览器阻止了弹窗，请允许弹窗后重试')
        return
      }
      const patientName = row.patientName || '—'
      const medicalNo = row.medicalRecordNo || '—'
      const date = this.formatCellDate(row.attendanceDate)
      const html = `
        <!doctype html><html><head><meta charset="utf-8"><title>挂号发票</title>
        <style>
          body { font-family: Arial, "Microsoft Yahei"; padding:20px; }
          .invoice { width:600px; margin:0 auto; }
          .header { text-align:center; margin-bottom:20px; }
          .row { display:flex; justify-content:space-between; margin:6px 0; }
          .label { color:#666; }
          .value { font-weight:600; }
          .foot { margin-top:30px; text-align:center; color:#999; font-size:12px; }
        </style></head><body>
        <div class="invoice">
          <div class="header"><h2>医院挂号发票（补打）</h2></div>
          <div class="row"><span class="label">病历号：</span><span class="value">${medicalNo}</span></div>
          <div class="row"><span class="label">姓名：</span><span class="value">${patientName}</span></div>
          <div class="row"><span class="label">科室：</span><span class="value">${deptName}</span></div>
          <div class="row"><span class="label">看诊日期：</span><span class="value">${date}</span></div>
          <div class="foot">此票据仅供核对使用</div>
        </div></body></html>`
      win.document.open()
      win.document.write(html)
      win.document.close()
      win.print()
    },

    getDeptNameById(id) {
      if (id == null || !Array.isArray(this.deptOptions)) return ''
      const found = this.deptOptions.find(d => String(d.id) === String(id))
      return found ? found.name : ''
    },

    getDoctorNameById(id) {
      if (id == null || !Array.isArray(this.doctorOptions)) return ''
      const found = this.doctorOptions.find(d => String(d.id) === String(id))
      return found ? found.name : ''
    },

    async loadDepartments() {
      try {
        const res = await getDepartments()
        if (res.code === 20000 && Array.isArray(res.data)) {
          this.deptOptions = res.data
        }
      } catch (e) {
        this.$message.error('获取科室列表失败')
      }
    },

    async loadRegistrationRanks() {
      try {
        const res = await listRegistrationRanks()
        if (res.code === 20000 && Array.isArray(res.data)) {
          this.rankOptions = res.data
        } else {
          this.rankOptions = []
        }
      } catch (e) {
        this.rankOptions = []
        this.$message.error('获取挂号号别失败，请确认已执行 document/sql/sms_registration_rank.sql')
      }
    },

    rankOptionLabel(r) {
      if (!r) return ''
      const p = this.fmtMoney(r.price)
      return `${r.name || '—'}（¥${p}）`
    },

    onRankChange(rankId) {
      const r = this.rankOptions.find(x => x.id === rankId)
      if (r) {
        this.form.level = r.name || ''
        this.form.amount = r.price != null ? Number(r.price) : 0
      } else {
        this.form.level = ''
        this.form.amount = 0
      }
    },

    async handleDeptChange(deptId) {
      this.form.doctor = ''
      this.doctorOptions = []
      if (!deptId) return
      await this.loadAvailableDoctors()
    },

    async handleIdCardInputFinish() {
      const idCard = (this.form.idCard || '').trim()
      if (!idCard || idCard.length !== 18) return
      try {
        // 身份证号 18 位超出 JS 安全整数，禁止 Number()，否则末尾数位会被篡改（如 …222 → …200）
        const res = await getPatientByIdCard(idCard)
        if (res.code !== 20000 || !res.data) {
          this.$message.warning(res.message || '未查到该患者信息，将按新患者处理')
          return
        }
        const p = res.data
        this.form.name = p.name || ''
        this.form.birthDate = p.dateOfBirth || ''
        if (p.gender === 1) this.form.gender = '男'
        else if (p.gender === 0) this.form.gender = '女'
        this.form.address = p.homeAddress || ''
        this.form.contact = p.phoneNo || ''
        if (p.medicalRecordNo) this.form.medicalRecord = '是'
        this.$message.success('已根据身份证号加载患者信息')
      } catch (e) {
        this.$message.error('查询患者信息失败')
      }
    },

    formatDate(d) {
      if (!d) return ''
      if (typeof d === 'string') return d.length >= 10 ? d.slice(0, 10) : d
      const dt = new Date(d)
      if (Number.isNaN(dt.getTime())) return ''
      const y = dt.getFullYear()
      const m = String(dt.getMonth() + 1).padStart(2, '0')
      const day = String(dt.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    },

    async loadAvailableDoctors() {
      const deptId = this.form.department
      const date = this.form.registrationDate
      const session = this.form.session
      if (!deptId || !date || !session || session === '默认') {
        this.doctorOptions = []
        this.form.doctor = ''
        return
      }
      try {
        const res = await getAvailableDoctors({ deptId, date, session })
        if (res.code === 20000 && Array.isArray(res.data)) {
          this.doctorOptions = res.data
          if (!this.doctorOptions.find(doc => doc.id === this.form.doctor)) {
            this.form.doctor = ''
          }
        } else {
          this.doctorOptions = []
          this.form.doctor = ''
        }
      } catch (e) {
        this.$message.error('获取可挂号医生失败')
        this.doctorOptions = []
        this.form.doctor = ''
      }
    },

    openPrintWindow(regInfo = {}) {
      const win = window.open('', '_blank')
      if (!win) {
        this.$message.error('浏览器阻止了弹窗，请允许弹窗后重试')
        return
      }
      const invoiceNo = regInfo.invoiceNo || this.invoiceNo || '—'
      const patientName = regInfo.name || this.form.name || '—'
      const idCard = regInfo.idCard || this.form.idCard || '—'
      const dept = regInfo.departmentName || this.getDeptNameById(this.form.department) || '—'
      const doctor = regInfo.doctorName || this.getDoctorNameById(this.form.doctor) || '—'
      const date = regInfo.registrationDate || this.form.registrationDate || '—'
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
      win.print()
    },

    fmtMoney(v) {
      const n = Number(v)
      if (!Number.isFinite(n)) return '0.00'
      return n.toFixed(2)
    },

    async handleRegisterAndPrint() {
      if (!this.form.name || !this.form.idCard || !this.form.department || !this.form.registrationDate) {
        this.$message.warning('请填写姓名、身份证号、科室和挂号日期')
        return
      }
      if (this.form.rankId == null || this.form.rankId === '') {
        this.$message.warning('请选择挂号号别')
        return
      }

      const payload = {
        // 以字符串提交，由后端 Jackson 转为 Long，避免前端 Number 精度丢失
        identificationNo: (this.form.idCard || '').trim(),
        name: this.form.name,
        birthDate: this.form.birthDate || null,
        gender: this.form.gender,
        address: this.form.address,
        contact: this.form.contact,
        departmentId: this.form.department,
        department: this.getDeptNameById(this.form.department),
        rankId: this.form.rankId,
        level: this.form.level,
        registrationDate: this.form.registrationDate,
        payment: this.form.payment,
        session: this.form.session,
        doctorId: this.form.doctor,
        doctor: this.getDoctorNameById(this.form.doctor),
        amount: this.form.amount,
        medicalRecord: this.form.medicalRecord === '是' ? this.form.medicalRecord : ''
      }

      this.submitting = true
      try {
        const res = await createRegistration(payload)
        if (res.code !== 20000) {
          this.$message.error(res.message || '挂号失败')
          return
        }
        const regId = res.data && res.data.registrationId
        if (regId == null) {
          this.$message.error('挂号成功但未返回挂号ID')
          return
        }
        const payRes = await payRegistrationItems(regId, [])
        if (payRes.code !== 20000) {
          this.$message.error(payRes.message || '挂号费缴费失败，请至收费处处理')
          return
        }
        if (payRes.data && payRes.data.invoiceNo != null && payRes.data.invoiceNo !== '') {
          this.invoiceNo = String(payRes.data.invoiceNo)
        } else {
          this.invoiceNo = ''
        }
        this.$message.success('挂号成功')
        this.registerDialogVisible = false
        this.loadList()
        this.openPrintWindow({
          name: this.form.name,
          idCard: this.form.idCard,
          departmentName: this.getDeptNameById(this.form.department),
          doctorName: this.getDoctorNameById(this.form.doctor),
          registrationDate: this.form.registrationDate,
          amount: this.form.amount,
          invoiceNo: this.invoiceNo
        })
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.registration-workbench {
  padding: 0;
}

.workbench-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f0f2f5;
  border-radius: 8px 8px 0 0;
  border: 1px solid #e8edf5;
  border-bottom: none;
}

.toolbar-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.toolbar-link {
  color: #409eff;
  font-weight: 500;
}

.list-panel {
  background: #fff;
  border: 1px solid #e8edf5;
  border-radius: 0 0 8px 8px;
  padding: 16px;
  box-shadow: 0 8px 22px rgba(31, 54, 84, 0.06);
}

.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  flex-wrap: wrap;
  gap: 12px;
}

.list-title {
  font-size: 15px;
  font-weight: 600;
  color: #27364a;
}

.list-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-input {
  width: 220px;
}

.refresh-link {
  color: #409eff;
  margin-left: 4px;
}

.reg-table {
  width: 100%;
}

.op-danger {
  color: #f56c6c;
}

.op-muted {
  color: #c0c4cc;
  font-size: 13px;
}

.pager-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.form-area :deep(.el-select),
.form-area :deep(.el-date-editor) {
  width: 100%;
}
</style>

<style>
.register-dialog .el-dialog__body {
  padding-top: 8px;
  max-height: 70vh;
  overflow-y: auto;
}
</style>
