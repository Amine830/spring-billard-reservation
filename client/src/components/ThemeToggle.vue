<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Moon, Sun } from 'lucide-vue-next'

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
  <button
    class="theme-toggle"
    :title="current === 'dark' ? 'Activer le theme clair' : 'Activer le theme sombre'"
    :aria-label="current === 'dark' ? 'Activer le theme clair' : 'Activer le theme sombre'"
    :aria-pressed="current === 'dark'"
    @click="toggle"
    type="button"
  >
    <Sun v-if="current === 'dark'" :size="16" aria-hidden="true" />
    <Moon v-else :size="16" aria-hidden="true" />
  </button>
</template>

<style scoped>
.theme-toggle {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  color: var(--color-text-soft);
  width: 2.25rem;
  height: 2.25rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  transition: background-color var(--duration-fast) var(--easing-standard), color var(--duration-fast) var(--easing-standard), border-color var(--duration-fast) var(--easing-standard);
}
.theme-toggle:hover {
  background: var(--color-surface-2);
  color: var(--color-text);
}
</style>
