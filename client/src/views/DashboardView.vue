<template>
  <div class="dashboard">
    <main class="dashboard-main">
      <div class="dashboard-content">
        <!-- Section Actions rapides -->
        <div class="quick-actions">
          <div class="page-header">
            <h1 class="page-title">Dashboard</h1>
            <p class="page-sub">Gestion rapide de vos réservations</p>
          </div>
          <div class="action-buttons">
            <button @click="showCreateModal = true" class="action-button primary">
              ➕ Nouvelle réservation
            </button>
            <router-link to="/users" class="action-button">
              👥 Voir les utilisateurs
            </router-link>
            <button @click="refreshReservations" class="action-button">
              🔄 Actualiser
            </button>
          </div>
        </div>

        <!-- Section Réservations actives -->
        <div class="reservations-section">
          <h2>Réservations actives</h2>
          
          <div v-if="reservationStore.loading" class="loading">
            Chargement des réservations...
          </div>
          
          <div v-else-if="reservationStore.error" class="error">
            {{ reservationStore.error }}
            <button @click="refreshReservations" class="retry-button">
              Réessayer
            </button>
          </div>
          
          <div v-else-if="reservationStore.activeReservations.length === 0" class="empty-state">
            Aucune réservation active
          </div>
          
          <div v-else class="reservations-grid">
            <div 
              v-for="reservation in reservationStore.activeReservations" 
              :key="reservation.id"
              class="reservation-card"
            >
              <div class="reservation-header">
                <h3>{{ reservation.title }}</h3>
                <span class="table-id">Table {{ reservation.tableId }}</span>
                <span v-if="isFull(reservation) && !isPast(reservation.endTime)" class="full-badge" title="Réservation complète">🈵 Complet</span>
              </div>
              
              <div class="reservation-details">
                <div class="time-info">
                  <span class="start-time">
                    🕐 {{ formatDateTime(reservation.startTime) }}
                  </span>
                  <span class="duration">
                    ⏱️ {{ reservation.duration }}min
                  </span>
                </div>
                <div class="creator">
                  👤 Créé par: {{ reservation.ownerId }}
                </div>
                <div class="players">
                  👥 Joueurs ({{ reservation.players.length }}/4):
                  {{ reservation.players.length > 0 ? reservation.players.join(', ') : 'Aucun joueur inscrit' }}
                </div>
              </div>
              
              <div class="reservation-actions">
                <template v-if="!isAlreadyRegistered(reservation)">
                  <button 
                    @click="handleRegister(reservation.id)"
                    class="action-btn register"
                    :disabled="reservationStore.loading || isPast(reservation.endTime) || isFull(reservation)"
                    :title="isPast(reservation.endTime) ? 'Réservation terminée' : isFull(reservation) ? 'Réservation complète' : ''"
                  >
                    S'inscrire
                  </button>
                </template>
                <template v-else>
                  <button 
                    @click="handleUnregister(reservation.id)"
                    class="action-btn unregister"
                    :disabled="reservationStore.loading || isPast(reservation.endTime)"
                    :title="isPast(reservation.endTime) ? 'Réservation terminée' : 'Se désinscrire de la réservation'"
                  >
                    Se désinscrire
                  </button>
                </template>
                <button 
                  class="action-btn view"
                  @click="toggleComments(reservation.id)"
                >
                  {{ showComments[reservation.id] ? 'Masquer commentaires' : 'Commentaires' }} ({{ reservation.comments.length }})
                </button>
                <button 
                  @click="viewReservation(reservation.id)"
                  class="action-btn view"
                >
                  Voir détails
                </button>
              </div>

              <div v-if="showComments[reservation.id]" class="comments-block">
                <div v-if="reservation.comments.length === 0" class="no-comments">Aucun commentaire.</div>
                <ul v-else class="comments-list">
                  <li v-for="(c, idx) in reservation.comments" :key="idx" class="comment-item">
                    <strong>{{ c.authorId }}</strong>: {{ c.content }}
                  </li>
                </ul>
                <form class="comment-form" @submit.prevent="submitComment(reservation.id)">
                  <input
                    type="text"
                    class="comment-input"
                    v-model="commentInputs[reservation.id]"
                    :placeholder="'Votre commentaire...'"
                    :disabled="loadingComment[reservation.id] || isPast(reservation.endTime)"
                  />
                  <button 
                    type="submit" 
                    class="comment-submit"
                    :disabled="loadingComment[reservation.id] || !commentInputs[reservation.id] || isPast(reservation.endTime)"
                  >
                    {{ loadingComment[reservation.id] ? 'Envoi...' : 'Envoyer' }}
                  </button>
                </form>
              </div>
            </div>
          </div>
        </div>

        <!-- Section Réservations terminées -->
        <div class="reservations-section">
          <h2>Réservations terminées</h2>
          
          <div v-if="reservationStore.completedReservations.length === 0" class="empty-state">
            Aucune réservation terminée
          </div>
          
          <div v-else class="reservations-grid">
            <div 
              v-for="reservation in reservationStore.completedReservations" 
              :key="reservation.id"
              class="reservation-card completed"
            >
              <div class="reservation-header">
                <h3>{{ reservation.title }}</h3>
                <span class="table-id">Table {{ reservation.tableId }}</span>
                <span class="completed-badge">✅ Terminé</span>
              </div>
              
              <div class="reservation-details">
                <div class="time-info">
                  <span class="start-time">
                    🕐 {{ formatDateTime(reservation.startTime) }}
                  </span>
                  <span class="duration">
                    ⏱️ {{ reservation.duration }}min
                  </span>
                </div>
                <div class="creator">
                  👤 Créé par: {{ reservation.ownerId }}
                </div>
                <div class="players">
                  👥 Inscrits: {{ reservation.players.length }}/4
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Modal de création de réservation -->
    <CreateReservationModal 
      v-if="showCreateModal"
      @close="showCreateModal = false"
      @created="handleReservationCreated"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useReservationStore } from '@/stores/reservations'
