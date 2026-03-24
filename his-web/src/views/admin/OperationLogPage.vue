<template>
  <div class="operation-log-page">
    <div class="ui-page-title">操作日志</div>
    <div class="ui-toolbar">
      <el-input v-model="keyword" placeholder="关键词搜索（接口/用户名/IP）" class="w320" clearable @keyup.enter.native="loadRows" />
      <el-button type="primary" icon="el-icon-search" @click="loadRows">搜索</el-button>
    </div>

    <div class="ui-table-wrap">
    <el-table :data="pagedRows" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="opType" label="操作类型" width="100" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="method" label="方法" width="90" />
      <el-table-column prop="path" label="接口路径" min-width="260" show-overflow-tooltip />
      <el-table-column prop="statusCode" label="状态码" width="90" />
      <el-table-column prop="clientIp" label="客户端IP" min-width="140" />
      <el-table-column prop="create_time" label="操作时间" min-width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template slot-scope="{ row }">
          <el-button type="text" size="small" class="ui-danger" @click="removeLog(row)">删除</el-button>
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
  </div>
</template>

<script>
import { deleteBackfillRow, pageBackfillRows } from '@/api/moduleBackfill'
import { getStaffList } from '@/api/staff'

export default {
  name: 'OperationLogPage',
  data() {
    return {
      keyword: '',
      rows: [],
      pageNo: 1,
      pageSize: 10,
      staffNameMap: {}
    }
  },
  computed: {
    pagedRows() {
      const s = (this.pageNo - 1) * this.pageSize
      return this.rows.slice(s, s + this.pageSize)
    }
  },
  mounted() {
    this.loadRows()
  },
  methods: {
    async loadRows() {
      await this.loadStaffNameMap()
      const res = await pageBackfillRows('sms_login_log', 1, 3000)
      if (!res || res.code !== 20000) {
        this.$message.error((res && res.message) || '加载失败')
        return
      }
      const all = (res.data && res.data.records) || []
      const kw = (this.keyword || '').trim()
      this.rows = all
        .filter(r => String((r && r.ip) || '').indexOf('OP|') === 0)
        .map(r => {
          const parsed = this.parseOpIp(r.ip || '')
          return {
            ...r,
            username: this.resolveUsername(r.user_id),
            method: parsed.method,
            path: parsed.path,
            statusCode: parsed.statusCode,
            clientIp: parsed.clientIp
          }
        })
        .filter(r => {
          if (!kw) return true
          return Object.values(r || {}).some(v => String(v || '').includes(kw))
        })
      this.pageNo = 1
    },
    parseOpIp(raw) {
      const text = String(raw || '')
      const body = text.startsWith('OP|') ? text.substring(3) : text
      const parts = body.split('|')
      let opType = ''
      let idxStart = 0
      if (parts.length > 0 && ['C', 'U', 'D', 'O'].includes((parts[0] || '').trim())) {
        const c = (parts[0] || '').trim()
        opType = c === 'D' ? '删除' : (c === 'U' ? '修改' : (c === 'C' ? '新增' : '其他'))
        idxStart = 1
      }
      const methodAndPath = (parts[idxStart] || '').trim()
      const idx = methodAndPath.indexOf(' ')
      const method = idx > 0 ? methodAndPath.substring(0, idx) : ''
      const path = idx > 0 ? methodAndPath.substring(idx + 1) : methodAndPath
      let statusCode = ''
      let clientIp = ''
      parts.slice(idxStart + 1).forEach(p => {
        if (p.indexOf('status=') === 0) statusCode = p.substring(7)
        if (p.indexOf('S=') === 0) statusCode = p.substring(2)
        if (p.indexOf('ip=') === 0) clientIp = p.substring(3)
        if (p.indexOf('I=') === 0) clientIp = p.substring(2)
      })
      if (!opType) {
        opType = method === 'DELETE' ? '删除' : (method === 'PUT' || method === 'PATCH' ? '修改' : (method === 'POST' ? '新增' : '其他'))
      }
      return { opType, method, path, statusCode, clientIp }
    },
    async loadStaffNameMap() {
      const res = await getStaffList({ page: 1, size: 2000 })
      if (!res || res.code !== 20000) {
        this.staffNameMap = {}
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
      if (userId === null || userId === undefined || userId === '') return '匿名'
      return this.staffNameMap[String(userId)] || `用户${userId}`
    },
    removeLog(row) {
      if (!row || !row.id) {
        return
      }
      this.$confirm('确认删除该操作日志？', '提示', { type: 'warning' })
        .then(async () => {
          const res = await deleteBackfillRow('sms_login_log', row.id)
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
.w320 { width: 320px; }
</style>
