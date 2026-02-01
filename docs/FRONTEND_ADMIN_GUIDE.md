# 校园互助平台管理前端开发规范指南

## 一、项目结构规范

### 1.1 目录结构

管理前端项目采用 Vue 3 + Element Plus 技术栈，目录结构与用户前端类似，按照功能类型组织代码。

```
frontend-admin/src/
├── api/                           # API 接口定义
│   ├── auth.js                    # 管理员认证接口
│   ├── user.js                    # 用户管理接口
│   ├── post.js                    # 帖子管理接口
│   ├── item.js                    # 闲置管理接口
│   ├── board.js                   # 板块管理接口
│   └── stats.js                   # 统计数据接口
├── components/                    # 通用组件
│   ├── Table/                     # 表格相关组件
│   │   ├── DataTable.vue          # 通用数据表格
│   │   └── TableColumn.vue        # 表格列配置
│   ├── Form/                      # 表单相关组件
│   │   ├── SearchForm.vue         # 搜索表单
│   │   └── FormDialog.vue         # 表单弹窗
│   ├── Charts/                    # 图表组件
│   │   ├── LineChart.vue          # 折线图
│   │   └── BarChart.vue           # 柱状图
│   └── Common/                    # 其他通用组件
│       ├── PageHeader.vue         # 页面头部
│       └── StatCard.vue           # 统计卡片
├── views/                         # 页面组件
│   ├── layout/                    # 布局组件
│   │   ├── index.vue              # 主布局
│   │   ├── Sidebar.vue            # 侧边栏
│   │   └── Navbar.vue             # 顶部导航
│   ├── dashboard/                 # 仪表盘模块
│   │   └── index.vue              # 数据概览页
│   ├── user/                      # 用户管理模块
│   │   ├── index.vue              # 用户列表页
│   │   └── detail.vue             # 用户详情页
│   ├── post/                      # 帖子管理模块
│   │   └── index.vue              # 帖子列表页
│   ├── item/                      # 闲置管理模块
│   │   └── index.vue              # 闲置列表页
│   ├── board/                     # 板块管理模块
│   │   ├── index.vue              # 板块列表页
│   │   └── edit.vue               # 板块编辑页
│   └── login/                     # 登录模块
│       └── index.vue              # 登录页
├── router/                        # 路由配置
│   └── index.js                   # 路由定义
├── stores/                        # Pinia 状态管理
│   ├── admin.js                   # 管理员状态
│   ├── menu.js                    # 菜单状态
│   └── settings.js                # 设置状态
├── utils/                         # 工具函数
│   ├── request.js                 # axios 请求封装
│   ├── format.js                  # 格式化工具
│   └── permission.js              # 权限工具
├── styles/                        # 样式文件
│   ├── main.scss                  # 主样式文件
│   ├── variables.scss             # SCSS 变量
│   └── mixins.scss                # SCSS 混入
├── App.vue                        # 根组件
└── main.js                        # 入口文件
```

### 1.2 文件命名规则

管理前端组件和页面文件同样使用 PascalCase 命名法。管理端更强调功能的明确性，文件命名应该清晰表达模块和功能。

```
# 页面组件示例
UserList.vue       # 用户列表页
UserDetail.vue     # 用户详情页
PostManagement.vue # 帖子管理页
Dashboard.vue      # 仪表盘页

# 组件示例
DataTable.vue      # 通用数据表格
FormDialog.vue     # 表单弹窗
StatCard.vue       # 统计卡片
Breadcrumb.vue     # 面包屑导航
```

## 二、Element Plus 使用规范

### 2.1 表格组件使用

表格是管理端最常用的组件，用于展示列表数据。Element Plus 的 Table 组件功能强大，支持排序、筛选、展开行等功能。

**基础表格使用**：

