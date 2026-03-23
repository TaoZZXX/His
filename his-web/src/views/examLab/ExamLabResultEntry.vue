<template>
  <div class="exam-lab-page">
    <div class="page-head">
      <h2 class="title">检查检验结果录入</h2>
      <div class="tools">
        <el-switch v-model="filterDept" active-text="按本科室过滤" inactive-text="全院" @change="load" />
        <el-button type="primary" icon="el-icon-refresh" @click="load">刷新</el-button>
      </div>
    </div>
    <p class="hint">仅显示已执行（待录入或已录入）的检查/检验项目。申请来源为<strong>门诊医生工作台</strong>保存的检查/检验单；默认按全院展示，可按需打开「按本科室过滤」。</p>
    <el-table v-loading="loading" :data="rows" stripe border>
      <el-table-column prop="type" label="类型" width="88" align="center">
        <template slot-scope="{ row }">
          <el-tag :type="row.type === 1 ? 'primary' : 'success'" size="small">{{ typeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="patientName" label="患者" min-width="100" />
      <el-table-column prop="medicalRecordNo" label="病历号" width="140" show-overflow-tooltip />
      <el-table-column prop="projectName" label="项目" min-width="140" show-overflow-tooltip />
      <el-table-column prop="deptName" label="挂号科室" width="100" />
      <el-table-column prop="checkResult" label="已有结果摘要" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="100" fixed="right" align="center">
        <template slot-scope="{ row }">
          <el-button type="text" @click="openDialog(row)">录入</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="录入检查/检验结果" :visible.sync="dialogVisible" width="640px" append-to-body @closed="resetForm">
      <el-form :model="form" label-width="100px">
        <el-form-item label="患者">
          <span>{{ currentRow ? currentRow.patientName : '—' }} / {{ currentRow ? currentRow.projectName : '—' }}</span>
        </el-form-item>
        <el-form-item label="结果" required>
          <el-input v-model="form.checkResult" type="textarea" :rows="5" placeholder="检查所见 / 检验数值与结论" />
        </el-form-item>
        <el-form-item label="临床印象">
          <el-input v-model="form.clinicalImpression" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="临床诊断">
          <el-input v-model="form.clinicalDiagnosis" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getExamLabQueue, saveExamLabResult } from '@/api/examLab'

export default {
  name: 'ExamLabResultEntry',
  data() {
    return {
      loading: false,
      rows: [],
      filterDept: false,
      dialogVisible: false,
      saving: false,
      currentRow: null,
      form: {
        checkResult: '',
        clinicalImpression: '',
        clinicalDiagnosis: ''
      }
    }
  },
  created() {
    this.load()
  },
  methods: {
    typeLabel(t) {
      if (t === 1) return '检查'
      if (t === 3) return '检验'
      return '—'
    },
    async load() {
      this.loading = true
      try {
        const res = await getExamLabQueue('result', this.filterDept ? 1 : 0)
        if (!res || res.code !== 20000) {
          this.$message.error((res && res.message) || '加载失败')
          this.rows = []
          return
        }
        this.rows = Array.isArray(res.data) ? res.data : []
      } catch (e) {
        this.$message.error('加载失败')
        this.rows = []
      } finally {
        this.loading = false
      }
    },
    openDialog(row) {
      this.currentRow = row
      this.form = {
        checkResult: row.checkResult || '',
        clinicalImpression: row.clinicalImpression || '',
        clinicalDiagnosis: row.clinicalDiagnosis || ''
      }
      this.dialogVisible = true
    },
    resetForm() {
      this.currentRow = null
      this.form = { checkResult: '', clinicalImpression: '', clinicalDiagnosis: '' }
    },
    async submit() {
      if (!this.currentRow) return
      const text = (this.form.checkResult || '').trim()
      if (!text) {
        this.$message.warning('请填写结果')
        return
      }
      this.saving = true
      try {
        const res = await saveExamLabResult(this.currentRow.id, {
          checkResult: text,
          clinicalImpression: (this.form.clinicalImpression || '').trim(),
          clinicalDiagnosis: (this.form.clinicalDiagnosis || '').trim()
        })
        if (!res || res.code !== 20000) {
          this.$message.error((res && res.message) || '保存失败')
          return
        }
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.load()
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.exam-lab-page {
  padding: 0;
}
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 12px;
}
.title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #27364a;
}
.tools {
  display: flex;
  align-items: center;
  gap: 12px;
}
.hint {
  font-size: 13px;
  color: #606266;
  margin: 0 0 14px;
  line-height: 1.5;
}
</style>
