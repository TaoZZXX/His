<template>
  <div class="sidebar-container">
    <!-- Element UI 折叠菜单 -->
    <el-menu
        ref="menu"
        :default-active="activeMenu"
        class="el-menu-vertical-demo"
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        mode="vertical"
        :default-openeds="defaultOpeneds"
    >
      <el-submenu index="1" @mouseenter.native="handleMenuMouseEnter('1')" @mouseleave.native="handleMenuMouseLeave('1')">
        <template slot="title">
          <i class="el-icon-s-home"></i>
          <span slot="title">首页</span>
        </template>

        <el-submenu index="1-1" @mouseenter.native="handleMenuMouseEnter('1-1')" @mouseleave.native="handleMenuMouseLeave('1-1')">
          <template slot="title">
            <i class="el-icon-s-order"></i>
            <span>门诊收费挂号</span>
          </template>
          <el-menu-item index="/charge/registration" @click="goTo('/charge/registration')">
            门诊挂号工作台
          </el-menu-item>
          <el-menu-item index="/charge/registration" @click="goTo('/charge/registration/editRegistration')">
            编辑
          </el-menu-item>
        </el-submenu>
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

      <el-menu-item index="/pharmacy" @click="goTo('/pharmacy')">
        <i class="el-icon-s-management"></i>
        <span slot="title">药房</span>
      </el-menu-item>

      <el-submenu index="4" @mouseenter.native="handleMenuMouseEnter('4')" @mouseleave.native="handleMenuMouseLeave('4')">
        <template slot="title">
          <i class="el-icon-s-home"></i>
          <span slot="title">系统管理</span>
        </template>

        <el-submenu index="4-1" @mouseenter.native="handleMenuMouseEnter('4-1')" @mouseleave.native="handleMenuMouseLeave('4-1')">
          <template slot="title">
            <i class="el-icon-s-order"></i>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/admin/staff" @click="goTo('/admin/staff')">
            角色权限管理
          </el-menu-item>
        </el-submenu>
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
      defaultOpeneds: ['1']
    };
  },
  computed: {
    activeMenu() {
      const { path } = this.$route;
      const pathToIndex = {
        '/home': '1',
        '/charge': '1-1',
        '/charge/registration': '1-1',
        '/pharmacy': '4',
        '/system': '5'
      };
      return pathToIndex[path] || path;
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
}

.el-menu-vertical-demo {
  width: 100%;
  height: 100%;
  border-right: none;
}

.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 180px;
  min-height: 400px;
}
</style>