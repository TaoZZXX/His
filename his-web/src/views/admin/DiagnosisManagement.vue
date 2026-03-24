<template>
  <div class="diagnosis-page">
    <div class="ui-toolbar">
      <el-input v-model="catKeyword" placeholder="请输入类别名" class="w220" clearable />
      <el-button type="primary" icon="el-icon-search" @click="filterCategories">搜索</el-button>
      <el-button type="danger" plain icon="el-icon-refresh" @click="resetCategoryFilter">重置</el-button>
    </div>

    <div class="layout">
      <div class="left ui-card">
        <div class="left-head">
          <span>诊断分类目录</span>
          <el-button type="text" size="small" @click="openCatDialog()">新增</el-button>
        </div>
        <div class="cat-list">
          <div
            v-for="c in shownCategories"
            :key="c.id"
            class="cat-item"
            :class="{ active: selectedCat && selectedCat.id === c.id }"
            @click="selectCategory(c)"
          >
            <span class="cat-name">{{ c.name }}</span>
            <span class="cat-ops">
              <el-button type="text" size="mini" @click.stop="openCatDialog(c)">修改</el-button>
              <el-button type="text" size="mini" class="ui-danger" @click.stop="removeCategory(c)">删除</el-button>
            </span>
          </div>
        </div>
      </div>

      <div class="right ui-card">
        <div class="right-head">
          <div>
            <span class="head-label">当前类别：</span>
            <span class="head-value">{{ selectedCat ? selectedCat.name : '未选择' }}</span>
          </div>
          <div>
            <el-input v-model="diseKeyword" placeholder="疾病名/编码/ICD" class="w240" clearable @keyup.enter.native="loadDiseases" />
            <el-button type="primary" icon="el-icon-search" @click="loadDiseases">查询诊断</el-button>
            <el-button type="primary" plain icon="el-icon-plus" :disabled="!selectedCat" @click="openDiseDialog()">新增诊断</el-button>
          </div>
        </div>

        <el-table :data="pagedDiseases" border stripe>
          <el-table-column prop="code" label="疾病编码" width="180" />
          <el-table-column prop="name" label="疾病名称" min-width="180" />
          <el-table-column prop="icd" label="ICD编码" width="160" />
          <el-table-column label="操作" width="130" fixed="right">
            <template slot-scope="{ row }">
              <el-button type="text" size="small" @click="openDiseDialog(row)">修改</el-button>
              <el-button type="text" size="small" class="ui-danger" @click="removeDisease(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="ui-pager">
          <el-pagination
            background
            layout="total, prev, pager, next"
            :current-page.sync="pageNo"
            :page-size="pageSize"
            :total="diseases.length"
          />
        </div>
      </div>
    </div>

    <el-dialog :title="catDialog.id ? '修改分类' : '新增分类'" :visible.sync="catDialogVisible" width="420px">
      <el-form :model="catDialog" label-width="90px">
        <el-form-item label="分类名称">
          <el-input v-model="catDialog.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="catDialog.status">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="catDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCategory">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog :title="diseDialog.id ? '修改诊断' : '新增诊断'" :visible.sync="diseDialogVisible" width="560px">
      <el-form :model="diseDialog" label-width="90px">
        <el-form-item label="疾病编码">
          <el-input v-model="diseDialog.code" maxlength="60" />
        </el-form-item>
        <el-form-item label="疾病名称">
          <el-input v-model="diseDialog.name" maxlength="120" />
        </el-form-item>
        <el-form-item label="ICD编码">
          <el-input v-model="diseDialog.icd" maxlength="60" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="diseDialog.status">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="diseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDisease">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { deleteBackfillRow, pageBackfillRows, saveBackfillRow } from '@/api/moduleBackfill'

