<template>
  <div class="outpatient-desk">
    <div class="left-panel">
      <div class="left-title">门诊队列</div>
      <el-alert
        v-if="activeTab === 'dept'"
        title="科室视图：列表为本科室所有未结束就诊的挂号。在此 Tab 下「开始诊疗/诊毕」按科室权限处理，可操作非本人排班的挂号；切换到「本人」则仅本人排班。"
        type="info"
        :closable="false"
        show-icon
        class="scope-tip"
      />
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
        <div class="section-title">
          <span>待诊患者</span>
          <el-tag size="mini" type="warning">{{ waitingPatients.length }}</el-tag>
        </div>
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
              <div class="patient-age">{{ p.age !== null && p.age !== undefined ? `${p.age}岁` : '—' }}</div>
            </div>
          </el-card>
          <div v-if="!loading && waitingPatients.length === 0" class="empty">暂无数据</div>
        </div>
      </div>

      <div class="patient-section">
        <div class="section-title">
          <span>诊中患者</span>
          <el-tag size="mini" type="success">{{ doingPatients.length }}</el-tag>
        </div>
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
              <div class="patient-age">{{ p.age !== null && p.age !== undefined ? `${p.age}岁` : '—' }}</div>
            </div>
          </el-card>
          <div v-if="!loading && doingPatients.length === 0" class="empty">暂无数据</div>
        </div>
      </div>
    </div>

    <div class="right-panel">
      <div class="page-title">门诊医生工作台</div>
      <div class="top-info card">
        <div class="info-left">
          <div class="field"><label>当前病人:</label> <span>{{ selectedPatient ? selectedPatient.name : '—' }}</span></div>
          <div class="field"><label>就诊号:</label> <span>{{ selectedPatient ? selectedPatient.medicalNo : '—' }}</span></div>
          <div class="field"><label>性别:</label> <span>{{ selectedPatient ? (selectedPatient.gender || '—') : '—' }}</span></div>
          <div class="field"><label>午别:</label> <span>{{ selectedPatient ? noonToText(selectedPatient.noon) : '—' }}</span></div>
          <div class="field"><label>年龄:</label> <span>{{ selectedPatient ? selectedPatient.age : '—' }}岁</span></div>
        </div>
        <div class="info-right">
          <el-tag v-if="selectedPatient && selectedPatient.status === 0" type="warning" size="small">待诊</el-tag>
          <el-tag v-else-if="selectedPatient" type="success" size="small">诊中</el-tag>
          <el-checkbox v-model="isFinished" :disabled="!selectedPatient" @change="onFinishedChange">诊毕</el-checkbox>
        </div>
      </div>

      <el-tabs v-model="activeWorkTab" class="work-tabs">
        <el-tab-pane label="病历首页" name="record" :disabled="!selectedPatient"></el-tab-pane>
        <el-tab-pane label="检查申请" name="exam" :disabled="!selectedPatient"></el-tab-pane>
        <el-tab-pane label="检验申请" name="lab" :disabled="!selectedPatient"></el-tab-pane>
        <el-tab-pane label="门诊确诊" name="diag" :disabled="!selectedPatient"></el-tab-pane>
        <el-tab-pane label="成药处方" name="prescription" :disabled="!selectedPatient"></el-tab-pane>
        <el-tab-pane label="草药处方" name="herbal" :disabled="!selectedPatient"></el-tab-pane>
        <el-tab-pane label="处置申请" name="proc" :disabled="!selectedPatient"></el-tab-pane>
        <el-tab-pane label="患者账单" name="bill" :disabled="!selectedPatient"></el-tab-pane>
      </el-tabs>

      <div class="work-area">
        <div v-if="!selectedPatient" class="empty">请选择左侧患者后开始诊疗</div>
        <div v-else>
          <div v-if="nonDrugResultRows.length" class="card exam-results-ref">
            <div class="exam-results-title-row">
              <div class="card-title">检查检验结果（医技回传）</div>
              <el-button type="text" size="small" @click="loadContext">刷新</el-button>
            </div>
            <p class="exam-results-tip">开药前可参考以下结果；与「成药处方」无自动关联，药品需医生自行开立。医技刚回传时请点「刷新」。</p>
            <div v-for="row in nonDrugResultRows" :key="row.id" class="exam-result-block">
              <div class="exam-result-head">
                <el-tag size="mini" type="primary">{{ row._kind }}</el-tag>
                <span class="proj-name">{{ nonDrugProjectName(row) }}</span>
                <el-tag size="mini" effect="plain">{{ nonDrugStatusLabel(row) }}</el-tag>
              </div>
              <div v-if="row.checkResult" class="exam-result-line">
                <span class="rk">检查结果：</span>{{ row.checkResult }}
              </div>
              <div v-else-if="row.status === 1" class="exam-result-line muted">尚无文字结果</div>
              <div v-if="row.clinicalDiagnosis" class="exam-result-line">
                <span class="rk">临床诊断：</span>{{ row.clinicalDiagnosis }}
              </div>
              <div v-if="row.clinicalImpression" class="exam-result-line">
                <span class="rk">临床印象：</span>{{ row.clinicalImpression }}
              </div>
              <div v-if="resultImgUrls(row).length" class="exam-result-imgs">
                <el-image
                  v-for="(u, i) in resultImgUrls(row)"
                  :key="i"
                  :src="resolveResultAssetUrl(u)"
                  :preview-src-list="resultImgUrls(row).map(resolveResultAssetUrl)"
                  fit="cover"
                  class="rimg"
                />
              </div>
            </div>
          </div>

          <div v-if="activeWorkTab === 'record'">
          <el-row :gutter="18">
            <el-col :span="14">
              <div class="card">
                <div class="card-title">主诊信息</div>
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
                    <el-date-picker
                      v-model="recordForm.onsetTime"
                      type="date"
                      placeholder="选择日期"
                      style="width: 220px"
                    />
                  </el-form-item>
                </el-form>

                <div class="btn-row">
                  <el-button type="primary" @click="saveRecord">保存病历</el-button>
                  <el-button @click="openCaseModelDialog">选择病历模板</el-button>
                  <el-button @click="quickAction">快捷操作</el-button>
                  <el-button
                    type="success"
                    :disabled="!selectedPatient || selectedPatient.status !== 0"
                    @click="startVisit"
                  >开始诊疗</el-button>
                  <el-button type="danger" @click="finishVisit">结束就诊</el-button>
                </div>
              </div>
            </el-col>

            <el-col :span="10">
              <div class="card">
                <div class="card-title">诊疗概览</div>
                <div class="summary-line"><span class="k">主诉:</span><span class="v">{{ recordForm.complaint || '—' }}</span></div>
                <div class="summary-line"><span class="k">现病史:</span><span class="v">{{ recordForm.history || '—' }}</span></div>
                <div class="summary-line"><span class="k">治疗情况:</span><span class="v">{{ recordForm.treatment || '—' }}</span></div>
                <div class="summary-line"><span class="k">既往史:</span><span class="v">{{ recordForm.pastHistory || '—' }}</span></div>
                <div class="summary-line"><span class="k">过敏史:</span><span class="v">{{ recordForm.allergy || '—' }}</span></div>
                <div class="summary-line"><span class="k">体格检查:</span><span class="v">{{ recordForm.exam || '—' }}</span></div>
                <div class="divider" />
                <el-button type="text" class="link-btn" @click="activeWorkTab='exam'">填写检查申请</el-button>
                <el-button type="text" class="link-btn" @click="activeWorkTab='lab'">填写检验申请</el-button>
                <el-button type="text" class="link-btn" @click="activeWorkTab='prescription'">开成药处方</el-button>
              </div>
            </el-col>
          </el-row>
        </div>

        <div v-else-if="activeWorkTab === 'exam'">
          <div class="card">
            <div class="card-title">检查申请</div>
            <p class="flow-tip">流程说明：开立检查后，请患者至<strong>门诊挂号工作台</strong>先完成缴费，医技执行完毕后可继续在本工作台查看结果、回诊处理（无需先诊毕再缴费）。</p>
            <el-form :model="examForm" label-width="90px">
              <el-form-item label="检查项目">
                <el-select v-model="examForm.noDrugId" placeholder="请选择检查项目" filterable style="width: 100%">
                  <el-option v-for="it in examDict" :key="it.id" :label="`${it.name}（${formatMoney(it.price)}元）`" :value="it.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="备注">
                <el-input v-model="examForm.remark" placeholder="请输入备注" type="textarea" :rows="3" />
              </el-form-item>
            </el-form>
            <div class="btn-row">
              <el-button @click="openNonDrugModelDialog(1)">选择检查模板</el-button>
              <el-button type="primary" @click="saveExam">保存检查申请</el-button>
            </div>
          </div>
        </div>

        <div v-else-if="activeWorkTab === 'lab'">
          <div class="card">
            <div class="card-title">检验申请</div>
            <p class="flow-tip">流程说明：开立检验后，请患者至<strong>门诊挂号工作台</strong>先完成缴费，结果回传后可继续回诊（诊中即可缴费）。</p>
            <el-form :model="labForm" label-width="90px">
              <el-form-item label="检验项目">
                <el-select v-model="labForm.noDrugId" placeholder="请选择检验项目" filterable style="width: 100%">
                  <el-option v-for="it in labDict" :key="it.id" :label="`${it.name}（${formatMoney(it.price)}元）`" :value="it.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="备注">
                <el-input v-model="labForm.remark" placeholder="请输入备注" type="textarea" :rows="3" />
              </el-form-item>
            </el-form>
            <div class="btn-row">
              <el-button @click="openNonDrugModelDialog(3)">选择检验模板</el-button>
              <el-button type="primary" @click="saveLab">保存检验申请</el-button>
            </div>
          </div>
        </div>

        <div v-else-if="activeWorkTab === 'diag'">
          <div class="card">
            <div class="card-title">门诊确诊</div>
            <el-form :model="diagForm" label-width="90px">
              <el-form-item label="诊断">
                <el-input v-model="diagForm.diagnosis" placeholder="请输入诊断结果" />
              </el-form-item>
              <el-form-item label="依据">
                <el-input v-model="diagForm.basis" placeholder="请输入诊断依据" type="textarea" :rows="3" />
              </el-form-item>
            </el-form>
            <div class="btn-row">
              <el-button type="primary" @click="saveDiag">保存确诊</el-button>
            </div>
          </div>
        </div>

        <div v-else-if="activeWorkTab === 'prescription'">
          <div class="card">
            <div class="card-title">成药处方</div>
            <div class="presc-top">
              <el-button size="small" @click="openMedicineModelDialog">选择处方模板</el-button>
              <el-button type="primary" size="small" :disabled="!selectedPatient" @click="openPrescriptionDetail">处方详情</el-button>
              <span class="presc-total">处方总计: {{ formatMoney(prescriptionItemsTotal) }} 元</span>
            </div>
            <el-table :data="prescriptionItems" border style="width:100%" v-if="prescriptionItems.length">
              <el-table-column prop="name" label="药品名称" />
              <el-table-column prop="spec" label="规格" width="120" />
              <el-table-column prop="qty" label="数量" width="90" />
              <el-table-column prop="unitPrice" label="单价(元)" width="120" />
              <el-table-column label="金额(元)" width="130">
                <template slot-scope="{ row }">{{ formatMoney(row.qty * row.unitPrice) }}</template>
              </el-table-column>
              <el-table-column prop="usage" label="使用方法" width="130" />
              <el-table-column label="操作" width="90">
                <template slot-scope="{ $index }">
                  <el-button type="text" size="small" @click="removePrescriptionItem($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-else class="empty">暂无处方条目</div>
            <div class="btn-row">
              <el-button type="primary" :disabled="!selectedPatient" @click="savePrescription">保存处方</el-button>
            </div>
          </div>
        </div>

        <div v-else-if="activeWorkTab === 'herbal'">
          <div class="card">
            <div class="card-title">草药处方</div>
            <el-form :model="herbalForm" label-width="100px">
              <el-form-item label="治法">
                <el-input v-model="herbalForm.therapy" />
              </el-form-item>
              <el-form-item label="治法详情">
                <el-input v-model="herbalForm.therapyDetails" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item label="医嘱">
                <el-input v-model="herbalForm.medicalAdvice" type="textarea" :rows="2" />
              </el-form-item>
            </el-form>
            <div class="empty" v-if="!herbalItems.length">暂无草药明细（可先在后端补齐后再扩展前端编辑）</div>
            <el-table :data="herbalItems" border style="width:100%" v-else>
              <el-table-column prop="drugId" label="药品ID" width="120" />
              <el-table-column prop="totalNum" label="总数量" width="120" />
              <el-table-column prop="usageNum" label="单次用量" width="120" />
              <el-table-column prop="medicalAdvice" label="医嘱" />
            </el-table>
            <div class="btn-row">
              <el-button @click="openHerbalModelDialog">选择草药模板</el-button>
              <el-button type="primary" @click="saveHerbalPrescription">保存草药处方</el-button>
            </div>
          </div>
        </div>

        <div v-else-if="activeWorkTab === 'proc'">
          <div class="card">
            <div class="card-title">处置申请</div>
            <div class="empty">当前系统未实现处置申请</div>
          </div>
        </div>

        <div v-else-if="activeWorkTab === 'bill'">
          <div class="card">
            <div class="card-title">患者账单</div>
            <p class="bill-tip">以下为本次挂号已开单项目；成药/草药保存处方后即计入下列明细与汇总。</p>
            <el-table v-if="billLines.length" :data="billLines" border size="small" style="width:100%; margin-bottom:16px">
              <el-table-column prop="category" label="类别" width="88" />
              <el-table-column prop="itemName" label="项目名称" min-width="160" show-overflow-tooltip />
              <el-table-column prop="quantity" label="数量" width="72" align="right" />
              <el-table-column label="单价(元)" width="100" align="right">
                <template slot-scope="{ row }">{{ formatMoney(row.unitPrice) }}</template>
              </el-table-column>
              <el-table-column label="金额(元)" width="110" align="right">
                <template slot-scope="{ row }">{{ formatMoney(row.amount) }}</template>
              </el-table-column>
            </el-table>
            <div v-else class="empty muted" style="margin-bottom:12px">暂无账单明细（未开检查/检验或处方）</div>
            <el-descriptions border :column="1">
              <el-descriptions-item label="检查/检验金额">{{ formatMoney(billSummary.examAmount) }} 元</el-descriptions-item>
              <el-descriptions-item label="成药处方金额">{{ formatMoney(billSummary.medicineAmount) }} 元</el-descriptions-item>
              <el-descriptions-item label="草药处方金额">{{ formatMoney(billSummary.herbalAmount) }} 元</el-descriptions-item>
              <el-descriptions-item label="总金额"><strong>{{ formatMoney(billSummary.totalAmount) }} 元</strong></el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
      </div>
      </div>

      <el-dialog title="病历模板" :visible.sync="caseModelDialogVisible" width="700px">
        <el-table :data="caseModels" border size="small" height="420" @row-dblclick="confirmCaseModel">
          <el-table-column prop="id" label="ID" width="90" />
          <el-table-column prop="name" label="模板名称" min-width="180" />
          <el-table-column prop="chiefComplaint" label="主诉" min-width="180" show-overflow-tooltip />
          <el-table-column label="操作" width="90">
            <template slot-scope="{ row }">
              <el-button type="text" size="small" @click="confirmCaseModel(row)">套用</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>

      <el-dialog :title="nonDrugModelDialogTitle" :visible.sync="nonDrugModelDialogVisible" width="700px">
        <el-table :data="nonDrugModels" border size="small" height="420" @row-dblclick="confirmNonDrugModel">
          <el-table-column prop="id" label="ID" width="90" />
          <el-table-column prop="name" label="模板名称" min-width="180" />
          <el-table-column prop="type" label="类型" width="100" />
          <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
          <el-table-column label="操作" width="90">
            <template slot-scope="{ row }">
              <el-button type="text" size="small" @click="confirmNonDrugModel(row)">套用</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>

      <el-dialog title="成药处方模板" :visible.sync="medicineModelDialogVisible" width="760px">
        <el-table :data="medicineModels" border size="small" height="420" @row-dblclick="confirmMedicineModel">
          <el-table-column prop="id" label="ID" width="90" />
          <el-table-column prop="name" label="模板名称" min-width="220" />
          <el-table-column prop="scope" label="范围" width="90" />
          <el-table-column label="操作" width="90">
            <template slot-scope="{ row }">
              <el-button type="text" size="small" @click="confirmMedicineModel(row)">套用</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>

      <el-dialog title="草药处方模板" :visible.sync="herbalModelDialogVisible" width="760px">
        <el-table :data="herbalModels" border size="small" height="420" @row-dblclick="confirmHerbalModel">
          <el-table-column prop="id" label="ID" width="90" />
          <el-table-column prop="name" label="模板名称" min-width="220" />
          <el-table-column prop="scope" label="范围" width="90" />
          <el-table-column label="操作" width="90">
            <template slot-scope="{ row }">
              <el-button type="text" size="small" @click="confirmHerbalModel(row)">套用</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>

      <!-- 处方详情弹窗（按模板图 3） -->
      <el-dialog title="处方详情" :visible.sync="prescriptionDialogVisible" width="96%">
        <el-row :gutter="14">
          <el-col :span="7">
            <div class="dialog-left">
              <el-input
                v-model="prescriptionDetailSearch"
                size="small"
                placeholder="搜索药品/商品名"
                clearable
              />
              <div class="dialog-left-title">常用药品</div>
              <el-table
                :data="filteredMedicineList"
                size="mini"
                border
                height="520"
                style="margin-top:8px"
              >
                <el-table-column prop="name" label="商品名" />
                <el-table-column prop="price" label="价格(元)" width="100" />
                <el-table-column label="操作" width="80">
                  <template slot-scope="{ row }">
                    <el-button type="text" size="small" @click="addMedicineToDetail(row)">加入</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-col>

          <el-col :span="17">
            <div class="dialog-right">
              <div class="dialog-right-toolbar">
                <div class="muted">当前已选: {{ prescriptionDetailItems.length }} 项</div>
                <div class="muted">合计: {{ formatMoney(prescriptionDetailTotal) }} 元</div>
              </div>

              <el-table :data="prescriptionDetailItems" border style="width:100%" height="500">
                <el-table-column prop="name" label="药品名" />
                <el-table-column prop="spec" label="规格" width="120" />
                <el-table-column prop="qty" label="数量" width="90" />
                <el-table-column prop="unitPrice" label="单价(元)" width="120" />
                <el-table-column label="金额(元)" width="130">
                  <template slot-scope="{ row }">{{ formatMoney(row.qty * row.unitPrice) }}</template>
                </el-table-column>
                <el-table-column prop="usage" label="使用方式" width="140" />
                <el-table-column label="操作" width="90">
                  <template slot-scope="{ $index }">
                    <el-button type="text" size="small" @click="removeDetailItem($index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>

              <div class="dialog-pagination">
                <el-pagination layout="prev, pager, next" :total="187" :page-size="10" />
              </div>

              <span slot="footer" class="dialog-footer">
                <el-button @click="prescriptionDialogVisible = false">关闭</el-button>
                <el-button type="primary" @click="confirmPrescriptionDetail">确认开方</el-button>
              </span>
            </div>
          </el-col>
        </el-row>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import {
  finishDoctorDeskVisit,
  getDoctorDeskContext,
  getDoctorDeskMedicines,
  getDoctorDeskNonDrugDict,
  getDoctorDeskPatients,
  saveDoctorDeskCaseHistory,
  saveDoctorDeskDiagnosis,
  saveDoctorDeskHerbalPrescription,
  saveDoctorDeskMedicinePrescription,
  saveDoctorDeskNonDrugItem,
  startDoctorDeskVisit
} from '@/api/doctorDesk'
import { pageBackfillRows } from '@/api/moduleBackfill'
import { getToken } from '@/utils/auth'

