<template>
  <div class="doctor-template-center">
    <div class="title">医生模板中心</div>

    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane label="非药品模板管理" name="nonDrug">
        <div class="toolbar">
          <el-input v-model="nonDrugQuery.name" placeholder="模板名称" class="w200" clearable />
          <el-select v-model="nonDrugQuery.scope" placeholder="范围" class="w140" clearable>
            <el-option label="个人" :value="1" />
            <el-option label="科室" :value="2" />
            <el-option label="全院" :value="3" />
          </el-select>
          <el-select v-model="nonDrugQuery.type" placeholder="类型" class="w140" clearable>
            <el-option label="检查" :value="1" />
            <el-option label="检验" :value="3" />
          </el-select>
          <el-button type="primary" @click="loadNonDrugTemplates">搜索模板</el-button>
          <el-button type="primary" plain @click="openNonDrugDialog()">新建模板</el-button>
        </div>

        <el-table :data="nonDrugTemplates" border stripe>
          <el-table-column prop="id" label="模板编号" width="90" />
          <el-table-column prop="name" label="模板名称" min-width="160" />
          <el-table-column prop="aim" label="模板简介" min-width="180" show-overflow-tooltip />
          <el-table-column prop="code" label="模板编码" width="150" />
          <el-table-column label="范围" width="90">
            <template slot-scope="{ row }">{{ scopeText(row.scope) }}</template>
          </el-table-column>
          <el-table-column label="模板类型" width="100">
            <template slot-scope="{ row }">{{ nonDrugTypeText(row.type) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="130" fixed="right">
            <template slot-scope="{ row }">
              <el-button type="text" size="small" @click="openNonDrugDialog(row)">修改</el-button>
              <el-button type="text" size="small" class="danger" @click="removeNonDrugTemplate(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="病历模板管理" name="caseModel">
        <div class="case-layout">
          <div class="left-tree card">
            <div class="tree-tools">
              <el-button size="mini" type="primary" plain @click="createCatalogRoot">新增目录</el-button>
              <el-button size="mini" @click="createCaseModelNode">新增模板</el-button>
            </div>
            <el-tree
              :data="caseCatalogTree"
              node-key="id"
              :props="{ children: 'children', label: 'label' }"
              default-expand-all
              highlight-current
              @node-click="onCatalogNodeClick"
            />
          </div>

          <div class="right-form card">
            <div class="form-top">
              <el-button type="primary" @click="saveCaseModel">保存修改</el-button>
              <el-button type="danger" plain @click="removeCaseModel">删除模板</el-button>
            </div>

            <el-form :model="caseModelForm" label-width="96px" size="small">
              <el-row :gutter="12">
                <el-col :span="12"><el-form-item label="模板名"><el-input v-model="caseModelForm.name" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="主述"><el-input v-model="caseModelForm.chief_complaint" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="12">
                <el-col :span="12"><el-form-item label="现病史"><el-input v-model="caseModelForm.history_of_present_illness" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="既往史"><el-input v-model="caseModelForm.past_history" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="12">
                <el-col :span="12"><el-form-item label="现病治疗情况"><el-input v-model="caseModelForm.history_of_treatment" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="体格检查"><el-input v-model="caseModelForm.health_checkup" /></el-form-item></el-col>
              </el-row>
              <el-row :gutter="12">
                <el-col :span="12"><el-form-item label="过敏史"><el-input v-model="caseModelForm.allergies" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="状态">
                  <el-select v-model="caseModelForm.status"><el-option label="启用" :value="1" /><el-option label="停用" :value="0" /></el-select>
                </el-form-item></el-col>
              </el-row>
            </el-form>

            <div class="diag-card">
              <div class="diag-head">
                <span>初步诊断</span>
                <el-button type="text" @click="openDiseDialog">添加诊断</el-button>
              </div>
              <el-table :data="selectedDiseases" size="small" border>
                <el-table-column prop="icd" label="ICD编码" width="140" />
                <el-table-column prop="name" label="名称" min-width="180" />
                <el-table-column prop="code" label="编码" width="140" />
                <el-table-column label="操作" width="80">
                  <template slot-scope="{ $index }">
                    <el-button type="text" size="small" @click="removeDisease($index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog :title="nonDrugDialog.id ? '修改模板' : '新建模板'" :visible.sync="nonDrugDialogVisible" width="680px">
      <el-form :model="nonDrugDialog" label-width="100px">
        <el-form-item label="模板名称"><el-input v-model="nonDrugDialog.name" /></el-form-item>
        <el-form-item label="范围">
          <el-select v-model="nonDrugDialog.scope">
            <el-option label="个人" :value="1" />
            <el-option label="科室" :value="2" />
            <el-option label="全院" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="nonDrugDialog.type" @change="onNonDrugTypeChange">
            <el-option label="检查" :value="1" />
            <el-option label="检验" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="非药品项目">
          <el-select v-model="nonDrugDialogIds" filterable multiple style="width: 100%">
            <el-option v-for="it in nonDrugDictFiltered" :key="it.id" :label="`${it.name}(${it.code})`" :value="it.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板简介"><el-input v-model="nonDrugDialog.aim" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="模板编码"><el-input v-model="nonDrugDialog.code" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="nonDrugDialog.status"><el-option label="启用" :value="1" /><el-option label="停用" :value="0" /></el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="nonDrugDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveNonDrugTemplate">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog title="选择诊断" :visible.sync="diseDialogVisible" width="760px">
      <div class="toolbar">
        <el-input v-model="diseKeyword" placeholder="搜索疾病名称/ICD/编码" class="w260" clearable />
        <el-button type="primary" @click="loadDiseases">搜索</el-button>
      </div>
      <el-table :data="diseList" border size="small" height="360" @row-dblclick="chooseDisease">
        <el-table-column prop="icd" label="ICD编码" width="150" />
        <el-table-column prop="name" label="名称" min-width="200" />
        <el-table-column prop="code" label="编码" width="150" />
        <el-table-column label="操作" width="90">
          <template slot-scope="{ row }">
            <el-button type="text" size="small" @click="chooseDisease(row)">添加</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { deleteBackfillRow, pageBackfillRows, saveBackfillRow } from '@/api/moduleBackfill'

export default {
  name: 'DoctorTemplateCenter',
  data() {
    return {
      activeTab: 'nonDrug',
      nonDrugQuery: { name: '', scope: undefined, type: undefined },
      nonDrugTemplates: [],
      nonDrugDialogVisible: false,
      nonDrugDialog: { id: null, name: '', scope: 1, type: 1, non_drug_id_list: '', aim: '', code: '', status: 1 },
      nonDrugDialogIds: [],
      nonDrugDict: [],
      caseCatalogTree: [],
      selectedCatalogNode: null,
      caseModelMap: {},
      caseModelForm: {
        id: null,
        name: '',
        chief_complaint: '',
        history_of_present_illness: '',
        history_of_treatment: '',
        past_history: '',
        allergies: '',
        health_checkup: '',
        status: 1,
        priliminary_dise_id_list: '',
        priliminary_dise_str_list: ''
      },
      selectedDiseases: [],
      diseDialogVisible: false,
      diseKeyword: '',
      diseList: []
    }
  },
  computed: {
    nonDrugDictFiltered() {
      const t = Number(this.nonDrugDialog.type || 1)
      return (this.nonDrugDict || []).filter(x => Number(x.record_type) === t)
    }
  },
  mounted() {
    this.bootstrap()
  },
  methods: {
    async bootstrap() {
      await Promise.all([this.loadNonDrugTemplates(), this.loadNonDrugDict(), this.loadCaseTree(), this.loadDiseases()])
    },
    scopeText(v) {
      if (Number(v) === 1) return '个人'
      if (Number(v) === 2) return '科室'
      if (Number(v) === 3) return '全院'
      return '—'
    },
    nonDrugTypeText(v) {
      if (Number(v) === 1) return '检查'
      if (Number(v) === 3) return '检验'
      return `类型${v}`
    },
    async loadNonDrugTemplates() {
      const res = await pageBackfillRows('dms_non_drug_model', 1, 500)
      if (!res || res.code !== 20000) return
      const rows = (res.data && res.data.records) || []
      this.nonDrugTemplates = rows.filter(r => {
        const okName = !this.nonDrugQuery.name || String(r.name || '').includes(this.nonDrugQuery.name)
        const okScope = this.nonDrugQuery.scope == null || Number(r.scope) === Number(this.nonDrugQuery.scope)
        const okType = this.nonDrugQuery.type == null || Number(r.type) === Number(this.nonDrugQuery.type)
        return okName && okScope && okType
      })
    },
    async loadNonDrugDict() {
      const res = await pageBackfillRows('dms_non_drug', 1, 1000)
      if (!res || res.code !== 20000) return
      const rows = (res.data && res.data.records) || []
      this.nonDrugDict = rows.filter(r => Number(r.status) === 1)
    },
    onNonDrugTypeChange() {
      this.nonDrugDialogIds = []
    },
    openNonDrugDialog(row) {
      const r = row || {}
      this.nonDrugDialog = {
        id: r.id || null,
        name: r.name || '',
        scope: Number(r.scope || 1),
        type: Number(r.type || 1),
        non_drug_id_list: r.non_drug_id_list || '',
        aim: r.aim || '',
        code: r.code || '',
        status: r.status == null ? 1 : Number(r.status)
      }
      this.nonDrugDialogIds = String(this.nonDrugDialog.non_drug_id_list || '')
        .split(',')
        .map(s => Number(s.trim()))
        .filter(n => Number.isFinite(n) && n > 0)
      this.nonDrugDialogVisible = true
    },
    async saveNonDrugTemplate() {
      const payload = { ...this.nonDrugDialog, non_drug_id_list: (this.nonDrugDialogIds || []).join(',') }
      const res = await saveBackfillRow('dms_non_drug_model', payload)
      if (!res || res.code !== 20000) {
        this.$message.error((res && res.message) || '保存失败')
        return
      }
      this.$message.success('保存成功')
      this.nonDrugDialogVisible = false
      await this.loadNonDrugTemplates()
    },
    removeNonDrugTemplate(row) {
      this.$confirm(`确认删除模板 ${row.name || row.id} ?`, '提示', { type: 'warning' })
        .then(async () => {
          const res = await deleteBackfillRow('dms_non_drug_model', row.id)
          if (!res || res.code !== 20000) {
            this.$message.error((res && res.message) || '删除失败')
            return
          }
          this.$message.success('删除成功')
          await this.loadNonDrugTemplates()
        })
        .catch(() => {})
    },
    async loadCaseTree() {
      const [catRes, modelRes] = await Promise.all([
        pageBackfillRows('dms_case_model_catalog', 1, 800),
        pageBackfillRows('dms_case_model', 1, 800)
      ])
      if (!catRes || catRes.code !== 20000 || !modelRes || modelRes.code !== 20000) return
      const cats = (catRes.data && catRes.data.records) || []
      const models = (modelRes.data && modelRes.data.records) || []
      this.caseModelMap = models.reduce((m, r) => {
        m[r.id] = r
        return m
      }, {})
      const nodes = cats.map(c => ({
        ...c,
        id: Number(c.id),
        parent_id: c.parent_id == null ? null : Number(c.parent_id),
        label: this.caseModelMap[c.model_id] ? this.caseModelMap[c.model_id].name : (c.code || `目录${c.id}`),
        children: []
      }))
      const map = {}
      nodes.forEach(n => { map[n.id] = n })
      const roots = []
      nodes.forEach(n => {
        if (n.parent_id && map[n.parent_id]) map[n.parent_id].children.push(n)
        else roots.push(n)
      })
      this.caseCatalogTree = roots
    },
    onCatalogNodeClick(node) {
      this.selectedCatalogNode = node
      const model = node && node.model_id ? this.caseModelMap[node.model_id] : null
      if (!model) {
        this.resetCaseForm()
        return
      }
      this.caseModelForm = {
        id: model.id,
        name: model.name || '',
        chief_complaint: model.chief_complaint || '',
        history_of_present_illness: model.history_of_present_illness || '',
        history_of_treatment: model.history_of_treatment || '',
        past_history: model.past_history || '',
        allergies: model.allergies || '',
        health_checkup: model.health_checkup || '',
        status: model.status == null ? 1 : Number(model.status),
        priliminary_dise_id_list: model.priliminary_dise_id_list || '',
        priliminary_dise_str_list: model.priliminary_dise_str_list || ''
      }
      const ids = String(this.caseModelForm.priliminary_dise_id_list || '')
        .split(',')
        .map(s => Number(s.trim()))
        .filter(n => Number.isFinite(n) && n > 0)
      this.selectedDiseases = ids
        .map(id => this.diseList.find(d => Number(d.id) === id))
        .filter(Boolean)
    },
    resetCaseForm() {
      this.caseModelForm = {
        id: null, name: '', chief_complaint: '', history_of_present_illness: '', history_of_treatment: '',
        past_history: '', allergies: '', health_checkup: '', status: 1, priliminary_dise_id_list: '', priliminary_dise_str_list: ''
      }
      this.selectedDiseases = []
    },
    async createCatalogRoot() {
      const code = `CAT${Date.now()}`
      const res = await saveBackfillRow('dms_case_model_catalog', {
        parent_id: null, level: 1, type: 1, status: 1, model_id: null, scope: 1, own_id: null, code
      })
      if (!res || res.code !== 20000) {
        this.$message.error((res && res.message) || '新增目录失败')
        return
      }
      this.$message.success('目录已新增')
      await this.loadCaseTree()
    },
    async createCaseModelNode() {
      const modelRes = await saveBackfillRow('dms_case_model', {
        name: `模板${Date.now().toString().slice(-6)}`,
        chief_complaint: '',
        history_of_present_illness: '',
        history_of_treatment: '',
        past_history: '',
        allergies: '',
        health_checkup: '',
        priliminary_dise_id_list: '',
        priliminary_dise_str_list: '',
        status: 1
      })
      if (!modelRes || modelRes.code !== 20000) {
        this.$message.error((modelRes && modelRes.message) || '新增模板失败')
        return
      }
      const modelId = modelRes.data && modelRes.data.id
      const parentId = this.selectedCatalogNode ? this.selectedCatalogNode.id : null
      const res = await saveBackfillRow('dms_case_model_catalog', {
        parent_id: parentId,
        level: parentId ? 2 : 1,
        type: 2,
        status: 1,
        model_id: modelId,
        scope: 1,
        own_id: null,
        code: `M${modelId}`
      })
      if (!res || res.code !== 20000) {
        this.$message.error((res && res.message) || '目录节点创建失败')
        return
      }
      this.$message.success('模板节点已新增')
      await this.loadCaseTree()
    },
    async saveCaseModel() {
      if (!this.caseModelForm.name) {
        this.$message.warning('模板名不能为空')
        return
      }
      const ids = this.selectedDiseases.map(d => d.id)
      const names = this.selectedDiseases.map(d => d.name)
      const payload = {
        ...this.caseModelForm,
        priliminary_dise_id_list: ids.join(','),
        priliminary_dise_str_list: names.join(',')
      }
      const res = await saveBackfillRow('dms_case_model', payload)
      if (!res || res.code !== 20000) {
        this.$message.error((res && res.message) || '保存失败')
        return
      }
      this.$message.success('保存成功')
      await this.loadCaseTree()
    },
    removeCaseModel() {
      if (!this.caseModelForm.id) {
        this.$message.warning('请先选择模板节点')
        return
      }
      this.$confirm('确认删除当前病历模板吗？', '提示', { type: 'warning' })
        .then(async () => {
          const res = await deleteBackfillRow('dms_case_model', this.caseModelForm.id)
          if (!res || res.code !== 20000) {
            this.$message.error((res && res.message) || '删除失败')
            return
          }
          this.$message.success('删除成功')
          this.resetCaseForm()
          await this.loadCaseTree()
        })
        .catch(() => {})
    },
    openDiseDialog() {
      this.diseDialogVisible = true
    },
    async loadDiseases() {
      const res = await pageBackfillRows('dms_dise', 1, 1000)
      if (!res || res.code !== 20000) return
      const all = (res.data && res.data.records) || []
      const kw = (this.diseKeyword || '').trim()
      this.diseList = all.filter(r => {
        if (Number(r.status) !== 1) return false
        if (!kw) return true
        return String(r.name || '').includes(kw) || String(r.code || '').includes(kw) || String(r.icd || '').includes(kw)
      })
    },
    chooseDisease(row) {
      if (!row) return
      if (this.selectedDiseases.some(d => Number(d.id) === Number(row.id))) return
      this.selectedDiseases.push(row)
    },
    removeDisease(index) {
      this.selectedDiseases.splice(index, 1)
    }
  }
}
</script>

<style scoped>
.doctor-template-center {
  padding: 0;
}
.title {
  font-size: 20px;
  font-weight: 700;
  color: #27364a;
  margin-bottom: 12px;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.w140 { width: 140px; }
.w200 { width: 200px; }
.w260 { width: 260px; }
.danger { color: #f56c6c; }
.case-layout {
  display: flex;
  gap: 12px;
}
.card {
  border: 1px solid #e8edf5;
  border-radius: 10px;
  background: #fff;
  padding: 12px;
}
.left-tree {
  width: 280px;
}
.right-form {
  flex: 1;
}
.tree-tools {
  margin-bottom: 8px;
  display: flex;
  gap: 8px;
}
.form-top {
  margin-bottom: 10px;
  display: flex;
  gap: 8px;
}
.diag-card {
  margin-top: 10px;
}
.diag-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-weight: 600;
}
</style>
