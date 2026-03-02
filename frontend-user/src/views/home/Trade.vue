<template>
  <div class="trade-page">
    <!-- Navigation Bar -->
    <NavBar title="闲置交易" />

    <!-- Search Bar -->
    <div class="trade-search">
      <SearchBar
        v-model="searchQuery"
        @search="handleSearch"
        placeholder="搜索闲置物品..."
        :show-search-button="true"
      />
    </div>

    <!-- Trade List -->
    <TradeList :keyword="searchKeyword" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import TradeList from '@/views/trade/components/TradeList.vue'
import NavBar from '@/components/navigation/NavBar.vue'
import SearchBar from '@/components/SearchBar.vue'

const searchQuery = ref('')
const searchKeyword = ref('')

function handleSearch(query) {
  searchKeyword.value = query
}
</script>

<style scoped>
.trade-page {
  min-height: 100vh;
  background-color: var(--bg-page);
}

.trade-search {
  padding: var(--space-3) var(--page-padding);
  padding-top: calc(var(--nav-height) + 8px);
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
}
</style>
