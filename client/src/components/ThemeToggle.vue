<script setup lang="ts">
import { ref, onMounted } from 'vue'

const THEME_KEY = 'theme'
const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
const current = ref<'light' | 'dark'>('light')

function applyTheme(theme: 'light' | 'dark') {
  current.value = theme
  document.body.setAttribute('data-theme', theme)
  localStorage.setItem(THEME_KEY, theme)
}

function toggle() {
  applyTheme(current.value === 'dark' ? 'light' : 'dark')
}

onMounted(() => {
  const stored = localStorage.getItem(THEME_KEY) as 'light' | 'dark' | null
  applyTheme(stored || (prefersDark ? 'dark' : 'light'))
})
</script>

<template>
  <button class="theme-toggle" :title="current === 'dark' ? 'Passer au thème clair' : 'Passer au thème sombre'" @click="toggle">
    <span v-if="current === 'dark'">☀️</span>
    <span v-else>🌙</span>
  </button>
</template>

<style scoped>
.theme-toggle {
  background: transparent;
  border: 1px solid var(--color-border, #4a5568);
  color: var(--color-text, #edf2f7);
  padding: 0.35rem 0.6rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color .2s, color .2s, border-color .2s;
}
.theme-toggle:hover {
  background: rgba(255,255,255,0.1);
}
body[data-theme='light'] .theme-toggle {
  color: #2d3748;
  border-color: #cbd5e0;
}
body[data-theme='light'] .theme-toggle:hover { background:#edf2f7; }
</style>
