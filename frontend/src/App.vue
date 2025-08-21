<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
// 컴포넌트 로직이 필요한 경우 여기에 추가
</script>

<style>
#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  margin: 0;
  padding: 0;
  min-height: 100vh;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  background-color: #f5f7fa;
}

/* Element Plus 커스텀 스타일 */
.el-button {
  border-radius: 6px;
}

.el-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.el-input__inner {
  border-radius: 6px;
}

.el-textarea__inner {
  border-radius: 6px;
}
</style>
