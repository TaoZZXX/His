<template>
  <div id="app">
    <!-- 修复：添加动态类绑定控制侧边栏折叠 -->
    <div class="app-sider-bar" :class="{ collapsed: isCollapse }">
      <Sidebar :isCollapse="isCollapse" />
    </div>

    <!-- 主容器 -->
    <div class="app-main-container">
      <div class="app-top-nav">
        <!-- 顶部导航第一行：折叠图标 + 标题 -->
        <div class="app-top-nav-title">
          <div class="left-panel">
            <el-button
                icon="el-icon-s-fold"
                circle
                @click="toggleCollapse"
                class="collapse-btn"
            ></el-button>
            {{ currentPageTitle }}
          </div>
          <div class="right-panel">
            <el-dropdown class="avatar-container" trigger="click" @command="handleCommand">
              <span class="el-dropdown-link">
                当前用户：{{ name }} <span v-if="username">({{ username }})</span>
                <i class="el-icon-caret-bottom" />
              </span>
              <el-dropdown-menu slot="dropdown" class="user-dropdown">
                <el-dropdown-item command="logout">
                  <span style="display:block;">退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>

        <!-- 顶部导航第二行：历史/其他内容 -->
        <div class="app-top-nav-history">
          <!-- 添加历史记录组件 -->
          <History
            v-if="visitedViews.length"
            :visitedViews="visitedViews"
            @close="handleClose"
          />
        </div>
      </div>

      <!-- 主内容区 -->
      <div class="app-content">
        <router-view/>
      </div>
    </div>
  </div>
</template>

<script>
import Sidebar from '@/layout/dashboard/component/Sidebar.vue';
import History from '@/layout/dashboard/component/History.vue';
import { mapState } from 'vuex';

export default {
  name: 'App',
  components: {
    Sidebar,
    History
  },
  data() {
    return {
      isCollapse: false,
      visitedViews: []
    };
  },
  watch: {
    $route() {
      this.addViewTags();
    }
  },
  mounted() {
    this.addViewTags();
  },
  methods: {
    toggleCollapse() {
      this.isCollapse = !this.isCollapse;
    },
    addViewTags() {
      const { name, path } = this.$route;
      if (name) {
        if (this.visitedViews.some(v => v.path === path)) {
          return;
        }
        // 优先使用 route.meta.breadcrumb（如果存在并且是数组）
        let title = 'no-name';
        const metaBreadcrumb = this.$route.meta && this.$route.meta.breadcrumb;
        if (Array.isArray(metaBreadcrumb) && metaBreadcrumb.length) {
          title = metaBreadcrumb.join(' / ');
        } else {
          // 使用路由 matched 列表自动生成面包屑：读取每一段的 meta.title 或回退到 name/path
          const breadcrumb = (this.$route.matched || []).map(record => {
            return (record.meta && record.meta.title) ? record.meta.title : (record.name || record.path);
          }).filter(Boolean).join(' / ');
          title = breadcrumb || (this.$route.meta && this.$route.meta.title) || title;
        }

        this.visitedViews.push(
          Object.assign({}, this.$route, {
            title
          })
        );
      }
    },
    handleClose(view) {
      const idx = this.visitedViews.findIndex(v => v.path === view.path)
      if (idx < 0) return
      this.visitedViews.splice(idx, 1);
      if (this.isActive(view)) {
        this.toLastView(this.visitedViews);
      }
    },
    isActive(route) {
      return route.path === this.$route.path;
    },
    toLastView(visitedViews) {
      const latestView = visitedViews.slice(-1)[0];
      if (latestView) {
        this.$router.push(latestView.path);
      } else {
        this.$router.push('/home');
      }
    },
    handleCommand(command) {
      if (command === 'logout') {
        this.logout();
      }
    },
    async logout() {
      try {
        await this.$store.dispatch('user/logout');
        this.$router.push(`/login?redirect=${this.$route.fullPath}`);
      } catch (error) {
        // 即使后端请求失败，前端也应该清除状态并跳转到登录页
        // 这里可以选择提示错误，或者静默处理
        console.error("Logout failed:", error);
        // 强制清除前端状态
        await this.$store.dispatch('user/resetToken');
        this.$router.push(`/login?redirect=${this.$route.fullPath}`);
      }
    }
  },
  computed: {
    ...mapState('user', ['name', 'username']),
    currentPageTitle() {
      return this.$route.meta.title || '';
    }
  }
};
</script>

<style scoped>
#app {
  height: 100vh;
  width: 100vw;
  display: flex;
  overflow: hidden;
}

/* 侧边栏容器：适配折叠/展开的宽度过渡 */
.app-sider-bar {
  flex-shrink: 0; /* 防止被挤压 */
  transition: width 0.2s ease; /* 宽度过渡动画 */
  width: 180px; /* 展开状态宽度（与 Sidebar 一致） */
  position: relative;
  z-index: 30; /* 避免主区表格层级盖住侧栏 */
}

/* 折叠状态下侧边栏宽度（匹配Element折叠后的64px） */
.app-sider-bar.collapsed {
  width: 64px !important; /* 加!important确保优先级 */
}

/* 移除错误的v-bind样式写法（Vue2不支持） */

/* 主容器：flex占满剩余宽度 */
.app-main-container {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

/* 顶部导航：核心修改 - 改为垂直布局，高度适配两行内容 */
.app-top-nav {
  height: 92px;
  width: 100%;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 18px;
  gap: 6px;
  border-bottom: 1px solid #ddd6f3;
  background: linear-gradient(180deg, #ffffff 0%, #f7f5ff 100%);
  box-shadow: 0 4px 16px rgba(78, 60, 138, 0.08);
}

/* 折叠按钮样式 */
.collapse-btn {
  margin-right: 14px;
  color: #6d50b3;
  border: 1px solid #ddd6f3;
  background: #f4f0ff;
}

/* 第一行：标题区域 */
.app-top-nav-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  height: 38px;
  line-height: 38px;
  color: #2e3a4b;
  font-weight: 600;
}

.left-panel {
  display: flex;
  align-items: center;
}

.right-panel {
  padding-right: 6px;
  font-size: 14px;
  color: #625b73;
}

.el-dropdown-link {
  cursor: pointer;
  color: #6d50b3;
}

.el-icon-caret-bottom {
  font-size: 12px;
}

/* 第二行：历史区域 */
.app-top-nav-history {
  display: block;
  width: 100%;
  height: 30px;
  line-height: 30px;
}

/* 主内容区 */
.app-content {
  flex: 1;
  width: 100%;
  padding: 16px;
  overflow: auto;
  background: #f7f5ff;
  min-width: 0;
}

.app-content :deep(.el-table) {
  width: 100% !important;
}
</style>