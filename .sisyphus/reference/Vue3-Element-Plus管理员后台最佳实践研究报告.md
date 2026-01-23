# Vue3 + Element Plus 管理员后台最佳实践研究报告

## 1. 管理员布局和导航模式

### 经典后台布局结构

```vue
<!-- AdminLayout.vue -->
<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo-container">
        <img src="@/assets/logo.png" alt="Logo" class="logo" />
        <span v-show="!isCollapsed" class="title">管理系统</span>
      </div>
      
      <!-- 导航菜单 -->
      <el-menu
        :default-active="currentRoute"
        :collapse="isCollapsed"
        :unique-opened="true"
        router
        class="sidebar-menu"
      >
        <template v-for="menu in menus" :key="menu.path">
          <!-- 单级菜单 -->
          <el-menu-item 
            v-if="!menu.children || menu.children.length === 0"
            :index="menu.path"
          >
            <el-icon><component :is="menu.icon" /></el-icon>
            <template #title>{{ menu.title }}</template>
          </el-menu-item>
          
          <!-- 多级菜单 -->
          <el-sub-menu v-else :index="menu.path">
            <template #title>
              <el-icon><component :is="menu.icon" /></el-icon>
              <span>{{ menu.title }}</span>
            </template>
            <el-menu-item 
              v-for="child in menu.children" 
              :key="child.path"
              :index="child.path"
            >
              <el-icon><component :is="child.icon" /></el-icon>
              <template #title>{{ child.title }}</template>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>
    
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleSidebar">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-for="crumb in breadcrumbs" :key="crumb.path">
              {{ crumb.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 全屏切换 -->
          <el-tooltip content="全屏" placement="bottom">
            <el-icon class="header-icon" @click="toggleFullscreen">
              <FullScreen />
            </el-icon>
          </el-tooltip>
          
          <!-- 用户下拉菜单 -->
          <el-dropdown @command="handleUserCommand" trigger="click">
            <div class="user-info">
              <el-avatar :size="32" :src="userInfo.avatar" />
              <span class="username">{{ userInfo.name }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>系统设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 主内容区域 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <keep-alive :include="cachedViews">
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  Fold, Expand, FullScreen, User, Setting, SwitchButton, ArrowDown 
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapsed = ref(false)
const cachedViews = ref(['Dashboard'])

const userInfo = computed(() => userStore.userInfo)

const menus = ref([
  {
    path: '/dashboard',
    title: '仪表盘',
    icon: 'Odometer',
    children: []
  },
  {
    path: '/system',
    title: '系统管理',
    icon: 'Setting',
    children: [
      { path: '/system/users', title: '用户管理', icon: 'User' },
      { path: '/system/roles', title: '角色管理', icon: 'UserFilled' },
      { path: '/system/permissions', title: '权限管理', icon: 'Lock' },
      { path: '/system/logs', title: '操作日志', icon: 'Document' }
    ]
  },
  {
    path: '/content',
    title: '内容管理',
    icon: 'Files',
    children: [
      { path: '/content/articles', title: '文章管理', icon: 'Document' },
      { path: '/content/categories', title: '分类管理', icon: 'Folder' },
      { path: '/content/tags', title: '标签管理', icon: 'PriceTag' }
    ]
  },
  {
    path: '/statistics',
    title: '统计分析',
    icon: 'DataAnalysis',
    children: [
      { path: '/statistics/overview', title: '数据概览', icon: 'TrendCharts' },
      { path: '/statistics/reports', title: '报表管理', icon: 'Reading' }
    ]
  }
])

const currentRoute = computed(() => route.path)

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta.title as string
  }))
})

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const handleUserCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await userStore.logout()
    router.push('/login')
    ElMessage.success('退出成功')
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  filterMenusByPermission()
})

const filterMenusByPermission = () => {
  const userPermissions = userStore.permissions
  menus.value = menus.value.filter(menu => {
    if (menu.children && menu.children.length > 0) {
      menu.children = menu.children.filter(child => 
        userPermissions.includes(child.path)
      )
      return menu.children.length > 0
    }
    return userPermissions.includes(menu.path)
  })
}
</script>

<style lang="scss" scoped>
.admin-layout {
  height: 100vh;
  
  .sidebar {
    background-color: #304156;
    transition: width 0.3s;
    
    .logo-container {
      height: 60px;
      display: flex;
      align-items: center;
      padding: 0 20px;
      background-color: #263445;
      
      .logo {
        width: 32px;
        height: 32px;
        margin-right: 12px;
      }
      
      .title {
        color: #fff;
        font-size: 16px;
        font-weight: bold;
      }
    }
    
    .sidebar-menu {
      border-right: none;
      background-color: transparent;
      
      :deep(.el-menu-item),
      :deep(.el-sub-menu__title) {
        color: #bfcbd9;
        
        &:hover {
          background-color: #263445;
        }
      }
      
      :deep(.el-menu-item.is-active) {
        color: #409EFF;
        background-color: #1f2d3d;
      }
    }
  }
  
  .header {
    background: #fff;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    
    .header-left {
      display: flex;
      align-items: center;
      
      .collapse-btn {
        font-size: 20px;
        cursor: pointer;
        margin-right: 16px;
        
        &:hover {
          color: #409EFF;
        }
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      
      .header-icon {
        font-size: 18px;
        cursor: pointer;
        margin-right: 20px;
        
        &:hover {
          color: #409EFF;
        }
      }
      
      .user-info {
        display: flex;
        align-items: center;
        cursor: pointer;
        
        .username {
          margin: 0 8px;
          color: #333;
        }
      }
    }
  }
  
  .main-content {
    background-color: #f0f2f5;
    padding: 20px;
    overflow-y: auto;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
```

