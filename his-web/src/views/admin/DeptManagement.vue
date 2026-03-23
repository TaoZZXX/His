<template>
  <div class="dept-mgmt">
    <div class="toolbar">
      <el-input v-model="filters.code" clearable placeholder="科室编码" class="w160" @keyup.enter.native="search" />
      <el-input v-model="filters.name" clearable placeholder="科室名称" class="w160" @keyup.enter.native="search" />
      <el-select v-model="filters.catId" clearable placeholder="科室分类" class="w140">
        <el-option v-for="o in catFilterOptions" :key="'c'+o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-select v-model="filters.type" clearable placeholder="科室类别" class="w140">
        <el-option v-for="o in typeFilterOptions" :key="'t'+o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-button type="primary" icon="el-icon-search" @click="search">搜索科室</el-button>
      <el-button type="primary" plain icon="el-icon-plus" @click="openCreate">新增科室</el-button>
      <el-button type="primary" plain icon="el-icon-download" @click="exportCsv">导出科室</el-button>
      <el-button type="danger" plain icon="el-icon-delete" :disabled="!selectedRows.length" @click="batchRemove">批量删除</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      @selection-change="rows => (selectedRows = rows)"
    >
      <el-table-column type="selection" width="48" align="center" />
      <el-table-column prop="code" label="科室编码" min-width="120" show-overflow-tooltip />
      <el-table-column prop="name" label="科室名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="catId" label="科室分类" width="100" align="center" />
      <el-table-column prop="type" label="科室类别" width="100" align="center" />
      <el-table-column label="状态" width="88" align="center">
        <template slot-scope="{ row }">
          <el-tag v-if="row.status === 1 || row.status == null" type="success" size="small">启用</el-tag>
          <el-tag v-else type="info" size="small">停用</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template slot-scope="{ row }">
          <el-button type="text" size="small" @click="openEdit(row)">修改</el-button>
          <el-button type="text" size="small" class="del-btn" @click="removeOne(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :current-page.sync="pageNo"
        :page-sizes="[10, 20, 50]"
        :page-size.sync="pageSize"
        :total="total"
        @current-change="load"
        @size-change="onSizeChange"
      />
    </div>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="520px" append-to-body @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="科室编码" prop="code">
          <el-input v-model="form.code" :disabled="isEdit" maxlength="32" show-word-limit placeholder="如 JZK" />
        </el-form-item>
        <el-form-item label="科室名称" prop="name">
          <el-input v-model="form.name" maxlength="64" show-word-limit />
        </el-form-item>
        <el-form-item label="科室分类" prop="catId">
          <el-input-number v-model="form.catId" :min="0" :max="9999" controls-position="right" class="full-num" />
          <span class="form-tip">业务分类编码，可与字典表对应</span>
        </el-form-item>
        <el-form-item label="科室类别" prop="type">
          <el-input-number v-model="form.type" :min="0" :max="9999" controls-position="right" class="full-num" />
          <span class="form-tip">如 0 临床 / 1 医技，按院内规则填写</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { pageDepts, createDept, updateDept, deleteDept, batchDeleteDepts } from '@/api/dept'

