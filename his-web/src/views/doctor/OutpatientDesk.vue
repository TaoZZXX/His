<template>
  <div class="outpatient-desk">
    <div class="left-panel">
      <div class="panel-header">
        <el-input v-model="patientQuery" placeholder="患者名" size="small" clearable @clear="fetchPatients">
          <template v-slot:append>
            <el-button icon="el-icon-refresh" @click="fetchPatients" />
          </template>
        </el-input>
      </div>

      <el-tabs v-model="activeTab" type="border-card" class="patient-tabs" @tab-click="fetchPatients">
        <el-tab-pane label="本人" name="self"></el-tab-pane>
        <el-tab-pane label="科室" name="dept"></el-tab-pane>
      </el-tabs>

      <div class="patient-section">
        <div class="section-title">待诊患者</div>
        <div class="patient-list">
          <el-card
            v-for="p in waitingPatients"
            :key="p.id"
            class="patient-card"
            :body-style="{ padding: '8px' }"
            @click.native="selectPatient(p)"
            :class="{ selected: selectedPatient && selectedPatient.id === p.id }"
          >
            <div class="patient-row">
              <div class="patient-id">{{ p.medicalNo }}</div>
              <div class="patient-name">{{ p.name }}</div>
              <div class="patient-age">{{ p.age }}岁</div>
            </div>
          </el-card>
          <div v-if="waitingPatients.length === 0" class="empty">暂无数据</div>
        </div>
      </div>

      <div class="patient-section">
        <div class="section-title">诊中患者</div>
        <div class="patient-list">
          <el-card
            v-for="p in doingPatients"
            :key="p.id"
            class="patient-card doing"
            :body-style="{ padding: '8px' }"
            @click.native="selectPatient(p)"
            :class="{ selected: selectedPatient && selectedPatient.id === p.id }"
          >
            <div class="patient-row">
              <div class="patient-id">{{ p.medicalNo }}</div>
              <div class="patient-name">{{ p.name }}</div>
              <div class="patient-age">{{ p.age }}岁</div>
            </div>
          </el-card>
          <div v-if="doingPatients.length === 0" class="empty">暂无数据</div>
        </div>
      </div>
    </div>

    <div class="right-panel">
      <div class="top-info">
        <div class="info-left">
          <div class="field"><label>姓名:</label> <span>{{ selectedPatient ? selectedPatient.name : '—' }}</span></div>
          <div class="field"><label>就诊号:</label> <span>{{ selectedPatient ? selectedPatient.medicalNo : '—' }}</span></div>
          <div class="field"><label>性别:</label> <span>{{ selectedPatient ? selectedPatient.gender : '—' }}</span></div>
          <div class="field"><label>年龄:</label> <span>{{ selectedPatient ? selectedPatient.age : '—' }}</span></div>
        </div>
        <div class="info-right">
          <el-checkbox v-model="isFinished">诊毕</el-checkbox>
        </div>
      </div>

      <el-tabs v-model="activeWorkTab" class="work-tabs">
        <el-tab-pane label="病历首页" name="record"></el-tab-pane>
        <el-tab-pane label="检查申请" name="exam"></el-tab-pane>
        <el-tab-pane label="检验申请" name="lab"></el-tab-pane>
        <el-tab-pane label="门诊确诊" name="diag"></el-tab-pane>
        <el-tab-pane label="成药处方" name="prescription"></el-tab-pane>
        <el-tab-pane label="草药处方" name="herbal"></el-tab-pane>
        <el-tab-pane label="处置申请" name="proc"></el-tab-pane>
        <el-tab-pane label="患者账单" name="bill"></el-tab-pane>
      </el-tabs>

      <div class="work-area">
        <el-form :model="recordForm" label-width="110px" class="record-form">
          <el-form-item label="主诉">
            <el-input type="textarea" v-model="recordForm.complaint" :rows="2" />
          </el-form-item>

          <el-form-item label="现病史">
            <el-input type="textarea" v-model="recordForm.history" :rows="3" />
          </el-form-item>

          <el-form-item label="现病治疗情况">
            <el-input type="textarea" v-model="recordForm.treatment" :rows="2" />
          </el-form-item>

          <el-form-item label="既往史">
            <el-input type="textarea" v-model="recordForm.pastHistory" :rows="2" />
          </el-form-item>

          <el-form-item label="过敏史">
            <el-input type="textarea" v-model="recordForm.allergy" :rows="1" />
          </el-form-item>

          <el-form-item label="体格检查">
            <el-input type="textarea" v-model="recordForm.exam" :rows="2" />
          </el-form-item>

          <el-form-item label="发病时间">
            <el-date-picker v-model="recordForm.onsetTime" type="date" placeholder="选择日期" style="width: 220px" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="saveRecord">保存</el-button>
            <el-button @click="quickAction">快捷操作</el-button>
            <el-button type="danger" @click="finishVisit">结束就诊</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OutpatientDesk',
  data() {
    return {
      patientQuery: '',
      activeTab: 'self',
      activeWorkTab: 'record',
      waitingPatients: [],
      doingPatients: [],
      selectedPatient: null,
      isFinished: false,
      recordForm: {
        complaint: '',
        history: '',
        treatment: '',
        pastHistory: '',
        allergy: '',
        exam: '',
        onsetTime: ''
      }
    }
  },
  mounted() {
    this.fetchPatients()
  },
  methods: {
    fetchPatients() {
      // 临时 mock 数据，后续可以替换为 API 调用
      this.waitingPatients = [
        { id: 'p1', medicalNo: '2019062918161312', name: '患者5号', age: 38, gender: '男' },
        { id: 'p2', medicalNo: '2019063020539999', name: '患者3号', age: 9, gender: '女' }
      ]
      this.doingPatients = [
        { id: 'p3', medicalNo: '201906300001', name: '正在就诊1', age: 45, gender: '男' }
      ]
    },
    selectPatient(p) {
      this.selectedPatient = p
      // 模拟加载患者既往病历填充表单
      this.recordForm = {
        complaint: '咳嗽两天',
        history: '近期感冒后出现咳嗽',
        treatment: '口服止咳药',
        pastHistory: '高血压',
        allergy: '青霉素过敏',
        exam: '双肺啰音',
        onsetTime: ''
      }
    },
    saveRecord() {
      if (!this.selectedPatient) {
        this.$message.warning('请选择患者')
        return
      }
      // 发送保存请求到后端（需要实现 API）
      console.log('保存病历', this.selectedPatient, this.recordForm)
      this.$message.success('保存成功')
    },
    quickAction() {
      this.$message.info('快捷操作（示例）')
    },
    finishVisit() {
      if (!this.selectedPatient) {
        this.$message.warning('请选择患者')
        return
      }
      this.$confirm('确认结束本次就诊吗？', '提示', { type: 'warning' })
        .then(() => {
          this.$message.success('就诊已结束')
          // 可从 doingPatients 移除，或设置状态
          this.selectedPatient = null
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
.outpatient-desk {
  display: flex;
  height: calc(100vh - 100px);
}
.left-panel {
  width: 320px;
  border-right: 1px solid #e6e6e6;
  padding: 12px;
  background: #fff;
  box-sizing: border-box;
}
.panel-header {
  margin-bottom: 8px;
}
.patient-tabs {
  margin-bottom: 12px;
}
.patient-section {
  margin-bottom: 12px;
}
.section-title {
  font-size: 13px;
  color: #409EFF;
  padding: 6px 2px;
}
.patient-list {
  max-height: 200px;
}
.patient-card {
  cursor: pointer;
  margin-bottom: 8px;
}
.patient-card.doing {
  background: #f6ffed;
}
.patient-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.patient-id { color: #999; width: 140px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.patient-name { flex: 1; font-weight: 600 }
.patient-age { width: 50px; text-align: right }
.patient-card.selected { border: 1px solid #409EFF }
.empty { padding: 12px; color: #999 }

.right-panel {
  flex: 1;
  padding: 12px 18px;
  overflow: auto;
  background: #f9fafc;
}
.top-info { display:flex; justify-content: space-between; align-items: center; margin-bottom: 12px }
.info-left { display:flex; gap: 16px }
.info-left .field { display:flex; gap:6px; align-items:center }
.info-left label { color: #999 }
.work-tabs { margin-bottom: 12px }
.work-area { background: #fff; padding: 16px; border-radius: 4px }
.record-form .el-form-item { margin-bottom: 12px }
</style>
