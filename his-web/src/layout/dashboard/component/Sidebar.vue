<template>
  <div class="sidebar-container">
    <!-- Element UI 折叠菜单 -->
    <el-menu
        ref="menu"
        :default-active="activeMenu"
        class="el-menu-vertical-demo"
        :collapse="isCollapse"
        background-color="#f1ebff"
        text-color="#534c66"
        active-text-color="#2f2a3a"
        mode="vertical"
        :default-openeds="defaultOpeneds"
    >
      <el-submenu index="1" @mouseenter.native="handleMenuMouseEnter('1')" @mouseleave.native="handleMenuMouseLeave('1')">
        <template slot="title">
          <i class="el-icon-s-home"></i>
          <span slot="title">首页</span>
        </template>
          <el-menu-item index="/charge/registration" @click="goTo('/charge/registration')">
            门诊挂号工作台
          </el-menu-item>
          <el-menu-item index="/charge/registration/editRegistration" @click="goTo('/charge/registration/editRegistration')">
            退号/编辑
          </el-menu-item>
      </el-submenu>

      <el-submenu index="7" @mouseenter.native="handleMenuMouseEnter('7')" @mouseleave.native="handleMenuMouseLeave('7')">
        <template slot="title">
          <i class="el-icon-money"></i>
          <span slot="title">收费与财务</span>
        </template>
        <el-menu-item index="/cashier/workbench" @click="goTo('/cashier/workbench')">
          门诊收费台
        </el-menu-item>
        <el-menu-item index="/cashier/daily-settlement/history" @click="goTo('/cashier/daily-settlement/history')">
          历史日结
        </el-menu-item>
        <el-menu-item index="/cashier/daily-settlement/audit" @click="goTo('/cashier/daily-settlement/audit')">
          日结核对
        </el-menu-item>
        <el-menu-item index="/cashier/extension" @click="goTo('/cashier/extension')">
          财务扩展
        </el-menu-item>
      </el-submenu>

      <el-submenu index="2" @mouseenter.native="handleMenuMouseEnter('2')" @mouseleave.native="handleMenuMouseLeave('2')">
        <template slot="title">
          <i class="el-icon-s-cooperation"></i>
          <span slot="title">门诊医生</span>
        </template>

        <el-menu-item index="/doctor/outpatient" @click="goTo('/doctor/outpatient')">
          门诊医生工作台
        </el-menu-item>
        <el-menu-item index="/doctor/schedule" @click="goTo('/doctor/schedule')">
          医生排班管理
        </el-menu-item>
        <el-menu-item index="/doctor/template-center" @click="goTo('/doctor/template-center')">
          医生模板中心
        </el-menu-item>
        <el-menu-item index="/doctor/diagnosis-management" @click="goTo('/doctor/diagnosis-management')">
          诊断目录管理
        </el-menu-item>
      </el-submenu>

      <el-submenu index="5" class="nav-group--exam" @mouseenter.native="handleMenuMouseEnter('5')" @mouseleave.native="handleMenuMouseLeave('5')">
        <template slot="title">
          <i class="el-icon-document"></i>
          <span slot="title">检查检验</span>
        </template>
        <el-menu-item index="/exam-lab/workbench" @click="goTo('/exam-lab/workbench')">
          门诊医技工作台
        </el-menu-item>
      </el-submenu>

      <el-submenu index="6" class="nav-group--pharmacy" @mouseenter.native="handleMenuMouseEnter('6')" @mouseleave.native="handleMenuMouseLeave('6')">
        <template slot="title">
          <i class="el-icon-s-management"></i>
          <span slot="title">药房</span>
        </template>
        <el-menu-item index="/pharmacy/workbench" @click="goTo('/pharmacy/workbench')">
          药房工作台
        </el-menu-item>
        <el-menu-item index="/pharmacy/drugs" @click="goTo('/pharmacy/drugs')">
          药品维护
        </el-menu-item>
      </el-submenu>

      <el-submenu index="4" @mouseenter.native="handleMenuMouseEnter('4')" @mouseleave.native="handleMenuMouseLeave('4')">
        <template slot="title">
          <i class="el-icon-s-home"></i>
          <span slot="title">系统管理</span>
        </template>
        <el-menu-item index="/admin/staff" @click="goTo('/admin/staff')">
          角色权限管理
        </el-menu-item>
        <el-menu-item index="/admin/roles" @click="goTo('/admin/roles')">
          权限分配
        </el-menu-item>
        <el-menu-item index="/admin/departments" @click="goTo('/admin/departments')">
          科室管理
        </el-menu-item>
        <el-menu-item index="/admin/module-backfill" @click="goTo('/admin/module-backfill')">
          模块补齐工作台
        </el-menu-item>
        <el-menu-item index="/admin/login-logs" @click="goTo('/admin/login-logs')">
          登录日志查看
        </el-menu-item>
        <el-menu-item index="/admin/operation-logs" @click="goTo('/admin/operation-logs')">
          操作日志
        </el-menu-item>
        <el-menu-item index="/admin/audit-ops" @click="goTo('/admin/audit-ops')">
          系统审计与运营
        </el-menu-item>
      </el-submenu>
    </el-menu>
  </div>