export default {
  name: 'DeptManagement',
  data() {
    return {
      loading: false,
      filters: { code: '', name: '', catId: undefined, type: undefined },
      catFilterOptions: [
        { label: '门诊(1)', value: 1 },
        { label: '医技(2)', value: 2 },
        { label: '行政(3)', value: 3 }
      ],
      typeFilterOptions: [
        { label: '临床(0)', value: 0 },
        { label: '医技(1)', value: 1 }
      ],
      tableData: [],
      total: 0,
      pageNo: 1,
      pageSize: 10,
      selectedRows: [],
      dialogVisible: false,
      dialogTitle: '新增科室',
      isEdit: false,
      editId: null,
      saving: false,
      form: {
        code: '',
        name: '',
        catId: 1,
        type: 0,
        status: 1
      },
      rules: {
        code: [{ required: true, message: '请输入科室编码', trigger: 'blur' }],
        name: [{ required: true, message: '请输入科室名称', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.load()
  },
  methods: {
    search() {
      this.pageNo = 1
      this.load()
    },
    onSizeChange() {
      this.pageNo = 1
      this.load()
    },
    async load() {
      this.loading = true
      try {
        const res = await pageDepts({
          page: this.pageNo,
          size: this.pageSize,
          code: (this.filters.code || '').trim() || undefined,
          name: (this.filters.name || '').trim() || undefined,
          catId: this.filters.catId,
          type: this.filters.type
        })
        if (res.code !== 20000) {
          this.$message.error(res.message || '加载失败')
          this.tableData = []
          this.total = 0
          return
        }
        const d = res.data || {}
        this.tableData = Array.isArray(d.list) ? d.list : []
        this.total = typeof d.total === 'number' ? d.total : 0
      } catch (e) {
        this.$message.error('加载失败')
        this.tableData = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    openCreate() {
      this.dialogTitle = '新增科室'
      this.isEdit = false
      this.editId = null
      this.resetForm()
      this.dialogVisible = true
    },
    openEdit(row) {
      this.dialogTitle = '修改科室'
      this.isEdit = true
      this.editId = row.id
      this.form = {
        code: row.code || '',
        name: row.name || '',
        catId: row.catId != null ? row.catId : 0,
        type: row.type != null ? row.type : 0,
        status: row.status === 0 ? 0 : 1
      }
      this.dialogVisible = true
    },
    resetForm() {
      this.form = {
        code: '',
        name: '',
        catId: 1,
        type: 0,
        status: 1
      }
      this.$nextTick(() => {
        if (this.$refs.formRef) this.$refs.formRef.clearValidate()
      })
    },
    submitForm() {
      this.$refs.formRef.validate(async valid => {
        if (!valid) return
        this.saving = true
        try {
          const body = {
            code: (this.form.code || '').trim(),
            name: (this.form.name || '').trim(),
            catId: this.form.catId,
            type: this.form.type,
            status: this.form.status
          }
          let res
          if (this.isEdit) {
            res = await updateDept(this.editId, body)
          } else {
            res = await createDept(body)
          }
          if (res.code !== 20000) {
            this.$message.error(res.message || '保存失败')
            return
          }
          this.$message.success('保存成功')
          this.dialogVisible = false
          this.load()
        } catch (e) {
          this.$message.error('保存失败')
        } finally {
          this.saving = false
        }
      })
    },
    removeOne(row) {
      this.$confirm(`确定删除科室「${row.name}」？`, '提示', { type: 'warning' })
        .then(async () => {
          try {
            const res = await deleteDept(row.id)
            if (res.code !== 20000) {
              this.$message.error(res.message || '删除失败')
              return
            }
            this.$message.success('已删除')
            this.load()
          } catch (e) {
            this.$message.error('删除失败')
          }
        })
        .catch(() => {})
    },
    batchRemove() {
      if (!this.selectedRows.length) return
      this.$confirm(`确定删除选中的 ${this.selectedRows.length} 条科室？`, '批量删除', { type: 'warning' })
        .then(async () => {
          try {
            const ids = this.selectedRows.map(r => r.id)
            const res = await batchDeleteDepts(ids)
            if (res.code !== 20000) {
              this.$message.error(res.message || '删除失败')
              return
            }
            this.$message.success('已删除')
            this.selectedRows = []
            this.load()
          } catch (e) {
            this.$message.error('删除失败')
          }
        })
        .catch(() => {})
    },
    exportCsv() {
      const headers = ['科室编码', '科室名称', '科室分类', '科室类别', '状态']
      const lines = [headers.join(',')]
      this.tableData.forEach(r => {
        const st = r.status === 0 ? '停用' : '启用'
        const row = [r.code, r.name, r.catId, r.type, st].map(v => {
          const s = v == null ? '' : String(v)
          if (/[",\n]/.test(s)) return `"${s.replace(/"/g, '""')}"`
          return s
        })
        lines.push(row.join(','))
      })
      const blob = new Blob(['\ufeff' + lines.join('\n')], { type: 'text/csv;charset=utf-8' })
      const a = document.createElement('a')
      a.href = URL.createObjectURL(blob)
      a.download = `科室列表_${new Date().toISOString().slice(0, 10)}.csv`
      a.click()
      URL.revokeObjectURL(a.href)
    }
  }
}
</script>

<style scoped>
.dept-mgmt {
  padding: 12px 16px 24px;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-bottom: 14px;
}
.w160 {
  width: 160px;
}
.w140 {
  width: 140px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.del-btn {
  color: #f56c6c;
}
.full-num {
  width: 100%;
}
.form-tip {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
