<template>
  <div class="history-container">
    <div class="history-scroll">
      <el-tag
        v-for="tag in visitedViews"
        :key="tag.path"
        closable
        :disable-transitions="false"
        @close="handleClose(tag)"
        @click="handleClick(tag)"
        :class="['history-tag', { active: isActive(tag) }]"
      >
        {{ shortTitle(tag.title) }}
      </el-tag>
    </div>
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
    shortTitle(title) {
      if (!title) return ''
      const parts = String(title).split('/').map(s => s.trim()).filter(Boolean)
      return parts.length ? parts[parts.length - 1] : String(title).trim()
    },
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
  padding: 4px 0;
  width: 100%;
}
.history-scroll {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 2px;
}
.history-scroll::-webkit-scrollbar {
  height: 6px;
}
.history-scroll::-webkit-scrollbar-thumb {
  background: #d8cff3;
  border-radius: 999px;
}
.history-tag {
  cursor: pointer;
  border-radius: 0;
  border: 1px solid #e1daf6;
  color: #5f5577;
  background: #f7f5ff;
  transition: all .2s ease;
}
.history-tag:hover {
  background: #f0eaff;
  border-color: #d2c5fb;
}
.history-tag.active {
  color: #fff;
  background: linear-gradient(135deg, #8b5cf6, #a78bfa);
  border-color: #8b5cf6;
}
</style>

