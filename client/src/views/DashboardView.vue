<template>
  <section class="dashboard-page">
    <div class="app-container dashboard-layout">
      <header class="dashboard-header ui-card">
        <div>
          <p class="eyebrow">Espace reservations</p>
          <h1>Tableau de bord</h1>
          <p class="subtitle">Consultez les parties en cours et gelez vos inscriptions rapidement.</p>
        </div>

        <div class="header-meta">
          <span class="ui-badge status-info">
            <ShieldCheck :size="14" aria-hidden="true" />
            {{ roleLabel }}
          </span>
          <span class="ui-badge status-success">
            <Clock3 :size="14" aria-hidden="true" />
            {{ reservationStore.activeReservations.length }} actives
          </span>
        </div>
      </header>

      <section class="quick-actions ui-card" aria-label="Actions rapides">
        <button type="button" class="btn btn-primary" @click="showCreateModal = true">
          <Plus :size="16" aria-hidden="true" />
          <span>Creer</span>
        </button>

        <button type="button" class="btn btn-secondary" @click="refreshReservations" :disabled="reservationStore.loading">
          <RefreshCw :size="16" aria-hidden="true" />
          <span>Actualiser</span>
        </button>

        <router-link v-if="isAdmin" to="/users" class="btn btn-secondary">
          <Users :size="16" aria-hidden="true" />
          <span>Utilisateurs</span>
        </router-link>
      </section>

      <section class="reservations-block ui-card" aria-labelledby="active-reservations-title">
        <div class="section-head">
          <h2 id="active-reservations-title">Reservations actives</h2>
          <span class="count-pill">{{ reservationStore.activeReservations.length }}</span>
        </div>

        <p v-if="reservationStore.loading" class="state-msg">Chargement des reservations...</p>

        <div v-else-if="reservationStore.error" class="error-message">
          <p>{{ reservationStore.error }}</p>
          <button type="button" class="btn btn-secondary" @click="refreshReservations">Reessayer</button>
        </div>

        <p v-else-if="reservationStore.activeReservations.length === 0" class="state-msg">Aucune reservation active.</p>

        <div v-else class="reservations-grid">
          <article v-for="reservation in reservationStore.activeReservations" :key="reservation.id" class="reservation-card ui-card">
            <header class="card-head">
              <h3>{{ reservation.title }}</h3>
              <div class="head-badges">
                <span class="ui-badge status-info">Table {{ reservation.tableId }}</span>
                <span v-if="isFull(reservation) && !isPast(reservation.endTime)" class="ui-badge status-danger">Complet</span>
              </div>
            </header>

            <dl class="reservation-metadata">
              <div>
                <dt>Debut</dt>
                <dd>{{ formatDateTime(reservation.startTime) }}</dd>
              </div>
              <div>
                <dt>Duree</dt>
                <dd>{{ reservation.duration }} min</dd>
              </div>
              <div>
                <dt>Proprietaire</dt>
                <dd>{{ reservation.ownerId }}</dd>
              </div>
              <div>
                <dt>Joueurs</dt>
                <dd>{{ reservation.players.length }}/4</dd>
              </div>
            </dl>

            <p class="players-line">
              {{ reservation.players.length > 0 ? reservation.players.join(', ') : 'Aucun joueur inscrit' }}
            </p>

            <div class="reservation-actions">
              <button
                v-if="!isAlreadyRegistered(reservation)"
                type="button"
                class="btn btn-primary"
                @click="handleRegister(reservation.id)"
                :disabled="reservationStore.loading || isPast(reservation.endTime) || isFull(reservation)"
              >
                Rejoindre
              </button>

              <button
                v-else
                type="button"
                class="btn btn-secondary"
                @click="handleUnregister(reservation.id)"
                :disabled="reservationStore.loading || isPast(reservation.endTime)"
              >
                Quitter
              </button>

              <button type="button" class="btn btn-secondary" @click="toggleComments(reservation.id)">
                {{ showComments[reservation.id] ? 'Fermer notes' : 'Notes' }}
              </button>

              <button type="button" class="btn btn-secondary" @click="viewReservation(reservation.id)">
                Details
              </button>
            </div>

            <section v-if="showComments[reservation.id]" class="comments-block" :aria-label="`Notes pour la reservation ${reservation.id}`">
              <p v-if="reservation.comments.length === 0" class="notes-empty">Aucune note.</p>

              <ul v-else class="notes-list">
                <li v-for="(comment, idx) in reservation.comments" :key="`${reservation.id}-${idx}`">
                  <strong>{{ comment.authorId }}</strong>
                  <span>{{ comment.content }}</span>
                </li>
              </ul>

              <form class="note-form" @submit.prevent="submitComment(reservation.id)">
                <label class="sr-only" :for="`note-${reservation.id}`">Ajouter une note</label>
                <input
                  :id="`note-${reservation.id}`"
                  v-model="commentInputs[reservation.id]"
                  type="text"
                  class="form-input"
                  placeholder="Ajouter une note"
                  :disabled="loadingComment[reservation.id] || isPast(reservation.endTime)"
                />
                <button
                  type="submit"
                  class="btn btn-primary"
                  :disabled="loadingComment[reservation.id] || !commentInputs[reservation.id] || isPast(reservation.endTime)"
                >
                  {{ loadingComment[reservation.id] ? 'Envoi' : 'Publier' }}
                </button>
              </form>
            </section>
          </article>
        </div>
      </section>

      <section class="reservations-block ui-card" aria-labelledby="completed-reservations-title">
        <div class="section-head">
          <h2 id="completed-reservations-title">Reservations terminees</h2>
          <span class="count-pill">{{ reservationStore.completedReservations.length }}</span>
        </div>

        <p v-if="reservationStore.completedReservations.length === 0" class="state-msg">Aucune reservation terminee.</p>

        <div v-else class="reservations-grid">
          <article v-for="reservation in reservationStore.completedReservations" :key="reservation.id" class="reservation-card ui-card done-card">
            <header class="card-head">
              <h3>{{ reservation.title }}</h3>
              <span class="ui-badge status-success">Terminee</span>
            </header>

            <dl class="reservation-metadata">
              <div>
                <dt>Debut</dt>
                <dd>{{ formatDateTime(reservation.startTime) }}</dd>
              </div>
              <div>
                <dt>Duree</dt>
                <dd>{{ reservation.duration }} min</dd>
              </div>
              <div>
                <dt>Table</dt>
                <dd>{{ reservation.tableId }}</dd>
              </div>
              <div>
                <dt>Participants</dt>
                <dd>{{ reservation.players.length }}/4</dd>
              </div>
            </dl>
          </article>
        </div>
      </section>
    </div>

    <CreateReservationModal v-if="showCreateModal" @close="showCreateModal = false" @created="handleReservationCreated" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Clock3, Plus, RefreshCw, ShieldCheck, Users } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { useReservationStore } from '@/stores/reservations'
