<template>
  <el-container class="admin-layout">
    <el-aside width="200px">
      <div class="logo">校园互助</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/boards">
          <el-icon><Grid /></el-icon>
          <span>板块管理</span>
        </el-menu-item>
        <el-menu-item index="/posts">
          <el-icon><Document /></el-icon>
          <span>帖子管理</span>
        </el-menu-item>
        <el-menu-item index="/items">
          <el-icon><ShoppingCart /></el-icon>
          <span>闲置管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              管理员
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped lang="scss">
.admin-layout {
  height: 100vh;

  .el-aside {
    background: #304156;

    .logo {
      height: 60px;
      line-height: 60px;
      text-align: center;
      color: #fff;
      font-size: 18px;
      font-weight: bold;
    }

    .el-menu {
      border-right: none;
    }
  }

  .el-header {
    background: #fff;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    display: flex;
    justify-content: flex-end;
    align-items: center;

    .header-right {
      padding-right: 20px;

      .user-info {
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 5px;
      }
    }
  }

  .el-main {
    background: #f0f2f5;
    padding: 20px;
  }
}
</style>