## 2. CRUD操作数据管理

### 通用CRUD组合式函数

```typescript
// composables/useCrud.ts
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

interface CrudConfig<T> {
  api: {
    list: (params: any) => Promise<{ data: T[]; total: number }>
    create: (data: Partial<T>) => Promise<T>
    update: (id: string | number, data: Partial<T>) => Promise<T>
    delete: (id: string | number) => Promise<void>
    detail: (id: string | number) => Promise<T>
  }
  primaryKey?: string
}

export function useCrud<T extends Record<string, any>>(config: CrudConfig<T>) {
  const loading = ref(false)
  const dataList = ref<T[]>([])
  const total = ref(0)
  const currentItem = ref<T | null>(null)
  const dialogVisible = ref(false)
  const searchForm = reactive({
    keyword: '',
    status: '',
    dateRange: []
  })
  
  const pagination = reactive({
    currentPage: 1,
    pageSize: 20,
    pageSizes: [10, 20, 50, 100]
  })
  
  const formRef = ref<FormInstance>()
  const formMode = ref<'create' | 'update'>('create')
  const formData = reactive<Partial<T>>({})
  const rules = reactive<FormRules<T>>({})
  
  const isUpdateMode = computed(() => formMode.value === 'update')
  
  const loadData = async () => {
    loading.value = true
    try {
      const res = await config.api.list({
        page: pagination.currentPage,
        pageSize: pagination.pageSize,
        keyword: searchForm.keyword,
        status: searchForm.status,
        startTime: searchForm.dateRange?.[0],
        endTime: searchForm.dateRange?.[1]
      })
      dataList.value = res.data
      total.value = res.total
    } catch (error) {
      console.error('加载数据失败:', error)
      ElMessage.error('加载数据失败')
    } finally {
      loading.value = false
    }
  }
  
  const handleSearch = () => {
    pagination.currentPage = 1
    loadData()
  }
  
  const handleReset = () => {
    searchForm.keyword = ''
    searchForm.status = ''
    searchForm.dateRange = []
    handleSearch()
  }
  
  const handleSizeChange = (size: number) => {
    pagination.pageSize = size
    loadData()
  }
  
  const handleCurrentChange = (page: number) => {
    pagination.currentPage = page
    loadData()
  }
  
  const handleCreate = () => {
    formMode.value = 'create'
    Object.keys(formData).forEach(key => {
      formData[key] = undefined
    })
    dialogVisible.value = true
  }
  
  const handleEdit = async (row: T) => {
    formMode.value = 'update'
    currentItem.value = row
    
    Object.keys(row).forEach(key => {
      if (formData.hasOwnProperty(key)) {
        formData[key] = row[key]
      }
    })
    
    dialogVisible.value = true
  }
  
  const handleDelete = async (row: T) => {
    const id = row[config.primaryKey || 'id']
    
    try {
      await ElMessageBox.confirm('确定要删除该记录吗？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      loading.value = true
      await config.api.delete(id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除失败:', error)
        ElMessage.error('删除失败')
      }
    } finally {
      loading.value = false
    }
  }
  
  const handleSubmit = async () => {
    if (!formRef.value) return
    
    try {
      await formRef.value.validate()
      loading.value = true
      
      if (formMode.value === 'create') {
        await config.api.create(formData)
        ElMessage.success('新增成功')
      } else {
        const id = currentItem.value?.[config.primaryKey || 'id']
        await config.api.update(id!, formData)
        ElMessage.success('更新成功')
      }
      
      dialogVisible.value = false
      loadData()
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error('提交失败')
    } finally {
      loading.value = false
    }
  }
  
  return {
    loading,
    dataList,
    total,
    pagination,
    searchForm,
    dialogVisible,
    formMode,
    formData,
    formRef,
    rules,
    isUpdateMode,
    loadData,
    handleSearch,
    handleReset,
    handleSizeChange,
    handleCurrentChange,
    handleCreate,
    handleEdit,
    handleDelete,
    handleSubmit
  }
}
```

