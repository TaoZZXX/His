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
          <el-button
              icon="el-icon-s-fold"
              circle
              @click="toggleCollapse"
              class="collapse-btn"
          ></el-button>
          {{ currentPageTitle }}
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
    // 切换折叠/展开状态
    toggleCollapse() {
      this.isCollapse = !this.isCollapse;
    },
    addViewTags() {
      const { name } = this.$route;
      if (name) {
        if (this.visitedViews.some(v => v.path === this.$route.path)) {
          return;
        }
        this.visitedViews.push(
          Object.assign({}, this.$route, {
            title: this.$route.meta.title || 'no-name'
          })
        );
      }
    },
    handleClose(view) {
      if (this.visitedViews.length <= 1) {
        this.$message.warning("最后一个标签页不能关闭！");
        return;
      }
      this.visitedViews.splice(this.visitedViews.indexOf(view), 1);
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
        this.$router.push('/');
      }
    }
  },
  computed: {
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
  width: 150px; /* 展开状态宽度 */
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
}

/* 顶部导航：核心修改 - 改为垂直布局，高度适配两行内容 */
.app-top-nav {
  /* 调整高度：适配两行内容，原60px不够，改为100px（可根据需求调整） */
  height: 100px;
  width: 100%;
  flex-shrink: 0;
  /* 改为垂直布局，让子元素独占两行 */
  display: flex;
  flex-direction: column;
  justify-content: center; /* 垂直居中 */
  padding: 0 20px;
  gap: 8px; /* 两行之间的间距 */
  border-bottom: 1px solid #d8dce5;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.12), 0 0 3px 0 rgba(0, 0, 0, 0.04);
}

/* 折叠按钮样式 */
.collapse-btn {
  margin-right: 20px;
  background-color: #409EFF;
  color: #fff;
}

/* 第一行：标题区域 */
.app-top-nav-title {
  /* 改为block，独占一行 */
  display: block;
  width: 100%;
  height: 40px; /* 第一行高度 */
  line-height: 40px; /* 文字垂直居中 */
}

/* 第二行：历史区域 */
.app-top-nav-history {
  /* 改为block，独占一行 */
  display: block;
  width: 100%;
  height: 30px; /* 第二行高度 */
  line-height: 30px; /* 文字垂直居中 */
}

/* 主内容区 */
.app-content {
  flex: 1;
  width: 100%;
  overflow: auto;
}
</style>