<script setup lang="ts">
import { RouterView, RouterLink, useRoute, useRouter } from 'vue-router'
import { onMounted, computed, onBeforeUnmount } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useSystemStore } from '@/stores/system'
import ThemeToggle from '@/components/ThemeToggle.vue'

const authStore = useAuthStore()
const systemStore = useSystemStore()
const route = useRoute()
const router = useRouter()

let healthTimer: number | null = null

onMounted(() => {
  if (authStore.isAuthenticated) {
    authStore.fetchCurrentUser()
  }
  const schedule = async () => {
    await systemStore.checkHealth()
    // Planifier le prochain selon nextDelay (backoff si DOWN)
    healthTimer = window.setTimeout(schedule, systemStore.nextDelay)
  }
  schedule()
})

onBeforeUnmount(() => {
  if (healthTimer) window.clearTimeout(healthTimer)
})

const isAuthenticated = computed(() => authStore.isAuthenticated)
const showNav = computed(() => route.name !== 'login')

function isActive(name: string) {
  return route.name === name
}

async function logout() {
  await authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div id="app">
    <nav v-if="showNav" class="main-nav">
      <div v-if="systemStore.backendUp === false" class="health-banner">
        <strong>Serveur indisponible :</strong> tentative de reconnexion... (dernier état {{ systemStore.health?.status || 'DOWN' }})
      </div>
      <div class="nav-inner">
        <div class="nav-left">
          <RouterLink to="/" class="brand" :class="{ active: isActive('home') }">
            <img src="/favicon-32x32.png" alt="Logo" class="brand-icon" />
            <span>Billard‑Book</span>
          </RouterLink>
          <RouterLink to="/" class="nav-link" :class="{ active: isActive('home') }">Accueil</RouterLink>
          <RouterLink v-if="isAuthenticated" to="/dashboard" class="nav-link" :class="{ active: isActive('dashboard') }">Dashboard</RouterLink>
          <RouterLink v-if="isAuthenticated" to="/users" class="nav-link" :class="{ active: isActive('users') }">Utilisateurs</RouterLink>
          <RouterLink v-if="isAuthenticated" to="/profile" class="nav-link" :class="{ active: isActive('profile') }">Profil</RouterLink>
          <RouterLink to="/about" class="nav-link" :class="{ active: isActive('about') }">À propos</RouterLink>
        </div>
        <div class="nav-right">
          <ThemeToggle />
          <span v-if="isAuthenticated" class="user-label">👤 {{ authStore.user?.login }}</span>
          <button v-if="isAuthenticated" @click="logout" class="nav-btn logout">Déconnexion</button>
          <RouterLink v-else to="/login" class="nav-btn login" :class="{ active: isActive('login') }">Connexion</RouterLink>
        </div>
      </div>
    </nav>
    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  line-height: 1.6;
  color: #333;
  background-color: #f8fafc;
}

#app {
  min-height: 100vh;
}

/* Navigation principale */
.main-nav {
  background: #1a202c;
  color: #edf2f7;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 50;
}
.nav-inner { max-width: 1200px; margin:0 auto; display:flex; justify-content:space-between; align-items:center; padding:0 1rem; height:60px; gap:1.25rem; }
.nav-left, .nav-right { display:flex; align-items:center; gap:1rem; flex-wrap:wrap; }
.brand { font-weight:600; font-size:1.05rem; letter-spacing:.5px; display:inline-flex; align-items:center; gap:0.4rem; }
.brand-icon { width:32px; height:32px; display:block; }
.nav-link { text-decoration:none; color:#cbd5e0; font-size:0.9rem; padding:0.4rem 0.65rem; border-radius:6px; transition:background-color .15s, color .15s; }
.nav-link:hover { color:#fff; background:#2d3748; }
.nav-link.active { background:#2b6cb0; color:#fff; }
.nav-btn { border:none; cursor:pointer; font-size:0.85rem; padding:0.45rem 0.9rem; border-radius:6px; font-weight:500; text-decoration:none; }
.nav-btn.login { background:#3182ce; color:#fff; }
.nav-btn.login:hover { background:#2b6cb0; }
.nav-btn.logout { background:#e53e3e; color:#fff; }
.nav-btn.logout:hover { background:#c53030; }
.user-label { font-size:0.8rem; color:#e2e8f0; }
.app-main { padding:1rem 0 2rem; }
@media (max-width: 640px) {
  .nav-inner { flex-direction:column; height:auto; padding:0.75rem 1rem 0.9rem; align-items:flex-start; }
  .nav-right { width:100%; justify-content:flex-end; }
  .app-main { padding-top:0.5rem; }
}

/* Styles globaux pour les formulaires */
.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: #4299e1;
}

/* Styles globaux pour les boutons */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none;
}

.btn-primary {
  background: #4299e1;
  color: white;
}

.btn-primary:hover {
  background: #3182ce;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(66, 153, 225, 0.3);
}

.btn-secondary {
  background: white;
  color: #4a5568;
  border: 2px solid #e2e8f0;
}

.btn-secondary:hover {
  background: #f7fafc;
  border-color: #cbd5e0;
}

.btn-danger {
  background: #e53e3e;
  color: white;
}

.btn-danger:hover {
  background: #c53030;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* Utilitaires */
.text-center {
  text-align: center;
}

.text-left {
  text-align: left;
}

.text-right {
  text-align: right;
}

.mt-1 { margin-top: 0.25rem; }
.mt-2 { margin-top: 0.5rem; }
.mt-3 { margin-top: 0.75rem; }
.mt-4 { margin-top: 1rem; }

.mb-1 { margin-bottom: 0.25rem; }
.mb-2 { margin-bottom: 0.5rem; }
.mb-3 { margin-bottom: 0.75rem; }
.mb-4 { margin-bottom: 1rem; }

.p-1 { padding: 0.25rem; }
.p-2 { padding: 0.5rem; }
.p-3 { padding: 0.75rem; }
.p-4 { padding: 1rem; }

/* Messages d'erreur/succès */
.error-message {
  background: #fed7d7;
  color: #c53030;
  padding: 0.75rem;
  border-radius: 6px;
  border: 1px solid #feb2b2;
  font-size: 0.9rem;
}

.success-message {
  background: #c6f6d5;
  color: #22543d;
  padding: 0.75rem;
  border-radius: 6px;
  border: 1px solid #9ae6b4;
  font-size: 0.9rem;
}

.info-message {
  background: #bee3f8;
  color: #2a4365;
  padding: 0.75rem;
  border-radius: 6px;
  border: 1px solid #90cdf4;
  font-size: 0.9rem;
}
</style>
<style>
.health-banner { background:#c53030; color:#fff; text-align:center; padding:0.4rem 0.75rem; font-size:0.8rem; }
</style>
