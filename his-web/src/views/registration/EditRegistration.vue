<template>
  <div class="cancel-registration-container">
    <div class="header-controls">
      <el-input v-model="search.patientId" placeholder="请输入患者ID或身份证号" class="patient-input" />
      <el-button type="primary" @click="fetchRegistrations(1, pageSize)" :loading="loading">查询</el-button>
      <el-button @click="clear">清除</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="registrations"
      style="width: 100%"
      stripe
      border
    >
      <el-table-column prop="id" label="挂号ID" width="160" />
      <el-table-column prop="name" label="患者姓名" width="120" />
      <el-table-column prop="department" label="科室" width="140" />
      <el-table-column prop="doctor" label="医生" width="140" />
      <el-table-column prop="registrationDate" label="挂号日期" width="140">
        <template v-slot="scope">
          {{ formatDate(scope.row.registrationDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="200">
        <template v-slot="scope">
          <el-button
            size="mini"
            type="primary"
            @click="openEdit(scope.row)"
            :disabled="!isEditable(scope.row)"
          >编辑</el-button>
          <el-button
            size="mini"
            type="danger"
            @click="confirmDelete(scope.row)"
            :loading="deleting[scope.row.id]"
            style="margin-left:8px"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="registrations.length === 0 && !loading" class="empty-tip">暂无挂号记录，输入患者ID后点击查询</div>

    <div class="pagination-container" v-if="total > 0 || registrations.length > 0">
      <el-pagination
        style="margin-top: 12px; text-align: right;"
        background
        :page-size="pageSize"
        :current-page="currentPage"
        :total="total"
        :page-sizes="pageSizes"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- 编辑对话框 -->
    <el-dialog title="编辑挂号信息" :visible.sync="editDialogVisible" width="600px">
      <el-form :model="current" ref="editForm" label-width="100px">
        <el-form-item label="患者姓名">
          <el-input v-model="current.name" disabled />
        </el-form-item>
        <el-form-item label="科室" prop="department">
          <el-input v-model="current.department" placeholder="科室" />
        </el-form-item>
        <el-form-item label="医生" prop="doctor">
          <el-input v-model="current.doctor" placeholder="医生" />
        </el-form-item>
        <el-form-item label="挂号日期" prop="registrationDate">
          <el-date-picker v-model="current.registrationDate" type="date" placeholder="选择日期" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input v-model="current.remarks" placeholder="备注" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveEdit">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getRegistrationsByPatient, updateRegistration, deleteRegistration } from '@/api/registService'

export default {
  name: 'EditRegistration',
  data() {
    return {
      search: {
        patientId: ''
      },
      registrations: [],
      loading: false,
      deleting: {}, // map registrationId => boolean

      // pagination
      currentPage: 1,
      pageSize: 10,
      total: 0,
      pageSizes: [10, 20, 50, 100],

       // 编辑相关
       editDialogVisible: false,
       current: {},
       saving: false
     }
   },
   methods: {
     formatDate(d) {
       if (!d) return ''
       if (typeof d === 'string') return d
       const dt = new Date(d)
       const y = dt.getFullYear()
       const m = String(dt.getMonth() + 1).padStart(2, '0')
       const day = String(dt.getDate()).padStart(2, '0')
       return `${y}-${m}-${day}`
     },

     // 支持分页的查询
     // page: 1-based page number for UI, size: page size
     // 注意：后端分页参数可能为 0-based page index；这里假设后端使用 0-based page（常见），因此发送 page-1
     async fetchRegistrations(page, size) {
      // fall back to current state when params are not provided
      page = (typeof page === 'number' && page > 0) ? page : this.currentPage
      size = (typeof size === 'number' && size > 0) ? size : this.pageSize

      if (!this.search.patientId) {
        this.$message.warning('请输入患者ID或身份证号后查询')
        return
      }
      // update pagination state
      this.currentPage = page
      this.pageSize = size
      this.loading = true
      try {
        const query = { page: Math.max(0, page - 1), size }
        const res = await getRegistrationsByPatient(this.search.patientId, query)
        const data = res.data || res || {}
        if (data.code && data.code !== 200 && data.code !== 20000) {
          this.$message.error(data.message || '查询失败')
          this.registrations = []
          this.total = 0
          return
        }

        // 解析常见分页结构：data.data/content/records/list/items/array
        const payload = data.data || data
        let items = []
        let total = 0

        if (payload && Array.isArray(payload)) {
          items = payload
          total = items.length
        } else if (payload && Array.isArray(payload.content)) {
          items = payload.content
          total = payload.totalElements || payload.total || payload.totalCount || payload.totalItems || 0
        } else if (payload && Array.isArray(payload.records)) {
          items = payload.records
          total = payload.total || payload.totalElements || 0
        } else if (payload && Array.isArray(payload.list)) {
          items = payload.list
          total = payload.total || 0
        } else if (payload && Array.isArray(payload.items)) {
          items = payload.items
          total = payload.total || 0
        } else if (payload && payload.content && Array.isArray(payload.content.items)) {
          items = payload.content.items
          total = payload.content.total || 0
        } else {
          // fallback: try nested fields or empty
          items = []
          total = payload.total || payload.totalElements || 0
        }

        this.registrations = (items || []).map(item => ({
          raw: item,
          id: item.id || item.registrationId || item.registration || item._id || item.registId,
          name: item.name || item.patientName || item.patient || '',
          department: item.department || item.dept || item.deptName || '',
          doctor: item.doctor || item.doctorName || '',
          registrationDate: item.registrationDate || item.date || item.createdAt || '',
          status: item.status || item.state || '已挂号',
          remarks: item.remarks || item.note || ''
        }))
        // 如果没有 total，则将 total 设为当前项数（用于分页控件基本显示）
        this.total = total || this.registrations.length
      } catch (err) {
        console.error(err)
        this.$message.error('查询挂号记录失败')
        this.registrations = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

     clear() {
      this.search.patientId = ''
      this.registrations = []
      this.currentPage = 1
      this.pageSize = 10
      this.total = 0
     },

     isEditable(row) {
      if (!row) return false
      // 根据状态决定是否可编辑，默认不可编辑已就诊或已退号记录
      const disabled = ['已就诊', '已退号', '已取消']
      return !disabled.includes(row.status)
    },

    openEdit(row) {
      if (!row || !row.id) return
      // 深拷贝当前行数据到 current
      this.current = JSON.parse(JSON.stringify(row))
      // 保证日期为 Date 或字符串可被 date-picker 处理
      if (this.current.registrationDate && typeof this.current.registrationDate === 'string') {
        // 留下字符串格式，date-picker 仍能处理 YYYY-MM-DD 字符串
      }
      this.editDialogVisible = true
    },

    async saveEdit() {
      // 简单校验
      if (!this.current.department || !this.current.registrationDate) {
        this.$message.warning('请填写科室和挂号日期')
        return
      }

      this.saving = true
      try {
        const payload = {
          // 按后端 updateRegistration 期望传递的字段构造
          registrationDate: this.current.registrationDate,
          department: this.current.department,
          doctor: this.current.doctor,
          remarks: this.current.remarks
        }
        const res = await updateRegistration(this.current.id, payload)
        const data = res.data || res || {}
        if (data.code && data.code !== 200 && data.code !== 20000) {
          this.$message.error(data.message || '保存失败')
          return
        }
        this.$message.success('保存成功')
        // 更新本地列表项
        const idx = this.registrations.findIndex(r => r.id === this.current.id)
        if (idx !== -1) {
          this.$set(this.registrations, idx, Object.assign({}, this.registrations[idx], {
            department: this.current.department,
            doctor: this.current.doctor,
            registrationDate: this.current.registrationDate,
            remarks: this.current.remarks
          }))
        }
        this.editDialogVisible = false
      } catch (err) {
        console.error(err)
        this.$message.error('保存失败')
      } finally {
        this.saving = false
      }
    },

    confirmDelete(row) {
      if (!row || !row.id) return
      this.$confirm('确认删除该挂号记录？删除后不可恢复', '删除确认', { type: 'warning' })
        .then(() => this.doDelete(row))
        .catch(() => {})
    },

    async doDelete(row) {
      const id = row.id
      if (!id) return
      this.$set(this.deleting, id, true)
      try {
        const res = await deleteRegistration(id)
        const data = res.data || res || {}
        if (data.code && data.code !== 200 && data.code !== 20000) {
          this.$message.error(data.message || '删除失败')
          return
        }
        this.$message.success('删除成功')
        // 从本地列表移除
        this.registrations = this.registrations.filter(r => r.id !== id)
      } catch (err) {
        console.error(err)
        this.$message.error('删除失败')
      } finally {
        this.$set(this.deleting, id, false)
      }
    },

    // 分页事件
    handlePageChange(page) {
      this.fetchRegistrations(page, this.pageSize)
    },

    handleSizeChange(size) {
      // 改变 pageSize 后回到第一页
      this.fetchRegistrations(1, size)
    }
   }
 }
</script>

<style scoped>
.cancel-registration-container {
  padding: 16px;
}
.header-controls {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  align-items: center;
}
.patient-input {
  width: 360px;
}
.empty-tip {
  padding: 24px;
  text-align: center;
  color: #999;
}
.pagination-container {
  margin-top: 8px;
}
</style>