### 完整的数据管理页面组件

```vue
<!-- UserManagement.vue -->
<template>
  <div class="user-management">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="用户名">
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="请输入用户名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 操作按钮区域 -->
    <el-card class="action-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <div class="actions">
            <el-button 
              type="primary" 
              @click="handleCreate"
            >
              <el-icon><Plus /></el-icon>新增
            </el-button>
            <el-button 
              type="danger" 
              :disabled="selectedRows.length === 0"
              @click="handleBatchDelete"
            >
              <el-icon><Delete /></el-icon>批量删除
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="dataList"
        stripe
        border
        @selection-change="handleSelectionChange"
        class="data-table"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="pagination.pageSizes"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isUpdateMode ? '编辑用户' : '新增用户'"
      width="600px"
      :close-on-click-modal="false"
      @close="handleClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="100px"
        class="user-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="formData.username" 
            :disabled="isUpdateMode"
            placeholder="请输入用户名"
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item v-if="!isUpdateMode" label="密码" prop="password">
          <el-input 
            v-model="formData.password" 
            type="password"
            show-password
            placeholder="请输入密码"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Plus, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useCrud } from '@/composables/useCrud'
import { userApi } from '@/api'
import type { User } from '@/types'

const {
  loading,
  dataList,
  total,
  pagination,
  searchForm,
  dialogVisible,
  formMode,
  formData,
  formRef,
  isUpdateMode,
  loadData,
  handleSearch,
  handleReset,
  handleSizeChange,
  handleCurrentChange,
  handleCreate,
  handleEdit,
  handleDelete,
  handleSubmit,
  handleClose
} = useCrud<User>({
  api: {
    list: userApi.getList,
    create: userApi.create,
    update: userApi.update,
    delete: userApi.delete,
    detail: userApi.getDetail
  },
  primaryKey: 'id'
})

const rules: FormRules<User> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const handleStatusChange = async (row: User) => {
  try {
    await userApi.updateStatus(row.id, row.status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

const handleView = (row: User) => {
  // 查看详情逻辑
}

const handleSelectionChange = (rows: User[]) => {
  // 处理选中行
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.user-management {
  .search-card {
    margin-bottom: 20px;
    
    .search-form {
      display: flex;
      flex-wrap: wrap;
      
      .el-form-item {
        margin-bottom: 0;
        margin-right: 20px;
      }
    }
  }
  
  .action-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .actions {
        display: flex;
        gap: 10px;
      }
    }
    
    .data-table {
      margin-bottom: 20px;
    }
    
    .pagination-container {
      display: flex;
      justify-content: flex-end;
      margin-top: 20px;
    }
  }
}
</style>
```

## 3. 表格组件和分页

### 高级表格组件

