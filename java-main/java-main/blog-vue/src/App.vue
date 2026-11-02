<script setup>
import { ref, computed } from 'vue'
import { useStore } from './store'
import AuthView from './components/AuthView.vue'
import BlogView from './components/BlogView.vue'
import AdminLoginView from './components/AdminLoginView.vue'
import AdminDashboardView from './components/AdminDashboardView.vue'

const { isLoggedIn } = useStore()

// 后台管理路由：#/admin/login 为登录页，#/admin 为控制台
const route = ref(typeof location !== 'undefined' ? location.hash : '')
if (typeof window !== 'undefined') {
  window.addEventListener('hashchange', () => {
    route.value = location.hash
  })
}
const isAdminLogin = computed(() => route.value === '#/admin/login')
const isAdminDash = computed(
  () => route.value.startsWith('#/admin') && route.value !== '#/admin/login'
)
</script>

<template>
  <div class="bubbles">
    <span></span><span></span><span></span><span></span><span></span>
    <span></span><span></span><span></span><span></span><span></span>
  </div>

  <AdminLoginView v-if="isAdminLogin" />
  <AdminDashboardView v-else-if="isAdminDash" />
  <AuthView v-else-if="!isLoggedIn" />
  <BlogView v-else />
</template>
