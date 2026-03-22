<template>
  <div class="daily-settlement-page">
    <div class="toolbar">
      <el-date-picker
        v-model="filterRange"
        type="daterange"
        range-separator="至"
        start-placeholder="起始日期"
        end-placeholder="截止日期"
        value-format="yyyy-MM-dd"
        unlink-panels
        class="toolbar-dates"
      />
      <el-button type="primary" icon="el-icon-search" @click="loadList">查询</el-button>
      <el-button icon="el-icon-printer" @click="printSheet">打印</el-button>
      <el-button v-if="!auditMode" type="success" plain icon="el-icon-document-add" @click="openGenDialog">生成日结</el-button>
      <el-button v-if="auditMode" type="danger" plain icon="el-icon-circle-check" :disabled="!canAudit" @click="doAudit">核对</el-button>
    </div>

    <div class="split">
      <div class="left-panel">
        <div class="panel-title">日结信息列表</div>
        <el-table
          ref="listTable"
          v-loading="listLoading"
          :data="list"
          highlight-current-row
          border
          height="100%"
          class="list-table"
          @current-change="onRowChange"
        >
          <el-table-column prop="operatorName" label="收款员" min-width="88" show-overflow-tooltip />
          <el-table-column label="创建时间" min-width="160">
            <template slot-scope="{ row }">{{ fmtDt(row.reportTime) }}</template>
          </el-table-column>
          <el-table-column label="日结状态" width="96" align="center">
            <template slot-scope="{ row }">
              <el-tag v-if="row.status === 1" type="success" size="small">已核对</el-tag>
              <el-tag v-else type="warning" size="small">未核对</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="right-panel">
        <div id="print-area" class="sheet-wrap">
          <template v-if="detail">
            <div class="sheet-title">医院门诊收费日结单</div>
            <div class="sheet-meta">
              <div>统计区间：{{ fmtDt(detail.rangeStart) }} — {{ fmtDt(detail.rangeEnd) }}</div>
              <div>报账人 / 收款员：{{ detail.operatorName }}（编号 {{ detail.operatorId || '—' }}）</div>
              <div>报表生成时间：{{ fmtDt(detail.reportTime) }}</div>
              <div v-if="detail.status === 1 && detail.auditTime">
                核对：{{ detail.auditorName || '—' }}　{{ fmtDt(detail.auditTime) }}
              </div>
            </div>

            <div class="sheet-section">发票信息（占位，可与发票模块对接）</div>
            <table class="sheet-grid">
              <tr>
                <td>常规发票号</td>
                <td colspan="3">{{ detail.invoiceNormal || '—' }}</td>
              </tr>
              <tr>
                <td>冲红发票号</td>
                <td colspan="3">{{ detail.invoiceRed || '—' }}</td>
              </tr>
              <tr>
                <td>重打发票号</td>
                <td colspan="3">{{ detail.invoiceReprint || '—' }}</td>
              </tr>
            </table>

            <div class="sheet-section">分项金额</div>
            <table class="sheet-grid">
              <tr>
                <td>检查金额</td>
                <td class="num">{{ money(detail.amtExam) }}</td>
                <td>检验金额</td>
                <td class="num">{{ money(detail.amtLab) }}</td>
              </tr>
              <tr>
                <td>挂号金额</td>
                <td class="num">{{ money(detail.amtRegistration) }}</td>
                <td>成药金额</td>
                <td class="num">{{ money(detail.amtMedicine) }}</td>
              </tr>
              <tr>
                <td>草药金额</td>
                <td class="num">{{ money(detail.amtHerbal) }}</td>
                <td>处置金额</td>
                <td class="num">{{ money(detail.amtTreatment) }}</td>
              </tr>
              <tr class="strong">
                <td>分项合计</td>
                <td class="num" colspan="3">{{ money(detail.amtTotal) }}</td>
              </tr>
            </table>

            <div class="sheet-section">缴费方式</div>
            <table class="sheet-grid">
              <tr>
                <td>医保</td>
                <td class="num">{{ money(detail.payInsurance) }}</td>
                <td>银行卡</td>
                <td class="num">{{ money(detail.payBank) }}</td>
              </tr>
              <tr>
                <td>支付宝</td>
                <td class="num">{{ money(detail.payAlipay) }}</td>
                <td>微信</td>
                <td class="num">{{ money(detail.payWechat) }}</td>
              </tr>
              <tr>
                <td>信用卡</td>
                <td class="num">{{ money(detail.payCreditcard) }}</td>
                <td>现金</td>
                <td class="num">{{ money(detail.payCash) }}</td>
              </tr>
              <tr>
                <td>其他</td>
                <td class="num">{{ money(detail.payOther) }}</td>
                <td>渠道合计</td>
                <td class="num">{{ money(detail.payChannelTotal) }}</td>
              </tr>
            </table>

            <div class="sheet-total">总金额（分项合计）　<strong>{{ money(detail.amtTotal) }}</strong></div>
          </template>
          <div v-else class="sheet-empty">请选择左侧一条日结记录</div>
        </div>
      </div>
    </div>

    <el-dialog title="生成日结" :visible.sync="genVisible" width="520px" append-to-body @open="initGenRange">
      <p class="gen-hint">将按下列时间统计收款流水及分摊明细，生成日结快照并写入数据库。</p>
      <el-date-picker
        v-model="genRange"
        type="datetimerange"
        range-separator="至"
        start-placeholder="开始"
        end-placeholder="结束"
        value-format="yyyy-MM-dd HH:mm:ss"
        style="width: 100%;"
      />
      <span slot="footer">
        <el-button @click="genVisible = false">取消</el-button>
        <el-button type="primary" :loading="genLoading" @click="submitGenerate">生成</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  listDailySettlements,
  getDailySettlement,
  generateDailySettlement,
  auditDailySettlement
} from '@/api/financeDailySettlement'

