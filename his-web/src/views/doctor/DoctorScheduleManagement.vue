<template>
  <div class="schedule-page">
    <div class="page-title">医生排班管理</div>
    <div class="toolbar card-box">
      <span class="label">科室选择：</span>
      <el-select v-model="query.deptId" placeholder="请选择科室" filterable class="dept-select">
        <el-option
          v-for="d in deptOptions"
          :key="d.id"
          :label="d.deptName || d.name"
          :value="d.id"
        />
      </el-select>
      <el-button type="primary" icon="el-icon-search" @click="handleQuery" :loading="loading" style="margin-left:12px;">
        查询
      </el-button>
      <el-button @click="handleReset" style="margin-left:8px;">清空</el-button>
    </div>

    <!-- 医生一周排班勾选表 -->
    <div class="card-box">
      <el-table
        v-loading="loading"
        :data="rows"
        border
        stripe
        style="width: 100%;"
        class="skd-table"
      >
      <el-table-column type="selection" width="42" />
      <el-table-column prop="doctorName" label="医生名称" min-width="160" />

      <el-table-column
        v-for="col in weekCols"
        :key="col.key"
        :label="col.label"
        min-width="92"
        align="center"
      >
        <template v-slot="scope">
          <div class="cell">
            <el-checkbox v-model="scope.row.skd[col.key].am" />
            <el-checkbox v-model="scope.row.skd[col.key].pm" />
          </div>
        </template>
      </el-table-column>

      <el-table-column label="排班限额" width="120" align="center">
        <template v-slot="scope">
          <el-input-number
            v-model="scope.row.limit"
            :min="0"
            :max="999"
            controls-position="right"
            size="mini"
          />
        </template>
      </el-table-column>
      </el-table>
    </div>

    <!-- 规则填写 + 操作 -->
    <div class="rule-bar card-box">
      <div class="rule-item">
        <span class="label">规则名称</span>
        <el-input v-model="rule.name" placeholder="请输入规则名称" class="rule-input" />
      </div>
      <div class="rule-item">
        <span class="label">规则描述</span>
        <el-input v-model="rule.desc" placeholder="请输入规则描述" class="rule-input" />
      </div>

      <el-button type="danger" icon="el-icon-close" @click="handleCancelRule" class="btn">
        取消
      </el-button>
      <el-button type="primary" icon="el-icon-plus" @click="handleCreateRule" class="btn">
        新增排班规则
      </el-button>
    </div>

    <!-- 规则列表 -->
    <div class="rule-list card-box">
      <h3 class="rule-list-title">已创建的排班规则</h3>
      <el-table
        :data="ruleList"
        stripe
        border
        size="small"
        style="width: 100%;"
      >
        <el-table-column prop="ruleName" label="规则名称" min-width="180" />
        <el-table-column prop="description" label="规则描述" min-width="220" />
        <el-table-column prop="status" label="状态" width="90">
          <template v-slot="scope">
            <span v-if="scope.row.status === 1">已发布</span>
            <span v-else>草稿</span>
          </template>
        </el-table-column>
        <el-table-column prop="operateTime" label="创建/更新时间" min-width="180" />
        <el-table-column label="操作" width="220" align="center">
          <template v-slot="scope">
            <el-button
              type="text"
              size="mini"
              @click="loadRuleToForm(scope.row)"
            >查看/加载</el-button>
            <el-button
              type="text"
              size="mini"
              @click="openGenerateDialog(scope.row)"
            >生成排班计划表</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 生成排班计划表弹窗 -->
    <el-dialog
      title="生成排班计划表"
      :visible.sync="generateDialog.visible"
      width="480px"
    >
      <el-form label-width="90px">
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="generateDialog.startDate"
            type="date"
            placeholder="请选择开始日期"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="generateDialog.endDate"
            type="date"
            placeholder="请选择结束日期"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="generateDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleGenerateSkd" :loading="generateDialog.loading">
          生成
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getDepartments,
  getDoctors,
  createSkdRule,
  listSkdRules,
  getSkdRuleDetail,
  generateSkdByRule
} from '@/api/registService'

