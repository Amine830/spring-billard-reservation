<template>
  <div class="users-view">
    <header class="page-header">
      <div class="header-content">
        <h1>👥 Liste des utilisateurs</h1>
        <p class="subtitle">Découvrez les membres de la communauté Billard-Book</p>
      </div>
    </header>

    <main class="page-content">
      <div class="container">
        <!-- États de chargement et erreur -->
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>Chargement des utilisateurs...</p>
        </div>

        <div v-else-if="error" class="error-state">
          <div class="error-icon">⚠️</div>
          <h3>Erreur de chargement</h3>
          <p>{{ error }}</p>
          <button @click="loadUsers" class="retry-button">
            Réessayer
          </button>
        </div>

        <!-- Liste des utilisateurs -->
        <div v-else class="users-section">
          <div class="section-header">
            <h2>Utilisateurs inscrits ({{ usersList.length }})</h2>
            <button @click="loadUsers" class="refresh-button" :disabled="loading">
              🔄 Actualiser
            </button>
          </div>

          <div v-if="usersList.length === 0" class="empty-state">
            <div class="empty-icon">👤</div>
            <h3>Aucun utilisateur trouvé</h3>
            <p>Il n'y a pas encore d'utilisateurs inscrits.</p>
          </div>

          <div v-else class="users-grid">
            <div 
              v-for="user in usersList" 
              :key="user.login"
              class="user-card"
              :class="{ 'current-user': user.login === currentUserLogin }"
            >
              <div class="user-avatar">
                <span class="avatar-icon">👤</span>
              </div>
              <div class="user-info">
                <h3 class="user-login">@{{ user.login }}</h3>
                <div v-if="user.login === currentUserLogin" class="current-user-badge">
                  ⭐ C'est vous !
                </div>
              </div>
              <div class="user-stats" v-if="user.ownedReservations || user.registeredReservations">
                <div class="stat" v-if="user.ownedReservations">
                  <span class="stat-number">{{ user.ownedReservations.length }}</span>
                  <span class="stat-label">Créées</span>
                </div>
                <div class="stat" v-if="user.registeredReservations">
                  <span class="stat-number">{{ user.registeredReservations.length }}</span>
                  <span class="stat-label">Participations</span>
                </div>
              </div>
              <div class="user-actions">
                <button 
                  @click="viewUserDetails(user.login)"
                  class="view-button"
                  :disabled="loadingUserDetails[user.login]"
                >
                  {{ loadingUserDetails[user.login] ? 'Chargement...' : 'Voir profil' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { userService } from '@/services/users'
import type { User } from '@/types'

const authStore = useAuthStore()

const loading = ref(false)
const error = ref<string | null>(null)
const userLinks = ref<{ link: string }[]>([])
const usersList = ref<User[]>([])
const loadingUserDetails = ref<Record<string, boolean>>({})

const currentUserLogin = computed(() => authStore.user?.login)

onMounted(() => {
  loadUsers()
})

async function loadUsers() {
  try {
    loading.value = true
    error.value = null
    
    // Récupérer la liste des liens vers les utilisateurs
    const usersResponse = await userService.getUsers()
    userLinks.value = usersResponse.users || []
    
    // Extraire les logins des liens et récupérer les détails de chaque utilisateur
    const users: User[] = []
    for (const linkObj of userLinks.value) {
      try {
        // Le login est directement dans la propriété 'link'
        const login = linkObj.link
        if (login) {
          const user = await userService.getUser(login)
          users.push(user)
        }
      } catch (err) {
        console.warn(`Erreur lors du chargement de l'utilisateur ${linkObj.link}:`, err)
      }
    }
    
    usersList.value = users
  } catch (err: unknown) {
    console.error('Erreur lors du chargement des utilisateurs:', err)
    error.value = (err as Error).message || 'Erreur lors du chargement des utilisateurs'
  } finally {
    loading.value = false
  }
}

async function viewUserDetails(login: string) {
  try {
    loadingUserDetails.value[login] = true
    
    // Pour l'instant, on affiche juste les détails dans la console
    // Dans une vraie application, on pourrait naviguer vers une page de profil
    // const user = await userService.getUser(login)
    // console.log('Détails de l\'utilisateur:', user)
    alert(`Profil de @${login}`)
  } catch (err: unknown) {
    console.error('Erreur lors du chargement des détails:', err)
    alert('Erreur lors du chargement du profil utilisateur')
  } finally {
    loadingUserDetails.value[login] = false
  }
}
</script>

<style scoped>
.users-view {
  min-height: 100vh;
  background: var(--color-bg);
  color: var(--color-text);
}

.page-header {
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  padding: 2rem 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
  text-align: center;
}

.page-header h1 {
  font-size: 2.5rem;
  color: var(--color-text);
  margin-bottom: 0.5rem;
  font-weight: 600;
}

.subtitle {
  color: var(--color-text-soft);
  font-size: 1.1rem;
  margin: 0;
}

.page-content {
  padding: 2rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.loading-state, .error-state {
  text-align: center;
  padding: 4rem 2rem;
  color: var(--color-text);
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e2e8f0;
  border-left: 4px solid #4299e1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-state {
  color: var(--color-danger);
}

.error-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.retry-button {
  background: #4299e1;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  margin-top: 1rem;
}

.retry-button:hover {
  background: #3182ce;
}

.users-section {
  background: var(--color-surface);
  border-radius: 12px;
  box-shadow: var(--shadow-elev);
  overflow: hidden;
  border: 1px solid var(--color-border);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--color-border);
  flex-wrap: wrap;
  gap: 1rem;
}

.section-header h2 {
  margin: 0;
  color: var(--color-text);
  font-size: 1.25rem;
}

.refresh-button {
  background: var(--color-bg);
  color: var(--color-text-soft);
  border: 2px solid var(--color-border);
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
}
.refresh-button:hover:not(:disabled) {
  background: var(--color-surface);
  border-color: var(--color-border);
}
.refresh-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.refresh-button:hover:not(:disabled) {
  background: #edf2f7;
  border-color: #cbd5e0;
}

.refresh-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: var(--color-text-soft);
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
  padding: 1.5rem;
}

.user-card {
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1.5rem;
  background: var(--color-surface);
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.user-card:hover {
  box-shadow: var(--shadow-elev);
  transform: translateY(-2px);
}
.user-card.current-user {
  border-color: var(--color-accent);
  box-shadow: 0 0 0 1px var(--color-accent);
}

.user-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.user-card.current-user {
  border-color: #4299e1;
  box-shadow: 0 0 0 1px #4299e1;
}

.user-avatar {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, var(--color-accent) 0%, #764ba2 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0 auto;
}

.avatar-icon {
  font-size: 2rem;
}

.user-info {
  text-align: center;
}

.user-login {
  margin: 0 0 0.5rem 0;
  color: var(--color-text-soft);
  font-size: 0.9rem;
}

.current-user-badge {
  background: #c6f6d5;
  color: #22543d;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  display: inline-block;
}

.user-stats {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
}

.stat {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--color-accent);
}

.stat-label {
  font-size: 0.8rem;
  color: #718096;
  text-transform: uppercase;
  font-weight: 500;
}

.user-actions {
  margin-top: auto;
}

.view-button {
  width: 100%;
  background: #4299e1;
  color: white;
  border: none;
  padding: 0.75rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s ease;
}

.view-button:hover:not(:disabled) {
  background: #3182ce;
}

.view-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .page-header h1 {
    font-size: 2rem;
  }
  
  .users-grid {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
    text-align: center;
  }
}
</style>