export default {
  name: 'OutpatientDesk',
  data() {
    return {
      loading: false,
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
      },
      examForm: {
        noDrugId: null,
        remark: ''
      },
      labForm: {
        noDrugId: null,
        remark: ''
      },
      diagForm: {
        diagnosis: '',
        basis: ''
      },

      // 处方详情弹窗
      prescriptionDialogVisible: false,
      prescriptionDetailSearch: '',
      prescriptionItems: [],
      prescriptionDetailItems: [],
      medicines: [],
      examDict: [],
      labDict: [],
      herbalItems: [],
      herbalForm: {
        therapy: '',
        therapyDetails: '',
        medicalAdvice: '',
        pairNum: 1,
        frequency: 1,
        usageMeans: 1
      },
      billSummary: {
        examAmount: 0,
        medicineAmount: 0,
        herbalAmount: 0,
        totalAmount: 0
      },
      billLines: [],
      examItemList: [],
      labItemList: [],
      caseModelDialogVisible: false,
      caseModels: [],
      nonDrugModelDialogVisible: false,
      nonDrugModelMode: 1,
      nonDrugModels: [],
      medicineModelDialogVisible: false,
      medicineModels: [],
      medicineModelItems: [],
      herbalModelDialogVisible: false,
      herbalModels: [],
      herbalModelItems: []
      ,
      examTemplateIds: [],
      labTemplateIds: []
    }
  },
  mounted() {
    this.fetchPatients()
    this.loadDicts()
  },
  computed: {
    filteredMedicineList() {
      const kw = (this.prescriptionDetailSearch || '').trim()
      if (!kw) return this.medicines
      return (this.medicines || []).filter(m => (m.name || '').includes(kw))
    },
    prescriptionItemsTotal() {
      const items = this.prescriptionItems || []
      return items.reduce((sum, it) => sum + (Number(it.qty) || 0) * (Number(it.unitPrice) || 0), 0)
    },
    prescriptionDetailTotal() {
      const items = this.prescriptionDetailItems || []
      return items.reduce((sum, it) => sum + (Number(it.qty) || 0) * (Number(it.unitPrice) || 0), 0)
    },
    nonDrugModelDialogTitle() {
      return this.nonDrugModelMode === 1 ? '检查模板' : '检验模板'
    },
    nonDrugResultRows() {
      const rows = []
      const push = (list, kind) => {
        ;(list || []).forEach(r => {
          if (r && r.status !== 2) rows.push({ ...r, _kind: kind })
        })
      }
      push(this.examItemList, '检查')
      push(this.labItemList, '检验')
      return rows.sort((a, b) => (a.id || 0) - (b.id || 0))
    }
  },
  watch: {
    activeWorkTab(v) {
      if (v === 'bill' && this.selectedPatient) {
        this.loadContext()
      }
    }
  },
  methods: {
    toNum(v, d = 0) {
      const n = Number(v)
      return Number.isFinite(n) ? n : d
    },
    toText(v, d = '') {
      if (v === null || v === undefined) return d
      return String(v)
    },
    async loadTemplateTable(table, size = 200) {
      const res = await pageBackfillRows(table, 1, size)
      if (!res || res.code !== 20000) {
        throw new Error((res && res.message) || '加载模板失败')
      }
      return (res.data && res.data.records) || []
    },
    async openCaseModelDialog() {
      try {
        const all = await this.loadTemplateTable('dms_case_model')
        this.caseModels = all.filter(r => Number(r.status) === 1 || r.status === null || r.status === undefined)
        this.caseModelDialogVisible = true
      } catch (e) {
        this.$message.error(e.message || '加载病历模板失败')
      }
    },
    confirmCaseModel(row) {
      if (!row) return
      // strict mapping: dms_case_model columns
      this.recordForm.complaint = this.toText(row.chief_complaint, '')
      this.recordForm.history = this.toText(row.history_of_present_illness, '')
      this.recordForm.treatment = this.toText(row.history_of_treatment, '')
      this.recordForm.pastHistory = this.toText(row.past_history, '')
      this.recordForm.allergy = this.toText(row.allergies, '')
      this.recordForm.exam = this.toText(row.health_checkup, '')
      // 初步诊断直接带入确诊页文本（医生可再编辑）
      const prelim = this.toText(row.priliminary_dise_str_list, '').trim()
      if (prelim) {
        this.diagForm.diagnosis = prelim
      }
      this.caseModelDialogVisible = false
      this.$message.success('病历模板已套用')
    },
    async openNonDrugModelDialog(type) {
      this.nonDrugModelMode = type === 3 ? 3 : 1
      try {
        const all = await this.loadTemplateTable('dms_non_drug_model')
        this.nonDrugModels = all.filter(r => (Number(r.status) === 1 || r.status == null) && this.toNum(r.type, -1) === this.nonDrugModelMode)
        this.nonDrugModelDialogVisible = true
      } catch (e) {
        this.$message.error(e.message || '加载模板失败')
      }
    },
    confirmNonDrugModel(row) {
      if (!row) return
      // strict mapping: dms_non_drug_model.non_drug_id_list + aim
      const ids = this.toText(row.non_drug_id_list, '')
        .split(',')
        .map(s => this.toNum(s.trim(), 0))
        .filter(n => n > 0)
      if (!ids.length) {
        this.$message.warning('该模板未配置 non_drug_id_list')
        return
      }
      const noDrugId = ids[0]
      const remark = this.toText(row.aim, '')
      if (this.nonDrugModelMode === 1) {
        this.examTemplateIds = ids
        if (noDrugId > 0) this.examForm.noDrugId = noDrugId
        this.examForm.remark = remark
      } else {
        this.labTemplateIds = ids
        if (noDrugId > 0) this.labForm.noDrugId = noDrugId
        this.labForm.remark = remark
      }
      this.nonDrugModelDialogVisible = false
      this.$message.success(`模板已套用（共 ${ids.length} 项）`)
    },
    async openMedicineModelDialog() {
      try {
        const all = await this.loadTemplateTable('dms_drug_model')
        // dms_drug_model.type: 0 西药 / 1 草药
        this.medicineModels = all.filter(r => (Number(r.status) === 1 || r.status == null) && this.toNum(r.type, 0) === 0)
        this.medicineModelItems = (await this.loadTemplateTable('dms_medicine_model_item', 500))
          .filter(r => Number(r.status) === 1 || r.status == null)
        this.medicineModelDialogVisible = true
      } catch (e) {
        this.$message.error(e.message || '加载处方模板失败')
      }
    },
    confirmMedicineModel(row) {
      if (!row) return
      const modelId = this.toNum(row.id, 0)
      const lines = (this.medicineModelItems || []).filter(it => {
        return this.toNum(it.model_id, -1) === modelId
      })
      if (!lines.length) {
        this.$message.warning('该模板未配置明细')
        return
      }
      this.prescriptionItems = lines.map(it => {
        const drugId = this.toNum(it.drug_id, 0)
        const dict = (this.medicines || []).find(m => m.id === drugId) || {}
        const qty = this.toNum(it.num, 1)
        const unitPrice = this.toNum(dict.price, 0)
        return {
          drugId,
          name: dict.name || `药品#${drugId}`,
          spec: dict.format || '',
          qty,
          unitPrice,
          usage: this.toText(it.medicine_usage, ''),
          days: this.toNum(it.days, 1),
          usageNum: this.toNum(it.usage_num, 1),
          usageNumUnit: this.toNum(it.usage_num_unit, 1),
          usageMeans: this.toNum(it.usage_means, 1),
          frequency: this.toNum(it.frequency, 1),
          medicalAdvice: this.toText(it.medical_advice, '')
        }
      })
      this.medicineModelDialogVisible = false
      this.$message.success('成药模板已套用')
    },
    async openHerbalModelDialog() {
      try {
        const all = await this.loadTemplateTable('dms_drug_model')
        this.herbalModels = all.filter(r => (Number(r.status) === 1 || r.status == null) && this.toNum(r.type, 0) === 1)
        this.herbalModelItems = (await this.loadTemplateTable('dms_herbal_model_item', 500))
          .filter(r => Number(r.status) === 1 || r.status == null)
        this.herbalModelDialogVisible = true
      } catch (e) {
        this.$message.error(e.message || '加载草药模板失败')
      }
    },
    confirmHerbalModel(row) {
      if (!row) return
      const modelId = this.toNum(row.id, 0)
      const lines = (this.herbalModelItems || []).filter(it => {
        return this.toNum(it.model_id, -1) === modelId
      })
      if (!lines.length) {
        this.$message.warning('该模板未配置草药明细')
        return
      }
      const pairNum = this.toNum(row.pair_num, 1)
      this.herbalItems = lines.map(it => ({
        drugId: this.toNum(it.drug_id, 0),
        totalNum: this.toNum(it.usage_num, 1) * pairNum,
        usageNum: this.toNum(it.usage_num, 1),
        usageNumUnit: this.toNum(it.usage_num_unit, 1),
        medicalAdvice: this.toText(row.medical_advice, ''),
        footnote: this.toText(it.footnote, '')
      }))
      this.herbalForm.therapy = this.toText(row.therapy, '')
      this.herbalForm.therapyDetails = this.toText(row.therapy_details, '')
      this.herbalForm.medicalAdvice = this.toText(row.medical_advice, '')
      this.herbalForm.pairNum = pairNum
      this.herbalForm.frequency = this.toNum(row.frequency, 1)
      this.herbalModelDialogVisible = false
      this.$message.success('草药模板已套用')
    },
    nonDrugProjectName(row) {
      const dict = row.type === 3 ? this.labDict : this.examDict
      const hit = (dict || []).find(x => x.id === row.noDrugId)
      return hit ? hit.name : row.noDrugId ? `项目#${row.noDrugId}` : '—'
    },
    nonDrugStatusLabel(row) {
      if (row.status === 2) return '已作废'
      if (row.status === 1) return '已登记/已执行'
      return '待医技登记'
    },
    resultImgUrls(row) {
      const s = row && row.resultImgUrlList
      if (!s || typeof s !== 'string') return []
      return s.split(',').map(x => x.trim()).filter(Boolean)
    },
    resolveResultAssetUrl(path) {
      if (!path) return ''
      if (path.startsWith('http://') || path.startsWith('https://')) return path
      const base = process.env.VUE_APP_BASE_API || ''
      const tok = getToken() || ''
      const sep = path.includes('?') ? '&' : '?'
      return `${base}${path}${sep}token=${encodeURIComponent(tok)}`
    },
    async loadDicts() {
      try {
        const [medRes, examRes, labRes] = await Promise.all([
          getDoctorDeskMedicines(),
          getDoctorDeskNonDrugDict(1),
          getDoctorDeskNonDrugDict(3)
        ])
        this.medicines = (medRes && medRes.data) || []
        this.examDict = (examRes && examRes.data) || []
        this.labDict = (labRes && labRes.data) || []
      } catch (e) {
        console.error(e)
      }
    },
    async fetchPatients() {
      this.loading = true
      this.selectedPatient = null
      try {
        const scope = this.activeTab === 'dept' ? 'dept' : 'self'
        const res = await getDoctorDeskPatients({
          scope,
          keyword: this.patientQuery
        })
        if (!res || res.code !== 20000) {
          this.$message.error((res && res.message) || '获取患者列表失败')
          return
        }
        const data = res.data || {}
        this.waitingPatients = data.waitingPatients || []
        this.doingPatients = data.doingPatients || []
      } catch (e) {
        console.error(e)
        this.$message.error('获取患者列表失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    async selectPatient(p) {
      this.selectedPatient = p
      this.isFinished = p && p.endAttendance === 1
      // 切换患者时清空各类表单/处方（病历保存接口当前未实现）
      this.prescriptionItems = []
      this.prescriptionDetailItems = []
      this.recordForm = { complaint: '', history: '', treatment: '', pastHistory: '', allergy: '', exam: '', onsetTime: '' }
      this.examForm = { noDrugId: null, remark: '' }
      this.labForm = { noDrugId: null, remark: '' }
      this.examTemplateIds = []
      this.labTemplateIds = []
      this.diagForm = { diagnosis: '', basis: '' }
      this.herbalItems = []
      this.herbalForm = { therapy: '', therapyDetails: '', medicalAdvice: '', pairNum: 1, frequency: 1, usageMeans: 1 }
      this.billSummary = { examAmount: 0, medicineAmount: 0, herbalAmount: 0, totalAmount: 0 }
      this.billLines = []
      this.examItemList = []
      this.labItemList = []
      await this.loadContext()
    },
    async loadContext() {
      if (!this.selectedPatient) return
      try {
        const res = await getDoctorDeskContext(this.selectedPatient.id)
        if (!res || res.code !== 20000) return
        const d = res.data || {}
        const ch = d.caseHistory || {}
        this.recordForm = {
          complaint: ch.chiefComplaint || '',
          history: ch.historyOfPresentIllness || '',
          treatment: ch.historyOfTreatment || '',
          pastHistory: ch.pastHistory || '',
          allergy: ch.allergies || '',
          exam: ch.healthCheckup || '',
          onsetTime: ch.startDate || ''
        }
        this.diagForm = {
          diagnosis: d.diagnosis || '',
          basis: d.diagnosisBasis || ''
        }
        this.examItemList = Array.isArray(d.examItems) ? d.examItems : []
        this.labItemList = Array.isArray(d.labItems) ? d.labItems : []
        const exam = this.examItemList.length ? this.examItemList[0] : {}
        const lab = this.labItemList.length ? this.labItemList[0] : {}
        this.examForm = { noDrugId: exam.noDrugId || null, remark: exam.demand || '' }
        this.labForm = { noDrugId: lab.noDrugId || null, remark: lab.demand || '' }
        const medPres = Array.isArray(d.medicinePrescriptions) ? d.medicinePrescriptions : []
        this.prescriptionItems = medPres.length ? (medPres[0].items || []) : []
        const herbPres = Array.isArray(d.herbalPrescriptions) ? d.herbalPrescriptions : []
        this.herbalItems = herbPres.length ? (herbPres[0].items || []) : []
        if (herbPres.length) {
          this.herbalForm.therapy = herbPres[0].therapy || ''
          this.herbalForm.medicalAdvice = herbPres[0].medicalAdvice || ''
        }
        this.billSummary = this.normalizeBillSummary(d.billSummary)
        this.billLines = Array.isArray(d.billLines) ? d.billLines : []
      } catch (e) {
        console.error(e)
      }
    },
    normalizeBillSummary(bs) {
      const n = (v) => {
        const x = Number(v)
        return Number.isFinite(x) ? x : 0
      }
      if (!bs || typeof bs !== 'object') {
        return { examAmount: 0, medicineAmount: 0, herbalAmount: 0, totalAmount: 0 }
      }
      const exam = n(bs.examAmount)
      const med = n(bs.medicineAmount)
      const herb = n(bs.herbalAmount)
      let total = n(bs.totalAmount)
      if (!total && (exam || med || herb)) {
        total = exam + med + herb
      }
      return { examAmount: exam, medicineAmount: med, herbalAmount: herb, totalAmount: total }
    },
    async saveRecord() {
      if (!this.selectedPatient) {
        this.$message.warning('请选择患者')
        return
      }
      const payload = {
        chiefComplaint: this.recordForm.complaint,
        historyOfPresentIllness: this.recordForm.history,
        historyOfTreatment: this.recordForm.treatment,
        pastHistory: this.recordForm.pastHistory,
        allergies: this.recordForm.allergy,
        healthCheckup: this.recordForm.exam,
        startDate: this.recordForm.onsetTime
      }
      const res = await saveDoctorDeskCaseHistory(this.selectedPatient.id, payload)
      if (res && res.code === 20000) {
        this.$message.success('保存病历成功')
        await this.loadContext()
      } else {
        this.$message.error((res && res.message) || '保存失败')
      }
    },
    quickAction() {
      this.$message.info('快捷操作（示例）')
    },
    noonToText(noon) {
      if (noon === 0) return '上午'
      if (noon === 1) return '下午'
      return '—'
    },
    async saveExam() {
      if (!this.selectedPatient) return
      if (!this.examForm.noDrugId) {
        this.$message.warning('请选择检查项目')
        return
      }
      const ids = (this.examTemplateIds && this.examTemplateIds.length) ? this.examTemplateIds : [this.examForm.noDrugId]
      for (let i = 0; i < ids.length; i += 1) {
        const res = await saveDoctorDeskNonDrugItem(this.selectedPatient.id, {
          type: 1,
          noDrugId: ids[i],
          remark: this.examForm.remark
        })
        if (!res || res.code !== 20000) {
          this.$message.error((res && res.message) || '保存失败')
          return
        }
      }
      this.$message.success(`保存检查申请成功（${ids.length} 项）`)
      await this.loadContext()
    },
    async saveLab() {
      if (!this.selectedPatient) return
      if (!this.labForm.noDrugId) {
        this.$message.warning('请选择检验项目')
        return
      }
      const ids = (this.labTemplateIds && this.labTemplateIds.length) ? this.labTemplateIds : [this.labForm.noDrugId]
      for (let i = 0; i < ids.length; i += 1) {
        const res = await saveDoctorDeskNonDrugItem(this.selectedPatient.id, {
          type: 3,
          noDrugId: ids[i],
          remark: this.labForm.remark
        })
        if (!res || res.code !== 20000) {
          this.$message.error((res && res.message) || '保存失败')
          return
        }
      }
      this.$message.success(`保存检验申请成功（${ids.length} 项）`)
      await this.loadContext()
    },
    async saveDiag() {
      if (!this.selectedPatient) return
      const res = await saveDoctorDeskDiagnosis(this.selectedPatient.id, {
        diagnosis: this.diagForm.diagnosis,
        basis: this.diagForm.basis
      })
      if (res && res.code === 20000) {
        this.$message.success('保存确诊成功')
        await this.loadContext()
      } else {
        this.$message.error((res && res.message) || '保存失败')
      }
    },
    async savePrescription() {
      if (!this.selectedPatient) return
      if (!this.prescriptionItems.length) {
        this.$message.warning('请先添加处方明细')
        return
      }
      const items = this.prescriptionItems.map(it => ({
        drugId: it.drugId,
        qty: it.qty || 1,
        usageMeans: it.usageMeans || 1,
        frequency: it.frequency || 1,
        medicalAdvice: it.medicalAdvice || ''
      }))
      const res = await saveDoctorDeskMedicinePrescription(this.selectedPatient.id, {
        name: this.selectedPatient.name,
        items
      })
      if (res && res.code === 20000) {
        this.$message.success('保存处方成功')
        await this.loadContext()
      } else {
        this.$message.error((res && res.message) || '保存失败')
      }
    },
    openPrescriptionDetail() {
      this.prescriptionDetailSearch = ''
      this.prescriptionDetailItems = []
      this.prescriptionDialogVisible = true
    },
    addMedicineToDetail(med) {
      this.prescriptionDetailItems.push({
        drugId: med.id,
        name: med.name,
        spec: med.format,
        qty: 1,
        unitPrice: med.price,
        usage: med.unit || '',
        usageMeans: 1,
        frequency: 1
      })
    },
    removeDetailItem(index) {
      this.prescriptionDetailItems.splice(index, 1)
    },
    confirmPrescriptionDetail() {
      this.prescriptionItems = this.prescriptionItems.concat(this.prescriptionDetailItems)
      this.prescriptionDialogVisible = false
      this.$message.success('处方已加入')
    },
    removePrescriptionItem(index) {
      this.prescriptionItems.splice(index, 1)
    },
    async saveHerbalPrescription() {
      if (!this.selectedPatient) return
      if (!this.herbalItems.length) {
        this.$message.warning('请先添加草药明细')
        return
      }
      const items = this.herbalItems.map(it => ({
        drugId: it.drugId,
        totalNum: it.totalNum || 1,
        usageNum: it.usageNum || 1,
        usageNumUnit: it.usageNumUnit || 1,
        medicalAdvice: it.medicalAdvice || '',
        footnote: it.footnote || ''
      }))
      const res = await saveDoctorDeskHerbalPrescription(this.selectedPatient.id, {
        name: this.selectedPatient.name,
        therapy: this.herbalForm.therapy,
        therapyDetails: this.herbalForm.therapyDetails,
        medicalAdvice: this.herbalForm.medicalAdvice,
        pairNum: this.herbalForm.pairNum,
        frequency: this.herbalForm.frequency,
        usageMeans: this.herbalForm.usageMeans,
        items
      })
      if (res && res.code === 20000) {
        this.$message.success('保存草药处方成功')
        await this.loadContext()
      } else {
        this.$message.error((res && res.message) || '保存失败')
      }
    },
    formatMoney(v) {
      if (v === null || v === undefined || v === '') return '0.00'
      const n = typeof v === 'number' ? v : Number(String(v).replace(/,/g, ''))
      if (!Number.isFinite(n)) return '0.00'
      return n.toFixed(2)
    },
    deskScope() {
      return this.activeTab === 'dept' ? 'dept' : 'self'
    },
    async startVisit() {
      if (!this.selectedPatient) {
        this.$message.warning('请选择患者')
        return
      }
      try {
        const res = await startDoctorDeskVisit(this.selectedPatient.id, this.deskScope())
        // request 拦截器在业务失败时仍 resolve，必须看 code
        if (!res || res.code !== 20000) {
          this.$message.error((res && res.message) || '开始诊疗失败')
          return
        }
        this.$message.success('已开始诊疗')
        this.selectedPatient = null
        this.isFinished = false
        await this.fetchPatients()
      } catch (e) {
        console.error(e)
        this.$message.error('开始诊疗失败')
      }
    },
    async finishVisit() {
      if (!this.selectedPatient) {
        this.$message.warning('请选择患者')
        return
      }
      this.$confirm('确认结束本次就诊吗？', '提示', { type: 'warning' })
        .then(async () => {
          try {
            const res = await finishDoctorDeskVisit(this.selectedPatient.id, this.deskScope())
            if (!res || res.code !== 20000) {
              this.$message.error((res && res.message) || '结束就诊失败')
              this.isFinished = false
              return
            }
            this.$message.success('就诊已结束')
            this.selectedPatient = null
            this.isFinished = false
            await this.fetchPatients()
          } catch (e) {
            console.error(e)
            this.$message.error('结束就诊失败')
            this.isFinished = false
          }
        })
        .catch(() => {
          this.isFinished = false
        })
    },
    onFinishedChange(checked) {
      if (!checked) {
        this.isFinished = false
        return
      }
      // 勾选“诊毕”即结束就诊
      this.finishVisit()
    }
  }
}
</script>

<style scoped>
.outpatient-desk {
  display: flex;
  height: calc(100vh - 96px);
  gap: 12px;
  padding: 12px;
  background: #f5f7fb;
}
.left-panel {
  width: 336px;
  padding: 12px;
  background: linear-gradient(180deg, #ffffff 0%, #fafcff 100%);
  box-sizing: border-box;
  border-radius: 12px;
  border: 1px solid #e9edf5;
  box-shadow: 0 6px 20px rgba(32, 56, 85, 0.05);
}
.left-title {
  font-size: 16px;
  font-weight: 600;
  color: #2f3a4d;
  margin-bottom: 10px;
}

.scope-tip {
  margin-bottom: 10px;
  font-size: 12px;
  line-height: 1.45;
}
.panel-header {
  margin-bottom: 10px;
}
.patient-tabs {
  margin-bottom: 12px;
}
.patient-section {
  margin-bottom: 14px;
}
.section-title {
  font-size: 13px;
  color: #8b5cf6;
  padding: 6px 2px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.patient-list {
  max-height: 230px;
  overflow: auto;
  padding-right: 4px;
}
.patient-card {
  cursor: pointer;
  margin-bottom: 8px;
  border-radius: 8px;
  border: 1px solid #edf2fa;
  transition: all .2s ease;
}
.patient-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(64, 90, 140, 0.12);
}
.patient-card.doing {
  background: #f6ffed;
}
.patient-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.patient-id { color: #8b95a8; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 12px; }
.patient-name { flex: 1; font-weight: 600; color: #2f3a4d; }
.patient-age { width: 60px; text-align: right; color: #7f8aa1; font-size: 12px; }
.patient-card.selected {
  border: 1px solid #b79bf8;
  box-shadow: 0 0 0 2px rgba(183, 155, 248, .22);
}
.empty {
  padding: 24px 12px;
  color: #99a2b3;
  text-align: center;
  background: #fbfcfe;
  border: 1px dashed #e6ebf2;
  border-radius: 8px;
}

.right-panel {
  flex: 1;
  padding: 0;
  overflow: auto;
  background: transparent;
}
.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #27364a;
  margin-bottom: 12px;
}
.top-info { display:flex; justify-content: space-between; align-items: center; margin-bottom: 12px; border-radius: 12px; }
.info-left { display:flex; gap: 16px; flex-wrap: wrap; }
.info-left .field { display:flex; gap:6px; align-items:center }
.info-left label { color: #8c95a6 }
.info-left span { color: #2f3a4d; font-weight: 500; }
.info-right { display: flex; align-items: center; gap: 10px; }
.work-tabs {
  margin-bottom: 12px;
  background: #fff;
  border: 1px solid #e9edf5;
  border-radius: 12px;
  padding: 0 12px;
}
.work-area {
  background: #fff;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid #e9edf5;
  box-shadow: 0 6px 20px rgba(32, 56, 85, 0.04);
  min-height: 420px;
}
.record-form .el-form-item { margin-bottom: 12px }

.card {
  border: 1px solid #ebeff6;
  border-radius: 10px;
  background: #fff;
  padding: 14px;
}
.card-title {
  font-weight: 600;
  margin-bottom: 12px;
  color: #2f3a4d;
  font-size: 15px;
}
.btn-row {
  display:flex;
  gap: 12px;
  margin-top: 14px;
  justify-content: flex-end;
  flex-wrap: wrap;
}
.summary-line {
  margin: 8px 0;
  line-height: 1.6;
}
.summary-line .k {
  color: #999;
  display: inline-block;
  width: 90px;
}
.summary-line .v {
  color: #333;
}
.divider {
  height: 1px;
  background: #edf1f7;
  margin: 14px 0;
}
.link-btn {
  padding: 0;
  margin: 6px 0;
  color: #8b5cf6;
}
.presc-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.presc-total {
  color: #999;
}
.dialog-left {
  padding-right: 8px;
  background: #fafcff;
  border: 1px solid #eef2f8;
  border-radius: 8px;
  padding: 10px;
}
.dialog-left-title {
  margin-top: 12px;
  font-weight: 600;
  color: #333;
  font-size: 13px;
}
.dialog-right {
  padding-left: 4px;
  border: 1px solid #eef2f8;
  border-radius: 8px;
  padding: 8px;
}
.dialog-right-toolbar {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}
.bill-tip {
  font-size: 13px;
  color: #606266;
  margin: 0 0 12px;
  line-height: 1.5;
}
.muted {
  color: #999;
}
.dialog-pagination {
  padding: 10px 0;
}
.dialog-footer {
  text-align: right;
}

.flow-tip {
  font-size: 13px;
  color: #606266;
  line-height: 1.55;
  margin: 0 0 14px;
  padding: 10px 12px;
  background: #f4f0ff;
  border-radius: 6px;
  border: 1px solid #ddd6f3;
}
.exam-results-ref {
  margin-bottom: 14px;
  border-left: 3px solid #8b5cf6;
}
.exam-results-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.exam-results-title-row .card-title {
  margin-bottom: 0;
}
.exam-results-tip {
  font-size: 12px;
  color: #909399;
  margin: 0 0 12px;
  line-height: 1.5;
}
.exam-result-block {
  padding: 10px 0;
  border-top: 1px dashed #ebeef5;
}
.exam-result-block:first-of-type {
  border-top: none;
  padding-top: 0;
}
.exam-result-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}
.exam-result-head .proj-name {
  font-weight: 600;
  color: #303133;
}
.exam-result-line {
  font-size: 13px;
  color: #606266;
  margin: 4px 0;
  line-height: 1.5;
  white-space: pre-wrap;
}
.exam-result-line .rk {
  color: #909399;
}
.exam-result-imgs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}
.exam-result-imgs .rimg {
  width: 64px;
  height: 64px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.left-panel ::v-deep .el-tabs--border-card {
  border: 1px solid #edf2fa;
  border-radius: 8px;
  overflow: hidden;
}

.work-tabs ::v-deep .el-tabs__item.is-active {
  color: #8b5cf6;
  font-weight: 600;
}

.work-tabs ::v-deep .el-tabs__active-bar {
  background-color: #8b5cf6;
}
</style>
