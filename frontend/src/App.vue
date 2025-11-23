<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="app-container">
    <RouterView />
  </div>
</template>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  background-color: #f0f7ff !important;
  color: #333;
  line-height: 1.6;
  margin: 0;
  padding: 0;
  min-width: 320px;
  min-height: 100vh;
  box-sizing: border-box;
}

.app-container {
  min-height: 100vh;
}
</style>
