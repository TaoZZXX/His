<template>
  <div class="cashier-workbench">
    <div class="workbench-toolbar">
      <span class="toolbar-title">门诊收费台</span>
      <div class="toolbar-actions">
        <el-input
          v-model="keyword"
          clearable
          placeholder="搜索病历号/姓名（筛选左侧列表）"
          class="search-input"
          @keyup.enter.native="handleSearch"
        />
        <el-button type="primary" icon="el-icon-search" @click="handleSearch" />
        <el-button type="text" icon="el-icon-refresh" class="refresh-link" @click="loadRecentPatients">刷新</el-button>
      </div>
    </div>

    <div class="layout-split">
      <!-- 左侧：最近患者 -->
      <aside class="recent-aside">
        <div class="aside-head">
          <span class="aside-title">最近患者</span>
          <span class="aside-hint">按最近挂号合并</span>
        </div>
        <div v-loading="loading" class="patient-scroll">
          <div
            v-for="p in recentPatients"
            :key="p.key"
            class="patient-item"
            :class="{ active: selectedKey === p.key }"
            @click="selectPatient(p)"
          >
            <div class="patient-item-top">
              <span class="p-name">{{ p.patientName || '—' }}</span>
              <span v-if="patientUnpaidCount(p) > 0" class="p-badge">{{ patientUnpaidCount(p) }} 待缴</span>
            </div>
            <div class="p-meta">病历号 {{ p.medicalRecordNo || '—' }}</div>
            <div class="p-date">最近看诊 {{ formatCellDate(latestAttendance(p)) }}</div>
          </div>
          <div v-if="!loading && !recentPatients.length" class="aside-empty">暂无挂号记录，请尝试搜索</div>
        </div>
      </aside>

      <!-- 右侧：缴费条目 -->
      <main class="detail-main">
        <template v-if="payPatientGroup">
          <div class="detail-head">
            <div class="detail-title">
              <h2>{{ payPatientGroup.patientName || '患者' }}</h2>
              <p class="detail-sub">病历号 {{ payPatientGroup.medicalRecordNo || '—' }}</p>
            </div>
            <div class="detail-actions">
              <el-button
                v-if="rowHasRefundable(payPatientGroup)"
                type="warning"
                plain
                size="small"
                @click="openRefundDialog(payPatientGroup)"
              >退费</el-button>
            </div>
          </div>
          <p class="detail-hint">以下为该患者相关挂号的收费明细。可勾选「缴纳选中」，或「缴清全部未付」。</p>
          <el-table
            ref="payableTable"
            v-loading="payLoading"
            :data="payablesList"
            border
            stripe
            size="small"
            class="pay-table"
            max-height="calc(100vh - 320px)"
            @selection-change="onPayableSelectionChange"
          >
            <el-table-column type="selection" width="48" :selectable="payableRowSelectable" />
            <el-table-column label="挂号号" width="88" align="center">
              <template slot-scope="{ row }">{{ row._registrationId }}</template>
            </el-table-column>
            <el-table-column label="看诊日期" width="108" align="center">
              <template slot-scope="{ row }">{{ formatCellDate(row._attendanceDate) }}</template>
            </el-table-column>
            <el-table-column label="科室" min-width="100" show-overflow-tooltip>
              <template slot-scope="{ row }">{{ row._deptName || '—' }}</template>
            </el-table-column>
            <el-table-column label="项目" min-width="140" show-overflow-tooltip>
              <template slot-scope="{ row }">{{ row.itemName || '—' }}</template>
            </el-table-column>
            <el-table-column label="类型" width="96" align="center">
              <template slot-scope="{ row }">{{ payableTypeLabel(row.itemType) }}</template>
            </el-table-column>
            <el-table-column label="应收" width="88" align="right">
              <template slot-scope="{ row }">{{ fmtMoney(row.amount) }}</template>
            </el-table-column>
            <el-table-column label="已收" width="88" align="right">
              <template slot-scope="{ row }">{{ fmtMoney(row.paidAmount) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="80" align="center">
              <template slot-scope="{ row }">
                <el-tag v-if="row.status === 1" type="success" size="mini">已付清</el-tag>
                <el-tag v-else type="warning" size="mini">待缴</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!payLoading && !payablesList.length" class="pay-empty">暂无分项明细。</div>
          <div class="detail-footer">
            <div v-if="unpaidTotal > 0" class="pay-total">待缴合计：<strong>¥ {{ fmtMoney(unpaidTotal) }}</strong></div>
            <div class="pay-btns">
              <el-button type="primary" plain :loading="paySubmitting" :disabled="!hasUnpaidPayables" @click="submitPayAll">
                缴清全部未付
              </el-button>
              <el-button type="primary" :loading="paySubmitting" :disabled="!selectedPayableRows.length" @click="submitPaySelected">
                缴纳选中
              </el-button>
            </div>
          </div>
        </template>
        <div v-else class="detail-empty">
          <i class="el-icon-user" />
          <p>请在左侧选择患者</p>
          <p class="detail-empty-sub">列表展示最近 {{ recentFetchSize }} 条挂号合并后的患者</p>
        </div>
      </main>
    </div>

    <el-dialog
      title="退费 — 选择挂号记录"
      :visible.sync="refundDialogVisible"
      width="720px"
      append-to-body
      @closed="onRefundDialogClosed"
    >
      <p v-if="refundPatientGroup" class="pay-dialog-hint">患者：<strong>{{ refundPatientGroup.patientName }}</strong></p>
      <el-table v-loading="refundLoading" :data="refundCandidates" border size="small" max-height="360">
        <el-table-column label="挂号号" width="88" align="center" prop="id" />
        <el-table-column label="看诊日期" width="108" align="center">
          <template slot-scope="{ row }">{{ formatCellDate(row.attendanceDate) }}</template>
        </el-table-column>
        <el-table-column prop="deptName" label="科室" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" size="small" class="op-warn" @click="confirmRefundOne(row)">退费</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!refundLoading && !refundCandidates.length" class="pay-empty">暂无可退费的已缴挂号</div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="refundDialogVisible = false">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAllRegistrations,
  payRegistration,
  payRegistrationItems,
  listRegistrationPayables,
  refundRegistration
} from '@/api/registService'

const ST = { CANCELED: 2, UNFINISHED: 0, FINISHED: 1 }

export default {
  name: 'CashierDesk',
  data() {
    return {
      keyword: '',
      loading: false,
      recentPatients: [],
      /** 左侧一次拉取的挂号条数，合并为患者 */
      recentFetchSize: 120,
      selectedKey: null,
      payPatientGroup: null,
      payLoading: false,
      paySubmitting: false,
      payablesList: [],
      selectedPayableRows: [],
      /** 左侧列表里每个患者的待缴条数（异步估算，仅作角标） */
      patientUnpaidBadge: {},
      refundDialogVisible: false,
      refundLoading: false,
      refundPatientGroup: null,
      refundCandidates: []
    }
  },
  computed: {
    hasUnpaidPayables() {
      return this.payablesList.some(p => p.status !== 1)
    },
    unpaidTotal() {
      return this.payablesList
        .filter(p => p.status !== 1)
        .reduce((s, p) => {
          const due = Number(p.amount) - Number(p.paidAmount || 0)
          return s + (Number.isFinite(due) ? due : 0)
        }, 0)
    }
  },
  created() {
    this.loadRecentPatients()
  },
  watch: {
    '$route.path'(p) {
      if (p === '/cashier/workbench') {
        this.loadRecentPatients()
      }
    }
  },
  methods: {
    patientGroupKey(row) {
      if (row.patientId != null && row.patientId !== '') {
        return `pid:${row.patientId}`
      }
      return `mr:${row.medicalRecordNo || ''}|${row.patientName || ''}`
    },

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

    /** 最近一条看诊日期（字符串比较适用于 yyyy-MM-dd） */
    latestAttendance(group) {
      const regs = group.registrations || []
      if (!regs.length) return null
      let best = regs[0].attendanceDate
      let bestId = Number(regs[0].id) || 0
      for (const r of regs) {
        const id = Number(r.id) || 0
        if (id >= bestId) {
          bestId = id
          best = r.attendanceDate
        }
      }
      return best
    },

    sortPatientGroups(groups) {
      return groups.sort((a, b) => {
        const ida = Math.max(...(a.registrations || []).map(r => Number(r.id) || 0), 0)
        const idb = Math.max(...(b.registrations || []).map(r => Number(r.id) || 0), 0)
        return idb - ida
      })
    },

    mergePatientsFromRecords(normalized) {
      const map = new Map()
      for (const r of normalized) {
        const k = this.patientGroupKey(r)
        if (!map.has(k)) {
          map.set(k, {
            key: k,
            patientId: r.patientId,
            patientName: r.patientName,
            medicalRecordNo: r.medicalRecordNo,
            genderStr: r.genderStr,
            dateOfBirth: r.dateOfBirth,
            registrations: []
          })
        }
        map.get(k).registrations.push(r)
      }
      return this.sortPatientGroups(Array.from(map.values()))
    },

    rowHasRefundable(group) {
      const regs = group.registrations || []
      return regs.some(r => this.canRefund(r))
    },

    canRefund(row) {
      if (!row || row.status === ST.CANCELED) return false
      return row.bindStatus === 1
    },

    patientUnpaidCount(group) {
      const n = this.patientUnpaidBadge[group.key]
      return typeof n === 'number' ? n : 0
    },

    handleSearch() {
      this.loadRecentPatients()
    },

    async loadRecentPatients() {
      this.loading = true
      this.patientUnpaidBadge = {}
      try {
        const res = await getAllRegistrations({
          page: 0,
          size: this.recentFetchSize,
          keyword: (this.keyword || '').trim() || undefined
        })
        if (res.code !== 20000) {
          this.$message.error(res.message || '查询失败')
          this.recentPatients = []
          return
        }
        const payload = res.data || {}
        const raw = Array.isArray(payload.records) ? payload.records : []
        const normalized = raw.map(r => this.normalizeRegistrationRow(r))
        this.recentPatients = this.mergePatientsFromRecords(normalized)

        const prevKey = this.selectedKey
        if (prevKey && this.recentPatients.some(p => p.key === prevKey)) {
          const p = this.recentPatients.find(x => x.key === prevKey)
          this.payPatientGroup = p
          await this.reloadPayablesForPatient()
        } else if (this.recentPatients.length) {
          this.selectPatient(this.recentPatients[0])
        } else {
          this.selectedKey = null
          this.payPatientGroup = null
          this.payablesList = []
          this.selectedPayableRows = []
        }

        this.prefetchUnpaidBadges()
      } catch (e) {
        this.$message.error('查询挂号列表失败')
        this.recentPatients = []
      } finally {
        this.loading = false
      }
    },

    /** 左侧角标：轻量统计待缴行数（不阻塞主流程） */
    prefetchUnpaidBadges() {
      const groups = this.recentPatients.slice(0, 30)
      groups.forEach(async g => {
        try {
          const regs = await this.fetchRegsForPatient(g)
          const active = regs.filter(r => r.status !== ST.CANCELED)
          if (!active.length) return
          const results = await Promise.all(active.map(r => listRegistrationPayables(r.id)))
          let unpaid = 0
          results.forEach(res => {
            const items = res && res.code === 20000 && Array.isArray(res.data) ? res.data : []
            unpaid += items.filter(p => p.status !== 1).length
          })
          this.$set(this.patientUnpaidBadge, g.key, unpaid)
        } catch (e) {
          /* ignore */
        }
      })
    },

    selectPatient(p) {
      this.selectedKey = p.key
      this.payPatientGroup = p
      this.selectedPayableRows = []
      this.reloadPayablesForPatient()
    },

    async fetchRegsForPatient(patientGroup) {
      const kw =
        (this.keyword || '').trim() ||
        (patientGroup.medicalRecordNo || '').trim() ||
        (patientGroup.patientName || '').trim()
      if (!kw) {
        return (patientGroup.registrations || []).slice()
      }
      const res = await getAllRegistrations({ page: 0, size: 500, keyword: kw })
      if (res.code !== 20000 || !res.data) {
        return (patientGroup.registrations || []).slice()
      }
      const raw = Array.isArray(res.data.records) ? res.data.records : []
      const normalized = raw.map(r => this.normalizeRegistrationRow(r))
      const pid = patientGroup.patientId
      if (pid != null && pid !== '') {
        return normalized.filter(r => Number(r.patientId) === Number(pid))
      }
      return normalized.filter(
        r =>
          (r.medicalRecordNo || '') === (patientGroup.medicalRecordNo || '') &&
          (r.patientName || '') === (patientGroup.patientName || '')
      )
    },

    async reloadPayablesForPatient() {
      if (!this.payPatientGroup) return
      this.payLoading = true
      this.payablesList = []
      this.selectedPayableRows = []
      try {
        const regs = await this.fetchRegsForPatient(this.payPatientGroup)
        const active = regs.filter(r => r.status !== ST.CANCELED)
        if (!active.length) {
          this.$message.warning('没有有效的挂号记录')
          return
        }
        const results = await Promise.all(active.map(r => listRegistrationPayables(r.id)))
        const flat = []
        active.forEach((r, i) => {
          const res = results[i]
          const items = res && res.code === 20000 && Array.isArray(res.data) ? res.data : []
          items.forEach(p => {
            flat.push({
              ...p,
              _registrationId: r.id,
              _attendanceDate: r.attendanceDate,
              _deptName: r.deptName
            })
          })
        })
        this.payablesList = flat
        this.$set(this.patientUnpaidBadge, this.payPatientGroup.key, flat.filter(p => p.status !== 1).length)
      } catch (e) {
        this.$message.error('加载收费明细失败')
      } finally {
        this.payLoading = false
      }
    },

    onPayableSelectionChange(rows) {
      this.selectedPayableRows = rows || []
    },

    payableRowSelectable(row) {
      return row.status !== 1
    },

    payableTypeLabel(t) {
      const m = { 1: '检查/检验', 2: '成药', 3: '草药', 4: '挂号费', 5: '其它' }
      return m[t] != null ? m[t] : '—'
    },

    fmtMoney(v) {
      const n = Number(v)
      if (!Number.isFinite(n)) return '0.00'
      return n.toFixed(2)
    },

    async submitPaySelected() {
      if (!this.selectedPayableRows.length) return
      const byReg = new Map()
      for (const row of this.selectedPayableRows) {
        const rid = row._registrationId
        if (rid == null) continue
        if (!byReg.has(rid)) byReg.set(rid, [])
        byReg.get(rid).push(row.id)
      }
      this.paySubmitting = true
      try {
        for (const [rid, ids] of byReg) {
          const res = await payRegistrationItems(rid, ids)
          if (res.code !== 20000) {
            this.$message.error(res.message || `挂号 ${rid} 缴费失败`)
            await this.loadRecentPatients()
            return
          }
        }
        this.$message.success('缴费成功')
        await this.loadRecentPatients()
      } catch (e) {
        this.$message.error('缴费失败')
      } finally {
        this.paySubmitting = false
      }
    },

    async submitPayAll() {
      const regIds = [
        ...new Set(
          this.payablesList.filter(p => p.status !== 1).map(p => p._registrationId).filter(id => id != null)
        )
      ]
      if (!regIds.length) {
        this.$message.info('没有待缴项目')
        return
      }
      this.paySubmitting = true
      try {
        for (const rid of regIds) {
          const res = await payRegistration(rid)
          if (res.code !== 20000) {
            this.$message.error(res.message || `挂号 ${rid} 缴清失败`)
            await this.loadRecentPatients()
            return
          }
        }
        this.$message.success('已全部缴清')
        await this.loadRecentPatients()
      } catch (e) {
        this.$message.error('缴费失败')
      } finally {
        this.paySubmitting = false
      }
    },

    async openRefundDialog(patientGroup) {
      this.refundPatientGroup = patientGroup
      this.refundDialogVisible = true
      this.refundCandidates = []
      this.refundLoading = true
      try {
        const regs = await this.fetchRegsForPatient(patientGroup)
        this.refundCandidates = regs.filter(r => this.canRefund(r))
        if (!this.refundCandidates.length) {
          this.$message.info('暂无可退费的已缴挂号')
        }
      } catch (e) {
        this.$message.error('加载失败')
      } finally {
        this.refundLoading = false
      }
    },

    onRefundDialogClosed() {
      this.refundPatientGroup = null
      this.refundCandidates = []
    },

    confirmRefundOne(row) {
      this.$confirm('确认退费？将撤销该挂号已缴费状态', '退费确认', { type: 'warning' })
        .then(async () => {
          try {
            const res = await refundRegistration(row.id)
            if (res.code !== 20000) {
              this.$message.error(res.message || '退费失败')
              return
            }
            this.$message.success('退费成功')
            this.refundDialogVisible = false
            await this.loadRecentPatients()
          } catch (e) {
            this.$message.error('退费失败')
          }
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
.cashier-workbench {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  min-height: calc(100vh - 120px);
  padding: 0 4px 16px;
  box-sizing: border-box;
}

.workbench-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.toolbar-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-input {
  width: 280px;
}

.refresh-link {
  color: #409eff;
}

.layout-split {
  display: flex;
  flex: 1;
  min-height: 0;
  gap: 12px;
  align-items: stretch;
}

.recent-aside {
  width: 268px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  padding: 12px 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  min-height: 420px;
}

.aside-head {
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.aside-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  display: block;
}

.aside-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  display: block;
}

.patient-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  margin: 0 -4px;
  padding: 0 4px;
}

.patient-item {
  padding: 10px 10px;
  margin-bottom: 6px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
  background: #fafafa;
}

.patient-item:hover {
  border-color: #c6e2ff;
  background: #f0f9ff;
}

.patient-item.active {
  border-color: #409eff;
  background: #ecf5ff;
  box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.2);
}

.patient-item-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.p-name {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.p-badge {
  flex-shrink: 0;
  font-size: 11px;
  color: #e6a23c;
  background: #fdf6ec;
  padding: 2px 6px;
  border-radius: 10px;
}

.p-meta,
.p-date {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.aside-empty {
  text-align: center;
  color: #909399;
  font-size: 13px;
  padding: 24px 8px;
}

.detail-main {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 8px;
  padding: 16px 18px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  min-height: 420px;
}

.detail-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.detail-title h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.detail-sub {
  margin: 6px 0 0;
  font-size: 13px;
  color: #909399;
}

.detail-hint {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  margin: 0 0 12px;
}

.pay-table {
  width: 100%;
}

.pay-empty {
  font-size: 13px;
  color: #909399;
  padding: 12px 0;
}

.detail-footer {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.pay-total {
  font-size: 14px;
  color: #303133;
}

.pay-btns {
  display: flex;
  gap: 10px;
}

.detail-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  min-height: 280px;
}

.detail-empty .el-icon-user {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.35;
}

.detail-empty p {
  margin: 0;
  font-size: 15px;
}

.detail-empty-sub {
  margin-top: 8px !important;
  font-size: 12px !important;
  color: #c0c4cc;
}

.pay-dialog-hint {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  margin: 0 0 12px;
}

.op-warn {
  color: #e6a23c;
}
</style>
