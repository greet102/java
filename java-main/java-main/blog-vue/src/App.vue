<script setup>
import { useStore } from './store'
import AuthView from './components/AuthView.vue'
import BlogView from './components/BlogView.vue'

const { isLoggedIn } = useStore()
</script>

<template>
  <div class="bubbles">
    <span></span><span></span><span></span><span></span><span></span>
    <span></span><span></span><span></span><span></span><span></span>
  </div>

  <AuthView v-if="!isLoggedIn" />
  <BlogView v-else />
</template>