export default {
  name: 'DailySettlementPage',
  data() {
    return {
      filterRange: [],
      list: [],
      listLoading: false,
      currentId: null,
      detail: null,
      genVisible: false,
      genRange: [],
      genLoading: false
    }
  },
  computed: {
    auditMode() {
      return this.$route.meta && this.$route.meta.settlementMode === 'audit'
    },
    canAudit() {
      return !!(this.detail && this.detail.status !== 1)
    }
  },
  created() {
    this.initFilterRange()
    this.loadList()
  },
  methods: {
    initFilterRange() {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 6)
      const fmt = d => {
        const y = d.getFullYear()
        const m = String(d.getMonth() + 1).padStart(2, '0')
        const day = String(d.getDate()).padStart(2, '0')
        return `${y}-${m}-${day}`
      }
      this.filterRange = [fmt(start), fmt(end)]
    },
    initGenRange() {
      const end = new Date()
      const start = new Date()
      start.setHours(0, 0, 0, 0)
      const pad = n => String(n).padStart(2, '0')
      const toStr = d =>
        `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
      this.genRange = [toStr(start), toStr(end)]
    },
    async loadList(preferId) {
      if (!this.filterRange || this.filterRange.length !== 2) {
        this.$message.warning('请选择查询日期范围')
        return
      }
      this.listLoading = true
      try {
        const res = await listDailySettlements(this.filterRange[0], this.filterRange[1])
        if (res.code !== 20000) {
          this.$message.error(res.message || '查询失败')
          this.list = []
          return
        }
        this.list = Array.isArray(res.data) ? res.data : []
        this.detail = null
        this.currentId = null
        if (this.list.length) {
          const pick = preferId ? this.list.find(r => r.id === preferId) : null
          const row = pick || this.list[0]
          this.$nextTick(() => {
            if (this.$refs.listTable) {
              this.$refs.listTable.setCurrentRow(row)
            }
            this.loadDetail(row.id)
          })
        }
      } catch (e) {
        this.$message.error('查询失败')
        this.list = []
      } finally {
        this.listLoading = false
      }
    },
    onRowChange(row) {
      if (!row) return
      this.loadDetail(row.id)
    },
    async loadDetail(id) {
      this.currentId = id
      try {
        const res = await getDailySettlement(id)
        if (res.code !== 20000) {
          this.$message.error(res.message || '加载失败')
          this.detail = null
          return
        }
        this.detail = res.data
      } catch (e) {
        this.detail = null
      }
    },
    fmtDt(v) {
      if (v == null || v === '') return '—'
      if (typeof v === 'string') {
        return v.length >= 19 ? v.slice(0, 19).replace('T', ' ') : v
      }
      return String(v)
    },
    money(v) {
      const n = Number(v)
      if (!Number.isFinite(n)) return '0.00'
      return n.toFixed(2)
    },
    printSheet() {
      if (!this.detail) {
        this.$message.warning('请先选择日结记录')
        return
      }
      const w = window.open('', '_blank')
      if (!w) {
        this.$message.error('请允许弹窗后打印')
        return
      }
      const el = document.getElementById('print-area')
      w.document.write(
        `<!DOCTYPE html><html><head><meta charset="utf-8"><title>日结单</title>
        <style>
          body{font-family:"Microsoft YaHei",Arial,sans-serif;padding:24px;color:#333}
          .sheet-title{text-align:center;font-size:20px;font-weight:700;margin-bottom:16px}
          .sheet-meta{font-size:13px;line-height:1.8;margin-bottom:16px;color:#555}
          .sheet-section{margin:14px 0 8px;font-weight:600;font-size:14px}
          .sheet-grid{width:100%;border-collapse:collapse;font-size:13px;margin-bottom:8px}
          .sheet-grid td{border:1px solid #ccc;padding:8px 10px}
          .sheet-grid .num{text-align:right}
          .sheet-grid .strong td{font-weight:700}
          .sheet-total{margin-top:16px;font-size:15px;text-align:right}
        </style></head><body>${el.innerHTML}</body></html>`
      )
      w.document.close()
      w.print()
      w.close()
    },
    openGenDialog() {
      this.genVisible = true
    },
    async submitGenerate() {
      if (!this.genRange || this.genRange.length !== 2) {
        this.$message.warning('请选择统计时间范围')
        return
      }
      this.genLoading = true
      try {
        const res = await generateDailySettlement(this.genRange[0], this.genRange[1])
        if (res.code !== 20000) {
          this.$message.error(res.message || '生成失败')
          return
        }
        this.$message.success('已生成日结')
        this.genVisible = false
        await this.loadList(res.data && res.data.id)
      } catch (e) {
        this.$message.error('生成失败')
      } finally {
        this.genLoading = false
      }
    },
    doAudit() {
      if (!this.canAudit || !this.currentId) return
      this.$confirm('确认核对本条日结？核对后将标记为已核对。', '日结核对', { type: 'warning' })
        .then(async () => {
          const keepId = this.currentId
          try {
            const res = await auditDailySettlement(keepId)
            if (res.code !== 20000) {
              this.$message.error(res.message || '核对失败')
              return
            }
            this.$message.success('已核对')
            await this.loadList(keepId)
          } catch (e) {
            this.$message.error('核对失败')
          }
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
.daily-settlement-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: calc(100vh - 140px);
  padding: 0 8px 16px;
  box-sizing: border-box;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.toolbar-dates {
  width: 280px;
}

.split {
  display: flex;
  flex: 1;
  min-height: 0;
  gap: 12px;
}

.left-panel {
  width: 360px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.panel-title {
  font-weight: 600;
  margin-bottom: 10px;
  color: #303133;
}

.list-table {
  flex: 1;
}

.right-panel {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 8px;
  padding: 20px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: auto;
}

.sheet-wrap {
  max-width: 720px;
  margin: 0 auto;
}

.sheet-title {
  text-align: center;
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #1f2a3d;
}

.sheet-meta {
  font-size: 13px;
  line-height: 1.85;
  color: #606266;
  margin-bottom: 16px;
}

.sheet-section {
  margin: 14px 0 8px;
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}

.sheet-grid {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  margin-bottom: 8px;
}

.sheet-grid td {
  border: 1px solid #dcdfe6;
  padding: 8px 12px;
}

.sheet-grid .num {
  text-align: right;
}

.sheet-grid .strong td {
  font-weight: 700;
  background: #f5f7fa;
}

.sheet-total {
  margin-top: 20px;
  font-size: 15px;
  text-align: right;
  color: #303133;
}

.sheet-empty {
  text-align: center;
  color: #909399;
  padding: 80px 16px;
  font-size: 14px;
}

.gen-hint {
  font-size: 13px;
  color: #606266;
  margin: 0 0 12px;
  line-height: 1.5;
}
</style>
