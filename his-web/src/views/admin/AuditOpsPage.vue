<template>
  <div class="audit-ops-page">
    <div class="ui-page-title">{{ isLoginOnly ? '登录日志查看' : '系统审计与运营' }}</div>
    <el-tabs v-if="!isLoginOnly" v-model="activeTab" type="card" @tab-click="onTabChange">
      <el-tab-pane label="登录日志" name="login" />
      <el-tab-pane label="工作量记录" name="workload" />
      <el-tab-pane label="常用组合" name="frequent" />
      <el-tab-pane label="描述字典" name="desc" />
    </el-tabs>

    <div class="ui-toolbar">
      <el-input v-model="keyword" placeholder="关键词搜索" class="w240" clearable @keyup.enter.native="loadRows" />
      <el-button type="primary" icon="el-icon-search" @click="loadRows">搜索</el-button>
      <el-button v-if="!isReadonlyTab" type="primary" plain icon="el-icon-plus" @click="openDialog()">新增</el-button>
    </div>

    <div class="ui-table-wrap">
    <el-table :data="pagedRows" border stripe>
      <el-table-column v-for="c in columns" :key="c.prop" :prop="c.prop" :label="c.label" :min-width="c.width || 120" show-overflow-tooltip />
      <el-table-column v-if="!isReadonlyTab" label="操作" width="130" fixed="right">
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
import { getStaffList } from '@/api/staff'

export default {
  name: 'AuditOpsPage',
  data() {
    return {
      activeTab: 'login',
      keyword: '',
      rows: [],
      pageNo: 1,
      pageSize: 10,
      dialogVisible: false,
      dialog: {},
      staffNameMap: {}
    }
  },
  computed: {
    isLoginOnly() {
      return !!(this.$route && this.$route.meta && this.$route.meta.loginOnly)
    },
    isReadonlyTab() {
      return this.activeTab === 'login'
    },
    tableName() {
      if (this.activeTab === 'login') return 'sms_login_log'
      if (this.activeTab === 'workload') return 'sms_workload_record'
      if (this.activeTab === 'frequent') return 'sms_frequent_used'
      return 'sms_description'
    },
    columns() {
      const map = {
        login: [
          { prop: 'id', label: 'ID', width: 80 },
          { prop: 'login_result', label: '结果', width: 100 },
          { prop: 'user_id', label: '用户ID', width: 100 },
          { prop: 'username', label: '用户名', width: 140 },
          { prop: 'ip', label: '登录IP', width: 140 },
          { prop: 'create_time', label: '登录时间', width: 180 }
        ],
        workload: [
          { prop: 'id', label: 'ID', width: 80 },
          { prop: 'staff_id', label: '医生ID', width: 100 },
          { prop: 'dept_id', label: '科室ID', width: 100 },
          { prop: 'date', label: '日期', width: 130 },
          { prop: 'amount', label: '金额', width: 100 },
          { prop: 'registration_num', label: '接诊人数', width: 100 },
          { prop: 'status', label: '状态', width: 90 }
        ],
        frequent: [
          { prop: 'id', label: 'ID', width: 80 },
          { prop: 'staff_id', label: '医生ID', width: 100 },
          { prop: 'medicine_dise_id_list', label: '成药诊断列表', width: 160 },
          { prop: 'herbal_dise_id_list', label: '草药诊断列表', width: 160 },
          { prop: 'check_model_id_list', label: '检查模板列表', width: 160 },
          { prop: 'test_model_id_list', label: '检验模板列表', width: 160 }
        ],
        desc: [
          { prop: 'id', label: 'ID', width: 80 },
          { prop: 'description', label: '描述内容', width: 220 },
          { prop: 'type', label: '类型', width: 90 },
          { prop: 'url', label: 'URL', width: 180 },
          { prop: 'the_id', label: '关联ID', width: 100 },
          { prop: 'star', label: '星级', width: 90 },
          { prop: 'status', label: '状态', width: 90 }
        ]
      }
      return map[this.activeTab]
    },
    formFields() {
      const map = {
        login: [
          { prop: 'user_id', label: '用户ID' },
          { prop: 'ip', label: '登录IP' },
          { prop: 'create_time', label: '登录时间' }
        ],
        workload: [
          { prop: 'staff_id', label: '医生ID' },
          { prop: 'dept_id', label: '科室ID' },
          { prop: 'date', label: '日期' },
          { prop: 'registration_num', label: '接诊人数' },
          { prop: 'amount', label: '金额' },
          { prop: 'status', label: '状态' }
        ],
        frequent: [
          { prop: 'staff_id', label: '医生ID' },
          { prop: 'medicine_dise_id_list', label: '成药诊断列表' },
          { prop: 'herbal_dise_id_list', label: '草药诊断列表' },
          { prop: 'check_model_id_list', label: '检查模板列表' },
          { prop: 'test_model_id_list', label: '检验模板列表' }
        ],
        desc: [
          { prop: 'description', label: '描述内容' },
          { prop: 'type', label: '类型' },
          { prop: 'url', label: 'URL' },
          { prop: 'the_id', label: '关联ID' },
          { prop: 'star', label: '星级' },
          { prop: 'status', label: '状态' }
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
    if (this.isLoginOnly) {
      this.activeTab = 'login'
    }
    this.loadRows()
  },
  watch: {
    '$route.meta.loginOnly': {
      immediate: false,
      handler(val) {
        if (val) {
          this.activeTab = 'login'
          this.pageNo = 1
          this.keyword = ''
          this.loadRows()
        }
      }
    }
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
      if (this.activeTab === 'login') {
        await this.loadStaffNameMap()
      }
      const all = (res.data && res.data.records) || []
      const kw = (this.keyword || '').trim()
      this.rows = all.filter(r => {
        if (this.activeTab !== 'login') return true
        return String((r && r.ip) || '').indexOf('OP|') !== 0
      }).map(r => {
        if (this.activeTab !== 'login') return r
        const uid = r.user_id
        return {
          ...r,
          login_result: (uid === null || uid === undefined || uid === '') ? '失败' : '成功',
          username: this.resolveUsername(uid)
        }
      }).filter(r => {
        if (!kw) return true
        return Object.values(r || {}).some(v => String(v || '').includes(kw))
      })
      this.pageNo = 1
    },
    async loadStaffNameMap() {
      const res = await getStaffList({ page: 1, size: 2000 })
      if (!res || res.code !== 20000) {
        return
      }
      const list = (res.data && res.data.list) || []
      const map = {}
      list.forEach(it => {
        if (it && it.id !== undefined && it.id !== null) {
          map[String(it.id)] = it.name || it.username || String(it.id)
        }
      })
      this.staffNameMap = map
    },
    resolveUsername(userId) {
      if (userId === null || userId === undefined || userId === '') {
        return '—'
      }
      return this.staffNameMap[String(userId)] || `用户${userId}`
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
