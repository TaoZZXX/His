import Vue from 'vue'
import VueRouter from 'vue-router'

import DashboardVue from "@/layout/dashboard/Dashboard.Vue.vue";
import { getToken } from '@/utils/auth'
import Home from '@/views/home/Home.vue';
import Registration from '@/views/registration/Registration.vue';
import EditRegistration from '@/views/registration/EditRegistration.vue';
import CashierDesk from '@/views/cashier/CashierDesk.vue';
import DailySettlementPage from '@/views/finance/DailySettlementPage.vue';
import OutpatientDesk from '@/views/doctor/OutpatientDesk.vue';
import DoctorScheduleManagement from '@/views/doctor/DoctorScheduleManagement.vue';
import StaffRoleManagement from '@/views/user/StaffRoleManagement.vue';
import RolePermissionManagement from '@/views/user/RolePermissionManagement.vue';
import DeptManagement from '@/views/admin/DeptManagement.vue';
import OperationLogPage from '@/views/admin/OperationLogPage.vue';
import ModuleBackfill from '@/views/admin/ModuleBackfill.vue';
import DoctorTemplateCenter from '@/views/admin/DoctorTemplateCenter.vue';
import DiagnosisManagement from '@/views/admin/DiagnosisManagement.vue';
import FinanceExtensionPage from '@/views/finance/FinanceExtensionPage.vue';
import AuditOpsPage from '@/views/admin/AuditOpsPage.vue';
import MedTechWorkbench from '@/views/examLab/MedTechWorkbench.vue';
import PharmacyWorkbench from '@/views/pharmacy/PharmacyWorkbench.vue';
import PharmacyDrugManage from '@/views/pharmacy/PharmacyDrugManage.vue';

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'dashboard',
    component: DashboardVue,
    redirect: '/home',
    meta: { title: '主页' },
    children: [
      {
        path: 'home',
        name: 'home',
        component: Home,
        meta: { title: '主页' }
      },
      {
        path: 'admin',
        name: 'admin',
        component: { render: h => h('router-view') },
        meta: { title: '系统管理', breadcrumb: ['首页', '系统管理'] },
        children: [
          {
            path: 'staff',
            name: 'staff-role-management',
            component: StaffRoleManagement,
            meta: { title: '用户管理/角色权限管理', breadcrumb: ['首页', '系统管理', '用户管理'] }
          },
          {
            path: 'roles',
            name: 'role-permission-management',
            component: RolePermissionManagement,
            meta: { title: '角色权限管理', breadcrumb: ['首页', '系统管理', '角色权限管理'] }
          },
          {
            path: 'departments',
            name: 'dept-management',
            component: DeptManagement,
            meta: { title: '科室管理', breadcrumb: ['首页', '系统管理', '科室管理'] }
          },
          {
            path: 'module-backfill',
            name: 'module-backfill',
            component: ModuleBackfill,
            meta: { title: '模块补齐工作台', breadcrumb: ['首页', '系统管理', '模块补齐工作台'] }
          },
          {
            path: 'audit-ops',
            name: 'audit-ops',
            component: AuditOpsPage,
            meta: { title: '系统审计与运营', breadcrumb: ['首页', '系统管理', '系统审计与运营'] }
          },
          {
            path: 'login-logs',
            name: 'login-logs',
            component: AuditOpsPage,
            meta: { title: '登录日志查看', breadcrumb: ['首页', '系统管理', '登录日志查看'], loginOnly: true }
          },
          {
            path: 'operation-logs',
            name: 'operation-logs',
            component: OperationLogPage,
            meta: { title: '操作日志', breadcrumb: ['首页', '系统管理', '操作日志'] }
          }
        ]
      },
      {
        path: 'charge',
        name: 'charge',
        component: { render: h => h('router-view') },
        meta: { title: '门诊收费挂号', breadcrumb: ['首页', '门诊收费挂号'] },
        children: [
          {
            path: 'registration',
            name: 'registration',
            component: Registration,
            meta: { title: '门诊挂号工作台', breadcrumb: ['首页', '门诊收费挂号', '门诊挂号工作台'] }
          },
          {
            path: 'registration/editRegistration',
            name: 'cancel-registration',
            component: EditRegistration,
            meta: { title: '退号', breadcrumb: ['首页', '门诊收费挂号', '退号'] }
          }
        ]
      },
      {
        path: 'cashier',
        name: 'cashier',
        component: { render: h => h('router-view') },
        meta: { title: '收费与财务', breadcrumb: ['首页', '收费与财务'] },
        redirect: { name: 'cashier-workbench' },
        children: [
          {
            path: 'workbench',
            name: 'cashier-workbench',
            component: CashierDesk,
            meta: { title: '门诊收费台', breadcrumb: ['首页', '收费与财务', '门诊收费台'] }
          },
          {
            path: 'daily-settlement/history',
            name: 'daily-settlement-history',
            component: DailySettlementPage,
            meta: {
              title: '门诊历史日结',
              breadcrumb: ['首页', '收费与财务', '历史日结'],
              settlementMode: 'history'
            }
          },
          {
            path: 'daily-settlement/audit',
            name: 'daily-settlement-audit',
            component: DailySettlementPage,
            meta: {
              title: '门诊日结核对',
              breadcrumb: ['首页', '收费与财务', '日结核对'],
              settlementMode: 'audit'
            }
          },
          {
            path: 'extension',
            name: 'finance-extension',
            component: FinanceExtensionPage,
            meta: { title: '财务扩展', breadcrumb: ['首页', '收费与财务', '财务扩展'] }
          }
        ]
      },
      { path: 'finance/daily-settlement/history', redirect: '/cashier/daily-settlement/history' },
      { path: 'finance/daily-settlement/audit', redirect: '/cashier/daily-settlement/audit' },
      {
        path: 'doctor',
        name: 'doctor',
        component: { render: h => h('router-view') },
        meta: { title: '门诊医生', breadcrumb: ['首页', '门诊医生'] },
        children: [
          {
            path: 'outpatient',
            name: 'outpatient-desk',
            component: OutpatientDesk,
            meta: { title: '门诊医生工作台', breadcrumb: ['首页', '门诊医生', '门诊医生工作台'] }
          },
          {
            path: 'schedule',
            name: 'doctor-schedule-management',
            component: DoctorScheduleManagement,
            meta: { title: '医生排班管理', breadcrumb: ['首页', '医生排班管理'] }
          },
          {
            path: 'template-center',
            name: 'doctor-template-center',
            component: DoctorTemplateCenter,
            meta: { title: '医生模板中心', breadcrumb: ['首页', '门诊医生', '医生模板中心'] }
          },
          {
            path: 'diagnosis-management',
            name: 'diagnosis-management',
            component: DiagnosisManagement,
            meta: { title: '诊断目录管理', breadcrumb: ['首页', '门诊医生', '诊断目录管理'] }
          }
        ]
      },
      {
        path: 'exam-lab',
        name: 'exam-lab',
        component: { render: h => h('router-view') },
        meta: { title: '检查检验', breadcrumb: ['首页', '检查检验'] },
        redirect: { name: 'exam-lab-workbench' },
        children: [
          {
            path: 'workbench',
            name: 'exam-lab-workbench',
            component: MedTechWorkbench,
            meta: { title: '门诊医技工作台', breadcrumb: ['首页', '检查检验', '门诊医技工作台'] }
          },
          { path: 'execute', redirect: { name: 'exam-lab-workbench' } },
          { path: 'result', redirect: { name: 'exam-lab-workbench' } }
        ]
      },
      {
        path: 'pharmacy',
        name: 'pharmacy',
        component: { render: h => h('router-view') },
        meta: { title: '药房', breadcrumb: ['首页', '药房'] },
        redirect: { name: 'pharmacy-workbench' },
        children: [
          {
            path: 'workbench',
            name: 'pharmacy-workbench',
            component: PharmacyWorkbench,
            meta: { title: '药房工作台', breadcrumb: ['首页', '药房', '药房工作台'] }
          },
          {
            path: 'drugs',
            name: 'pharmacy-drugs',
            component: PharmacyDrugManage,
            meta: { title: '药品维护', breadcrumb: ['首页', '药房', '药品维护'] }
          }
        ]
      }
    ]
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/LoginVue.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/register/RegisterVue.vue'),
    meta: { title: '注册' }
  }
]

function createRouter() {
  return new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes
  })
}

const router = createRouter()

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title
  }

  // 登录白名单
  const whiteList = ['/login', '/register']
  const hasToken = getToken()

  if (hasToken) {
    // 已登录，访问登录页则跳转主页
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      next()
    }
  } else {
    // 未登录，访问白名单以外页面则跳转登录页
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next({ path: '/login', query: { redirect: to.fullPath } })
    }
  }
})

export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export default router
