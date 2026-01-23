<template>
  <div class="min-h-screen bg-gray-100">
    <!-- Navigation Bar -->
    <NavBar title="消息" />
    
    <!-- Message List -->
    <MessageList />
  </div>
</template>

<script setup lang="ts">
import MessageList from '@/views/messages/components/MessageList.vue'
import NavBar from '@/components/navigation/NavBar.vue'
</script>