</template>

<script>
export default {
  name: 'Sidebar',
  props: {
    isCollapse: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      // 须同时展开「首页」与「门诊收费挂号」，否则第二层下的「门诊挂号工作台」默认折叠，容易以为没有收银入口
      defaultOpeneds: ['1', '6', '7']
    };
  },
  computed: {
    // 与 el-menu-item 的 index（路由 path）一致，高亮才正确；不要填子菜单的 index（如 1-1）
    activeMenu() {
      return this.$route.path
    }
  },
  methods: {
    goTo(path) {
      if (this.$route.path !== path) {
        this.$router.push(path);
      }
    },
    handleMenuMouseEnter(index) {
      if (this.isCollapse) return;
      this.$refs.menu.open(index);
    },
    handleMenuMouseLeave(index) {
      if (this.isCollapse) return;
      this.$refs.menu.close(index);
    }
  }
};
</script>

<style scoped>
.sidebar-container {
  height: 100%;
  transition: width 0.2s ease;
  background: linear-gradient(180deg, #f4f0ff 0%, #ece4ff 100%);
  border-right: 1px solid #ddd6f3;
  box-shadow: none;
}

.el-menu-vertical-demo {
  width: 100%;
  height: 100%;
  border-right: none;
  padding-top: 8px;
}

.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 180px;
  min-height: 400px;
}

.el-menu-vertical-demo :deep(.el-submenu__title),
.el-menu-vertical-demo :deep(.el-menu-item) {
  border-radius: 0;
  margin: 4px 8px;
  height: 42px;
  line-height: 42px;
}

/* 嵌套子菜单区域背景：与一级区分 */
.el-menu-vertical-demo :deep(.el-submenu .el-menu) {
  background: #f7f5ff !important;
  margin: 4px 6px 8px;
  padding: 6px 0 8px;
  border-radius: 0;
  border: 1px solid #e3dcf8;
}

/* 再嵌一层（如 首页 > 门诊收费挂号）更深一点 */
.el-menu-vertical-demo :deep(.el-submenu .el-submenu .el-menu) {
  background: #f1ebff !important;
  border-color: #ddd6f3;
}

.el-menu-vertical-demo :deep(.el-submenu .el-menu-item) {
  margin: 3px 10px;
  background: transparent !important;
}

.el-menu-vertical-demo :deep(.el-submenu .el-menu-item:hover) {
  background: #ece4ff !important;
}

.el-menu-vertical-demo :deep(.el-menu-item.is-active) {
  background: #e2d7ff !important;
  color: #2f2a3a !important;
}

.el-menu-vertical-demo :deep(.el-submenu .el-menu-item.is-active) {
  background: #dfd3ff !important;
}
</style>