export default {
  name: 'DiagnosisManagement',
  data() {
    return {
      catKeyword: '',
      rawCategories: [],
      shownCategories: [],
      selectedCat: null,
      diseases: [],
      diseKeyword: '',
      pageNo: 1,
      pageSize: 10,
      catDialogVisible: false,
      catDialog: { id: null, name: '', status: 1 },
      diseDialogVisible: false,
      diseDialog: { id: null, cat_id: null, code: '', name: '', icd: '', status: 1 }
    }
  },
  computed: {
    pagedDiseases() {
      const start = (this.pageNo - 1) * this.pageSize
      return this.diseases.slice(start, start + this.pageSize)
    }
  },
  mounted() {
    this.loadCategories()
  },
  methods: {
    async loadCategories() {
      const res = await pageBackfillRows('dms_dise_catalog', 1, 1000)
      if (!res || res.code !== 20000) {
        this.$message.error((res && res.message) || '加载分类失败')
        return
      }
      this.rawCategories = ((res.data && res.data.records) || []).sort((a, b) => (a.id || 0) - (b.id || 0))
      this.filterCategories()
      if (!this.selectedCat && this.shownCategories.length) {
        this.selectCategory(this.shownCategories[0])
      }
    },
    filterCategories() {
      const kw = (this.catKeyword || '').trim()
      this.shownCategories = this.rawCategories.filter(c => !kw || String(c.name || '').includes(kw))
    },
    resetCategoryFilter() {
      this.catKeyword = ''
      this.filterCategories()
    },
    selectCategory(cat) {
      this.selectedCat = cat
      this.pageNo = 1
      this.loadDiseases()
    },
    async loadDiseases() {
      if (!this.selectedCat) {
        this.diseases = []
        return
      }
      const res = await pageBackfillRows('dms_dise', 1, 2000)
      if (!res || res.code !== 20000) {
        this.$message.error((res && res.message) || '加载诊断失败')
        return
      }
      const all = (res.data && res.data.records) || []
      const kw = (this.diseKeyword || '').trim()
      this.diseases = all.filter(r => {
        if (Number(r.cat_id) !== Number(this.selectedCat.id)) return false
        if (!kw) return true
        return String(r.name || '').includes(kw) || String(r.code || '').includes(kw) || String(r.icd || '').includes(kw)
      })
      this.pageNo = 1
    },
    openCatDialog(row) {
      const r = row || {}
      this.catDialog = { id: r.id || null, name: r.name || '', status: r.status == null ? 1 : Number(r.status) }
      this.catDialogVisible = true
    },
    async saveCategory() {
      if (!this.catDialog.name) {
        this.$message.warning('分类名称不能为空')
        return
      }
      const res = await saveBackfillRow('dms_dise_catalog', this.catDialog)
      if (!res || res.code !== 20000) {
        this.$message.error((res && res.message) || '保存失败')
        return
      }
      this.$message.success('保存成功')
      this.catDialogVisible = false
      await this.loadCategories()
    },
    removeCategory(row) {
      this.$confirm(`确认删除分类 ${row.name} ?`, '提示', { type: 'warning' })
        .then(async () => {
          const res = await deleteBackfillRow('dms_dise_catalog', row.id)
          if (!res || res.code !== 20000) {
            this.$message.error((res && res.message) || '删除失败')
            return
          }
          this.$message.success('删除成功')
          if (this.selectedCat && Number(this.selectedCat.id) === Number(row.id)) {
            this.selectedCat = null
          }
          await this.loadCategories()
        })
        .catch(() => {})
    },
    openDiseDialog(row) {
      const r = row || {}
      this.diseDialog = {
        id: r.id || null,
        cat_id: this.selectedCat ? this.selectedCat.id : null,
        code: r.code || '',
        name: r.name || '',
        icd: r.icd || '',
        status: r.status == null ? 1 : Number(r.status)
      }
      this.diseDialogVisible = true
    },
    async saveDisease() {
      if (!this.selectedCat) {
        this.$message.warning('请先选择分类')
        return
      }
      if (!this.diseDialog.name) {
        this.$message.warning('疾病名称不能为空')
        return
      }
      const payload = { ...this.diseDialog, cat_id: this.selectedCat.id }
      const res = await saveBackfillRow('dms_dise', payload)
      if (!res || res.code !== 20000) {
        this.$message.error((res && res.message) || '保存失败')
        return
      }
      this.$message.success('保存成功')
      this.diseDialogVisible = false
      await this.loadDiseases()
    },
    removeDisease(row) {
      this.$confirm(`确认删除诊断 ${row.name} ?`, '提示', { type: 'warning' })
        .then(async () => {
          const res = await deleteBackfillRow('dms_dise', row.id)
          if (!res || res.code !== 20000) {
            this.$message.error((res && res.message) || '删除失败')
            return
          }
          this.$message.success('删除成功')
          await this.loadDiseases()
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
.diagnosis-page {
  padding: 0;
}
.w220 { width: 220px; }
.w240 { width: 240px; }
.layout {
  display: flex;
  gap: 12px;
}
.left,
.right { padding: 10px; }
.left {
  width: 300px;
}
.right {
  flex: 1;
}
.left-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  margin-bottom: 8px;
}
.cat-list {
  max-height: 560px;
  overflow: auto;
}
.cat-item {
  border: 1px solid #edf1f7;
  border-radius: 6px;
  margin-bottom: 8px;
  padding: 8px 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
}
.cat-item.active {
  border-color: #b79bf8;
  background: #f1ebff;
}
.cat-name {
  font-size: 13px;
  color: #303133;
}
.cat-ops {
  white-space: nowrap;
}
.right-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  gap: 10px;
}
.head-label {
  color: #7b8798;
}
.head-value {
  font-weight: 700;
  color: #303133;
}
</style>
