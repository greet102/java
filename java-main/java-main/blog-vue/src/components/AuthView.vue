<script setup>
import { ref, nextTick } from 'vue'
import { useStore } from '../store'

const { register, login } = useStore()

const mode = ref('login') // 'login' | 'register'
const loginUser = ref('')
const loginPass = ref('')
const regUser = ref('')
const regPass = ref('')
const regPass2 = ref('')
const msg = ref('')
const msgType = ref('')

// ---------- 图形验证码 ----------
const captchaText = ref('')
const captchaInput = ref('')
const captchaCanvas = ref(null)
const CHARSET = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789' // 去掉易混字符 0/O/1/l

function rand(a, b) {
  return Math.floor(Math.random() * (b - a) + a)
}

function genCaptcha() {
  let code = ''
  for (let i = 0; i < 4; i++) code += CHARSET[rand(0, CHARSET.length)]
  captchaText.value = code
  nextTick(drawCaptcha)
}

function drawCaptcha() {
  const cv = captchaCanvas.value
  if (!cv) return
  const ctx = cv.getContext('2d')
  const w = cv.width
  const h = cv.height
  // 背景
  ctx.fillStyle = '#fdfbff'
  ctx.fillRect(0, 0, w, h)
  // 干扰线
  for (let i = 0; i < 4; i++) {
    ctx.strokeStyle = `rgba(${rand(150, 220)},${rand(150, 220)},${rand(150, 220)},0.6)`
    ctx.beginPath()
    ctx.moveTo(rand(0, w), rand(0, h))
    ctx.lineTo(rand(0, w), rand(0, h))
    ctx.stroke()
  }
  // 干扰点
  for (let i = 0; i < 12; i++) {
    ctx.fillStyle = `rgba(${rand(150, 220)},${rand(150, 220)},${rand(150, 220)},0.5)`
    ctx.fillRect(rand(0, w), rand(0, h), 2, 2)
  }
  // 字符（随机颜色 + 轻微旋转）
  const colors = ['#ff9ec4', '#cdb4f6', '#a0e7ff', '#ffb86b', '#7ec8a0']
  for (let i = 0; i < 4; i++) {
    ctx.save()
    ctx.translate(16 + i * 26, h / 2)
    ctx.rotate((Math.random() - 0.5) * 0.5)
    ctx.font = 'bold 26px "Baloo 2", sans-serif'
    ctx.fillStyle = colors[i % colors.length]
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillText(captchaText.value[i], 0, 0)
    ctx.restore()
  }
}

function showMsg(text, type) {
  msg.value = text
  msgType.value = type
}

function switchMode(m) {
  mode.value = m
  msg.value = ''
  msgType.value = ''
  if (m === 'register') genCaptcha()
}

function refreshCaptcha() {
  captchaInput.value = ''
  genCaptcha()
}

function onLogin() {
  doLogin()
}

async function doLogin() {
  const res = await login(loginUser.value, loginPass.value)
  if (!res.ok) return showMsg(res.msg, 'err')
  loginUser.value = ''
  loginPass.value = ''
  showMsg('登录成功，欢迎回家 🐾', 'ok')
}

function onRegister() {
  // 先校验验证码
  if (captchaInput.value.trim().toUpperCase() !== captchaText.value.toUpperCase()) {
    showMsg('验证码不对哦～', 'err')
    refreshCaptcha()
    return
  }
  if (regPass.value !== regPass2.value) {
    showMsg('两次密码不一致呢', 'err')
    refreshCaptcha()
    return
  }
  doRegister()
}

async function doRegister() {
  const res = await register(regUser.value, regPass.value)
  if (!res.ok) {
    showMsg(res.msg, 'err')
    refreshCaptcha()
    return
  }
  // 注册成功会自动登录，App 会切换到主页
  showMsg('注册成功，欢迎回家 🐾', 'ok')
  regUser.value = ''
  regPass.value = ''
  regPass2.value = ''
  captchaInput.value = ''
}
</script>

<template>
  <main class="auth-view">
    <div class="auth-card">
      <div class="brand">
        <div class="brand-logo">🐱</div>
        <h1 class="brand-title">喵喵博客</h1>
        <p class="brand-sub">一个软软糯糯、五彩斑斓的小天地</p>
      </div>

      <!-- 登录 -->
      <form v-if="mode === 'login'" class="form" @submit.prevent="onLogin">
        <h2 class="form-title">欢迎回来喵～</h2>
        <label class="field">
          <span>用户名</span>
          <input type="text" v-model="loginUser" placeholder="请输入用户名" required />
        </label>
        <label class="field">
          <span>密码</span>
          <input type="password" v-model="loginPass" placeholder="请输入密码" required />
        </label>
        <p class="msg" :class="msgType">{{ msg }}</p>
        <button type="submit" class="btn btn-primary">登 录</button>
        <p class="switch">还没有账号？<a href="#" @click.prevent="switchMode('register')">去注册喵 →</a></p>
      </form>

      <!-- 注册 -->
      <form v-else class="form" @submit.prevent="onRegister">
        <h2 class="form-title">创建一个新窝～</h2>
        <label class="field">
          <span>用户名</span>
          <input type="text" v-model="regUser" placeholder="想被人怎么叫你？" required />
        </label>
        <label class="field">
          <span>密码</span>
          <input type="password" v-model="regPass" placeholder="至少 4 位哦" required minlength="4" />
        </label>
        <label class="field">
          <span>确认密码</span>
          <input type="password" v-model="regPass2" placeholder="再输一次" required />
        </label>
        <label class="field">
          <span>验证码</span>
          <div class="captcha-row">
            <input
              class="captcha-input"
              type="text"
              v-model="captchaInput"
              placeholder="输入右边字符"
              maxlength="4"
              autocomplete="off"
            />
            <canvas
              ref="captchaCanvas"
              class="captcha-canvas"
              width="120"
              height="44"
              title="看不清？点我换一张"
              @click="refreshCaptcha"
            ></canvas>
            <button type="button" class="captcha-refresh" title="换一张" @click="refreshCaptcha">🔄</button>
          </div>
        </label>
        <p class="msg" :class="msgType">{{ msg }}</p>
        <button type="submit" class="btn btn-primary">注 册</button>
        <p class="switch">已经有账号啦？<a href="#" @click.prevent="switchMode('login')">去登录 ←</a></p>
      </form>
    </div>
  </main>
</template>

<style scoped>
.captcha-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.captcha-input {
  flex: 1;
  min-width: 0;
  font-family: inherit;
  font-size: 15px;
  color: var(--ink);
  border: 2px solid #f0e6f7;
  border-radius: 14px;
  padding: 12px 14px;
  background: #fdfbff;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.captcha-input:focus {
  outline: none;
  border-color: var(--pink);
  box-shadow: 0 0 0 4px rgba(255, 158, 196, 0.18);
}
.captcha-canvas {
  border-radius: 12px;
  border: 2px solid #f0e6f7;
  background: #fdfbff;
  cursor: pointer;
  flex-shrink: 0;
}
.captcha-refresh {
  border: none;
  background: #f6effb;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  cursor: pointer;
  font-size: 18px;
  flex-shrink: 0;
  transition: all 0.15s;
}
.captcha-refresh:hover {
  background: #efe3f8;
  transform: rotate(-20deg);
}
</style>
