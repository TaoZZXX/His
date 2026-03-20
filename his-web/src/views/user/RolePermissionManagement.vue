<template>
  <div class="role-permission-management">
    <div class="page-title">角色与权限管理</div>
    <div class="page-header">
      <el-button type="primary" @click="openCreate">新增角色</el-button>
      <el-input v-model="keyword" placeholder="搜索角色" style="margin-left:12px; width:240px" @keyup.enter.native="onSearch" />
    </div>

    <el-row :gutter="20">
      <el-col :span="10">
        <el-table :data="roles" stripe style="width:100%" v-loading="loading" @row-click="onRoleSelect">
          <el-table-column prop="name" label="角色名" />
          <el-table-column prop="description" label="描述" width="240" />
          <el-table-column label="操作" width="160">
            <template v-slot="{ row }">
              <el-button type="text" size="small" @click.stop="openEdit(row)">编辑</el-button>
              <el-button type="text" size="small" style="color:#f56c6c" @click.stop="confirmDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>

      <el-col :span="14">
        <div class="permissions-box">
          <h3>权限分配</h3>
          <el-row>
            <el-col :span="16">
              <el-tree
                :data="permissionTree"
                show-checkbox
                node-key="id"
                default-expand-all
                :props="{ children: 'children', label: 'label' }"
                ref="permTree"
                @node-click="onPermissionNodeClick"
              />
            </el-col>
            <el-col :span="8">
              <div style="padding-left:12px">
                <el-button type="primary" size="mini" @click="openAddPermission" style="margin-bottom:8px">新增子权限</el-button>
                <el-button type="warning" size="mini" @click="openEditPermission" :disabled="!selectedPermission" style="margin-bottom:8px">编辑权限</el-button>
                <el-button type="danger" size="mini" @click="confirmDeletePermission" :disabled="!selectedPermission">删除权限</el-button>
              </div>
            </el-col>
          </el-row>

          <div style="margin-top:12px">
            <el-button type="primary" :disabled="!selectedRole" @click="savePermissions">保存权限</el-button>
            <span style="margin-left:12px;color:#888">当前选中角色: {{ selectedRole ? selectedRole.name : '无' }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- permission dialog -->
    <el-dialog :title="permEditing.id ? '编辑权限' : '新增权限'" :visible.sync="permDialogVisible">
      <el-form :model="permEditing" label-width="90px">
        <el-form-item label="名称">
          <el-input v-model="permEditing.name" />
        </el-form-item>
        <el-form-item label="值">
          <el-input v-model="permEditing.value" />
        </el-form-item>
        <el-form-item label="URL">
          <el-input v-model="permEditing.url" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="permEditing.sort" :min="0" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePermission">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="editing.id ? '编辑角色' : '新增角色'" :visible.sync="editingDialog">
      <el-form :model="editing" label-width="90px">
        <el-form-item label="角色名">
          <el-input v-model="editing.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editing.description" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editing.status">
            <el-option :label="'启用'" :value="1" />
            <el-option :label="'禁用'" :value="0" />
          </el-select>
        </el-form-item>

        <!-- Permission tree inside role dialog -->
        <el-form-item label="权限分配">
          <el-tree
            :data="permissionTree"
            show-checkbox
            node-key="id"
            default-expand-all
            :props="{ children: 'children', label: 'label' }"
            ref="rolePermTree"
          />
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
import { getRoleList, getPermissions, createRole, updateRole, deleteRole, updateRolePermissions, getRolePermissions, createPermission, updatePermission, deletePermission } from '@/api/role'
export default {
  name: 'RolePermissionManagement',
  data() {
    return {
      loading: false,
      roles: [],
      permissionTree: [],
      selectedRole: null,
      selectedPermission: null,
      editingDialog: false,
      editing: {},
      permDialogVisible: false,
      permEditing: {},
      keyword: '',
      lastRolesRaw: null // debug: store last fetched roles raw data
    }
  },
  mounted() {
    this.fetchRoles()
    this.fetchPermissions()
  },
  methods: {
    async fetchRoles() {
      this.loading = true
      try {
        const res = await getRoleList({})
        const resp = res && (res.data || res) || {}

        // helper: find first array anywhere in an object (depth-limited)
        const findArray = (obj, depth = 5) => {
          if (!obj || depth < 0) return null
          if (Array.isArray(obj)) return obj
          if (typeof obj !== 'object') return null
          for (const key of Object.keys(obj)) {
            try {
              const val = obj[key]
              if (Array.isArray(val)) return val
              if (val && typeof val === 'object') {
                const found = findArray(val, depth - 1)
                if (Array.isArray(found)) return found
              }
            } catch (e) {
              // ignore
            }
          }
          return null
        }

        let items = findArray(resp) || []

        // fallback: if there's no nested array, check common properties directly
        if (!items || items.length === 0) {
          if (Array.isArray(resp)) items = resp
          else if (Array.isArray(res)) items = res
          else items = []
        }

        console.debug('[RolePermission] fetched roles raw:', res, 'extracted items:', items)

        this.roles = (items || []).map(r => ({
          id: r.id,
          name: r.name || r.roleName || r.loginName || r.username,
          description: r.description || r.remark || r.note || r.memo || '',
          status: r.status != null ? r.status : (r.enabled != null ? (r.enabled ? 1 : 0) : null)
        }))

        // debug: store the raw response for inspection
        this.lastRolesRaw = res
      } catch (err) {
        console.error(err)
        this.$message.error('获取角色失败')
      } finally {
        this.loading = false
      }
    },
    async fetchPermissions() {
      try {
        const res = await getPermissions()
        const data = res.data || res || []
        // backend returns PermissionNode tree; keep fields for "edit permission" dialog.
        const mapNode = (p) => {
          const children = Array.isArray(p && p.children) ? p.children.map(mapNode) : []
          return {
            // el-tree
            id: p.id,
            label: p.label || p.name || '',
            children,
            // permission form fields
            name: p.name || p.label || '',
            value: p.value,
            url: p.url,
            sort: p.sort,
            status: p.status,
            pid: p.pid,
            type: p.type
          }
        }

        if (Array.isArray(data)) {
          this.permissionTree = data.map(mapNode)
        } else {
          this.permissionTree = []
        }
      } catch (err) {
        console.error(err)
        this.permissionTree = []
      }
    },
    async onRoleSelect(row) {
      this.selectedRole = row
      this.$nextTick(async () => {
        if (!this.selectedRole) return
        try {
          const res = await getRolePermissions(this.selectedRole.id)
          const data = res.data || res || []
          const ids = Array.isArray(data) ? data : (data.data || [])
          this.$refs.permTree.setCheckedKeys(ids)
        } catch (err) {
          console.error(err)
        }
      })
    },
    openCreate() {
      this.editing = { name: '', description: '', status: 1 }
      this.editingDialog = true
      this.$nextTick(() => {
        if (this.$refs.rolePermTree) this.$refs.rolePermTree.setCheckedKeys([])
      })
    },
    openEdit(row) {
      this.editing = Object.assign({}, row)
      this.editingDialog = true
      // load role permissions into dialog tree
      this.$nextTick(async () => {
        try {
          const res = await getRolePermissions(row.id)
          const data = res.data || res || []
          const ids = Array.isArray(data) ? data : (data.data || [])
          if (this.$refs.rolePermTree) this.$refs.rolePermTree.setCheckedKeys(ids)
        } catch (err) {
          console.error('failed to load role permissions for edit', err)
        }
      })
    },
    async saveEditing() {
      if (!this.editing.name) { this.$message.warning('请填写角色名'); return }
      try {
        // collect selected permission ids from dialog tree
        const permTree = this.$refs.rolePermTree
        const checked = permTree ? permTree.getCheckedKeys() : []

        if (this.editing.id) {
          await updateRole(this.editing.id, this.editing)
          // update permissions
          await updateRolePermissions(this.editing.id, checked)
          this.$message.success('更新成功')
        } else {
          // create role first
          await createRole(this.editing)
          // re-fetch roles to find created id (by name)
          await this.fetchRoles()
          const created = this.roles.find(r => r.name === this.editing.name)
          if (created && created.id) {
            await updateRolePermissions(created.id, checked)
          } else {
            console.warn('created role id not found after create; permissions not applied')
          }
          this.$message.success('创建成功')
        }
        this.editingDialog = false
        this.fetchRoles()
      } catch (err) {
        console.error(err)
        this.$message.error('保存失败')
      }
    },
    confirmDelete(row) {
      this.$confirm('确认删除该角色？', '提示', { type: 'warning' })
        .then(async () => {
          try {
            await deleteRole(row.id)
            this.$message.success('删除成功')
            this.fetchRoles()
          } catch (err) {
            console.error(err)
            this.$message.error('删除失败')
          }
        }).catch(() => {})
    },
    async savePermissions() {
      if (!this.selectedRole) { this.$message.warning('请选择角色'); return }
      const checked = this.$refs.permTree.getCheckedKeys()
      try {
        await updateRolePermissions(this.selectedRole.id, checked)
        this.$message.success('保存成功')
      } catch (err) {
        console.error(err)
        this.$message.error('保存失败')
      }
    },
    onSearch() {
      // simple client-side filter; replace with server search if preferred
      if (!this.keyword) { this.fetchRoles(); return }
      this.roles = this.roles.filter(r => (r.name || '').toLowerCase().includes(this.keyword.toLowerCase()) || (r.description || '').toLowerCase().includes(this.keyword.toLowerCase()))
    },
    onPermissionNodeClick(node) {
      this.selectedPermission = node
    },
    openAddPermission() {
      this.permEditing = { name: '', value: '', url: '', sort: 0 }
      this.permDialogVisible = true
    },
    openEditPermission() {
      if (!this.selectedPermission) {
        const tree = this.$refs.permTree
        if (!tree) return
        const checkedNodes = tree.getCheckedNodes(false, true) || []
        if (checkedNodes.length === 0) return
        if (checkedNodes.length > 1) {
          this.$message.warning('当前勾选了多个权限，编辑将以第一个为准')
        }
        this.selectedPermission = checkedNodes[0]
      }
      this.permEditing = Object.assign({}, this.selectedPermission)
      this.permDialogVisible = true
    },
    async savePermission() {
      if (!this.permEditing.name) { this.$message.warning('请填写权限名称'); return }
      try {
        if (this.permEditing.id) {
          await updatePermission(this.permEditing.id, this.permEditing)
          this.$message.success('权限更新成功')
        } else {
          await createPermission(this.permEditing)
          this.$message.success('权限创建成功')
        }
        this.permDialogVisible = false
        this.fetchPermissions()
      } catch (err) {
        console.error(err)
        this.$message.error('保存权限失败')
      }
    },
    confirmDeletePermission() {
      if (!this.selectedPermission) return
      this.$confirm('确认删除该权限？', '提示', { type: 'warning' })
        .then(async () => {
          try {
            await deletePermission(this.selectedPermission.id)
            this.$message.success('权限删除成功')
            this.fetchPermissions()
          } catch (err) {
            console.error(err)
            this.$message.error('删除权限失败')
          }
        }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.role-permission-management {
  padding: 0;
}
.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #27364a;
  margin-bottom: 14px;
}
.role-permission-management .page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  background: #fff;
  border: 1px solid #e8edf5;
  border-radius: 12px;
  padding: 12px;
}
.permissions-box {
  padding: 14px;
  border: 1px solid #e8edf5;
  border-radius: 12px;
  background: #fff;
}
.permissions-box h3 {
  margin-bottom: 12px;
  color: #30435a;
}
.role-permission-management :deep(.el-table) {
  border: 1px solid #e8edf5;
  border-radius: 12px;
  overflow: hidden;
}
</style>