import CreateReservationModal from '@/components/CreateReservationModal.vue'
import type { ReservationUI } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
const reservationStore = useReservationStore()

const showCreateModal = ref(false)
const showComments = reactive<Record<string, boolean>>({})
const commentInputs = reactive<Record<string, string>>({})
const loadingComment = reactive<Record<string, boolean>>({})

const isAdmin = computed(() => authStore.user?.login?.toLowerCase() === 'admin')
const roleLabel = computed(() => (isAdmin.value ? 'Admin' : 'Membre'))

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }

  await refreshReservations()
})

async function refreshReservations() {
  await reservationStore.fetchReservations()

  for (const reservation of reservationStore.activeReservations) {
    if (reservation.comments.length > 0 && showComments[reservation.id] === undefined) {
      showComments[reservation.id] = true
    }
  }
}

async function handleRegister(reservationId: string) {
  const success = await reservationStore.registerToReservation(reservationId)
  if (success) {
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

function isFull(reservation: ReservationUI): boolean {
  return reservation.players.length >= 4
}

function isAlreadyRegistered(reservation: ReservationUI): boolean {
  const currentLogin = authStore.user?.login
  if (!currentLogin) return false
  return reservation.players.includes(currentLogin)
}

function toggleComments(reservationId: string) {
  showComments[reservationId] = !showComments[reservationId]
}

async function submitComment(reservationId: string) {
  const content = (commentInputs[reservationId] || '').trim()
  if (!content) return

  try {
    loadingComment[reservationId] = true
    const ok = await reservationStore.addComment(reservationId, { content })
    if (ok) {
      commentInputs[reservationId] = ''
      showComments[reservationId] = true
      await refreshReservations()
    }
  } finally {
    loadingComment[reservationId] = false
  }
}
</script>

<style scoped>
.dashboard-page {
  min-height: 100vh;
  padding: var(--space-8) 0 var(--space-10);
}

.dashboard-layout {
  display: grid;
  gap: var(--space-6);
}

.dashboard-header {
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

.dashboard-header h1 {
  margin: 0;
  font-family: var(--font-display);
  font-size: clamp(1.5rem, 1.8vw, 1.9rem);
}

.subtitle {
  margin: var(--space-2) 0 0;
  color: var(--color-text-soft);
}

.header-meta {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.quick-actions {
  padding: var(--space-4);
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.reservations-block {
  padding: var(--space-5);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-5);
}

.section-head h2 {
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

.state-msg {
  margin: 0;
  color: var(--color-text-soft);
}

.reservations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--space-4);
}

.reservation-card {
  padding: var(--space-4);
  border-radius: var(--radius-lg);
}

.card-head {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
  align-items: flex-start;
  margin-bottom: var(--space-3);
}

.card-head h3 {
  margin: 0;
  font-size: 1rem;
}

.head-badges {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  justify-content: flex-end;
}

.reservation-metadata {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-3);
  margin: 0;
}

.reservation-metadata div {
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
}

.reservation-metadata dt {
  font-size: 0.72rem;
  color: var(--color-text-soft);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.reservation-metadata dd {
  margin: var(--space-1) 0 0;
  font-size: 0.87rem;
  font-weight: 600;
}

.players-line {
  margin: var(--space-3) 0;
  color: var(--color-text-soft);
  font-size: 0.87rem;
}

.reservation-actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.comments-block {
  margin-top: var(--space-4);
  border-top: 1px solid var(--color-border);
  padding-top: var(--space-3);
}

.notes-empty {
  color: var(--color-text-soft);
  margin: 0 0 var(--space-2);
  font-size: 0.85rem;
}

.notes-list {
  list-style: none;
  margin: 0 0 var(--space-3);
  padding: 0;
  display: grid;
  gap: var(--space-2);
}

.notes-list li {
  border: 1px solid var(--color-border);
  background: var(--color-surface-2);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
  font-size: 0.85rem;
  display: grid;
  gap: var(--space-1);
}

.notes-list strong {
  font-size: 0.75rem;
  color: var(--color-text-soft);
}

.note-form {
  display: flex;
  gap: var(--space-2);
  align-items: center;
}

.note-form .form-input {
  flex: 1;
}

.done-card {
  opacity: 0.9;
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
  .dashboard-page {
    padding-top: var(--space-6);
  }

  .dashboard-header {
    flex-direction: column;
  }
}

@media (max-width: 640px) {
  .note-form {
    flex-direction: column;
    align-items: stretch;
  }

  .reservation-metadata {
    grid-template-columns: 1fr;
  }
}
</style>