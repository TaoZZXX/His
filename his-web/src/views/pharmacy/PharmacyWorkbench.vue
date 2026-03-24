<template>
  <div class="pharmacy-page">
    <div class="page-head">
      <h2 class="title">药房工作台</h2>
      <el-button type="primary" icon="el-icon-refresh" @click="refreshAll">刷新</el-button>
    </div>

    <el-alert
      v-show="queueMode === 'pending'"
      class="flow-tip"
      type="info"
      :closable="false"
      show-icon
      title="为什么开了药却看不到患者？"
      description="本系统「未发药」只列出：① 医生在「成药处方」里保存的西药处方；② 该次挂号已在收费处完成缴费（bind_status=1）。医生开始诊疗后即可缴费，无需等诊毕。仅开「草药处方」或尚未缴费时，不会出现在此列表。"
    />

    <div class="layout">
      <aside class="side">
        <div class="search-row">
          <span class="label">查询患者名</span>
          <el-input
            v-model="keyword"
            clearable
            placeholder="姓名 / 病历号（自动筛选）"
            @keyup.enter.native="searchPatientsNow"
          />
          <el-button type="primary" icon="el-icon-search" @click="searchPatientsNow" />
        </div>
        <p class="search-hint">默认显示最近患者；输入关键字后自动查询，也可点搜索立即刷新。</p>
        <el-radio-group v-model="queueMode" size="small" class="tabs" @change="onQueueModeChange">
          <el-radio-button label="pending">未发药</el-radio-button>
          <el-radio-button label="dispensed">已发药</el-radio-button>
        </el-radio-group>
        <el-scrollbar class="patient-list">
          <div
            v-for="p in patients"
            :key="p.registrationId"
            :class="['patient-card', { active: selectedRegistrationId === p.registrationId }]"
            @click="selectPatient(p)"
          >
            <div class="muted">病历号</div>
            <div class="strong">{{ p.medicalRecordNo || '—' }}</div>
            <div class="muted">名字</div>
            <div class="strong">{{ p.patientName || '—' }}</div>
          </div>
          <div v-if="!patients.length" class="empty-hint">暂无患者</div>
        </el-scrollbar>
      </aside>

      <main class="main">
        <template v-if="selectedPatient">
          <div class="main-head">
            <div class="patient-summary">
              <span>姓名：<b>{{ selectedPatient.patientName }}</b></span>
              <span class="sep">就诊号：<b>{{ selectedPatient.registrationId }}</b></span>
            </div>
            <div class="actions">
              <el-button
                v-if="queueMode === 'pending'"
                type="success"
                :disabled="!selectedPendingIds.length"
                @click="onConfirmDispense"
              >
                确认发药
              </el-button>
              <el-button
                v-else
                type="danger"
                plain
                :disabled="!selectedRefundableIds.length"
                @click="onBulkRefund"
              >
                退药
              </el-button>
            </div>
          </div>

          <el-table
            v-loading="detailLoading"
            :data="detailRows"
            border
            stripe
            class="rx-table"
            :span-method="spanMethod"
            @selection-change="onSelectionChange"
          >
            <el-table-column type="selection" width="48" :selectable="rowSelectable" />
            <el-table-column prop="prescriptionId" label="处方号" width="88" align="center" />
            <el-table-column prop="prescriptionName" label="处方名" min-width="120" show-overflow-tooltip />
            <el-table-column prop="drugName" label="药品名" min-width="180" show-overflow-tooltip />
            <el-table-column prop="num" label="药品数量" width="92" align="center" />
            <el-table-column prop="refundNum" label="已退药数量" width="100" align="center">
              <template slot-scope="{ row }">{{ row.refundNum != null ? row.refundNum : 0 }}</template>
            </el-table-column>
            <el-table-column label="单价" width="88" align="right">
              <template slot-scope="{ row }">{{ formatPrice(row.unitPrice) }}</template>
            </el-table-column>
            <el-table-column prop="orderDoctorName" label="开立医生" width="100" show-overflow-tooltip>
              <template slot-scope="{ row }">{{ row.orderDoctorName || '—' }}</template>
            </el-table-column>
            <el-table-column prop="frequency" label="频次" width="72" align="center">
              <template slot-scope="{ row }">{{ row.frequency != null ? row.frequency : '—' }}</template>
            </el-table-column>
            <el-table-column prop="medicalAdvice" label="使用建议" min-width="100" show-overflow-tooltip>
              <template slot-scope="{ row }">{{ row.medicalAdvice || '—' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template slot-scope="{ row }">
                <el-tag v-if="row.itemStatus === 2" type="info" size="small">已退药</el-tag>
                <el-tag v-else-if="row.currentNum > 0" type="info" size="small" effect="plain">未取药</el-tag>
                <el-tag v-else type="warning" size="small" effect="plain">已取药</el-tag>
              </template>
            </el-table-column>
            <el-table-column v-if="queueMode === 'dispensed'" label="操作" width="100" align="center" fixed="right">
              <template slot-scope="{ row }">
                <el-button
                  v-if="row.itemStatus !== 2"
                  type="danger"
                  size="mini"
                  plain
                  @click="refundOne(row)"
                >
                  退药
                </el-button>
                <span v-else>—</span>
              </template>
            </el-table-column>
          </el-table>
        </template>
        <div v-else class="empty-main">请从左侧选择患者</div>
      </main>
    </div>
  </div>
</template>

<script>
import {
  getPharmacyPatients,
  getPharmacyMedicineLines,
  dispensePharmacyItems,
  refundPharmacyItems
} from '@/api/pharmacy'

export default {
  name: 'PharmacyWorkbench',
  data() {
    return {
      keyword: '',
      _patientKwTimer: null,
      queueMode: 'pending',
      patients: [],
      selectedRegistrationId: null,
      selectedPatient: null,
      detailRows: [],
      detailLoading: false,
      tableSelection: []
    }
  },
  computed: {
    selectedPendingIds() {
      return this.tableSelection.filter(r => r.itemStatus !== 2 && r.currentNum > 0).map(r => r.itemId)
    },
    selectedRefundableIds() {
      return this.tableSelection.filter(r => r.itemStatus !== 2).map(r => r.itemId)
    }
  },
  created() {
    this.loadPatients()
  },
  watch: {
    keyword() {
      this.schedulePatientSearch()
    }
  },
  beforeDestroy() {
    if (this._patientKwTimer) {
      clearTimeout(this._patientKwTimer)
      this._patientKwTimer = null
    }
  },
  methods: {
    schedulePatientSearch() {
      if (this._patientKwTimer) clearTimeout(this._patientKwTimer)
      this._patientKwTimer = setTimeout(() => {
        this._patientKwTimer = null
        this.loadPatients()
      }, 400)
    },
    searchPatientsNow() {
      if (this._patientKwTimer) {
        clearTimeout(this._patientKwTimer)
        this._patientKwTimer = null
      }
      this.loadPatients()
    },
    formatPrice(v) {
      if (v === null || v === undefined) return '—'
      return Number(v).toFixed(2)
    },
    async loadPatients() {
      try {
        const res = await getPharmacyPatients(this.queueMode, this.keyword)
        if (!res || res.code !== 20000) {
          this.$message.error((res && res.message) || '加载失败')
          this.patients = []
          return
        }
        this.patients = Array.isArray(res.data) ? res.data : []
        if (
          this.selectedRegistrationId &&
          !this.patients.some(p => p.registrationId === this.selectedRegistrationId)
        ) {
          this.selectedRegistrationId = null
          this.selectedPatient = null
          this.detailRows = []
        }
      } catch (e) {
        this.$message.error('加载失败')
        this.patients = []
      }
    },
    onQueueModeChange() {
      this.selectedRegistrationId = null
      this.selectedPatient = null
      this.detailRows = []
      this.searchPatientsNow()
    },
    async selectPatient(p) {
      this.selectedRegistrationId = p.registrationId
      this.selectedPatient = p
      this.detailLoading = true
      this.tableSelection = []
      try {
        const res = await getPharmacyMedicineLines(p.registrationId)
        if (!res || res.code !== 20000) {
          this.$message.error((res && res.message) || '加载处方失败')
          this.detailRows = []
          return
        }
        this.detailRows = Array.isArray(res.data) ? res.data : []
      } catch (e) {
        this.$message.error('加载处方失败')
        this.detailRows = []
      } finally {
        this.detailLoading = false
      }
    },
    spanMethod({ row, column, rowIndex, columnIndex }) {
      const mergeCols = [1, 2]
      if (!mergeCols.includes(columnIndex)) {
        return { rowspan: 1, colspan: 1 }
      }
      const rows = this.detailRows
      if (!rows.length) return { rowspan: 1, colspan: 1 }
      if (rowIndex === 0 || rows[rowIndex].prescriptionId !== rows[rowIndex - 1].prescriptionId) {
        let rowspan = 1
        for (let i = rowIndex + 1; i < rows.length; i++) {
          if (rows[i].prescriptionId === row.prescriptionId) rowspan++
          else break
        }
        return { rowspan, colspan: 1 }
      }
      return { rowspan: 0, colspan: 0 }
    },
    rowSelectable(row) {
      if (row.itemStatus === 2) return false
      if (this.queueMode === 'pending') {
        return row.currentNum > 0
      }
      return true
    },
    onSelectionChange(val) {
      this.tableSelection = val || []
    },
    async refreshAll() {
      await this.loadPatients()
      if (this.selectedPatient) {
        await this.selectPatient(this.selectedPatient)
      }
    },
    onConfirmDispense() {
      const ids = this.selectedPendingIds
      if (!ids.length) {
        this.$message.warning('请勾选待发药明细')
        return
      }
      this.$confirm('确认对所选药品完成发药？', '提示', { type: 'warning' })
        .then(async () => {
          const res = await dispensePharmacyItems(ids)
          if (!res || res.code !== 20000) {
            this.$message.error((res && res.message) || '发药失败')
            return
          }
          this.$message.success((res && res.message) || '发药成功')
          await this.refreshAll()
        })
        .catch(() => {})
    },
    onBulkRefund() {
      const ids = this.selectedRefundableIds
      if (!ids.length) {
        this.$message.warning('请勾选要退药的明细')
        return
      }
      this.$confirm('确认退药？（演示版不联动收银退费）', '提示', { type: 'warning' })
        .then(async () => {
          const res = await refundPharmacyItems(ids)
          if (!res || res.code !== 20000) {
            this.$message.error((res && res.message) || '退药失败')
            return
          }
          this.$message.success((res && res.message) || '已退药')
          await this.refreshAll()
        })
        .catch(() => {})
    },
    refundOne(row) {
      if (!row || row.itemStatus === 2) return
      this.$confirm('确认对该药品退药？', '提示', { type: 'warning' })
        .then(async () => {
          const res = await refundPharmacyItems([row.itemId])
          if (!res || res.code !== 20000) {
            this.$message.error((res && res.message) || '退药失败')
            return
          }
          this.$message.success((res && res.message) || '已退药')
          await this.refreshAll()
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
.pharmacy-page {
  padding: 16px 20px 24px;
  background: #fff;
  min-height: 100%;
}
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.page-head .title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}
.flow-tip {
  margin-bottom: 14px;
}
.flow-tip ::v-deep .el-alert__description {
  margin-top: 6px;
  line-height: 1.55;
}
.layout {
  display: flex;
  gap: 16px;
  min-height: 520px;
}
.side {
  width: 280px;
  flex-shrink: 0;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px;
  background: #fafafa;
}
.search-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.search-row .label {
  white-space: nowrap;
  font-size: 13px;
  color: #606266;
}
.search-row .el-input {
  flex: 1;
}
.search-hint {
  font-size: 12px;
  color: #909399;
  margin: 0 0 10px;
  line-height: 1.4;
}
.tabs {
  margin-bottom: 10px;
  display: flex;
  width: 100%;
}
.tabs ::v-deep .el-radio-button__inner {
  width: 120px;
}
.patient-list {
  height: 420px;
}
.patient-card {
  padding: 10px 12px;
  margin-bottom: 8px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  background: #fff;
  cursor: pointer;
}
.patient-card:hover {
  border-color: #d2c5fb;
}
.patient-card.active {
  border-color: #b79bf8;
  background: #f1ebff;
}
.patient-card .muted {
  font-size: 12px;
  color: #909399;
}
.patient-card .strong {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}
.empty-hint {
  text-align: center;
  color: #909399;
  padding: 24px 0;
  font-size: 13px;
}
.main {
  flex: 1;
  min-width: 0;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px 16px;
}
.main-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
}
.patient-summary {
  font-size: 14px;
  color: #606266;
}
.patient-summary .sep {
  margin-left: 20px;
}
.empty-main {
  color: #909399;
  text-align: center;
  padding: 80px 0;
  font-size: 14px;
}
.rx-table {
  width: 100%;
}
</style>
