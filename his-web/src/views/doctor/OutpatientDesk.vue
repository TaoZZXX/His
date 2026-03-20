<template>
  <div class="outpatient-desk">
    <div class="left-panel">
      <div class="left-title">门诊队列</div>
      <div class="panel-header">
        <el-input v-model="patientQuery" placeholder="患者名" size="small" clearable @clear="fetchPatients">
          <template v-slot:append>
            <el-button icon="el-icon-refresh" @click="fetchPatients" />
          </template>
        </el-input>
      </div>

      <el-tabs v-model="activeTab" type="border-card" class="patient-tabs" @tab-click="fetchPatients">
        <el-tab-pane label="本人" name="self"></el-tab-pane>
        <el-tab-pane label="科室" name="dept"></el-tab-pane>
      </el-tabs>

      <div class="patient-section">
        <div class="section-title">
          <span>待诊患者</span>
          <el-tag size="mini" type="warning">{{ waitingPatients.length }}</el-tag>
        </div>
        <div class="patient-list">
          <el-card
            v-for="p in waitingPatients"
            :key="p.id"
            class="patient-card"
            :body-style="{ padding: '8px' }"
            @click.native="selectPatient(p)"
            :class="{ selected: selectedPatient && selectedPatient.id === p.id }"
          >
            <div class="patient-row">
              <div class="patient-id">{{ p.medicalNo }}</div>
              <div class="patient-name">{{ p.name }}</div>
              <div class="patient-age">{{ p.age !== null && p.age !== undefined ? `${p.age}岁` : '—' }}</div>
            </div>
          </el-card>
          <div v-if="!loading && waitingPatients.length === 0" class="empty">暂无数据</div>
        </div>
      </div>

      <div class="patient-section">
        <div class="section-title">
          <span>诊中患者</span>
          <el-tag size="mini" type="success">{{ doingPatients.length }}</el-tag>
        </div>
        <div class="patient-list">
          <el-card
            v-for="p in doingPatients"
            :key="p.id"
            class="patient-card doing"
            :body-style="{ padding: '8px' }"
            @click.native="selectPatient(p)"
            :class="{ selected: selectedPatient && selectedPatient.id === p.id }"
          >
            <div class="patient-row">
              <div class="patient-id">{{ p.medicalNo }}</div>
              <div class="patient-name">{{ p.name }}</div>
              <div class="patient-age">{{ p.age !== null && p.age !== undefined ? `${p.age}岁` : '—' }}</div>
            </div>
          </el-card>
          <div v-if="!loading && doingPatients.length === 0" class="empty">暂无数据</div>
        </div>
      </div>
    </div>

    <div class="right-panel">
      <div class="page-title">门诊医生工作台</div>
      <div class="top-info card">
        <div class="info-left">
          <div class="field"><label>当前病人:</label> <span>{{ selectedPatient ? selectedPatient.name : '—' }}</span></div>
          <div class="field"><label>就诊号:</label> <span>{{ selectedPatient ? selectedPatient.medicalNo : '—' }}</span></div>
          <div class="field"><label>性别:</label> <span>{{ selectedPatient ? (selectedPatient.gender || '—') : '—' }}</span></div>
          <div class="field"><label>午别:</label> <span>{{ selectedPatient ? noonToText(selectedPatient.noon) : '—' }}</span></div>
          <div class="field"><label>年龄:</label> <span>{{ selectedPatient ? selectedPatient.age : '—' }}岁</span></div>
        </div>
        <div class="info-right">
          <el-tag v-if="selectedPatient && selectedPatient.status === 0" type="warning" size="small">待诊</el-tag>
          <el-tag v-else-if="selectedPatient" type="success" size="small">诊中</el-tag>
          <el-checkbox v-model="isFinished" :disabled="!selectedPatient" @change="onFinishedChange">诊毕</el-checkbox>
        </div>
      </div>

      <el-tabs v-model="activeWorkTab" class="work-tabs">
        <el-tab-pane label="病历首页" name="record" :disabled="!selectedPatient"></el-tab-pane>
        <el-tab-pane label="检查申请" name="exam" :disabled="!selectedPatient"></el-tab-pane>
        <el-tab-pane label="检验申请" name="lab" :disabled="!selectedPatient"></el-tab-pane>
        <el-tab-pane label="门诊确诊" name="diag" :disabled="!selectedPatient"></el-tab-pane>
        <el-tab-pane label="成药处方" name="prescription" :disabled="!selectedPatient"></el-tab-pane>
        <el-tab-pane label="草药处方" name="herbal" :disabled="!selectedPatient"></el-tab-pane>
        <el-tab-pane label="处置申请" name="proc" :disabled="!selectedPatient"></el-tab-pane>
        <el-tab-pane label="患者账单" name="bill" :disabled="!selectedPatient"></el-tab-pane>
      </el-tabs>

      <div class="work-area">
        <div v-if="!selectedPatient" class="empty">请选择左侧患者后开始诊疗</div>
        <div v-else>
          <div v-if="activeWorkTab === 'record'">
          <el-row :gutter="18">
            <el-col :span="14">
              <div class="card">
                <div class="card-title">主诊信息</div>
                <el-form :model="recordForm" label-width="110px" class="record-form">
                  <el-form-item label="主诉">
                    <el-input type="textarea" v-model="recordForm.complaint" :rows="2" />
                  </el-form-item>
                  <el-form-item label="现病史">
                    <el-input type="textarea" v-model="recordForm.history" :rows="3" />
                  </el-form-item>
                  <el-form-item label="现病治疗情况">
                    <el-input type="textarea" v-model="recordForm.treatment" :rows="2" />
                  </el-form-item>
                  <el-form-item label="既往史">
                    <el-input type="textarea" v-model="recordForm.pastHistory" :rows="2" />
                  </el-form-item>
                  <el-form-item label="过敏史">
                    <el-input type="textarea" v-model="recordForm.allergy" :rows="1" />
                  </el-form-item>
                  <el-form-item label="体格检查">
                    <el-input type="textarea" v-model="recordForm.exam" :rows="2" />
                  </el-form-item>
                  <el-form-item label="发病时间">
                    <el-date-picker
                      v-model="recordForm.onsetTime"
                      type="date"
                      placeholder="选择日期"
                      style="width: 220px"
                    />
                  </el-form-item>
                </el-form>

                <div class="btn-row">
                  <el-button type="primary" @click="saveRecord">保存病历</el-button>
                  <el-button @click="quickAction">快捷操作</el-button>
                  <el-button
                    type="success"
                    :disabled="!selectedPatient || selectedPatient.status !== 0"
                    @click="startVisit"
                  >开始诊疗</el-button>
                  <el-button type="danger" @click="finishVisit">结束就诊</el-button>
                </div>
              </div>
            </el-col>

            <el-col :span="10">
              <div class="card">
                <div class="card-title">诊疗概览</div>
                <div class="summary-line"><span class="k">主诉:</span><span class="v">{{ recordForm.complaint || '—' }}</span></div>
                <div class="summary-line"><span class="k">现病史:</span><span class="v">{{ recordForm.history || '—' }}</span></div>
                <div class="summary-line"><span class="k">治疗情况:</span><span class="v">{{ recordForm.treatment || '—' }}</span></div>
                <div class="summary-line"><span class="k">既往史:</span><span class="v">{{ recordForm.pastHistory || '—' }}</span></div>
                <div class="summary-line"><span class="k">过敏史:</span><span class="v">{{ recordForm.allergy || '—' }}</span></div>
                <div class="summary-line"><span class="k">体格检查:</span><span class="v">{{ recordForm.exam || '—' }}</span></div>
                <div class="divider" />
                <el-button type="text" class="link-btn" @click="activeWorkTab='exam'">填写检查申请</el-button>
                <el-button type="text" class="link-btn" @click="activeWorkTab='lab'">填写检验申请</el-button>
                <el-button type="text" class="link-btn" @click="activeWorkTab='prescription'">开成药处方</el-button>
              </div>
            </el-col>
          </el-row>
        </div>

        <div v-else-if="activeWorkTab === 'exam'">
          <div class="card">
            <div class="card-title">检查申请</div>
            <el-form :model="examForm" label-width="90px">
              <el-form-item label="检查项目">
                <el-select v-model="examForm.noDrugId" placeholder="请选择检查项目" filterable style="width: 100%">
                  <el-option v-for="it in examDict" :key="it.id" :label="`${it.name}（${formatMoney(it.price)}元）`" :value="it.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="备注">
                <el-input v-model="examForm.remark" placeholder="请输入备注" type="textarea" :rows="3" />
              </el-form-item>
            </el-form>
            <div class="btn-row">
              <el-button type="primary" @click="saveExam">保存检查申请</el-button>
            </div>
          </div>
        </div>

        <div v-else-if="activeWorkTab === 'lab'">
          <div class="card">
            <div class="card-title">检验申请</div>
            <el-form :model="labForm" label-width="90px">
              <el-form-item label="检验项目">
                <el-select v-model="labForm.noDrugId" placeholder="请选择检验项目" filterable style="width: 100%">
                  <el-option v-for="it in labDict" :key="it.id" :label="`${it.name}（${formatMoney(it.price)}元）`" :value="it.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="备注">
                <el-input v-model="labForm.remark" placeholder="请输入备注" type="textarea" :rows="3" />
              </el-form-item>
            </el-form>
            <div class="btn-row">
              <el-button type="primary" @click="saveLab">保存检验申请</el-button>
            </div>
          </div>
        </div>

        <div v-else-if="activeWorkTab === 'diag'">
          <div class="card">
            <div class="card-title">门诊确诊</div>
            <el-form :model="diagForm" label-width="90px">
              <el-form-item label="诊断">
                <el-input v-model="diagForm.diagnosis" placeholder="请输入诊断结果" />
              </el-form-item>
              <el-form-item label="依据">
                <el-input v-model="diagForm.basis" placeholder="请输入诊断依据" type="textarea" :rows="3" />
              </el-form-item>
            </el-form>
            <div class="btn-row">
              <el-button type="primary" @click="saveDiag">保存确诊</el-button>
            </div>
          </div>
        </div>

        <div v-else-if="activeWorkTab === 'prescription'">
          <div class="card">
            <div class="card-title">成药处方</div>
            <div class="presc-top">
              <el-button type="primary" size="small" :disabled="!selectedPatient" @click="openPrescriptionDetail">处方详情</el-button>
              <span class="presc-total">处方总计: {{ formatMoney(prescriptionItemsTotal) }} 元</span>
            </div>
            <el-table :data="prescriptionItems" border style="width:100%" v-if="prescriptionItems.length">
              <el-table-column prop="name" label="药品名称" />
              <el-table-column prop="spec" label="规格" width="120" />
              <el-table-column prop="qty" label="数量" width="90" />
              <el-table-column prop="unitPrice" label="单价(元)" width="120" />
              <el-table-column label="金额(元)" width="130">
                <template slot-scope="{ row }">{{ formatMoney(row.qty * row.unitPrice) }}</template>
              </el-table-column>
              <el-table-column prop="usage" label="使用方法" width="130" />
              <el-table-column label="操作" width="90">
                <template slot-scope="{ $index }">
                  <el-button type="text" size="small" @click="removePrescriptionItem($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-else class="empty">暂无处方条目</div>
            <div class="btn-row">
              <el-button type="primary" :disabled="!selectedPatient" @click="savePrescription">保存处方</el-button>
            </div>
          </div>
        </div>

        <div v-else-if="activeWorkTab === 'herbal'">
          <div class="card">
            <div class="card-title">草药处方</div>
            <el-form :model="herbalForm" label-width="100px">
              <el-form-item label="治法">
                <el-input v-model="herbalForm.therapy" />
              </el-form-item>
              <el-form-item label="治法详情">
                <el-input v-model="herbalForm.therapyDetails" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item label="医嘱">
                <el-input v-model="herbalForm.medicalAdvice" type="textarea" :rows="2" />
              </el-form-item>
            </el-form>
            <div class="empty" v-if="!herbalItems.length">暂无草药明细（可先在后端补齐后再扩展前端编辑）</div>
            <el-table :data="herbalItems" border style="width:100%" v-else>
              <el-table-column prop="drugId" label="药品ID" width="120" />
              <el-table-column prop="totalNum" label="总数量" width="120" />
              <el-table-column prop="usageNum" label="单次用量" width="120" />
              <el-table-column prop="medicalAdvice" label="医嘱" />
            </el-table>
            <div class="btn-row">
              <el-button type="primary" @click="saveHerbalPrescription">保存草药处方</el-button>
            </div>
          </div>
        </div>

        <div v-else-if="activeWorkTab === 'proc'">
          <div class="card">
            <div class="card-title">处置申请</div>
            <div class="empty">当前系统未实现处置申请</div>
          </div>
        </div>

        <div v-else-if="activeWorkTab === 'bill'">
          <div class="card">
            <div class="card-title">患者账单</div>
            <el-descriptions border :column="1">
              <el-descriptions-item label="检查/检验金额">{{ formatMoney(billSummary.examAmount) }} 元</el-descriptions-item>
              <el-descriptions-item label="成药处方金额">{{ formatMoney(billSummary.medicineAmount) }} 元</el-descriptions-item>
              <el-descriptions-item label="草药处方金额">{{ formatMoney(billSummary.herbalAmount) }} 元</el-descriptions-item>
              <el-descriptions-item label="总金额"><strong>{{ formatMoney(billSummary.totalAmount) }} 元</strong></el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </div>
      </div>

      <!-- 处方详情弹窗（按模板图 3） -->
      <el-dialog title="处方详情" :visible.sync="prescriptionDialogVisible" width="96%">
        <el-row :gutter="14">
          <el-col :span="7">
            <div class="dialog-left">
              <el-input
                v-model="prescriptionDetailSearch"
                size="small"
                placeholder="搜索药品/商品名"
                clearable
              />
              <div class="dialog-left-title">常用药品</div>
              <el-table
                :data="filteredMedicineList"
                size="mini"
                border
                height="520"
                style="margin-top:8px"
              >
                <el-table-column prop="name" label="商品名" />
                <el-table-column prop="price" label="价格(元)" width="100" />
                <el-table-column label="操作" width="80">
                  <template slot-scope="{ row }">
                    <el-button type="text" size="small" @click="addMedicineToDetail(row)">加入</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-col>

          <el-col :span="17">
            <div class="dialog-right">
              <div class="dialog-right-toolbar">
                <div class="muted">当前已选: {{ prescriptionDetailItems.length }} 项</div>
                <div class="muted">合计: {{ formatMoney(prescriptionDetailTotal) }} 元</div>
              </div>

              <el-table :data="prescriptionDetailItems" border style="width:100%" height="500">
                <el-table-column prop="name" label="药品名" />
                <el-table-column prop="spec" label="规格" width="120" />
                <el-table-column prop="qty" label="数量" width="90" />
                <el-table-column prop="unitPrice" label="单价(元)" width="120" />
                <el-table-column label="金额(元)" width="130">
                  <template slot-scope="{ row }">{{ formatMoney(row.qty * row.unitPrice) }}</template>
                </el-table-column>
                <el-table-column prop="usage" label="使用方式" width="140" />
                <el-table-column label="操作" width="90">
                  <template slot-scope="{ $index }">
                    <el-button type="text" size="small" @click="removeDetailItem($index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>

              <div class="dialog-pagination">
                <el-pagination layout="prev, pager, next" :total="187" :page-size="10" />
              </div>

              <span slot="footer" class="dialog-footer">
                <el-button @click="prescriptionDialogVisible = false">关闭</el-button>
                <el-button type="primary" @click="confirmPrescriptionDetail">确认开方</el-button>
              </span>
            </div>
          </el-col>
        </el-row>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import {
  finishDoctorDeskVisit,
  getDoctorDeskContext,
  getDoctorDeskMedicines,
  getDoctorDeskNonDrugDict,
  getDoctorDeskPatients,
  saveDoctorDeskCaseHistory,
  saveDoctorDeskDiagnosis,
  saveDoctorDeskHerbalPrescription,
  saveDoctorDeskMedicinePrescription,
  saveDoctorDeskNonDrugItem,
  startDoctorDeskVisit
} from '@/api/doctorDesk'

