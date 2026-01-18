<template>
  <div class="sidebar-container">
    <!-- Element UI 折叠菜单 -->
    <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical-demo"
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        collapse-transition
    >
    <el-menu-item index="1" @click="goTo('/')">
      <i class="el-icon-s-home"></i>
      <span slot="title">首页</span>
    </el-menu-item>
    <el-menu-item index="2" @click="goTo('/register')">
      <i class="el-icon-s-order"></i>
      <span slot="title">挂号</span>
    </el-menu-item>
    <el-menu-item index="3" @click="goTo('/charge')">
      <i class="el-icon-s-cooperation"></i>
      <span slot="title">收费</span>
    </el-menu-item>
    <el-menu-item index="4" @click="goTo('/pharmacy')">
      <i class="el-icon-s-management"></i>
      <span slot="title">药房</span>
    </el-menu-item>
    <el-menu-item index="5" @click="goTo('/system')">
      <i class="el-icon-s-platform"></i>
      <span slot="title">系统管理</span>
    </el-menu-item>
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
  computed: {
    activeMenu() {
      const route = this.$route;
      const { meta, path } = route;
      // 适配路由路径和菜单index的对应（可选，根据你的实际路由调整）
      if (meta.activeMenu) {
        return meta.activeMenu;
      }
      // 映射路由路径到菜单index（示例）
      const pathToIndex = {
        '/': '1',
        '/register': '2',
        '/charge': '3',
        '/pharmacy': '4',
        '/system': '5'
      };
      return pathToIndex[path] || path;
    }
  },
  methods: {
    goTo(path) {
      this.$router.push(path);
    }
  }
};
</script>

<style scoped>
.sidebar-container {
  height: 100%;
  transition: width 0.2s ease; /* 折叠/展开过渡动画 */
}

/* 展开状态宽度150px，折叠状态自动适配 */
.el-menu-vertical-demo {
  width: 100%;
  height: 100%;
  border-right: none;
}

/* 折叠状态下的最小宽度（Element默认折叠后宽度为64px） */
.el-menu--collapse {
  width: 64px !important;
}

/* 展开状态的基础宽度 */
.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 150px;
  min-height: 400px;
}
</style>