<script setup>
import { ref, onMounted } from 'vue'
import { useStore } from '../store'

const { adminLogin, adminMe } = useStore()

const username = ref('')
const password = ref('')
const msg = ref('')
const msgType = ref('')
const loading = ref(false)

onMounted(async () => {
  // 已经登录过后台，直接进入控制台
  const res = await adminMe()
  if (res.ok) location.hash = '#/admin'
})

function showMsg(text, type) {
  msg.value = text
  msgType.value = type
}

function goHome() {
  location.hash = '#/'
}

async function onSubmit() {
  if (loading.value) return
  if (!username.value.trim() || !password.value) {
    return showMsg('请输入管理员账号和密码', 'err')
  }
  loading.value = true
  msg.value = ''
  const res = await adminLogin(username.value.trim(), password.value)
  loading.value = false
  if (!res.ok) return showMsg(res.msg, 'err')
  location.hash = '#/admin'
}
</script>

<template>
  <main class="auth-view">
    <div class="auth-card">
      <div class="brand">
        <div class="brand-logo">🛠️</div>
        <h1 class="brand-title">后台管理系统</h1>
        <p class="brand-sub">喵喵博客 · 管理控制台</p>
      </div>

      <!-- 仅登录，无注册 -->
      <form class="form" @submit.prevent="onSubmit">
        <h2 class="form-title">管理员登录</h2>
        <label class="field">
          <span>管理员账号</span>
          <input
            type="text"
            v-model="username"
            placeholder="请输入管理员账号"
            autocomplete="username"
            required
          />
        </label>
        <label class="field">
          <span>密码</span>
          <input
            type="password"
            v-model="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            required
          />
        </label>
        <p class="msg" :class="msgType">{{ msg }}</p>
        <button type="submit" class="btn btn-primary" :disabled="loading">
          {{ loading ? '登录中…' : '登 录' }}
        </button>
        <p class="switch">
          <a href="#/" @click.prevent="goHome">← 返回前台首页</a>
        </p>
      </form>
    </div>
  </main>
</template>
