<script setup lang="ts">
import { RouterView, RouterLink, useRoute, useRouter } from 'vue-router'
import { onMounted, computed, onBeforeUnmount } from 'vue'
import { House, LayoutDashboard, Users, UserRound, Info, LogOut, LogIn, CircleAlert } from 'lucide-vue-next'
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
const isAdmin = computed(() => authStore.user?.login?.toLowerCase() === 'admin')
const showNav = computed(() => route.name !== 'login')

function isActive(name: string) {
  return route.name === name
}

async function logout() {
  await authStore.logout()
  router.push('/login')
}

const navItems = computed(() => {
  const items = [
    { name: 'home', label: 'Accueil', to: '/', icon: House, public: true },
    { name: 'dashboard', label: 'Reservations', to: '/dashboard', icon: LayoutDashboard, public: false },
    { name: 'users', label: 'Utilisateurs', to: '/users', icon: Users, public: false },
    { name: 'profile', label: 'Profil', to: '/profile', icon: UserRound, public: false },
    { name: 'about', label: 'A propos', to: '/about', icon: Info, public: true }
  ]

  return items.filter(item => {
    if (!item.public && !isAuthenticated.value) return false
    if (item.name === 'users' && !isAdmin.value) return false
    return true
  })
})
</script>

<template>
  <div id="app" class="app-shell">
    <header v-if="showNav" class="main-nav">
      <div v-if="systemStore.backendUp === false" class="health-banner" role="status" aria-live="polite">
        <CircleAlert :size="16" aria-hidden="true" />
        <span>Serveur indisponible. Reconnexion en cours. Etat: {{ systemStore.health?.status || 'DOWN' }}</span>
      </div>

      <div class="app-container nav-inner">
        <div class="nav-left">
          <RouterLink to="/" class="brand" :class="{ active: isActive('home') }" aria-label="Aller a l'accueil">
            <img src="/favicon-32x32.png" alt="Logo Billard Book" class="brand-icon" />
            <span>Billard Book</span>
          </RouterLink>

          <nav class="nav-links" aria-label="Navigation principale">
            <RouterLink
              v-for="item in navItems"
              :key="item.name"
              :to="item.to"
              class="nav-link"
              :class="{ active: isActive(item.name) }"
            >
              <component :is="item.icon" :size="16" aria-hidden="true" />
              <span>{{ item.label }}</span>
            </RouterLink>
          </nav>
        </div>

        <div class="nav-right">
          <ThemeToggle />
          <span v-if="isAuthenticated" class="user-label">{{ authStore.user?.login }}</span>
          <button v-if="isAuthenticated" @click="logout" class="btn btn-danger nav-action" type="button">
            <LogOut :size="16" aria-hidden="true" />
            <span>Quitter</span>
          </button>
          <RouterLink v-else to="/login" class="btn btn-primary nav-action" :class="{ active: isActive('login') }">
            <LogIn :size="16" aria-hidden="true" />
            <span>Connexion</span>
          </RouterLink>
        </div>
      </div>
    </header>

    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
}

.main-nav {
  position: sticky;
  top: 0;
  z-index: 40;
  backdrop-filter: blur(10px);
  background: color-mix(in srgb, var(--color-surface) 87%, transparent);
  border-bottom: 1px solid var(--color-border);
}

.health-banner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  background: color-mix(in srgb, var(--color-danger) 80%, black 20%);
  color: #fff;
  font-size: 0.8125rem;
  padding: var(--space-2) var(--space-4);
}

.nav-inner {
  min-height: 4.25rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
  padding-block: var(--space-2);
}

.nav-left,
.nav-right {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-family: var(--font-display);
  font-weight: 800;
  letter-spacing: 0.01em;
  color: var(--color-text);
}

.brand-icon {
  width: 2rem;
  height: 2rem;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.nav-link {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: 0.45rem 0.65rem;
  border-radius: var(--radius-md);
  color: var(--color-text-soft);
  font-size: 0.875rem;
  font-weight: 600;
  transition: color var(--duration-fast) var(--easing-standard), background-color var(--duration-fast) var(--easing-standard);
}

.nav-link:hover {
  background: color-mix(in srgb, var(--color-primary) 12%, transparent);
  color: var(--color-text);
}

.nav-link.active {
  background: var(--color-primary);
  color: var(--color-primary-contrast);
}

.nav-action {
  min-height: 2.25rem;
  padding-inline: var(--space-3);
  font-size: 0.8125rem;
}

.user-label {
  background: color-mix(in srgb, var(--color-info) 15%, transparent);
  color: var(--color-info);
  border: 1px solid color-mix(in srgb, var(--color-info) 35%, transparent);
  border-radius: 999px;
  font-weight: 700;
  font-size: 0.75rem;
  padding: 0.25rem 0.55rem;
}

.app-main {
  flex: 1;
}

@media (max-width: 900px) {
  .nav-inner {
    align-items: flex-start;
    flex-direction: column;
  }

  .nav-left,
  .nav-right {
    width: 100%;
    justify-content: space-between;
    flex-wrap: wrap;
  }

  .nav-links {
    width: 100%;
  }
}

@media (max-width: 640px) {
  .nav-link {
    font-size: 0.8125rem;
  }

  .user-label {
    order: 3;
  }
}
</style>
