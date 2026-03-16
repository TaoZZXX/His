<template>
  <div class="user-role-management">
    <div class="page-header">
      <el-button type="primary" @click="openCreate">新增用户</el-button>

      <el-select v-model="filters.dept" placeholder="科室选择" clearable style="margin-left:12px; width:180px" @change="onFilterChange">
        <el-option v-for="d in departments" :key="d.value" :label="d.label" :value="d.value" />
      </el-select>

      <el-select v-model="filters.role" placeholder="用户角色" clearable style="margin-left:12px; width:160px" @change="onFilterChange">
        <el-option v-for="r in roles" :key="r.value" :label="r.label" :value="r.value" />
      </el-select>
    </div>

    <el-table :data="pagedUsers" stripe style="width:100%" v-loading="loading">
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="realName" label="真实姓名" width="140" />
      <el-table-column prop="roleLabel" label="角色" width="120" />
      <el-table-column prop="deptLabel" label="科室" width="140" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="180">
        <template v-slot="scope">
          <el-button size="mini" type="primary" @click="openEdit(scope.row)">修改权限</el-button>
          <el-button size="mini" type="danger" @click="confirmDelete(scope.row)" style="margin-left:8px">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        background
        :page-size="pageSize"
        :current-page.sync="currentPage"
        :total="totalCount"
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10,20,50]"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 编辑/新增对话框 -->
    <el-dialog :title="editing.id ? '修改权限' : '新增用户'" :visible.sync="editingDialog" width="520px">
      <el-form :model="editing" label-width="110px">
        <el-form-item label="用户名">
          <el-input v-model="editing.username" :disabled="editing.id" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="editing.realName" />
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model="editing.dept" placeholder="请选择科室">
            <el-option v-for="d in departments" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editing.role" placeholder="请选择角色">
            <el-option v-for="r in roles" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editing.remark" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editingDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEditing">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import md5 from 'js-md5'
import { getStaffList, getDepartments, getRoles, createStaff, updateStaff, deleteStaff } from '@/api/staff'
export default {
  name: 'UserRoleManagement',
  data() {
    return {
      loading: false,
      filters: {
        dept: null,
        role: null
      },
      departments: [
        { value: 'cardio', label: '心血管内科' },
        { value: 'neuro', label: '神经内科' },
        { value: 'gi', label: '消化内科' }
      ],
      roles: [
        { value: 'admin', label: '管理员' },
        { value: 'doctor', label: '门诊医生' },
        { value: 'pharmacist', label: '药房医生' }
      ],
      users: [],

      // pagination
      currentPage: 1,
      pageSize: 10,
      totalCount: 0,

      // edit dialog
      editingDialog: false,
      editing: {}
    }
  },
  computed: {
    // server-side pagination, users is current page
    pagedUsers() {
      return (this.users || []).map(u => ({
        ...u,
        roleLabel: (this.roles.find(r => r.value === u.role) || {}).label || u.role,
        deptLabel: u.deptName || (this.departments.find(d => d.value === u.dept) || {}).label || u.dept
      }))
    }
  },
  mounted() {
    this.fetchMeta()
    this.fetchList()
  },
  methods: {
    async fetchMeta() {
      // fetch departments and roles from backend if available
      try {
        const [deptRes, roleRes] = await Promise.all([getDepartments(), getRoles()])
        // normalize responses: backend might return { data: [...] } or [...] directly
        const dr = deptRes.data || deptRes
        const rr = roleRes.data || roleRes
        this.departments = Array.isArray(dr) ? dr.map(d => ({ value: d.value || d.id || d.code, label: d.label || d.name })) : this.departments
        this.roles = Array.isArray(rr) ? rr.map(r => ({ value: r.value || r.id || r.code, label: r.label || r.name })) : this.roles
      } catch (err) {
        // ignore and keep default meta
        console.error('failed to load meta', err)
      }
    },

    async fetchList(page = this.currentPage, size = this.pageSize) {
      this.loading = true
      try {
        // build query params: backend expects page (0-based) and size
        const params = { page: Math.max(0, page - 1), size }
        // backend expects deptId and roleId
        if (this.filters.dept) params.deptId = this.filters.dept
        if (this.filters.role) params.roleId = this.filters.role
        const res = await getStaffList(params)
        const data = res.data || res || {}
        // handle paginated response shape
        let items = []
        let total = 0
        if (data.content && Array.isArray(data.content)) {
          items = data.content
          total = data.totalElements || data.total || data.totalCount || 0
        } else if (Array.isArray(data)) {
          items = data
          total = items.length
        } else if (data.records && Array.isArray(data.records)) {
          items = data.records
          total = data.total || 0
        } else if (data.data && Array.isArray(data.data)) {
          items = data.data
          total = data.total || 0
        } else {
          // fallback: try to use data.list
          items = data.list || []
          total = data.total || 0
        }
        this.users = (items || []).map(u => ({
          username: u.username || u.loginName || u.name,
          name: u.realName || u.name || u.displayName,
          role: u.role || u.roleId || u.roleCode,
          dept: u.dept || u.deptId || u.deptCode,
          deptName: u.deptName || null,
          createTime: u.createTime || u.createdAt || u.create_date,
          remark: u.remark || u.notes || u.comment || ''
        }))
        this.currentPage = page
        this.pageSize = size
        this.totalCount = total
      } catch (err) {
        console.error(err)
        this.$message.error('获取用户列表失败')
      } finally {
        this.loading = false
      }
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.fetchList(1, size)
    },
    handlePageChange(page) {
      this.currentPage = page
      this.fetchList(page, this.pageSize)
    },
    onFilterChange() {
      this.fetchList(1, this.pageSize)
    },
    openEdit(row) {
      this.editing = Object.assign({}, row)
      this.editingDialog = true
    },
    openCreate() {
      this.editing = { username: '', realName: '', role: null, dept: null, remark: '' }
      this.editingDialog = true
    },
    async saveEditing() {
      if (!this.editing.username) {
        this.$message.warning('请填写用户名')
        return
      }
      this.loading = true
      try {
        if (this.editing.id) {
          await updateStaff(this.editing.id, this.editing)
          this.$message.success('更新成功')
        } else {
          // set default password to MD5('123456') for new users
          // don't overwrite if frontend explicitly set a password
          if (!this.editing.password) {
            this.editing.password = md5('123456')
          }
          await createStaff(this.editing)
          this.$message.success('创建成功')
        }
        this.editingDialog = false
        this.fetchList(1, this.pageSize)
      } catch (err) {
        console.error(err)
        this.$message.error('保存失败')
      } finally {
        this.loading = false
      }
    },
    confirmDelete(row) {
      this.$confirm('确认删除该用户？', '提示', { type: 'warning' })
        .then(async () => {
          try {
            await deleteStaff(row.username)
            this.$message.success('删除成功')
            this.fetchList(this.currentPage, this.pageSize)
          } catch (err) {
            console.error(err)
            this.$message.error('删除失败')
          }
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
.user-role-management .page-header {
  display:flex;
  align-items:center;
  gap:12px;
  margin-bottom:12px;
}
.pager { margin-top:12px; text-align:right }
</style>