import CreateReservationModal from '@/components/CreateReservationModal.vue'
import type { ReservationUI } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
const reservationStore = useReservationStore()

const showCreateModal = ref(false)
// Etats pour les commentaires (réactifs)
const showComments = reactive<Record<string, boolean>>({})
const commentInputs = reactive<Record<string, string>>({})
const loadingComment = reactive<Record<string, boolean>>({})

onMounted(async () => {
  // Vérifier si l'utilisateur est connecté
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }
  
  // Charger les réservations
  await refreshReservations()
})

async function refreshReservations() {
  await reservationStore.fetchReservations()
  // Ouvrir automatiquement la zone commentaires si déjà existants
  for (const r of reservationStore.activeReservations) {
    if (r.comments && r.comments.length > 0 && showComments[r.id] === undefined) {
      showComments[r.id] = true
    }
  }
}


async function handleRegister(reservationId: string) {
  const success = await reservationStore.registerToReservation(reservationId)
  if (success) {
    // Actualiser les réservations pour refléter les changements
    await refreshReservations()
  }
}

async function handleUnregister(reservationId: string) {
  const success = await reservationStore.unregisterFromReservation(reservationId)
  if (success) {
    await refreshReservations()
  }
}

function viewReservation(reservationId: string) {
  router.push(`/reservations/${reservationId}`)
}

function handleReservationCreated() {
  showCreateModal.value = false
  refreshReservations()
}

function formatDateTime(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function isPast(endTime: string): boolean {
  return new Date(endTime) <= new Date()
}

function isFull(res: ReservationUI): boolean {
  // Max 4 joueurs selon le serveur
  return (res.players?.length || 0) >= 4
}

function isAlreadyRegistered(res: ReservationUI): boolean {
  const me = authStore.user?.login
  if (!me) return false
  // players contient les logins extraits des liens users/{login}
  return res.players.includes(me)
}

function toggleComments(reservationId: string) {
  showComments[reservationId] = !showComments[reservationId]
}

async function submitComment(reservationId: string) {
  const raw = commentInputs[reservationId]
  const content = (raw || '').trim()
  if (!content) return
  try {
    loadingComment[reservationId] = true
    const ok = await reservationStore.addComment(reservationId, { content })
    if (ok) {
      commentInputs[reservationId] = ''
      await refreshReservations()
      showComments[reservationId] = true
    }
  } finally {
    loadingComment[reservationId] = false
  }
}
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: var(--color-bg);
  color: var(--color-text);
}

/* En-tête de page interne */
.page-header { display:flex; flex-direction:column; gap:0.25rem; margin-bottom:1rem; }
.page-title { margin:0; font-size:1.4rem; color: var(--color-text); }
.page-sub { margin:0; font-size:0.9rem; color: var(--color-text-soft); }

.dashboard-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.quick-actions {
  background: var(--color-surface);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: var(--shadow-elev);
  border:1px solid var(--color-border);
}