```vue
<!-- AdvancedTable.vue -->
<template>
  <div class="advanced-table">
    <!-- 表格工具栏 -->
    <div class="table-toolbar" v-if="showToolbar">
      <div class="toolbar-left">
        <el-button 
          v-if="exportable"
          type="primary" 
          @click="handleExport"
        >
          <el-icon><Download /></el-icon>导出
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-input
          v-if="searchable"
          v-model="searchKeyword"
          placeholder="搜索..."
          prefix-icon="Search"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
          style="width: 200px"
        />
      </div>
    </div>
    
    <!-- 数据表格 -->
    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="tableData"
      :stripe="stripe"
      :border="border"
      :height="tableHeight"
      @selection-change="handleSelectionChange"
      @cell-click="handleCellClick"
      @row-click="handleRowClick"
      class="data-table"
    >
      <el-table-column
        v-if="showSelection"
        type="selection"
        width="50"
      />
      
      <el-table-column
        v-if="showIndex"
        type="index"
        :index="indexMethod"
        label="序号"
        width="60"
      />
      
      <el-table-column
        v-for="column in visibleColumns"
        :key="column.prop"
        :prop="column.prop"
        :label="column.label"
        :width="column.width"
        :min-width="column.minWidth"
        :fixed="column.fixed"
        :sortable="column.sortable"
        :formatter="column.formatter"
      >
        <template #default="{ row }">
          <template v-if="column.type === 'image'">
            <el-image
              :src="row[column.prop]"
              :preview-src-list="[row[column.prop]]"
              fit="cover"
              style="width: 50px; height: 50px"
            />
          </template>
          <template v-else-if="column.type === 'switch'">
            <el-switch
              v-model="row[column.prop]"
              :active-value="column.activeValue"
              :inactive-value="column.inactiveValue"
              @change="(val) => handleSwitchChange(row, column, val)"
            />
          </template>
          <template v-else-if="column.type === 'tag'">
            <el-tag :type="getTagType(row, column)">
              {{ getTagLabel(row, column) }}
            </el-tag>
          </template>
          <template v-else-if="column.formatter">
            {{ column.formatter(row, column, row[column.prop]) }}
          </template>
          <template v-else>
            {{ row[column.prop] }}
          </template>
        </template>
      </el-table-column>
      
      <el-table-column
        v-if="showActions"
        label="操作"
        :width="actionWidth"
        fixed="right"
      >
        <template #default="{ row, $index }">
          <div class="action-buttons">
            <template v-for="(action, index) in getActions(row)" :key="index">
              <el-button
                :type="action.type || 'primary'"
                :size="action.size || 'small'"
                :link="action.link"
                :disabled="action.disabled"
                @click="handleAction(action, row, $index)"
              >
                {{ action.label }}
              </el-button>
              <el-divider 
                v-if="index < getActions(row).length - 1"
                direction="vertical" 
              />
            </template>
          </div>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="table-pagination" v-if="pagination">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="pagination.pageSizes"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handlePaginationSizeChange"
        @current-change="handlePaginationCurrentChange"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue'
import { Download, Refresh } from '@element-plus/icons-vue'

interface Column {
  prop: string
  label: string
  type?: 'image' | 'switch' | 'tag' | 'default'
  width?: number
  minWidth?: number
  fixed?: 'left' | 'right' | boolean
  sortable?: boolean
  formatter?: (row: any, column: any, cellValue: any) => any
  activeValue?: any
  inactiveValue?: any
  tagOptions?: { label: string; value: any; type?: string }[]
}

interface Action {
  label: string
  type?: string
  size?: string
  link?: boolean
  disabled?: boolean
  action?: string
}

interface PaginationConfig {
  currentPage: number
  pageSize: number
  pageSizes?: number[]
  total: number
}

const props = withDefaults(defineProps<{
  data: any[]
  columns: Column[]
  loading?: boolean
  stripe?: boolean
  border?: boolean
  height?: string | number
  showSelection?: boolean
  showIndex?: boolean
  showActions?: boolean
  showToolbar?: boolean
  exportable?: boolean
  refreshable?: boolean
  searchable?: boolean
  pagination?: PaginationConfig | false
  actions?: Action[]
}>(), {
  stripe: true,
  border: true,
  showSelection: false,
  showIndex: false,
  showActions: true,
  showToolbar: true,
  exportable: true,
  refreshable: true,
  searchable: true
})

const emit = defineEmits<{
  (e: 'selection-change', rows: any[]): void
  (e: 'action', action: Action, row: any, index: number): void
  (e: 'export'): void
  (e: 'refresh'): void
  (e: 'search', keyword: string): void
  (e: 'pagination-change', config: PaginationConfig): void
}>()

const tableRef = ref()
const tableData = ref(props.data)
const searchKeyword = ref('')
const visibleColumns = computed(() => props.columns)

const tableHeight = computed(() => props.height || 'calc(100% - 60px)')

const indexMethod = (index: number) => {
  const { currentPage, pageSize } = props.pagination || { currentPage: 1, pageSize: 20 }
  return (currentPage - 1) * pageSize + index + 1
}

const getActions = (row: any): Action[] => {
  return (props.actions || []).filter(action => true) // 添加权限检查
}

const handleAction = (action: Action, row: any, index: number) => {
  if (action.action) {
    emit('action', action, row, index)
  }
}

const handleSwitchChange = (row: any, column: Column, value: any) => {
  // 处理开关变化
}

const getTagType = (row: any, column: Column): string => {
  const option = column.tagOptions?.find(opt => opt.value === row[column.prop])
  return option?.type || 'info'
}

const getTagLabel = (row: any, column: Column): string => {
  const option = column.tagOptions?.find(opt => opt.value === row[column.prop])
  return option?.label || row[column.prop]
}

const handleSelectionChange = (rows: any[]) => {
  emit('selection-change', rows)
}

const handleCellClick = (row: any, column: any, cell: any, event: Event) => {
  // 处理单元格点击
}

const handleRowClick = (row: any, column: any, event: Event) => {
  // 处理行点击
}

const handleExport = () => {
  emit('export')
}

const handleRefresh = () => {
  emit('refresh')
}

const handleSearch = () => {
  emit('search', searchKeyword.value)
}

const handlePaginationSizeChange = (size: number) => {
  if (props.pagination) {
    props.pagination.pageSize = size
    emit('pagination-change', props.pagination)
  }
}

const handlePaginationCurrentChange = (page: number) => {
  if (props.pagination) {
    props.pagination.currentPage = page
    emit('pagination-change', props.pagination)
  }
}
</script>

<style lang="scss" scoped>
.advanced-table {
  .table-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .toolbar-left,
    .toolbar-right {
      display: flex;
      align-items: center;
      gap: 10px;
    }
  }
  
  .data-table {
    margin-bottom: 16px;
    
    .action-buttons {
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
  
  .table-pagination {
    display: flex;
    justify-content: flex-end;
  }
}
</style>
```

