<template>
  <div class="history-container">
    <el-tag
      v-for="tag in visitedViews"
      :key="tag.path"
      closable
      :disable-transitions="false"
      @close="handleClose(tag)"
      @click="handleClick(tag)"
      :type="isActive(tag) ? '' : 'info'"
    >
      {{ tag.title }}
    </el-tag>
  </div>
</template>

<script>
export default {
  name: 'History',
  props: {
    visitedViews: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    isActive(route) {
      return route.path === this.$route.path;
    },
    handleClose(tag) {
      this.$emit('close', tag);
    },
    handleClick(tag) {
      // 防止重复点击 同一路由
      if (this.$router.currentRoute.path === tag.path) {
        return;
      }

      this.$router.push(tag.path);
    }
  }
};
</script>

<style scoped>
.history-container {
  padding: 5px;
}
.el-tag {
  margin-right: 5px;
  cursor: pointer;
}
</style>

