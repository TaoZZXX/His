<template>
  <div class="exam-lab-page">
    <div class="page-head">
      <h2 class="title">检查检验登记、执行</h2>
      <div class="tools">
        <el-switch v-model="filterDept" active-text="按本科室过滤" inactive-text="全院" @change="load" />
        <el-button type="primary" icon="el-icon-refresh" @click="load">刷新</el-button>
      </div>
    </div>
    <p class="hint">
      <strong>说明：</strong>检查/检验申请是在<strong>门诊医生工作台</strong>里选患者后，在「检查申请」页签保存的，并<strong>不会</strong>在「门诊挂号工作台」生成。
      下方列表为待执行记录；若仍为空，请打开「按本科室过滤」前先看全院数据是否已有。
    </p>
    <el-table v-loading="loading" :data="rows" stripe border>
      <el-table-column prop="type" label="类型" width="88" align="center">
        <template slot-scope="{ row }">
          <el-tag :type="row.type === 1 ? 'primary' : 'success'" size="small">{{ typeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="patientName" label="患者" min-width="100" />
      <el-table-column prop="medicalRecordNo" label="病历号" width="140" show-overflow-tooltip />
      <el-table-column prop="projectName" label="项目" min-width="160" show-overflow-tooltip />
      <el-table-column prop="deptName" label="挂号科室" width="110" />
      <el-table-column prop="attendanceDate" label="就诊日" width="110" />
      <el-table-column prop="demand" label="申请说明" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="120" fixed="right" align="center">
        <template slot-scope="{ row }">
          <el-button type="primary" size="small" @click="onExecute(row)">确认执行</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { getExamLabQueue, executeExamLabItem } from '@/api/examLab'

export default {
  name: 'ExamLabExecute',
  data() {
    return {
      loading: false,
      rows: [],
      filterDept: false
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
        const res = await getExamLabQueue('execute', this.filterDept ? 1 : 0)
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
    onExecute(row) {
      this.$confirm('确认已登记执行该项目？', '提示', { type: 'warning' })
        .then(async () => {
          const res = await executeExamLabItem(row.id)
          if (!res || res.code !== 20000) {
            this.$message.error((res && res.message) || '操作失败')
            return
          }
          this.$message.success('已登记执行')
          this.load()
        })
        .catch(() => {})
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