## 4. 表单处理

### 动态表单组件

```vue
<!-- DynamicForm.vue -->
<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="formRules"
    :label-position="labelPosition"
    :label-width="labelWidth"
    class="dynamic-form"
  >
    <el-row :gutter="20">
      <template v-for="field in visibleFields" :key="field.prop">
        <el-col :span="field.span || defaultSpan">
          <el-form-item
            :prop="field.prop"
            :label="field.label"
            :required="field.required"
            :rules="field.rules"
          >
            <!-- 输入框 -->
            <template v-if="field.type === 'input'">
              <el-input
                v-model="formData[field.prop]"
                :type="field.inputType || 'text'"
                :placeholder="field.placeholder"
                :disabled="field.disabled"
                :readonly="field.readonly"
                :clearable="field.clearable !== false"
                :show-password="field.showPassword"
                :maxlength="field.maxlength"
                :prefix-icon="field.prefixIcon"
                @blur="handleFieldBlur(field)"
                @change="handleFieldChange(field)"
              />
            </template>
            
            <!-- 数字输入框 -->
            <template v-else-if="field.type === 'number'">
              <el-input-number
                v-model="formData[field.prop]"
                :min="field.min"
                :max="field.max"
                :step="field.step || 1"
                :disabled="field.disabled"
                :controls-position="field.controlsPosition || 'right'"
                style="width: 100%"
              />
            </template>
            
            <!-- 下拉选择 -->
            <template v-else-if="field.type === 'select'">
              <el-select
                v-model="formData[field.prop]"
                :placeholder="`请选择${field.label}`"
                :disabled="field.disabled"
                :clearable="field.clearable !== false"
                :filterable="field.filterable"
                :multiple="field.multiple"
                style="width: 100%"
                @change="handleFieldChange(field)"
              >
                <el-option
                  v-for="option in field.options"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </template>
            
            <!-- 日期选择 -->
            <template v-else-if="field.type === 'date'">
              <el-date-picker
                v-model="formData[field.prop]"
                :type="field.dateType || 'date'"
                :placeholder="`请选择${field.label}`"
                :disabled="field.disabled"
                :clearable="field.clearable !== false"
                :format="field.format"
                :value-format="field.valueFormat"
                style="width: 100%"
              />
            </template>
            
            <!-- 开关 -->
            <template v-else-if="field.type === 'switch'">
              <el-switch
                v-model="formData[field.prop]"
                :disabled="field.disabled"
                :active-text="field.activeText"
                :inactive-text="field.inactiveText"
              />
            </template>
            
            <!-- 多选框 -->
            <template v-else-if="field.type === 'checkbox'">
              <el-checkbox-group
                v-model="formData[field.prop]"
                :disabled="field.disabled"
              >
                <el-checkbox
                  v-for="option in field.options"
                  :key="option.value"
                  :label="option.value"
                >
                  {{ option.label }}
                </el-checkbox>
              </el-checkbox-group>
            </template>
            
            <!-- 单选框 -->
            <template v-else-if="field.type === 'radio'">
              <el-radio-group
                v-model="formData[field.prop]"
                :disabled="field.disabled"
              >
                <el-radio
                  v-for="option in field.options"
                  :key="option.value"
                  :label="option.value"
                >
                  {{ option.label }}
                </el-radio>
              </el-radio-group>
            </template>
            
            <!-- 文本域 -->
            <template v-else-if="field.type === 'textarea'">
              <el-input
                v-model="formData[field.prop]"
                type="textarea"
                :placeholder="field.placeholder"
                :disabled="field.disabled"
                :readonly="field.readonly"
                :rows="field.rows || 3"
                :maxlength="field.maxlength"
                :show-word-limit="field.showWordLimit"
              />
            </template>
            
            <!-- 富文本编辑器 -->
            <template v-else-if="field.type === 'editor'">
              <el-input
                v-model="formData[field.prop]"
                type="textarea"
                :id="field.editorId || 'editor'"
                class="tinymce-editor"
              />
            </template>
            
            <!-- 滑块 -->
            <template v-else-if="field.type === 'slider'">
              <el-slider
                v-model="formData[field.prop]"
                :min="field.min || 0"
                :max="field.max || 100"
                :step="field.step || 1"
                :disabled="field.disabled"
                :show-input="field.showInput"
              />
            </template>
            
            <!-- 评分 -->
            <template v-else-if="field.type === 'rate'">
              <el-rate
                v-model="formData[field.prop]"
                :max="field.max || 5"
                :disabled="field.disabled"
                :allow-half="field.allowHalf"
              />
            </template>
            
            <!-- 颜色选择 -->
            <template v-else-if="field.type === 'color'">
              <el-color-picker
                v-model="formData[field.prop]"
                :disabled="field.disabled"
                :show-alpha="field.showAlpha"
              />
            </template>
            
            <!--  cascader -->
            <template v-else-if="field.type === 'cascader'">
              <el-cascader
                v-model="formData[field.prop]"
                :options="field.options || []"
                :placeholder="`请选择${field.label}`"
                :disabled="field.disabled"
                :clearable="field.clearable !== false"
                :filterable="field.filterable"
                :props="field.cascaderProps"
                style="width: 100%"
              />
            </template>
            
            <!-- 自定义 -->
            <template v-else-if="field.type === 'custom'">
              <slot 
                :name="field.slotName" 
                :field="field" 
                :form-data="formData"
                :value="formData[field.prop]"
                @input="(val) => (formData[field.prop] = val)"
              />
            </template>
          </el-form-item>
        </el-col>
      </template>
    </el-row>
  </el-form>
</template>

<script setup lang="ts" generic="T extends Record<string, any>">
import { ref, computed, watch, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

interface FormField {
  prop: string
  label: string
  type: 'input' | 'number' | 'select' | 'date' | 'switch' | 'checkbox' | 'radio' | 'textarea' | 'editor' | 'slider' | 'rate' | 'color' | 'cascader' | 'custom'
  span?: number
  placeholder?: string
  required?: boolean
  disabled?: boolean
  readonly?: boolean
  clearable?: boolean
  rules?: FormRules[T]['prop'] extends keyof T ? FormRules[T][T['prop'] extends keyof T ? T['prop'] : string] : never
  options?: { label: string; value: any }[]
  defaultValue?: any
  // 数字类型特有
  min?: number
  max?: number
  step?: number
  // 选择类型特有
  filterable?: boolean
  multiple?: boolean
  // 日期类型特有
  dateType?: 'year' | 'month' | 'date' | 'dates' | 'week' | 'datetime' | 'datetimerange' | 'daterange' | 'monthrange'
  format?: string
  valueFormat?: string
  // 开关类型特有
  activeText?: string
  inactiveText?: string
  // 文本域特有
  rows?: number
  maxlength?: number
  showWordLimit?: boolean
  // 滑块特有
  showInput?: boolean
  // 评分特有
  allowHalf?: boolean
  // 颜色选择特有
  showAlpha?: boolean
  // cascader特有
  cascaderProps?: Record<string, any>
  // 自定义类型特有
  slotName?: string
  // 上传类型特有
  action?: string
  headers?: Record<string, string>
  limit?: number
}

const props = withDefaults(defineProps<{
  fields: FormField[]
  modelValue: T
  labelPosition?: 'left' | 'right' | 'top'
  labelWidth?: string
}>(), {
  labelPosition: 'right',
  labelWidth: '100px'
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: T): void
  (e: 'field-change', field: FormField, value: any): void
  (e: 'field-blur', field: FormField, value: any): void
}>()

const formRef = ref<FormInstance>()
const formData = ref<T>(props.modelValue)
const defaultSpan = ref(24)

const visibleFields = computed(() => 
  props.fields.filter(field => field.type !== 'hidden')
)

watch(
  () => props.modelValue,
  (newVal) => {
    formData.value = newVal
  },
  { deep: true }
)

watch(
  formData,
  (newVal) => {
    emit('update:modelValue', newVal)
  },
  { deep: true }
)

const handleFieldChange = (field: FormField) => {
  emit('field-change', field, formData.value[field.prop])
}

const handleFieldBlur = (field: FormField) => {
  emit('field-blur', field, formData.value[field.prop])
}

const validate = async () => {
  return formRef.value?.validate()
}

const resetFields = () => {
  formRef.value?.resetFields()
}

const clearValidate = (props?: string[]) => {
  formRef.value?.clearValidate(props)
}

defineExpose({
  validate,
  resetFields,
  clearValidate
})
</script>

<style scoped>
.dynamic-form {
  padding: 20px;
}

.dynamic-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.dynamic-form :deep(.el-input__wrapper),
.dynamic-form :deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

.dynamic-form :deep(.el-input__wrapper:hover),
.dynamic-form :deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

.dynamic-form :deep(.el-input__wrapper.is-focus),
.dynamic-form :deep(.el-textarea__inner.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}
</style>
```