```vue
<template>
  <el-table :data="tableData" stripe border style="width: 100%">
    <el-table-column prop="id" label="ID" width="80" sortable />
    <el-table-column prop="username" label="用户名" min-width="120" />
    <el-table-column prop="nickname" label="昵称" min-width="120" />
    <el-table-column prop="status" label="状态" width="100">
      <template #default="{ row }">
        <el-tag :type="row.status === 1 ? 'success' : 'danger'">
          {{ row.status === 1 ? '正常' : '禁用' }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="createdAt" label="创建时间" width="180" sortable />
    <el-table-column label="操作" width="150" fixed="right">
      <template #default="{ row }">
        <el-button type="primary" link @click="handleView(row)">
          查看
        </el-button>
        <el-button
          :type="row.status === 1 ? 'danger' : 'success'"
          link
          @click="handleToggleStatus(row)"
        >
          {{ row.status === 1 ? '禁用' : '启用' }}
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import { ref } from 'vue'

const tableData = ref([])

const handleView = (row) => {
  // 查看详情
}

const handleToggleStatus = (row) => {
  // 切换状态
}
</script>
```

**分页表格**：

```vue
<template>
  <div>
    <el-table :data="tableData" v-loading="loading">
      <!-- 表格列定义 -->
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'

const loading = ref(false)
const tableData = ref([])
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserList(pagination)
    tableData.value = res.data.list
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchData()
}

const handlePageChange = (page) => {
  pagination.pageNum = page
  fetchData()
}
</script>
```

### 2.2 表单组件使用

表单用于数据录入和编辑场景。Element Plus 的 Form 组件提供了完整的表单验证和布局功能。

**基础表单使用**：

```vue
<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-width="120px"
    status-icon
  >
    <el-form-item label="用户名" prop="username">
      <el-input v-model="form.username" placeholder="请输入用户名" />
    </el-form-item>
    <el-form-item label="昵称" prop="nickname">
      <el-input v-model="form.nickname" placeholder="请输入昵称" />
    </el-form-item>
    <el-form-item label="手机号" prop="phone">
      <el-input v-model="form.phone" placeholder="请输入手机号" />
    </el-form-item>
    <el-form-item label="状态" prop="status">
      <el-switch v-model="form.status" />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="handleSubmit(formRef)">
        保存
      </el-button>
      <el-button @click="handleReset(formRef)">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

const formRef = ref(null)

const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  status: true
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名4-20位', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const handleSubmit = async (formEl) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      // 提交表单
      ElMessage.success('保存成功')
    } else {
      console.log('验证失败', fields)
    }
  })
}

const handleReset = (formEl) => {
  if (!formEl) return
  formEl.resetFields()
}
</script>
```

### 2.3 弹窗组件使用

弹窗用于展示详情、编辑表单、确认操作等场景。

**编辑弹窗**：

```vue
<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑用户' : '新增用户'"
    width="600px"
    destroy-on-close
    @close="handleClose"
  >
    <UserForm
      ref="formRef"
      :data="currentData"
      @submit="handleSubmit"
    />
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirm">
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import UserForm from './UserForm.vue'

const props = defineProps({
  modelValue: Boolean,
  isEdit: Boolean,
  data: Object
})

const emit = defineEmits(['update:modelValue', 'success'])

const dialogVisible = ref(false)
const formRef = ref(null)
const currentData = ref({})

watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
})

watch(dialogVisible, (val) => {
  emit('update:modelValue', val)
})

watch(() => props.data, (val) => {
  currentData.value = { ...val }
})

const handleClose = () => {
  dialogVisible.value = false
}

const handleConfirm = () => {
  formRef.value?.submit()
}

const handleSubmit = async (formData) => {
  // 提交数据
  emit('success')
  handleClose()
}
</script>
```

## 三、图表集成规范

### 3.1 图表库选择

管理端使用 ECharts 作为图表库，Element Plus 也提供了与 ECharts 的集成方案。通过 ECharts 可以展示丰富的数据可视化效果。

### 3.2 折线图使用

```vue
<template>
  <div ref="chartRef" class="chart-container" />
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref(null)
let chartInstance = null

const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})

const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption({
    title: {
      text: '用户增长趋势'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['新增用户', '活跃用户']
    },
    xAxis: {
      type: 'category',
      data: props.data.dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        data: props.data.newUsers,
        smooth: true
      },
      {
        name: '活跃用户',
        type: 'line',
        data: props.data.activeUsers,
        smooth: true
      }
    ]
  })
}

onMounted(() => {
  initChart()
})

watch(() => props.data, () => {
  initChart()
}, { deep: true })
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 400px;
}
</style>
```

### 3.3 柱状图使用

