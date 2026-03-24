<template>
  <div class="module-page">
    <div class="toolbar">
      <el-select v-model="activeTable" placeholder="选择数据表" class="w260" @change="onTableChange">
        <el-option v-for="t in tables" :key="t" :label="t" :value="t" />
      </el-select>
      <el-button type="primary" icon="el-icon-refresh" @click="loadRows">刷新</el-button>
      <el-button type="success" icon="el-icon-plus" :disabled="!activeTable" @click="openCreate">新增记录</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe class="table-wrap">
      <el-table-column type="index" width="54" align="center" label="#" />
      <el-table-column v-for="c in columns" :key="c.name" :prop="c.name" :label="columnLabel(c.name)" min-width="130" show-overflow-tooltip />
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template slot-scope="{ row }">
          <el-button type="text" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button type="text" size="small" class="danger" @click="removeRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :current-page.sync="pageNo"
        :page-size.sync="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        @current-change="loadRows"
        @size-change="onSizeChange"
      />
    </div>

    <el-dialog :title="isEdit ? '编辑记录' : '新增记录'" :visible.sync="dialogVisible" width="860px" @closed="resetEditor">
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="编辑 JSON：key 必须是当前表字段名。留空字符串会按 null 保存。"
        class="editor-tip"
      />
      <el-input v-model="editorJson" type="textarea" :rows="16" />
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitSave">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  listBackfillColumns,
  pageBackfillRows,
  saveBackfillRow,
  deleteBackfillRow
} from '@/api/moduleBackfill'

export default {
  name: 'ModuleTableWorkbench',
  props: {
    tables: {
      type: Array,
      default: () => []
    },
    defaultTable: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      activeTable: '',
      columns: [],
      rows: [],
      loading: false,
      pageNo: 1,
      pageSize: 20,
      total: 0,
      dialogVisible: false,
      editorJson: '{}',
      isEdit: false,
      saving: false
    }
  },
  mounted() {
    this.activeTable = this.defaultTable || (this.tables[0] || '')
    this.loadColumns().then(() => this.loadRows())
  },
  methods: {
    columnLabel(name) {
      const map = {
        id: '主键ID',
        name: '名称',
        code: '编码',
        status: '状态',
        type: '类型',
        scope: '适用范围',
        own_id: '所属人ID',
        create_time: '创建时间',
        update_time: '更新时间',
        remark: '备注',
        aim: '目的',
        days: '天数',
        num: '数量',
        frequency: '频次',
        medical_advice: '医嘱',
        medicine_usage: '用法',
        usage_means: '用药方式',
        usage_num: '每次用量',
        usage_num_unit: '每次用量单位',
        pair_num: '付数',
        therapy: '治法',
        therapy_details: '治法详情',
        footnote: '脚注',
        model_id: '模板ID',
        drug_id: '药品ID',
        non_drug_id_list: '非药品项目ID列表',
        chief_complaint: '主诉',
        history_of_present_illness: '现病史',
        history_of_treatment: '现病治疗情况',
        past_history: '既往史',
        allergies: '过敏史',
        health_checkup: '体格检查',
        priliminary_dise_id_list: '初步诊断ID列表',
        priliminary_dise_str_list: '初步诊断文本',
        invoice_no: '发票号',
        bill_id: '账单ID',
        freeze_status: '冻结状态',
        associate_id: '关联ID',
        operator_id: '操作员ID',
        settlement_cat_id: '结算类别ID',
        settle_record_id: '结算记录ID',
        item_list: '项目列表',
        invoice_num: '发票数量',
        rush_invoice_num: '冲红发票数量',
        reprint_invoice_num: '重打发票数量',
        start_end_invoice_id_str: '起止发票号',
        rush_invoice_id_list_str: '冲红发票号列表',
        reprint_invoice_id_list_str: '重打发票号列表',
        user_id: '用户ID',
        ip: '登录IP',
        role_id: '角色ID',
        dept_id: '科室ID'
      }
      return map[name] || name
    },
    async onTableChange() {
      this.pageNo = 1
      await this.loadColumns()
      await this.loadRows()
    },
    async loadColumns() {
      if (!this.activeTable) {
        this.columns = []
        return
      }
      const res = await listBackfillColumns(this.activeTable)
      if (!res || res.code !== 20000) {
        this.$message.error((res && res.message) || '字段加载失败')
        return
      }
      this.columns = res.data || []
    },
    async loadRows() {
      if (!this.activeTable) {
        this.rows = []
        this.total = 0
        return
      }
      this.loading = true
      try {
        const res = await pageBackfillRows(this.activeTable, this.pageNo, this.pageSize)
        if (!res || res.code !== 20000) {
          this.$message.error((res && res.message) || '数据加载失败')
          return
        }
        const data = res.data || {}
        this.rows = Array.isArray(data.records) ? data.records : []
        this.total = Number(data.total || 0)
      } finally {
        this.loading = false
      }
    },
    onSizeChange() {
      this.pageNo = 1
      this.loadRows()
    },
    openCreate() {
      this.isEdit = false
      this.editorJson = '{}'
      this.dialogVisible = true
    },
    openEdit(row) {
      this.isEdit = true
      this.editorJson = JSON.stringify(row || {}, null, 2)
      this.dialogVisible = true
    },
    resetEditor() {
      this.editorJson = '{}'
      this.isEdit = false
    },
    async submitSave() {
      let payload
      try {
        payload = JSON.parse(this.editorJson || '{}')
      } catch (e) {
        this.$message.error('JSON 格式错误')
        return
      }
      this.saving = true
      try {
        const res = await saveBackfillRow(this.activeTable, payload)
        if (!res || res.code !== 20000) {
          this.$message.error((res && res.message) || '保存失败')
          return
        }
        this.$message.success('保存成功')
        this.dialogVisible = false
        await this.loadRows()
      } finally {
        this.saving = false
      }
    },
    removeRow(row) {
      const id = row && row.id
      if (id == null) {
        this.$message.warning('当前记录缺少 id，无法删除')
        return
      }
      this.$confirm(`确认删除 ${this.activeTable} #${id} ?`, '提示', { type: 'warning' })
        .then(async () => {
          const res = await deleteBackfillRow(this.activeTable, id)
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
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.w260 {
  width: 260px;
}

.table-wrap {
  min-height: 360px;
}

.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.danger {
  color: #f56c6c;
}

.editor-tip {
  margin-bottom: 10px;
}
</style>