export default {
  name: 'OutpatientDesk',
  data() {
    return {
      loading: false,
      patientQuery: '',
      activeTab: 'self',
      activeWorkTab: 'record',
      waitingPatients: [],
      doingPatients: [],
      selectedPatient: null,
      isFinished: false,
      recordForm: {
        complaint: '',
        history: '',
        treatment: '',
        pastHistory: '',
        allergy: '',
        exam: '',
        onsetTime: ''
      },
      examForm: {
        noDrugId: null,
        remark: ''
      },
      labForm: {
        noDrugId: null,
        remark: ''
      },
      diagForm: {
        diagnosis: '',
        basis: ''
      },

      // 处方详情弹窗
      prescriptionDialogVisible: false,
      prescriptionDetailSearch: '',
      prescriptionItems: [],
      prescriptionDetailItems: [],
      medicines: [],
      examDict: [],
      labDict: [],
      herbalItems: [],
      herbalForm: {
        therapy: '',
        therapyDetails: '',
        medicalAdvice: '',
        pairNum: 1,
        frequency: 1,
        usageMeans: 1
      },
      billSummary: {
        examAmount: 0,
        medicineAmount: 0,
        herbalAmount: 0,
        totalAmount: 0
      }
    }
  },
  mounted() {
    this.fetchPatients()
    this.loadDicts()
  },
  computed: {
    filteredMedicineList() {
      const kw = (this.prescriptionDetailSearch || '').trim()
      if (!kw) return this.medicines
      return (this.medicines || []).filter(m => (m.name || '').includes(kw))
    },
    prescriptionItemsTotal() {
      const items = this.prescriptionItems || []
      return items.reduce((sum, it) => sum + (Number(it.qty) || 0) * (Number(it.unitPrice) || 0), 0)
    },
    prescriptionDetailTotal() {
      const items = this.prescriptionDetailItems || []
      return items.reduce((sum, it) => sum + (Number(it.qty) || 0) * (Number(it.unitPrice) || 0), 0)
    }
  },
  methods: {
    async loadDicts() {
      try {
        const [medRes, examRes, labRes] = await Promise.all([
          getDoctorDeskMedicines(),
          getDoctorDeskNonDrugDict(1),
          getDoctorDeskNonDrugDict(3)
        ])
        this.medicines = (medRes && medRes.data) || []
        this.examDict = (examRes && examRes.data) || []
        this.labDict = (labRes && labRes.data) || []
      } catch (e) {
        console.error(e)
      }
    },
    async fetchPatients() {
      this.loading = true
      this.selectedPatient = null
      try {
        const scope = this.activeTab === 'dept' ? 'dept' : 'self'
        const res = await getDoctorDeskPatients({
          scope,
          keyword: this.patientQuery
        })
        if (!res || res.code !== 20000) {
          this.$message.error((res && res.message) || '获取患者列表失败')
          return
        }
        const data = res.data || {}
        this.waitingPatients = data.waitingPatients || []
        this.doingPatients = data.doingPatients || []
      } catch (e) {
        console.error(e)
        this.$message.error('获取患者列表失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    async selectPatient(p) {
      this.selectedPatient = p
      this.isFinished = p && p.endAttendance === 1
      // 切换患者时清空各类表单/处方（病历保存接口当前未实现）
      this.prescriptionItems = []
      this.prescriptionDetailItems = []
      this.recordForm = { complaint: '', history: '', treatment: '', pastHistory: '', allergy: '', exam: '', onsetTime: '' }
      this.examForm = { noDrugId: null, remark: '' }
      this.labForm = { noDrugId: null, remark: '' }
      this.diagForm = { diagnosis: '', basis: '' }
      this.herbalItems = []
      this.herbalForm = { therapy: '', therapyDetails: '', medicalAdvice: '', pairNum: 1, frequency: 1, usageMeans: 1 }
      this.billSummary = { examAmount: 0, medicineAmount: 0, herbalAmount: 0, totalAmount: 0 }
      await this.loadContext()
    },
    async loadContext() {
      if (!this.selectedPatient) return
      try {
        const res = await getDoctorDeskContext(this.selectedPatient.id)
        if (!res || res.code !== 20000) return
        const d = res.data || {}
        const ch = d.caseHistory || {}
        this.recordForm = {
          complaint: ch.chiefComplaint || '',
          history: ch.historyOfPresentIllness || '',
          treatment: ch.historyOfTreatment || '',
          pastHistory: ch.pastHistory || '',
          allergy: ch.allergies || '',
          exam: ch.healthCheckup || '',
          onsetTime: ch.startDate || ''
        }
        this.diagForm = {
          diagnosis: d.diagnosis || '',
          basis: d.diagnosisBasis || ''
        }
        const exam = Array.isArray(d.examItems) && d.examItems.length ? d.examItems[0] : {}
        const lab = Array.isArray(d.labItems) && d.labItems.length ? d.labItems[0] : {}
        this.examForm = { noDrugId: exam.noDrugId || null, remark: exam.demand || '' }
        this.labForm = { noDrugId: lab.noDrugId || null, remark: lab.demand || '' }
        const medPres = Array.isArray(d.medicinePrescriptions) ? d.medicinePrescriptions : []
        this.prescriptionItems = medPres.length ? (medPres[0].items || []) : []
        const herbPres = Array.isArray(d.herbalPrescriptions) ? d.herbalPrescriptions : []
        this.herbalItems = herbPres.length ? (herbPres[0].items || []) : []
        if (herbPres.length) {
          this.herbalForm.therapy = herbPres[0].therapy || ''
          this.herbalForm.medicalAdvice = herbPres[0].medicalAdvice || ''
        }
        this.billSummary = d.billSummary || this.billSummary
      } catch (e) {
        console.error(e)
      }
    },
    async saveRecord() {
      if (!this.selectedPatient) {
        this.$message.warning('请选择患者')
        return
      }
      const payload = {
        chiefComplaint: this.recordForm.complaint,
        historyOfPresentIllness: this.recordForm.history,
        historyOfTreatment: this.recordForm.treatment,
        pastHistory: this.recordForm.pastHistory,
        allergies: this.recordForm.allergy,
        healthCheckup: this.recordForm.exam,
        startDate: this.recordForm.onsetTime
      }
      const res = await saveDoctorDeskCaseHistory(this.selectedPatient.id, payload)
      if (res && res.code === 20000) {
        this.$message.success('保存病历成功')
        await this.loadContext()
      } else {
        this.$message.error((res && res.message) || '保存失败')
      }
    },
    quickAction() {
      this.$message.info('快捷操作（示例）')
    },
    noonToText(noon) {
      if (noon === 0) return '上午'
      if (noon === 1) return '下午'
      return '—'
    },
    async saveExam() {
      if (!this.selectedPatient) return
      if (!this.examForm.noDrugId) {
        this.$message.warning('请选择检查项目')
        return
      }
      const res = await saveDoctorDeskNonDrugItem(this.selectedPatient.id, {
        type: 1,
        noDrugId: this.examForm.noDrugId,
        remark: this.examForm.remark
      })
      if (res && res.code === 20000) {
        this.$message.success('保存检查申请成功')
        await this.loadContext()
      } else {
        this.$message.error((res && res.message) || '保存失败')
      }
    },
    async saveLab() {
      if (!this.selectedPatient) return
      if (!this.labForm.noDrugId) {
        this.$message.warning('请选择检验项目')
        return
      }
      const res = await saveDoctorDeskNonDrugItem(this.selectedPatient.id, {
        type: 3,
        noDrugId: this.labForm.noDrugId,
        remark: this.labForm.remark
      })
      if (res && res.code === 20000) {
        this.$message.success('保存检验申请成功')
        await this.loadContext()
      } else {
        this.$message.error((res && res.message) || '保存失败')
      }
    },
    async saveDiag() {
      if (!this.selectedPatient) return
      const res = await saveDoctorDeskDiagnosis(this.selectedPatient.id, {
        diagnosis: this.diagForm.diagnosis,
        basis: this.diagForm.basis
      })
      if (res && res.code === 20000) {
        this.$message.success('保存确诊成功')
        await this.loadContext()
      } else {
        this.$message.error((res && res.message) || '保存失败')
      }
    },
    async savePrescription() {
      if (!this.selectedPatient) return
      if (!this.prescriptionItems.length) {
        this.$message.warning('请先添加处方明细')
        return
      }
      const items = this.prescriptionItems.map(it => ({
        drugId: it.drugId,
        qty: it.qty || 1,
        usageMeans: it.usageMeans || 1,
        frequency: it.frequency || 1,
        medicalAdvice: it.medicalAdvice || ''
      }))
      const res = await saveDoctorDeskMedicinePrescription(this.selectedPatient.id, {
        name: this.selectedPatient.name,
        items
      })
      if (res && res.code === 20000) {
        this.$message.success('保存处方成功')
        await this.loadContext()
      } else {
        this.$message.error((res && res.message) || '保存失败')
      }
    },
    openPrescriptionDetail() {
      this.prescriptionDetailSearch = ''
      this.prescriptionDetailItems = []
      this.prescriptionDialogVisible = true
    },
    addMedicineToDetail(med) {
      this.prescriptionDetailItems.push({
        drugId: med.id,
        name: med.name,
        spec: med.format,
        qty: 1,
        unitPrice: med.price,
        usage: med.unit || '',
        usageMeans: 1,
        frequency: 1
      })
    },
    removeDetailItem(index) {
      this.prescriptionDetailItems.splice(index, 1)
    },
    confirmPrescriptionDetail() {
      this.prescriptionItems = this.prescriptionItems.concat(this.prescriptionDetailItems)
      this.prescriptionDialogVisible = false
      this.$message.success('处方已加入')
    },
    removePrescriptionItem(index) {
      this.prescriptionItems.splice(index, 1)
    },
    async saveHerbalPrescription() {
      if (!this.selectedPatient) return
      if (!this.herbalItems.length) {
        this.$message.warning('请先添加草药明细')
        return
      }
      const items = this.herbalItems.map(it => ({
        drugId: it.drugId,
        totalNum: it.totalNum || 1,
        usageNum: it.usageNum || 1,
        usageNumUnit: it.usageNumUnit || 1,
        medicalAdvice: it.medicalAdvice || '',
        footnote: it.footnote || ''
      }))
      const res = await saveDoctorDeskHerbalPrescription(this.selectedPatient.id, {
        name: this.selectedPatient.name,
        therapy: this.herbalForm.therapy,
        therapyDetails: this.herbalForm.therapyDetails,
        medicalAdvice: this.herbalForm.medicalAdvice,
        pairNum: this.herbalForm.pairNum,
        frequency: this.herbalForm.frequency,
        usageMeans: this.herbalForm.usageMeans,
        items
      })
      if (res && res.code === 20000) {
        this.$message.success('保存草药处方成功')
        await this.loadContext()
      } else {
        this.$message.error((res && res.message) || '保存失败')
      }
    },
    formatMoney(v) {
      const n = Number(v)
      if (Number.isNaN(n)) return '0.00'
      return n.toFixed(2)
    },
    async startVisit() {
      if (!this.selectedPatient) {
        this.$message.warning('请选择患者')
        return
      }
      try {
        await startDoctorDeskVisit(this.selectedPatient.id)
        this.$message.success('已开始诊疗')
        this.selectedPatient = null
        this.isFinished = false
        await this.fetchPatients()
      } catch (e) {
        console.error(e)
        this.$message.error('开始诊疗失败')
      }
    },
    async finishVisit() {
      if (!this.selectedPatient) {
        this.$message.warning('请选择患者')
        return
      }
      this.$confirm('确认结束本次就诊吗？', '提示', { type: 'warning' })
        .then(async () => {
          try {
            await finishDoctorDeskVisit(this.selectedPatient.id)
            this.$message.success('就诊已结束')
            this.selectedPatient = null
            this.isFinished = false
            await this.fetchPatients()
          } catch (e) {
            console.error(e)
            this.$message.error('结束就诊失败')
          }
        })
        .catch(() => {})
    },
    onFinishedChange(checked) {
      if (!checked) {
        this.isFinished = false
        return
      }
      // 勾选“诊毕”即结束就诊
      this.finishVisit()
    }
  }
}
</script>

<style scoped>
.outpatient-desk {
  display: flex;
  height: calc(100vh - 96px);
  gap: 12px;
  padding: 12px;
  background: #f5f7fb;
}
.left-panel {
  width: 336px;
  padding: 12px;
  background: linear-gradient(180deg, #ffffff 0%, #fafcff 100%);
  box-sizing: border-box;
  border-radius: 12px;
  border: 1px solid #e9edf5;
  box-shadow: 0 6px 20px rgba(32, 56, 85, 0.05);
}
.left-title {
  font-size: 16px;
  font-weight: 600;
  color: #2f3a4d;
  margin-bottom: 10px;
}
.panel-header {
  margin-bottom: 10px;
}
.patient-tabs {
  margin-bottom: 12px;
}
.patient-section {
  margin-bottom: 14px;
}
.section-title {
  font-size: 13px;
  color: #409EFF;
  padding: 6px 2px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.patient-list {
  max-height: 230px;
  overflow: auto;
  padding-right: 4px;
}
.patient-card {
  cursor: pointer;
  margin-bottom: 8px;
  border-radius: 8px;
  border: 1px solid #edf2fa;
  transition: all .2s ease;
}
.patient-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(64, 90, 140, 0.12);
}
.patient-card.doing {
  background: #f6ffed;
}
.patient-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.patient-id { color: #8b95a8; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 12px; }
.patient-name { flex: 1; font-weight: 600; color: #2f3a4d; }
.patient-age { width: 60px; text-align: right; color: #7f8aa1; font-size: 12px; }
.patient-card.selected {
  border: 1px solid #5b8ff9;
  box-shadow: 0 0 0 2px rgba(91, 143, 249, .14);
}
.empty {
  padding: 24px 12px;
  color: #99a2b3;
  text-align: center;
  background: #fbfcfe;
  border: 1px dashed #e6ebf2;
  border-radius: 8px;
}

.right-panel {
  flex: 1;
  padding: 0;
  overflow: auto;
  background: transparent;
}
.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #27364a;
  margin-bottom: 12px;
}
.top-info { display:flex; justify-content: space-between; align-items: center; margin-bottom: 12px; border-radius: 12px; }
.info-left { display:flex; gap: 16px; flex-wrap: wrap; }
.info-left .field { display:flex; gap:6px; align-items:center }
.info-left label { color: #8c95a6 }
.info-left span { color: #2f3a4d; font-weight: 500; }
.info-right { display: flex; align-items: center; gap: 10px; }
.work-tabs {
  margin-bottom: 12px;
  background: #fff;
  border: 1px solid #e9edf5;
  border-radius: 12px;
  padding: 0 12px;
}
.work-area {
  background: #fff;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid #e9edf5;
  box-shadow: 0 6px 20px rgba(32, 56, 85, 0.04);
  min-height: 420px;
}
.record-form .el-form-item { margin-bottom: 12px }

.card {
  border: 1px solid #ebeff6;
  border-radius: 10px;
  background: #fff;
  padding: 14px;
}
.card-title {
  font-weight: 600;
  margin-bottom: 12px;
  color: #2f3a4d;
  font-size: 15px;
}
.btn-row {
  display:flex;
  gap: 12px;
  margin-top: 14px;
  justify-content: flex-end;
  flex-wrap: wrap;
}
.summary-line {
  margin: 8px 0;
  line-height: 1.6;
}
.summary-line .k {
  color: #999;
  display: inline-block;
  width: 90px;
}
.summary-line .v {
  color: #333;
}
.divider {
  height: 1px;
  background: #edf1f7;
  margin: 14px 0;
}
.link-btn {
  padding: 0;
  margin: 6px 0;
  color: #409EFF;
}
.presc-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.presc-total {
  color: #999;
}
.dialog-left {
  padding-right: 8px;
  background: #fafcff;
  border: 1px solid #eef2f8;
  border-radius: 8px;
  padding: 10px;
}
.dialog-left-title {
  margin-top: 12px;
  font-weight: 600;
  color: #333;
  font-size: 13px;
}
.dialog-right {
  padding-left: 4px;
  border: 1px solid #eef2f8;
  border-radius: 8px;
  padding: 8px;
}
.dialog-right-toolbar {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}
.muted {
  color: #999;
}
.dialog-pagination {
  padding: 10px 0;
}
.dialog-footer {
  text-align: right;
}

.left-panel ::v-deep .el-tabs--border-card {
  border: 1px solid #edf2fa;
  border-radius: 8px;
  overflow: hidden;
}

.work-tabs ::v-deep .el-tabs__item.is-active {
  color: #2f6bff;
  font-weight: 600;
}

.work-tabs ::v-deep .el-tabs__active-bar {
  background-color: #2f6bff;
}
</style>
