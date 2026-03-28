<template>
  <section class="users-page">
    <div class="app-container users-layout">
      <header class="users-header ui-card">
        <div>
          <p class="eyebrow">Communaute</p>
          <h1>Utilisateurs</h1>
          <p class="subtitle">Retrouvez les membres actifs et leurs statistiques de participation.</p>
        </div>

        <div class="header-actions">
          <label class="search-field" for="user-search">
            <Search :size="16" aria-hidden="true" />
            <span class="sr-only">Rechercher un utilisateur</span>
            <input id="user-search" v-model.trim="search" type="search" class="form-input" placeholder="Rechercher" />
          </label>

          <button type="button" class="btn btn-secondary" @click="loadUsers" :disabled="loading">
            <RefreshCw :size="16" aria-hidden="true" />
            <span>Actualiser</span>
          </button>
        </div>
      </header>

      <section class="users-panel ui-card" aria-labelledby="users-list-title">
        <div class="panel-head">
          <h2 id="users-list-title">Membres inscrits</h2>
          <span class="count-pill">{{ filteredUsers.length }}</span>
        </div>

        <div v-if="loading" class="state-box" role="status" aria-live="polite">
          <LoaderCircle :size="18" class="spin" aria-hidden="true" />
          <p>Chargement des utilisateurs...</p>
        </div>

        <div v-else-if="error" class="error-message state-box">
          <p>{{ error }}</p>
          <button type="button" class="btn btn-secondary" @click="loadUsers">Reessayer</button>
        </div>

        <div v-else-if="filteredUsers.length === 0" class="state-box">
          <p>Aucun utilisateur ne correspond a votre recherche.</p>
        </div>

        <div v-else class="users-grid">
          <article
            v-for="user in filteredUsers"
            :key="user.login"
            class="user-card ui-card"
            :class="{ 'is-current': user.login === currentUserLogin }"
          >
            <header class="user-card-head">
              <div class="avatar" aria-hidden="true">{{ initials(user.login) }}</div>
              <div>
                <h3>@{{ user.login }}</h3>
                <p class="user-role">{{ roleFor(user.login) }}</p>
              </div>
            </header>

            <div class="badges-row">
              <span v-if="user.login === currentUserLogin" class="ui-badge status-success">Vous</span>
              <span class="ui-badge status-info">{{ roleFor(user.login) }}</span>
            </div>

            <dl class="stats-grid">
              <div>
                <dt>Reservations creees</dt>
                <dd>{{ user.ownedReservations?.length || 0 }}</dd>
              </div>
              <div>
                <dt>Participations</dt>
                <dd>{{ user.registeredReservations?.length || 0 }}</dd>
              </div>
            </dl>

            <div class="user-actions">
              <router-link v-if="user.login === currentUserLogin" to="/profile" class="btn btn-primary">Mon profil</router-link>
            </div>
          </article>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { LoaderCircle, RefreshCw, Search } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { userService } from '@/services/users'
import type { User } from '@/types'

const authStore = useAuthStore()

const loading = ref(false)
const error = ref<string | null>(null)
const usersList = ref<User[]>([])
const search = ref('')

const currentUserLogin = computed(() => authStore.user?.login || '')

const filteredUsers = computed(() => {
  const keyword = search.value.toLowerCase()
  if (!keyword) return usersList.value
  return usersList.value.filter(user => user.login.toLowerCase().includes(keyword))
})

onMounted(() => {
  loadUsers()
})

async function loadUsers() {
  try {
    loading.value = true
    error.value = null

    const response = await userService.getUsers()
    const users: User[] = []

    for (const linkObj of response.users || []) {
      const login = linkObj.link
      if (!login) continue

      try {
        const user = await userService.getUser(login)
        users.push(user)
      } catch (requestError) {
        console.warn(`Impossible de charger ${login}`, requestError)
      }
    }

    usersList.value = users.sort((a, b) => a.login.localeCompare(b.login))
  } catch (requestError: unknown) {
    const requestMessage = requestError instanceof Error ? requestError.message : 'Erreur lors du chargement des utilisateurs'
    error.value = requestMessage
  } finally {
    loading.value = false
  }
}

function initials(login: string): string {
  return login.slice(0, 2).toUpperCase()
}

function roleFor(login: string): string {
  return login.toLowerCase() === 'admin' ? 'Admin' : 'Membre'
}
</script>

<style scoped>
.users-page {
  min-height: 100vh;
  padding: var(--space-8) 0 var(--space-10);
}

.users-layout {
  display: grid;
  gap: var(--space-6);
}

.users-header {
  padding: var(--space-6);
  display: flex;
  justify-content: space-between;
  gap: var(--space-6);
  align-items: flex-start;
}

.eyebrow {
  margin: 0 0 var(--space-2);
  text-transform: uppercase;
  font-size: 0.72rem;
  letter-spacing: 0.08em;
  color: var(--color-text-soft);
  font-weight: 700;
}

.users-header h1 {
  margin: 0;
  font-family: var(--font-display);
  font-size: clamp(1.5rem, 1.8vw, 1.9rem);
}

.subtitle {
  margin: var(--space-2) 0 0;
  color: var(--color-text-soft);
}

.header-actions {
  display: flex;
  gap: var(--space-3);
  align-items: center;
  flex-wrap: wrap;
}

.search-field {
  position: relative;
  display: flex;
  align-items: center;
  min-width: 15rem;
}

.search-field svg {
  position: absolute;
  left: var(--space-3);
  color: var(--color-text-soft);
}

.search-field .form-input {
  padding-left: 2.2rem;
}

.users-panel {
  padding: var(--space-5);
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-5);
}

.panel-head h2 {
  margin: 0;
  font-size: 1.2rem;
}

.count-pill {
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: 999px;
  padding: 0.25rem 0.55rem;
  font-size: 0.8rem;
  color: var(--color-text-soft);
  font-weight: 700;
}

.state-box {
  min-height: 9rem;
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: var(--space-3);
  color: var(--color-text-soft);
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-4);
}

.user-card {
  padding: var(--space-4);
  border-radius: var(--radius-lg);
}

.user-card.is-current {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 1px color-mix(in srgb, var(--color-primary) 45%, transparent);
}

.user-card-head {
  display: flex;
  gap: var(--space-3);
  align-items: center;
  margin-bottom: var(--space-3);
}

.avatar {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 999px;
  display: grid;
  place-items: center;
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--color-primary-contrast);
  background: linear-gradient(140deg, var(--color-primary), var(--color-info));
}

.user-card-head h3 {
  margin: 0;
  font-size: 1rem;
}

.user-role {
  margin: var(--space-1) 0 0;
  color: var(--color-text-soft);
  font-size: 0.8rem;
}

.badges-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.stats-grid {
  margin: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-2);
}

.stats-grid div {
  border: 1px solid var(--color-border);
  background: var(--color-surface-2);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
}

.stats-grid dt {
  margin: 0;
  color: var(--color-text-soft);
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.stats-grid dd {
  margin: var(--space-1) 0 0;
  font-size: 1rem;
  font-weight: 700;
}

.user-actions {
  margin-top: var(--space-3);
  min-height: 2.5rem;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

@media (max-width: 900px) {
  .users-page {
    padding-top: var(--space-6);
  }

  .users-header {
    flex-direction: column;
  }

  .header-actions {
    width: 100%;
  }

  .search-field {
    flex: 1;
    min-width: 12rem;
  }
}

@media (max-width: 560px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>