## 5. 权限和认证

### 完整的权限系统实现

```typescript
// router/permission.ts
import router from './index'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 白名单路由
const whiteList = ['/login', '/register', '/forgot-password', '/404', '/403']

// 进度条配置
NProgress.configure({ showSpinner: false })

// 全局前置守卫
router.beforeEach(async (to, from, next) => {
  // 开始进度条
  NProgress.start()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 管理系统` : '管理系统'
  
  const userStore = useUserStore()
  const hasToken = userStore.token
  
  if (hasToken) {
    if (to.path === '/login') {
      // 已登录且目标为登录页，直接跳转首页
      next({ path: '/' })
      NProgress.done()
    } else else {
      // 判断是否已获取用户信息
      const hasUserInfo = userStore.userInfo
      if (hasUserInfo) {
        next()
      } else {
        try {
          // 获取用户信息
          await userStore.getUserInfo()
          
          // 根据角色获取可访问路由
          const accessRoutes = await userStore.generateRoutes()
          
          // 动态添加路由
          accessRoutes.forEach(route => {
            router.addRoute(route)
          })
          
          // 确保 addRoutes 完成
          next({ ...to, replace: true })
        } catch (error) {
          // 获取用户信息失败，清除token并跳转登录页
          await userStore.logout()
          ElMessage.error(error.message || '获取用户信息失败')
          next(`/login?redirect=${to.path}`)
          NProgress.done()
        }
      }
    }
  } else {
    // 未登录
    if (whiteList.includes(to.path)) {
      // 白名单路由直接放行
      next()
    } else {
      // 其他路由跳转登录页
      next(`/login?redirect=${to.path}`)
      NProgress.done()
    }
  }
})

