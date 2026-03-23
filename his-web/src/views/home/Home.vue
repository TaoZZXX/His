<template>
  <div class="dashboard-page">
    <div class="dashboard-toolbar">
      <router-link class="quick-link" to="/charge/registration">门诊挂号工作台</router-link>
    </div>

    <el-row :gutter="16" class="stat-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-icon stat-icon--teal">
            <i class="el-icon-user" />
          </div>
          <div class="stat-body">
            <div class="stat-label">个人接诊人数</div>
            <div class="stat-value">{{ formatInt(stats.personalTodayCount) }}</div>
            <div class="stat-hint">今日 · 按排班医生统计</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-icon stat-icon--blue">
            <i class="el-icon-s-custom" />
          </div>
          <div class="stat-body">
            <div class="stat-label">科室接诊人数</div>
            <div class="stat-value">{{ formatInt(stats.deptTodayCount) }}</div>
            <div class="stat-hint">今日 · 本科室</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-icon stat-icon--red">
            <i class="el-icon-s-data" />
          </div>
          <div class="stat-body">
            <div class="stat-label">全院接诊人数</div>
            <div class="stat-value">{{ formatInt(stats.hospitalTodayCount) }}</div>
            <div class="stat-hint">今日 · 不含已取消</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-icon stat-icon--green">
            <i class="el-icon-time" />
          </div>
          <div class="stat-body">
            <div class="stat-label">当前时间</div>
            <div class="stat-value stat-value--time">{{ nowStr }}</div>
            <div class="stat-hint">本地时间</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="24">
        <el-card shadow="never" class="chart-card">
          <div ref="lineChart" class="chart-box chart-box--line" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <div ref="pieChart" class="chart-box chart-box--half" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <div ref="barChart" class="chart-box chart-box--half" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { Message } from 'element-ui'
import { getHomeDashboard } from '@/api/homeDashboard'

function pad2(n) {
  return n < 10 ? `0${n}` : `${n}`
}

