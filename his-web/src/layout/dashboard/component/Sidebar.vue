<template>
  <div class="sidebar-container">
    <!-- Element UI 折叠菜单 -->
    <el-menu
        ref="menu"
        :default-active="activeMenu"
        class="el-menu-vertical-demo"
        :collapse="isCollapse"
        background-color="#1f2a3d"
        text-color="#c7d2e0"
        active-text-color="#7cb3ff"
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
  background: linear-gradient(180deg, #1f2a3d 0%, #162033 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 2px 0 14px rgba(11, 22, 40, 0.22);
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
  border-radius: 8px;
  margin: 4px 8px;
  height: 42px;
  line-height: 42px;
}

/* 嵌套子菜单区域背景：与一级区分 */
.el-menu-vertical-demo :deep(.el-submenu .el-menu) {
  background: rgba(12, 22, 38, 0.72) !important;
  margin: 4px 6px 8px;
  padding: 6px 0 8px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

/* 再嵌一层（如 首页 > 门诊收费挂号）更深一点 */
.el-menu-vertical-demo :deep(.el-submenu .el-submenu .el-menu) {
  background: rgba(8, 16, 30, 0.88) !important;
  border-color: rgba(124, 179, 255, 0.12);
}

.el-menu-vertical-demo :deep(.el-submenu .el-menu-item) {
  margin: 3px 10px;
  background: transparent !important;
}

.el-menu-vertical-demo :deep(.el-submenu .el-menu-item:hover) {
  background: rgba(124, 179, 255, 0.12) !important;
}

.el-menu-vertical-demo :deep(.el-menu-item.is-active) {
  background: rgba(124, 179, 255, 0.18) !important;
  color: #dcebff !important;
}

.el-menu-vertical-demo :deep(.el-submenu .el-menu-item.is-active) {
  background: rgba(124, 179, 255, 0.22) !important;
}
</style>