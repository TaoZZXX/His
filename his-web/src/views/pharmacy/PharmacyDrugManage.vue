<template>
  <div class="pharmacy-page drug-manage">
    <div class="page-head">
      <h2 class="title">药品维护</h2>
      <el-button type="primary" icon="el-icon-plus" @click="openForm()">新增药品</el-button>
    </div>

    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        clearable
        placeholder="名称 / 编码 / 助记码 / 通用名"
        class="kw"
        @keyup.enter.native="search"
      />
      <el-select v-model="query.status" placeholder="状态" clearable class="st">
        <el-option label="全部" :value="''" />
        <el-option label="启用" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
      <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="72" align="center" />
      <el-table-column prop="code" label="编码" width="110" show-overflow-tooltip />
      <el-table-column prop="name" label="名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="genericName" label="通用名" min-width="120" show-overflow-tooltip />
      <el-table-column prop="format" label="规格" width="120" show-overflow-tooltip />
      <el-table-column prop="unit" label="单位" width="72" align="center" />
      <el-table-column label="单价(元)" width="100" align="right">
        <template slot-scope="{ row }">{{ formatMoney(row.price) }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" align="center" />
      <el-table-column prop="manufacturer" label="厂家" min-width="120" show-overflow-tooltip />
      <el-table-column prop="mnemonicCode" label="助记码" width="100" show-overflow-tooltip />
      <el-table-column label="状态" width="88" align="center">
        <template slot-scope="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template slot-scope="{ row }">
          <el-button type="text" size="small" @click="openForm(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        background
        layout="total, prev, pager, next, sizes"
        :current-page.sync="query.page"
        :page-size.sync="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        @current-change="load"
        @size-change="load"
      />
    </div>

    <el-dialog
      :title="form.id ? '编辑药品' : '新增药品'"
      :visible.sync="dialogVisible"
      width="560px"
      append-to-body
      destroy-on-close
      @closed="resetFormModel"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="small">
        <el-form-item label="药品编码" prop="code">
          <el-input v-model="form.code" placeholder="可选" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="必填" />
        </el-form-item>
        <el-form-item label="通用名" prop="genericName">
          <el-input v-model="form.genericName" placeholder="可选" />
        </el-form-item>
        <el-form-item label="规格" prop="format">
          <el-input v-model="form.format" placeholder="如 0.5g*12片" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="如 盒、瓶" />
        </el-form-item>
        <el-form-item label="单价(元)" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="0.01" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :step="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="生产厂家" prop="manufacturer">
          <el-input v-model="form.manufacturer" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
        <el-form-item label="助记码" prop="mnemonicCode">
          <el-input v-model="form.mnemonicCode" placeholder="可选，便于检索" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getPharmacyDrugPage, createPharmacyDrug, updatePharmacyDrug } from '@/api/pharmacy'

function emptyForm() {
  return {
    id: null,
    code: '',
    name: '',
    genericName: '',
    format: '',
    unit: '',
    price: 0,
    stock: 0,
    manufacturer: '',
    mnemonicCode: '',
    status: 1
  }
}

export default {
  name: 'PharmacyDrugManage',
  data() {
    return {
      loading: false,
      saving: false,
      rows: [],
      total: 0,
      query: {
        page: 1,
        size: 10,
        keyword: '',
        status: ''
      },
      dialogVisible: false,
      form: emptyForm(),
      rules: {
        name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.load()
  },
  methods: {
    formatMoney(v) {
      if (v === null || v === undefined || v === '') return '—'
      return Number(v).toFixed(2)
    },
    async load() {
      this.loading = true
      try {
        const res = await getPharmacyDrugPage({
          page: this.query.page,
          size: this.query.size,
          keyword: this.query.keyword,
          status: this.query.status === '' ? undefined : this.query.status
        })
        if (!res || res.code !== 20000) {
          this.$message.error((res && res.message) || '加载失败')
          this.rows = []
          this.total = 0
          return
        }
        const d = res.data || {}
        this.rows = Array.isArray(d.records) ? d.records : []
        this.total = Number(d.total) || 0
      } catch (e) {
        this.$message.error('加载失败')
        this.rows = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    search() {
      this.query.page = 1
      this.load()
    },
    resetQuery() {
      this.query = { page: 1, size: 10, keyword: '', status: '' }
      this.load()
    },
    openForm(row) {
      if (row) {
        this.form = {
          id: row.id,
          code: row.code || '',
          name: row.name || '',
          genericName: row.genericName || '',
          format: row.format || '',
          unit: row.unit || '',
          price: row.price != null ? Number(row.price) : 0,
          stock: row.stock != null ? Number(row.stock) : 0,
          manufacturer: row.manufacturer || '',
          mnemonicCode: row.mnemonicCode || '',
          status: row.status === 0 ? 0 : 1
        }
      } else {
        this.form = emptyForm()
      }
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.formRef && this.$refs.formRef.clearValidate())
    },
    resetFormModel() {
      this.form = emptyForm()
    },
    submitForm() {
      this.$refs.formRef.validate(async valid => {
        if (!valid) return
        const payload = {
          code: this.form.code || null,
          name: this.form.name,
          genericName: this.form.genericName || null,
          format: this.form.format || null,
          unit: this.form.unit || null,
          price: this.form.price,
          stock: this.form.stock,
          manufacturer: this.form.manufacturer || null,
          mnemonicCode: this.form.mnemonicCode || null,
          status: this.form.status
        }
        this.saving = true
        try {
          let res
          if (this.form.id) {
            res = await updatePharmacyDrug(this.form.id, payload)
          } else {
            res = await createPharmacyDrug(payload)
          }
          if (!res || res.code !== 20000) {
            this.$message.error((res && res.message) || '保存失败')
            return
          }
          this.$message.success((res && res.message) || '保存成功')
          this.dialogVisible = false
          this.load()
        } catch (e) {
          this.$message.error('保存失败')
        } finally {
          this.saving = false
        }
      })
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
  color: #303133;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  padding: 12px 14px;
  background: #f5f7fa;
  border-radius: 4px;
}
.toolbar .kw {
  width: 260px;
}
.toolbar .st {
  width: 120px;
}
.pager {
  margin-top: 16px;
  text-align: right;
}
</style>