export default {
  name: 'Home',
  data() {
    return {
      nowStr: '',
      stats: {
        personalTodayCount: 0,
        deptTodayCount: 0,
        hospitalTodayCount: 0
      },
      visitSeries: [],
      feeByCategory: [],
      deptFeeBar: { categories: [], series: [] },
      chartLine: null,
      chartPie: null,
      chartBar: null,
      clockTimer: null
    }
  },
  mounted() {
    this.tickClock()
    this.clockTimer = setInterval(this.tickClock, 1000)
    this.loadDashboard()
    window.addEventListener('resize', this.onResize)
  },
  beforeDestroy() {
    if (this.clockTimer) {
      clearInterval(this.clockTimer)
    }
    window.removeEventListener('resize', this.onResize)
    this.disposeCharts()
  },
  methods: {
    formatInt(n) {
      return Number(n || 0).toLocaleString('zh-CN')
    },
    tickClock() {
      const d = new Date()
      this.nowStr = `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())} ${pad2(d.getHours())}:${pad2(d.getMinutes())}:${pad2(d.getSeconds())}`
    },
    onResize() {
      ;[this.chartLine, this.chartPie, this.chartBar].forEach(c => {
        if (c) c.resize()
      })
    },
    disposeCharts() {
      ;[this.chartLine, this.chartPie, this.chartBar].forEach(c => {
        if (c) {
          c.dispose()
        }
      })
      this.chartLine = null
      this.chartPie = null
      this.chartBar = null
    },
    async loadDashboard() {
      try {
        const res = await getHomeDashboard()
        if (!res || res.code !== 20000 || !res.data) {
          Message.error((res && res.message) || '加载首页数据失败')
          return
        }
        const d = res.data
        this.stats = {
          personalTodayCount: d.personalTodayCount,
          deptTodayCount: d.deptTodayCount,
          hospitalTodayCount: d.hospitalTodayCount
        }
        this.visitSeries = d.hospitalVisitLast7Days || []
        this.feeByCategory = d.feeByCategory7Days || []
        this.deptFeeBar = d.deptFeeBar7Days || { categories: [], series: [] }
        this.$nextTick(() => {
          this.ensureCharts()
          this.applyChartOptions()
        })
      } catch (e) {
        Message.error('加载首页数据失败')
      }
    },
    ensureCharts() {
      if (!this.chartLine && this.$refs.lineChart) {
        this.chartLine = echarts.init(this.$refs.lineChart)
      }
      if (!this.chartPie && this.$refs.pieChart) {
        this.chartPie = echarts.init(this.$refs.pieChart)
      }
      if (!this.chartBar && this.$refs.barChart) {
        this.chartBar = echarts.init(this.$refs.barChart)
      }
    },
    applyChartOptions() {
      const dates = this.visitSeries.map(p => p.date)
      const counts = this.visitSeries.map(p => p.value)
      if (this.chartLine) {
        this.chartLine.setOption({
          title: {
            text: '全院七日接诊人数',
            left: 'center',
            top: 8,
            textStyle: { fontSize: 15, color: '#243447', fontWeight: 600 }
          },
          tooltip: { trigger: 'axis' },
          grid: { left: 48, right: 24, top: 52, bottom: 28 },
          xAxis: { type: 'category', data: dates, boundaryGap: false, axisLabel: { color: '#5b6b7b' } },
          yAxis: { type: 'value', minInterval: 1, axisLabel: { color: '#5b6b7b' }, splitLine: { lineStyle: { color: '#e8edf5' } } },
          series: [
            {
              type: 'line',
              smooth: true,
              data: counts,
              symbolSize: 6,
              lineStyle: { width: 3, color: '#3a7afe' },
              itemStyle: { color: '#3a7afe' },
              areaStyle: { color: 'rgba(58, 122, 254, 0.12)' }
            }
          ]
        })
      }

      const pieData = (this.feeByCategory || []).map(s => ({
        name: s.name,
        value: Number(s.amount || 0)
      }))
      if (this.chartPie) {
        this.chartPie.setOption({
          title: {
            text: '七日分类费用统计',
            left: 'center',
            top: 8,
            textStyle: { fontSize: 15, color: '#243447', fontWeight: 600 }
          },
          tooltip: { trigger: 'item', formatter: '{b}<br/>{c} 元 ({d}%)' },
          legend: { bottom: 4, type: 'scroll' },
          color: ['#3a7afe', '#21ba45', '#ff9f1a', '#f56c6c', '#9b59b6', '#1abc9c'],
          series: [
            {
              type: 'pie',
              radius: ['38%', '62%'],
              center: ['50%', '52%'],
              data: pieData.length ? pieData : [{ name: '暂无数据', value: 1 }],
              label: { formatter: '{b}\n{c} 元' },
              itemStyle: pieData.length
                ? {}
                : {
                    color: '#e8edf5'
                  }
            }
          ]
        })
      }

      const cats = (this.deptFeeBar && this.deptFeeBar.categories) || []
      const rawSeries = (this.deptFeeBar && this.deptFeeBar.series) || []
      const barSeries = rawSeries.map(s => ({
        name: s.name,
        type: 'bar',
        barMaxWidth: 22,
        emphasis: { focus: 'series' },
        data: (s.data || []).map(v => Number(v || 0))
      }))
      if (this.chartBar) {
        this.chartBar.setOption({
          title: {
            text: '科室分类费用统计',
            left: 'center',
            top: 8,
            textStyle: { fontSize: 15, color: '#243447', fontWeight: 600 }
          },
          tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
          legend: { type: 'scroll', bottom: 4 },
          grid: { left: 48, right: 16, top: 52, bottom: cats.length ? 72 : 48 },
          xAxis: {
            type: 'category',
            data: cats.length ? cats : ['暂无'],
            axisLabel: { color: '#5b6b7b', interval: 0, rotate: cats.length > 5 ? 24 : 0 }
          },
          yAxis: {
            type: 'value',
            axisLabel: { color: '#5b6b7b' },
            splitLine: { lineStyle: { color: '#e8edf5' } }
          },
          color: ['#3a7afe', '#21ba45', '#ff9f1a', '#f56c6c', '#9b59b6'],
          series:
            barSeries.length > 0
              ? barSeries
              : [{ name: '金额', type: 'bar', data: [0], itemStyle: { color: '#e8edf5' } }]
        })
      }
    }
  }
}
</script>

<style scoped>
.dashboard-page {
  padding: 0 0 24px;
}

.dashboard-toolbar {
  margin-bottom: 16px;
}

.quick-link {
  font-size: 14px;
  font-weight: 600;
  color: var(--brand, #3a7afe);
  text-decoration: none;
}

.quick-link:hover {
  text-decoration: underline;
}

.stat-row {
  margin-bottom: 8px;
}

.stat-card {
  display: flex;
  align-items: center;
  background: var(--bg-card, #fff);
  border: 1px solid var(--border-soft, #e8edf5);
  border-radius: 12px;
  box-shadow: var(--shadow-soft, 0 8px 24px rgba(32, 56, 85, 0.07));
  padding: 20px 18px;
  margin-bottom: 16px;
  min-height: 112px;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: #fff;
  flex-shrink: 0;
  margin-right: 16px;
}

.stat-icon--teal {
  background: linear-gradient(135deg, #1abc9c, #16a085);
}

.stat-icon--blue {
  background: linear-gradient(135deg, #3a7afe, #5b8cff);
}

.stat-icon--red {
  background: linear-gradient(135deg, #f56c6c, #e74c3c);
}

.stat-icon--green {
  background: linear-gradient(135deg, #21ba45, #27ae60);
}

.stat-body {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary, #5b6b7b);
  font-weight: 600;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-main, #243447);
  line-height: 1.2;
}

.stat-value--time {
  font-size: 22px;
  letter-spacing: 0.02em;
}

.stat-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #8a96a8;
}

.chart-row {
  margin-bottom: 8px;
}

.chart-card {
  border-radius: 12px !important;
  margin-bottom: 16px;
}

.chart-box {
  width: 100%;
}

.chart-box--line {
  height: 360px;
}

.chart-box--half {
  height: 340px;
}
</style>
