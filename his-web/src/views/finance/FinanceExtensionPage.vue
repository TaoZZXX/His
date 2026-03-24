<template>
  <div class="finance-extension-page">
    <div class="ui-page-title">财务扩展</div>
    <el-tabs v-model="activeTab" type="card" @tab-click="onTabChange">
      <el-tab-pane label="结算类别" name="cat" />
      <el-tab-pane label="账单记录" name="bill" />
      <el-tab-pane label="操作员日结记录" name="operator" />
      <el-tab-pane label="发票扩展字典" name="invoiceExp" />
    </el-tabs>

    <div class="ui-toolbar">
      <el-input v-model="keyword" placeholder="名称/编码/ID" class="w240" clearable @keyup.enter.native="loadRows" />
      <el-button type="primary" icon="el-icon-search" @click="loadRows">查询</el-button>
      <el-button type="primary" plain icon="el-icon-plus" @click="openDialog()">新增</el-button>
    </div>

    <div class="ui-table-wrap">
    <el-table :data="pagedRows" border stripe>
      <el-table-column v-for="c in columns" :key="c.prop" :prop="c.prop" :label="c.label" :min-width="c.width || 120" show-overflow-tooltip />
      <el-table-column label="操作" width="130" fixed="right">
        <template slot-scope="{ row }">
          <el-button type="text" size="small" @click="openDialog(row)">修改</el-button>
          <el-button type="text" size="small" class="ui-danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>

    <div class="ui-pager">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :current-page.sync="pageNo"
        :page-size.sync="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="rows.length"
      />
    </div>

    <el-dialog :title="dialog.id ? '修改' : '新增'" :visible.sync="dialogVisible" width="760px">
      <el-form :model="dialog" label-width="120px" size="small">
        <el-form-item v-for="f in formFields" :key="f.prop" :label="f.label">
          <el-input v-model="dialog[f.prop]" :type="f.type || 'text'" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { deleteBackfillRow, pageBackfillRows, saveBackfillRow } from '@/api/moduleBackfill'

export default {
  name: 'FinanceExtensionPage',
  data() {
    return {
      activeTab: 'cat',
      keyword: '',
      rows: [],
      pageNo: 1,
      pageSize: 10,
      dialogVisible: false,
      dialog: {}
    }
  },
  computed: {
    tableName() {
      if (this.activeTab === 'cat') return 'bms_settlement_cat'
      if (this.activeTab === 'bill') return 'bms_bills_record'
      if (this.activeTab === 'operator') return 'bms_operator_settle_record'
      return 'bms_invoice_exp'
    },
    columns() {
      const map = {
        cat: [
          { prop: 'id', label: 'ID', width: 80 },
          { prop: 'name', label: '名称' },
          { prop: 'code', label: '编码' },
          { prop: 'status', label: '状态', width: 90 }
        ],
        bill: [
          { prop: 'id', label: 'ID', width: 80 },
          { prop: 'bill_no', label: '账单号' },
          { prop: 'type', label: '类型', width: 90 },
          { prop: 'invoice_num', label: '发票数量', width: 100 },
          { prop: 'registration_id', label: '挂号ID', width: 100 },
          { prop: 'status', label: '状态', width: 90 },
          { prop: 'create_time', label: '创建时间', width: 170 }
        ],
        operator: [
          { prop: 'id', label: 'ID', width: 80 },
          { prop: 'cashier_id', label: '收款员ID', width: 100 },
          { prop: 'start_datetime', label: '开始时间', width: 170 },
          { prop: 'end_datetime', label: '结束时间', width: 170 },
          { prop: 'amount', label: '总金额', width: 100 },
          { prop: 'verify_status', label: '核对状态', width: 100 }
        ],
        invoiceExp: [
          { prop: 'id', label: 'ID', width: 80 },
          { prop: 'code', label: '编码' },
          { prop: 'name', label: '名称' },
          { prop: 'type', label: '类型', width: 100 }
        ]
      }
      return map[this.activeTab]
    },
    formFields() {
      const map = {
        cat: [
          { prop: 'name', label: '名称' },
          { prop: 'code', label: '编码' },
          { prop: 'status', label: '状态' }
        ],
        bill: [
          { prop: 'type', label: '类型' },
          { prop: 'bill_no', label: '账单号' },
          { prop: 'status', label: '状态' },
          { prop: 'invoice_num', label: '发票数量' },
          { prop: 'registration_id', label: '挂号ID' },
          { prop: 'record_list', label: '流水列表' }
        ],
        operator: [
          { prop: 'cashier_id', label: '收款员ID' },
          { prop: 'start_datetime', label: '开始时间' },
          { prop: 'end_datetime', label: '结束时间' },
          { prop: 'invoice_num', label: '发票数量' },
          { prop: 'amount', label: '总金额' },
          { prop: 'verify_status', label: '核对状态' }
        ],
        invoiceExp: [
          { prop: 'code', label: '编码' },
          { prop: 'name', label: '名称' },
          { prop: 'type', label: '类型' }
        ]
      }
      return map[this.activeTab]
    },
    pagedRows() {
      const s = (this.pageNo - 1) * this.pageSize
      return this.rows.slice(s, s + this.pageSize)
    }
  },
  mounted() {
    this.loadRows()
  },
  methods: {
    onTabChange() {
      this.pageNo = 1
      this.keyword = ''
      this.loadRows()
    },
    async loadRows() {
      const res = await pageBackfillRows(this.tableName, 1, 2000)
      if (!res || res.code !== 20000) {
        this.$message.error((res && res.message) || '加载失败')
        return
      }
      const all = (res.data && res.data.records) || []
      const kw = (this.keyword || '').trim()
      this.rows = all.filter(r => {
        if (!kw) return true
        return Object.values(r || {}).some(v => String(v || '').includes(kw))
      })
      this.pageNo = 1
    },
    openDialog(row) {
      this.dialog = row ? { ...row } : {}
      this.dialogVisible = true
    },
    async save() {
      const res = await saveBackfillRow(this.tableName, this.dialog)
      if (!res || res.code !== 20000) {
        this.$message.error((res && res.message) || '保存失败')
        return
      }
      this.$message.success('保存成功')
      this.dialogVisible = false
      await this.loadRows()
    },
    remove(row) {
      this.$confirm('确认删除该记录？', '提示', { type: 'warning' })
        .then(async () => {
          const res = await deleteBackfillRow(this.tableName, row.id)
          if (!res || res.code !== 20000) {
            this.$message.error((res && res.message) || '删除失败')
            return
          }
          this.$message.success('删除成功')
          await this.loadRows()
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
.w240 {
  width: 240px;
}
</style>