// 全局后置守卫
router.afterEach((to, from) => {
  // 结束进度条
  NProgress.done()
})
```

### 路由配置

```typescript
// router/index.ts
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Layout from '@/layouts/AdminLayout.vue'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { title: '首页', icon: 'Odometer' },
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    redirect: '/system/users',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'users',
        component: () => import('@/views/system/users/index.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['admin'] }
      },
      {
        path: 'roles',
        component: () => import('@/views/system/roles/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled', roles: ['admin'] }
      },
      {
        path: 'permissions',
        component: () => import('@/views/system/permissions/index.vue'),
        meta: { title: '权限管理', icon: 'Lock', roles: ['admin'] }
      },
      {
        path: 'logs',
        component: () => import('@/views/system/logs/index.vue'),
        meta: { title: '操作日志', icon: 'Document', roles: ['admin', 'manager'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
```

## 6. API集成模式

### 完整的API服务层实现

```typescript
// api/request.ts
import axios, { AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    
    // 添加token
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    
    return config
  },
  (error: AxiosRequestConfig) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, message, data } = response.data
    
    // 业务成功
    if (code === 200) {
      return { code, message, data }
    }
    
    // 业务失败
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  (error: any) => {
    const { status, data } = error.response
    
    switch (status) {
      case 401:
        // token过期或无效
        ElMessageBox.confirm('登录状态已过期，请重新登录', '提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
        })
        break
      case 403:
        ElMessage.error('没有权限访问')
        break
      case 404:
        ElMessage.error('请求的资源不存在')
        break
      case 500:
        ElMessage.error('服务器错误，请联系管理员')
        break
      default:
        ElMessage.error(data?.message || '请求失败')
    }
    
    return Promise.reject(error)
  }
)

export default service
```

### API模块定义

```typescript
// api/modules/user.ts
import request from '../request'

// API响应类型定义
interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

interface User {
  id: number
  username: string
  nickname: string
  phone: string
  email: string
  avatar: string
  status: number
  createdAt: string
}

interface UserListParams {
  page?: number
  pageSize?: number
  keyword?: string
  status?: string
}

