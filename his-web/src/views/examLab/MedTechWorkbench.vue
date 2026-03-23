<template>
  <div class="exam-lab-page med-tech-workbench">
    <div class="page-head">
      <h2 class="title">门诊医技工作台</h2>
      <div class="tools">
        <el-switch
          v-model="filterDept"
          active-text="按本科室过滤"
          inactive-text="全院"
          @change="searchNow"
        />
        <el-button type="primary" icon="el-icon-refresh" @click="searchNow">刷新</el-button>
      </div>
    </div>

    <div class="search-bar">
      <span class="search-label">门诊医技工作台</span>
      <el-input
        v-model="keyword"
        clearable
        placeholder="病历号 / 姓名 / 项目名称（自动筛选）"
        class="search-input"
        @keyup.enter.native="searchNow"
      />
      <el-button type="primary" icon="el-icon-search" @click="searchNow">搜索</el-button>
    </div>
    <p class="search-hint">默认展示最近检查检验单；输入关键字后自动查询，也可点搜索立即刷新。</p>

    <el-table v-loading="loading" :data="rows" stripe border>
      <el-table-column prop="medicalRecordNo" label="病历号" width="100" align="center" />
      <el-table-column prop="patientName" label="姓名" min-width="100" />
      <el-table-column label="年龄" width="80" align="center">
        <template slot-scope="{ row }">{{ formatAge(row.age) }}</template>
      </el-table-column>
      <el-table-column prop="genderStr" label="性别" width="72" align="center" />
      <el-table-column prop="projectName" label="开立项目" min-width="160" show-overflow-tooltip />
      <el-table-column prop="orderDoctorName" label="开立医生" min-width="100" show-overflow-tooltip>
        <template slot-scope="{ row }">{{ row.orderDoctorName || '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="{ row }">
          <el-tag :type="statusTagType(row)" size="small" effect="plain">{{ statusText(row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right" align="center">
        <template slot-scope="{ row }">
          <template v-if="!isRegistered(row)">
            <el-button type="primary" size="small" @click="onRegister(row, false)">登记</el-button>
            <el-button type="danger" size="small" plain @click="onRegister(row, true)">补录</el-button>
          </template>
          <el-button v-else type="primary" size="small" @click="openResultDialog(row)">上传结果</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      title="结果"
      :visible.sync="resultVisible"
      width="580px"
      append-to-body
      destroy-on-close
      @closed="resetResultForm"
    >
      <el-form label-width="96px" label-position="left">
        <el-form-item label="检查结果">
          <el-input
            v-model="resultForm.checkResult"
            type="textarea"
            :rows="6"
            placeholder="请输入检查/检验所见与结论"
          />
        </el-form-item>
        <el-form-item label="临床诊断">
          <el-input
            v-model="resultForm.clinicalDiagnosis"
            type="textarea"
            :rows="2"
            placeholder="确诊结论（选填）"
          />
        </el-form-item>
        <el-form-item label="结果图片">
          <div class="img-toolbar">
            <el-upload
              action="#"
              :show-file-list="false"
              accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
              :http-request="handleImageUpload"
            >
              <el-button size="small" type="primary" icon="el-icon-upload2">上传图片</el-button>
            </el-upload>
            <span class="img-tip">文件保存到服务器 HIS-TempFile，库中仅存路径（如 /sms/exam-result-files/xxx.jpg）</span>
          </div>
          <el-input
            v-model="resultForm.resultImgUrlList"
            type="textarea"
            :rows="2"
            placeholder="上传后自动追加；也可手工粘贴多个 URL，英文逗号分隔"
          />
          <div v-if="resultImgUrls.length" class="img-previews">
            <el-image
              v-for="(u, i) in resultImgUrls"
              :key="i"
              :src="resolveAssetUrl(u)"
              :preview-src-list="resultImgPreviewList"
              fit="cover"
              class="thumb"
            />
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="resultSaving" @click="submitResult">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getMedTechWorkbench, executeExamLabItem, saveExamLabResult, uploadExamResultImage } from '@/api/examLab'
import { getToken } from '@/utils/auth'

export default {
  name: 'MedTechWorkbench',
  data() {
    return {
      loading: false,
      rows: [],
      keyword: '',
      filterDept: false,
      resultVisible: false,
      resultSaving: false,
      resultRow: null,
      resultForm: {
        checkResult: '',
        clinicalDiagnosis: '',
        resultImgUrlList: ''
      },
      _keywordTimer: null
    }
  },
  created() {
    this.load()
  },
  watch: {
    keyword() {
      this.scheduleLoad()
    }
  },
  beforeDestroy() {
    if (this._keywordTimer) {
      clearTimeout(this._keywordTimer)
      this._keywordTimer = null
    }
  },
  computed: {
    resultImgUrls() {
      return (this.resultForm.resultImgUrlList || '')
        .split(',')
        .map(s => s.trim())
        .filter(Boolean)
    },
    resultImgPreviewList() {
      return this.resultImgUrls.map(u => this.resolveAssetUrl(u))
    }
  },
  methods: {
    resolveAssetUrl(path) {
      if (!path) return ''
      if (path.startsWith('http://') || path.startsWith('https://')) return path
      const base = process.env.VUE_APP_BASE_API || ''
      const tok = getToken() || ''
      const sep = path.includes('?') ? '&' : '?'
      return `${base}${path}${sep}token=${encodeURIComponent(tok)}`
    },
    handleImageUpload(opt) {
      const fd = new FormData()
      fd.append('file', opt.file)
      uploadExamResultImage(fd)
        .then(res => {
          if (res && res.code === 20000 && res.data && res.data.url) {
            const list = [...this.resultImgUrls]
            list.push(res.data.url)
            this.resultForm.resultImgUrlList = list.join(',')
            this.$message.success('上传成功')
            opt.onSuccess(res)
          } else {
            this.$message.error((res && res.message) || '上传失败')
            opt.onError(new Error('upload fail'))
          }
        })
        .catch(() => {
          this.$message.error('上传失败')
          opt.onError(new Error('upload fail'))
        })
    },
    formatAge(age) {
      if (age === null || age === undefined || age === '') return '—'
      return `${age}岁`
    },
    isRegistered(row) {
      return row.status === 1
    },
    hasResultText(row) {
      return row.checkResult && String(row.checkResult).trim().length > 0
    },
    statusText(row) {
      if (!this.isRegistered(row)) return '未登记'
      if (this.hasResultText(row)) return '已出结果'
      return '已登记'
    },
    statusTagType(row) {
      if (!this.isRegistered(row)) return 'info'
      if (this.hasResultText(row)) return 'success'
      return 'primary'
    },
    scheduleLoad() {
      if (this._keywordTimer) clearTimeout(this._keywordTimer)
      this._keywordTimer = setTimeout(() => {
        this._keywordTimer = null
        this.load()
      }, 400)
    },
    searchNow() {
      if (this._keywordTimer) {
        clearTimeout(this._keywordTimer)
        this._keywordTimer = null
      }
      this.load()
    },
    async load() {
      this.loading = true
      try {
        const res = await getMedTechWorkbench(this.keyword, this.filterDept ? 1 : 0)
        if (!res || res.code !== 20000) {
          this.$message.error((res && res.message) || '加载失败')
          this.rows = []
          return
        }
        this.rows = Array.isArray(res.data) ? res.data : []
      } catch (e) {
        this.$message.error('加载失败')
        this.rows = []
      } finally {
        this.loading = false
      }
    },
    onRegister(row, supplement) {
      const msg = supplement
        ? '补录将把本条登记为已执行，确认？'
        : '确认登记该检查检验项目？'
      this.$confirm(msg, '提示', { type: 'warning' })
        .then(async () => {
          const res = await executeExamLabItem(row.id)
          if (!res || res.code !== 20000) {
            this.$message.error((res && res.message) || '操作失败')
            return
          }
          this.$message.success((res && res.message) || '登记成功')
          this.load()
        })
        .catch(() => {})
    },
    openResultDialog(row) {
      this.resultRow = row
      this.resultForm.checkResult = row.checkResult || ''
      this.resultForm.clinicalDiagnosis = row.clinicalDiagnosis || ''
      this.resultForm.resultImgUrlList = row.resultImgUrlList || ''
      this.resultVisible = true
    },
    resetResultForm() {
      this.resultRow = null
      this.resultForm = { checkResult: '', clinicalDiagnosis: '', resultImgUrlList: '' }
    },
    async submitResult() {
      if (!this.resultRow) return
      const checkResult = (this.resultForm.checkResult || '').trim()
      if (!checkResult) {
        this.$message.warning('请填写检查结果')
        return
      }
      this.resultSaving = true
      try {
        const res = await saveExamLabResult(this.resultRow.id, {
          checkResult,
          clinicalDiagnosis: (this.resultForm.clinicalDiagnosis || '').trim() || null,
          resultImgUrlList: (this.resultForm.resultImgUrlList || '').trim() || null
        })
        if (!res || res.code !== 20000) {
          this.$message.error((res && res.message) || '保存失败')
          return
        }
        this.$message.success((res && res.message) || '已保存')
        this.resultVisible = false
        this.load()
      } catch (e) {
        this.$message.error('保存失败')
      } finally {
        this.resultSaving = false
      }
    }
  }
}
</script>

<style scoped>
.exam-lab-page {
  padding: 16px 20px 24px;
  background: #fff;
  min-height: 100%;
}
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.page-head .title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.tools {
  display: flex;
  align-items: center;
  gap: 12px;
}
.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 14px;
  background: #f5f7fa;
  border-radius: 4px;
}
.search-label {
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
}
.search-input {
  max-width: 320px;
}
.search-hint {
  font-size: 12px;
  color: #909399;
  margin: -8px 0 16px 0;
  line-height: 1.4;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.img-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 8px;
}
.img-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}
.img-previews {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}
.thumb {
  width: 72px;
  height: 72px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}
</style>