.quick-actions h2 {
  margin: 0 0 1rem 0;
  color: var(--color-text);
  font-size: 1.25rem;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.action-button {
  padding: 0.75rem 1.5rem;
  border: 2px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-surface);
  color: var(--color-text-soft);
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
}

.action-button.primary {
  background: var(--color-accent);
  color: #fff;
  border-color: var(--color-accent);
}

.action-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.reservations-section {
  background: var(--color-surface);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: var(--shadow-elev);
  border:1px solid var(--color-border);
}

.reservations-section h2 {
  margin: 0 0 1.5rem 0;
  color: var(--color-text);
  font-size: 1.25rem;
}

.loading, .error, .empty-state {
  text-align: center;
  padding: 2rem;
  color: var(--color-text-soft);
}

.error {
  color: var(--color-danger);
}

.retry-button {
  margin-left: 1rem;
  background: var(--color-accent);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}

/* Layout cartes : passage à Flex pour éviter les colonnes fantômes de auto-fill */
.reservations-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
  align-items: stretch;
}

/* Carte avec largeur fixe pour remplir une ligne avant de retourner à la suivante */
.reservations-grid .reservation-card {
  flex: 0 0 350px; /* largeur fixe (ligne prend autant de cartes que possible) */
  max-width: 350px;
}

/* Responsive : sur petits écrans, on laisse la carte prendre toute la largeur */
@media (max-width: 600px) {
  .reservations-grid .reservation-card {
    flex: 1 1 100%;
    max-width: 100%;
  }
}

.reservation-card {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1.5rem;
  background: var(--color-surface);
  transition: box-shadow 0.2s ease;
}

.reservation-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.reservation-card.completed {
  opacity: 0.85;
  border-color: #68d391;
}

.action-btn.unregister {
  background: #f6ad55;
  border: none;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  cursor: pointer;
  color: #1a202c;
  font-weight: 500;
  transition: background-color 0.2s ease;
}

.action-btn.unregister:hover:not(:disabled) {
  background: #ed8936;
  color: #fff;
}

.comments-block {
  margin-top: 1rem;
  border-top: 1px solid #e2e8f0;
  padding-top: 1rem;
}
.comments-list {
  list-style: none;
  margin: 0 0 0.75rem 0;
  padding: 0;
  max-height: 160px;
  overflow-y: auto;
  font-size: 0.875rem;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}
.comment-item {
  background: var(--color-bg);
  padding: 0.5rem 0.6rem;
  border-radius: 4px;
  line-height: 1.2;
}
.comment-form {
  display: flex;
  gap: 0.5rem;
}
.comment-input {
  flex: 1;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  padding: 0.5rem 0.6rem;
  font-size: 0.85rem;
  background: var(--color-surface);
  color: var(--color-text);
}
.comment-input:disabled {
  background: var(--color-bg);
}
.comment-submit {
  background: var(--color-accent);
  border: none;
  color: #fff;
  padding: 0.5rem 0.9rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
}
.comment-submit:disabled {
  opacity: .6;
  cursor: not-allowed;
}
.no-comments {
  font-size: 0.75rem;
  color: var(--color-text-soft);
  margin-bottom: 0.5rem;
}

.reservation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.reservation-header h3 {
  margin: 0;
  color: var(--color-text);
  font-size: 1.1rem;
}

.table-id {
  background: #edf2f7;
  color: #4a5568;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.875rem;
  font-weight: 500;
}

.completed-badge {
  background: #c6f6d5;
  color: #22543d;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.875rem;
  font-weight: 500;
}

.full-badge {
  background: #fed7d7;
  color: #9b2c2c;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: .5px;
}

.reservation-details {
  margin-bottom: 1rem;
}

.description {
  color: var(--color-text-soft);
  margin: 0 0 0.75rem 0;
}

.time-info {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.5rem;
  flex-wrap: wrap;
}

.start-time, .duration, .creator, .players {
  color: var(--color-text-soft);
  font-size: 0.875rem;
  margin-bottom: 0.25rem;
}

.reservation-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.action-btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: background-color 0.2s ease;
}

.action-btn.register {
  background: #48bb78;
  color: white;
}

.action-btn.register:hover {
  background: #38a169;
}

.action-btn.view {
  background: #4299e1;
  color: white;
}

.action-btn.view:hover {
  background: #3182ce;
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
  
  .reservations-grid {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    justify-content: center;
  }
}
</style>
