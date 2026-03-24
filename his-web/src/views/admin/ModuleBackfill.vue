<template>
  <div class="module-backfill-page">
    <div class="head-card">
      <div class="head-title">模板补齐工作台</div>
      <div class="head-desc">入口页：优先进入独立业务页维护。下方“临时直连模式”保留给快速排障或紧急数据修复。</div>
    </div>

    <el-row :gutter="12" class="link-grid">
      <el-col :xs="24" :md="8">
        <el-card class="link-card" shadow="hover">
          <div class="link-title">医生模板中心</div>
          <div class="link-tip">病历模板、诊断字典、成药/草药/检查检验模板维护</div>
          <el-button type="primary" size="small" @click="go('/doctor/template-center')">进入页面</el-button>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="link-card" shadow="hover">
          <div class="link-title">财务扩展</div>
          <div class="link-tip">结算类别、账单记录、操作员日结、发票扩展维护</div>
          <el-button type="primary" size="small" @click="go('/cashier/extension')">进入页面</el-button>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="link-card" shadow="hover">
          <div class="link-title">系统审计与运营</div>
          <div class="link-tip">登录日志、工作量、常用语、描述配置维护</div>
          <el-button type="primary" size="small" @click="go('/admin/audit-ops')">进入页面</el-button>
        </el-card>
      </el-col>
    </el-row>

    <div class="legacy-wrap">
      <el-divider content-position="left">临时直连模式（可选）</el-divider>
      <div class="toolbar">
        <el-switch v-model="legacyMode" active-text="开启" inactive-text="关闭" />
        <el-select v-model="activeModuleKey" :disabled="!legacyMode" placeholder="选择模块" class="w220" @change="onModuleChange">
          <el-option v-for="m in modules" :key="m.key" :label="m.name" :value="m.key" />
        </el-select>
        <el-select v-model="activeTable" :disabled="!legacyMode" placeholder="选择数据表" class="w260" @change="onTableChange">
          <el-option v-for="t in activeTables" :key="t" :label="t" :value="t" />
        </el-select>
        <el-button :disabled="!legacyMode" type="primary" icon="el-icon-refresh" @click="loadRows">刷新</el-button>
        <el-button :disabled="!legacyMode || !activeTable" type="success" icon="el-icon-plus" @click="openCreate">新增记录</el-button>
      </div>

      <el-table v-if="legacyMode" v-loading="loading" :data="rows" border stripe class="table-wrap">
        <el-table-column type="index" width="54" align="center" label="#" />
        <el-table-column v-for="c in columns" :key="c.name" :prop="c.name" :label="c.name" min-width="130" show-overflow-tooltip />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" size="small" class="danger" @click="removeRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="legacyMode" class="pager">
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
    </div>

    <el-dialog :title="isEdit ? '编辑记录' : '新增记录'" :visible.sync="dialogVisible" width="860px" @closed="resetEditor">
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="编辑 JSON：key 必须是当前表字段名。留空字符串会自动按 null 保存。"
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
  listBackfillModules,
  listBackfillColumns,
  pageBackfillRows,
  saveBackfillRow,
  deleteBackfillRow
} from '@/api/moduleBackfill'

export default {
  name: 'ModuleBackfill',
  data() {
    return {
      legacyMode: false,
      modules: [],
      activeModuleKey: '',
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
  computed: {
    activeModule() {
      return this.modules.find(m => m.key === this.activeModuleKey) || null
    },
    activeTables() {
      return this.activeModule && Array.isArray(this.activeModule.tables) ? this.activeModule.tables : []
    }
  },
  mounted() {
    this.loadModules()
  },
  methods: {
    go(path) {
      if (this.$route.path !== path) {
        this.$router.push(path)
      }
    },
    async loadModules() {
      const res = await listBackfillModules()
      if (!res || res.code !== 20000) {
        this.$message.error((res && res.message) || '模块加载失败')
        return
      }
      this.modules = res.data || []
      if (!this.modules.length) return
      this.activeModuleKey = this.modules[0].key
      this.activeTable = (this.modules[0].tables && this.modules[0].tables[0]) || ''
      await this.loadColumns()
      await this.loadRows()
    },
    async onModuleChange() {
      this.activeTable = this.activeTables[0] || ''
      this.pageNo = 1
      await this.loadColumns()
      await this.loadRows()
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
      this.$confirm(`确认删除 ${this.activeTable} #${id} ?`, '提示', {
        type: 'warning'
      }).then(async () => {
        const res = await deleteBackfillRow(this.activeTable, id)
        if (!res || res.code !== 20000) {
          this.$message.error((res && res.message) || '删除失败')
          return
        }
        this.$message.success('删除成功')
        await this.loadRows()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.module-backfill-page {
  padding: 0;
}

.head-card {
  border: 1px solid #e8edf5;
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 12px;
}

.head-title {
  font-size: 18px;
  font-weight: 700;
  color: #27364a;
}

.head-desc {
  margin-top: 6px;
  color: #66758a;
  font-size: 13px;
}

.link-grid {
  margin-bottom: 12px;
}

.link-card {
  min-height: 128px;
}

.link-title {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 8px;
}

.link-tip {
  font-size: 13px;
  color: #7b8798;
  margin-bottom: 10px;
  line-height: 1.45;
}

.legacy-wrap {
  border: 1px solid #e8edf5;
  background: #fff;
  border-radius: 12px;
  padding: 10px 12px 14px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.w220 {
  width: 220px;
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