// 用户相关API
export const userApi = {
  // 获取用户列表
  getList: (params: UserListParams) => {
    return request.get<ApiResponse<{ list: User[]; total: number }>>('/users', { params })
  },
  
  // 获取用户详情
  getDetail: (id: number) => {
    return request.get<ApiResponse<User>>(`/users/${id}`)
  },
  
  // 创建用户
  create: (data: Partial<User>) => {
    return request.post<ApiResponse<User>>('/users', data)
  },
  
  // 更新用户
  update: (id: number, data: Partial<User>) => {
    return request.put<ApiResponse<User>>(`/users/${id}`, data)
  },
  
  // 删除用户
  delete: (id: number) => {
    return request.delete<ApiResponse<void>>(`/users/${id}`)
  },
  
  // 更新状态
  updateStatus: (id: number, status: number) => {
    return request.put<ApiResponse<void>>(`/users/${id}/status`, { status })
  },
  
  // 批量删除
  batchDelete: (ids: number[]) => {
    return request.delete<ApiResponse<void>>('/users/batch', { data: { ids } })
  },
  
  // 重置密码
  resetPassword: (id: number, password: string) => {
    return request.put<ApiResponse<void>>(`/users/${id}/password`, { password })
  }
}
```

## 7. 管理员仪表盘

### 仪表盘页面

```vue
<!-- Dashboard.vue -->
<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="stat in statistics" :key="stat.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" :style="{ background: stat.color }">
            <el-icon :size="24"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-title">{{ stat.title }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户增长趋势</span>
              <el-radio-group v-model="chartPeriod" size="small">
                <el-radio-button label="week">周</el-radio-button>
                <el-radio-button label="month">月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span>用户分布</span>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 最新动态 -->
    <el-card shadow="hover" class="recent-activity">
      <template #header>
        <span>最新动态</span>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="activity in recentActivities"
          :key="activity.id"
          :timestamp="activity.time"
          placement="top"
        >
          <p>{{ activity.content }}</p>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const chartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
const chartPeriod = ref('week')

const statistics = ref([
  { title: '用户总数', value: '1,234', icon: 'User', color: '#409EFF' },
  { title: '今日新增', value: '56', icon: 'Plus', color: '#67C23A' },
  { title: '帖子总数', value: '3,456', icon: 'Document', color: '#E6A23C' },
  { title: '活跃用户', value: '789', icon: 'UserFilled', color: '#F56C6C' }
])

const recentActivities = ref([
  { id: 1, content: '用户"张三"注册成功', time: '10分钟前' },
  { id: 2, content: '新帖子"校园二手交易"发布', time: '30分钟前' },
  { id: 3, content: '用户"李四"完成了账号认证', time: '1小时前' },
  { id: 4, content: '系统完成每日备份', time: '2小时前' }
])

let chartInstance: echarts.ECharts | null = null
let pieChartInstance: echarts.ECharts | null = null

const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增用户', '活跃用户'] },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: { type: 'value' },
    series: [
      { name: '新增用户', type: 'line', data: [120, 132, 101, 134, 90, 230, 210] },
      { name: '活跃用户', type: 'line', data: [220, 182, 191, 234, 290, 330, 310] }
    ]
  })
}

const initPieChart = () => {
  if (!pieChartRef.value) return
  
  pieChartInstance = echarts.init(pieChartRef.value)
  pieChartInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '5%' },
    series: [
      {
        name: '用户来源',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        label: { show: false },
        data: [
          { value: 1048, name: '搜索引擎' },
          { value: 735, name: '直接访问' },
          { value: 580, name: '推荐' },
          { value: 484, name: '社交媒体' }
        ]
      }
    ]
  })
}

onMounted(() => {
  initChart()
  initPieChart()
  
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  pieChartInstance?.dispose()
})

const handleResize = () => {
  chartInstance?.resize()
  pieChartInstance?.resize()
}
</script>

<style lang="scss" scoped>
.dashboard {
  .stat-cards {
    margin-bottom: 20px;
    
    .stat-card {
      display: flex;
      align-items: center;
      padding: 10px;
      
      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        margin-right: 15px;
      }
      
      .stat-info {
        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #333;
        }
        
        .stat-title {
          font-size: 14px;
          color: #999;
        }
      }
    }
  }
  
  .chart-row {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .chart-container {
      height: 300px;
    }
  }
  
  .recent-activity {
    .el-timeline {
      padding-left: 20px;
    }
  }
}
</style>
```

## 8. 总结

本研究报告涵盖了Vue3 + Element Plus构建管理员后台的最佳实践：

1. **管理员布局**：经典的后台管理布局结构（侧边栏+顶部导航+主内容区）
2. **CRUD操作**：通用CRUD组合式函数和完整的数据管理页面组件
3. **表格组件**：高级表格组件，支持分页、排序、筛选、操作按钮
4. **表单处理**：动态表单组件，支持多种表单项类型
5. **权限控制**：基于RBAC的完整权限系统实现
6. **API集成**：Axios请求封装和API模块定义
7. **仪表盘**：统计卡片、图表展示、最新动态

这些实践基于Element Plus官方文档和Vue 3最佳实践，提供了生产级别的后台管理开发指导。