export default {
  name: 'DoctorScheduleManagement',
  data() {
    return {
      loading: false,
      deptOptions: [],
      query: {
        deptId: ''
      },
      weekCols: [
        { key: 'mon', label: '星期一\n上午 下午' },
        { key: 'tue', label: '星期二\n上午 下午' },
        { key: 'wed', label: '星期三\n上午 下午' },
        { key: 'thu', label: '星期四\n上午 下午' },
        { key: 'fri', label: '星期五\n上午 下午' },
        { key: 'sat', label: '星期六\n上午 下午' },
        { key: 'sun', label: '星期日\n上午 下午' }
      ],
      rows: [],
      rule: {
        name: '',
        desc: ''
      },
      // 规则列表
      ruleList: [],
      // 生成排班计划表弹窗
      generateDialog: {
        visible: false,
        ruleId: null,
        startDate: '',
        endDate: '',
        loading: false
      }
    }
  },
  created() {
    this.loadDepartments()
  },
  methods: {
    makeEmptySkd() {
      return {
        mon: { am: false, pm: false },
        tue: { am: false, pm: false },
        wed: { am: false, pm: false },
        thu: { am: false, pm: false },
        fri: { am: false, pm: false },
        sat: { am: false, pm: false },
        sun: { am: false, pm: false }
      }
    },
    async loadDepartments() {
      try {
        const res = await getDepartments()
        if (res.code === 20000 && Array.isArray(res.data)) {
          this.deptOptions = res.data
        }
      } catch (e) {
        this.$message.error('获取科室列表失败')
      }
    },
    async handleQuery() {
      if (!this.query.deptId) {
        this.$message.warning('请选择科室')
        return
      }
      this.loading = true
      try {
        const res = await getDoctors(this.query.deptId)
        if (res.code !== 20000 || !Array.isArray(res.data)) {
          this.$message.error(res.message || '获取医生列表失败')
          return
        }
        this.rows = res.data.map(d => ({
          doctorId: d.id,
          doctorName: d.name,
          deptId: d.deptId || this.query.deptId,
          limit: 20,
          skd: this.makeEmptySkd()
        }))
      } catch (e) {
        this.$message.error('获取医生列表失败')
      } finally {
        this.loading = false
      }
      // 同时刷新规则列表
      this.fetchRuleList()
    },
    handleReset() {
      this.query.deptId = ''
      this.rows = []
      this.rule.name = ''
      this.rule.desc = ''
      this.ruleList = []
    },
    handleCancelRule() {
      this.rule.name = ''
      this.rule.desc = ''
    },
    handleCreateRule() {
      // 目前仅实现页面结构与前端数据收集；后端保存接口可后续补
      if (!this.rule.name) {
        this.$message.warning('请填写规则名称')
        return
      }
      if (!this.query.deptId) {
        this.$message.warning('请选择科室')
        return
      }
      if (!this.rows.length) {
        this.$message.warning('请先查询医生并勾选排班')
        return
      }

      const payload = {
        deptId: this.query.deptId,
        ruleName: this.rule.name,
        ruleDesc: this.rule.desc,
        items: this.rows.map(r => ({
          doctorId: r.doctorId,
          limit: r.limit,
          skd: r.skd
        }))
      }
      this.loading = true
      createSkdRule(payload)
        .then(res => {
          if (res.code !== 20000) {
            this.$message.error(res.message || '新增排班规则失败')
            return
          }
          const id = res.data && res.data.id
          this.$message.success(`新增排班规则成功${id ? '，ID=' + id : ''}`)
          this.fetchRuleList()
        })
        .finally(() => {
          this.loading = false
        })
    },
    async fetchRuleList() {
      if (!this.query.deptId) return
      try {
        const res = await listSkdRules(this.query.deptId)
        if (res.code === 20000 && Array.isArray(res.data)) {
          this.ruleList = res.data
        }
      } catch (e) {
        this.$message.error('获取排班规则列表失败')
      }
    },
    // 查看/加载规则到上面的勾选表
    async loadRuleToForm(ruleRow) {
      if (!ruleRow || !ruleRow.id) return
      try {
        const res = await getSkdRuleDetail(ruleRow.id)
        if (res.code !== 20000 || !res.data) {
          this.$message.error(res.message || '获取规则详情失败')
          return
        }
        const items = res.data.items || []
        // 先按当前科室重新加载医生列表，确保所有医生都在 rows 中
        await this.handleQuery()
        // 清空勾选
        this.rows.forEach(r => {
          r.skd = this.makeEmptySkd()
          r.limit = 20
        })
        // 根据 ruleItem 的 daysOfWeek 还原勾选
        items.forEach(it => {
          const row = this.rows.find(r => String(r.doctorId) === String(it.staffId))
          if (!row) return
          row.limit = it.skLimit || row.limit
          const daysStr = it.daysOfWeek || ''
          if (!daysStr) return
          daysStr.split(',').forEach(p => {
            const [day, part] = p.split('_')
            if (!day || !part) return
            if (!row.skd[day]) return
            if (part === 'am') row.skd[day].am = true
            if (part === 'pm') row.skd[day].pm = true
          })
        })
        this.rule.name = ruleRow.ruleName
        this.rule.desc = ruleRow.description
      } catch (e) {
        this.$message.error('加载规则失败')
      }
    },
    openGenerateDialog(ruleRow) {
      if (!ruleRow || !ruleRow.id) return
      this.generateDialog.ruleId = ruleRow.id
      this.generateDialog.startDate = ''
      this.generateDialog.endDate = ''
      this.generateDialog.visible = true
    },
    async handleGenerateSkd() {
      const { ruleId, startDate, endDate } = this.generateDialog
      if (!ruleId) {
        this.$message.warning('请先选择规则')
        return
      }
      if (!startDate || !endDate) {
        this.$message.warning('请选择开始和结束日期')
        return
      }
      const fmt = d => {
        if (!d) return ''
        if (typeof d === 'string') return d
        const dt = new Date(d)
        const y = dt.getFullYear()
        const m = String(dt.getMonth() + 1).padStart(2, '0')
        const day = String(dt.getDate()).padStart(2, '0')
        return `${y}-${m}-${day}`
      }
      const payload = {
        startDate: fmt(startDate),
        endDate: fmt(endDate)
      }
      this.generateDialog.loading = true
      try {
        const res = await generateSkdByRule(ruleId, payload)
        if (res.code !== 20000) {
          this.$message.error(res.message || '生成排班计划表失败')
          return
        }
        this.$message.success('生成排班计划表成功')
        this.generateDialog.visible = false
      } finally {
        this.generateDialog.loading = false
      }
    }
  }
}
</script>

<style scoped>
.schedule-page {
  padding: 0;
}
.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #27364a;
  margin-bottom: 14px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.label {
  color: #333;
  font-size: 14px;
  white-space: nowrap;
}

.dept-select {
  width: 220px;
}

.skd-table ::v-deep .el-table__header th .cell {
  white-space: pre-line;
  line-height: 16px;
}

.cell {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.rule-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 14px;
  flex-wrap: wrap;
}

.rule-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rule-input {
  width: 260px;
}

.btn {
  margin-left: auto;
}

.rule-list {
  margin-top: 20px;
}

.rule-list-title {
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #33475d;
}

.card-box {
  background: #fff;
  border: 1px solid #e8edf5;
  border-radius: 12px;
  padding: 14px;
  box-shadow: 0 6px 16px rgba(33, 53, 82, 0.05);
}
</style>