```vue
<template>
  <div ref="chartRef" class="chart-container" />
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref(null)
let chartInstance = null

const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})

const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption({
    title: {
      text: '内容统计'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: props.data.categories
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '数量',
        type: 'bar',
        data: props.data.values,
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  })
}

onMounted(() => {
  initChart()
})

watch(() => props.data, () => {
  initChart()
}, { deep: true })
</script>
```

## 四、权限管理集成

### 4.1 路由权限控制

管理端根据用户角色动态生成路由菜单，只有具有相应权限的用户才能访问对应页面。

```javascript
// router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Monitor' }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'posts',
        name: 'PostManagement',
        component: () => import('@/views/post/index.vue'),
        meta: { title: '帖子管理', icon: 'Document' }
      },
      {
        path: 'items',
        name: 'ItemManagement',
        component: () => import('@/views/item/index.vue'),
        meta: { title: '闲置管理', icon: 'ShoppingCart' }
      },
      {
        path: 'boards',
        name: 'BoardManagement',
        component: () => import('@/views/board/index.vue'),
        meta: { title: '板块管理', icon: 'Grid' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const adminStore = useAdminStore()

  if (to.meta.public) {
    next()
    return
  }

  if (!adminStore.token) {
    next('/login')
    return
  }

  next()
})

export default router
```

### 4.2 按钮权限控制

页面内的按钮也需要根据权限控制显示和隐藏。

```vue
<template>
  <div>
    <el-button v-if="hasPermission('user:create')" type="primary" @click="handleAdd">
      新增用户
    </el-button>
    <el-button v-if="hasPermission('user:export')" @click="handleExport">
      导出
    </el-button>
  </div>
</template>

<script setup>
import { usePermission } from '@/utils/permission'

const { hasPermission } = usePermission()
</script>
```

## 五、响应式设计规范

### 5.1 布局响应式

管理端采用固定侧边栏布局，在不同屏幕尺寸下保持良好的展示效果。

```vue
<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <Sidebar :collapse="isCollapse" />
    </el-aside>
    <el-container>
      <el-header class="header">
        <Navbar @toggle="isCollapse = !isCollapse" />
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import Sidebar from '@/views/layout/Sidebar.vue'
import Navbar from '@/views/layout/Navbar.vue'

const isCollapse = ref(false)
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  transition: width 0.3s;
}

.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
```

### 5.2 表格响应式

表格在不同屏幕尺寸下自适应显示，重要列固定显示。

```vue
<template>
  <el-table
    :data="tableData"
    :max-height="tableHeight"
    stripe
    border
  >
    <el-table-column prop="id" label="ID" width="80" />
    <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
    <el-table-column prop="nickname" label="昵称" min-width="120" />
    <el-table-column prop="status" label="状态" width="100" />
    <el-table-column label="操作" width="150" fixed="right" />
  </el-table>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const tableHeight = computed(() => {
  return window.innerHeight - 200
})

const handleResize = () => {
  // 响应窗口大小变化
}
</script>
```

## 六、主题定制规范

### 6.1 SCSS 变量

管理端使用 SCSS 预处理器，通过变量定义主题色和样式。

```scss
// styles/variables.scss

// 主题色
$primary-color: #409EFF;
$success-color: #67C23A;
$warning-color: #E6A23C;
$danger-color: #F56C6C;
$info-color: #909399;

// 背景色
$bg-color: #f0f2f5;
$white: #ffffff;

// 文字颜色
$text-primary: #303133;
$text-regular: #606266;
$text-secondary: #909399;

// 边框颜色
$border-color: #DCDFE6;
$border-color-light: #E4E7ED;

// 圆角
$border-radius-base: 4px;
$border-radius-small: 2px;

// 阴影
$box-shadow-base: 0 2px 4px rgba(0, 0, 0, .12);
```

### 6.2 Element Plus 主题覆盖

通过 SCSS 变量覆盖 Element Plus 默认主题。

```scss
// styles/element-variables.scss
@forward 'element-plus/theme-chalk/src/common/var.scss' with (
  $colors: (
    'primary': (
      'base': #409EFF
    ),
    'success': (
      'base': #67C23A
    ),
    'warning': (
      'base': #E6A23C
    ),
    'danger': (
      'base': #F56C6C
    ),
    'info': (
      'base': #909399
    )
  ),
  $border-radius: (
    'base': 4px
  )
);
```

---

**文档版本**：1.0

**最后更新**：2026年2月

**维护团队**：校园互助平台开发团队
