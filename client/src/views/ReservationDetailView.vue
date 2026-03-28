<template>
  <section v-if="loaded && reservation" class="reservation-detail-page">
    <div class="app-container detail-layout">
      <header class="detail-header ui-card">
        <div>
          <p class="eyebrow">Reservation</p>
          <h1>{{ reservation.title || `Reservation ${reservation.id}` }}</h1>
          <p class="subtitle">Consultez les details, les participants et les notes associees.</p>
        </div>

        <div class="header-actions">
          <button type="button" class="btn btn-secondary" @click="goBack">
            <ArrowLeft :size="16" aria-hidden="true" />
            <span>Retour</span>
          </button>

          <button v-if="isOwner" type="button" class="btn btn-secondary" @click="goEdit" :disabled="past">
            <PencilLine :size="16" aria-hidden="true" />
            <span>Modifier</span>
          </button>

          <button v-if="isOwner" type="button" class="btn btn-danger" @click="openDeleteDialog" :disabled="deleting">
            <Trash2 :size="16" aria-hidden="true" />
            <span>Supprimer</span>
          </button>
        </div>
      </header>

      <section class="grid-panels">
        <article class="ui-card panel">
          <div class="panel-head">
            <h2>Informations</h2>
            <span class="ui-badge" :class="past ? 'status-info' : 'status-success'">{{ past ? 'Terminee' : 'Active' }}</span>
          </div>

          <dl class="metadata-grid">
            <div>
              <dt>Table</dt>
              <dd>{{ reservation.tableId }}</dd>
            </div>
            <div>
              <dt>Proprietaire</dt>
              <dd>{{ reservation.ownerId }}</dd>
            </div>
            <div>
              <dt>Debut</dt>
              <dd>{{ formatDateTime(reservation.startTime) }}</dd>
            </div>
            <div>
              <dt>Fin</dt>
              <dd>{{ formatDateTime(reservation.endTime) }}</dd>
            </div>
            <div>
              <dt>Duree</dt>
              <dd>{{ reservation.duration }} min</dd>
            </div>
            <div>
              <dt>Places</dt>
              <dd>
                {{ reservation.players.length }}/4
                <span v-if="full && !past" class="ui-badge status-danger">Complet</span>
              </dd>
            </div>
          </dl>
        </article>

        <article class="ui-card panel">
          <div class="panel-head">
            <h2>Participants</h2>
            <span class="count-pill">{{ reservation.players.length }}</span>
          </div>

          <p v-if="reservation.players.length === 0" class="state-text">Aucun joueur inscrit.</p>

          <ul v-else class="player-list">
            <li v-for="player in reservation.players" :key="player">{{ player }}</li>
          </ul>

          <div class="panel-actions">
            <button
              v-if="!isAlreadyRegistered"
              type="button"
              class="btn btn-primary"
              @click="register"
              :disabled="loadingAction || full || past"
            >
              Rejoindre
            </button>

            <button
              v-else
              type="button"
              class="btn btn-secondary"
              @click="unregister"
              :disabled="loadingAction || past"
            >
              Quitter
            </button>
          </div>
        </article>

        <article class="ui-card panel comments-panel">
          <div class="panel-head">
            <h2>Notes</h2>
            <span class="count-pill">{{ reservation.comments.length }}</span>
          </div>

          <p v-if="reservation.comments.length === 0" class="state-text">Aucune note pour le moment.</p>

          <ul v-else class="comment-list">
            <li v-for="(commentItem, index) in reservation.comments" :key="`${reservation.id}-${index}`">
              <strong>{{ commentItem.authorId }}</strong>
              <span>{{ commentItem.content }}</span>
            </li>
          </ul>

          <form class="comment-form" @submit.prevent="submitComment">
            <label class="sr-only" for="commentInput">Ajouter une note</label>
            <input id="commentInput" v-model="comment" type="text" class="form-input" placeholder="Ajouter une note" :disabled="past || sendingComment" />
            <button type="submit" class="btn btn-primary" :disabled="!comment.trim() || sendingComment || past">
              {{ sendingComment ? 'Envoi' : 'Publier' }}
            </button>
          </form>
        </article>
      </section>
    </div>

    <dialog ref="deleteDialog" class="confirm-dialog">
      <form method="dialog" class="confirm-box" @submit.prevent>
        <h3>Supprimer la reservation</h3>
        <p>Cette action est definitive. Voulez-vous continuer ?</p>
        <div class="dialog-actions">
          <button type="button" class="btn btn-secondary" @click="closeDeleteDialog" :disabled="deleting">Annuler</button>
          <button type="button" class="btn btn-danger" @click="doDelete" :disabled="deleting">Supprimer</button>
        </div>
        <p v-if="deleteError" class="error-message">{{ deleteError }}</p>
      </form>
    </dialog>
  </section>

  <section v-else-if="error" class="reservation-detail-page">
    <div class="app-container ui-card fallback-panel">
      <h2>Erreur</h2>
      <p>{{ error }}</p>
      <button type="button" class="btn btn-primary" @click="reload">Reessayer</button>
    </div>
  </section>

  <section v-else class="reservation-detail-page">
    <div class="app-container ui-card fallback-panel">
      <p>Chargement...</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, PencilLine, Trash2 } from 'lucide-vue-next'
import { useReservationStore } from '@/stores/reservations'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const reservationStore = useReservationStore()
const authStore = useAuthStore()

const id = route.params.id as string
const loaded = ref(false)
const error = ref<string | null>(null)
const comment = ref('')
const sendingComment = ref(false)
const loadingAction = ref(false)
const deleting = ref(false)
const deleteError = ref<string | null>(null)
const deleteDialog = ref<HTMLDialogElement | null>(null)

const reservation = computed(() => reservationStore.currentReservation)
const past = computed(() => (reservation.value ? new Date(reservation.value.endTime) <= new Date() : false))
const full = computed(() => (reservation.value ? reservation.value.players.length >= 4 : false))

const isAlreadyRegistered = computed(() => {
  const currentLogin = authStore.user?.login
  if (!currentLogin || !reservation.value) return false
  return reservation.value.players.includes(currentLogin)
})

const isOwner = computed(() => {
  const currentLogin = authStore.user?.login
  return !!currentLogin && !!reservation.value && reservation.value.ownerId === currentLogin
})

onMounted(async () => {
  await load()
})

async function load() {
  error.value = null
  loaded.value = false
  const response = await reservationStore.fetchReservation(id)
  if (!response) {
    error.value = 'Reservation introuvable.'
  }
  loaded.value = true
}

function formatDateTime(value: string) {
  return new Date(value).toLocaleString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

async function register() {
  if (!reservation.value) return
  loadingAction.value = true
  const ok = await reservationStore.registerToReservation(reservation.value.id)
  if (ok) await load()
  loadingAction.value = false
}

async function unregister() {
  if (!reservation.value) return
  loadingAction.value = true
  const ok = await reservationStore.unregisterFromReservation(reservation.value.id)
  if (ok) await load()
  loadingAction.value = false
}

async function submitComment() {
  if (!reservation.value) return
  const content = comment.value.trim()
  if (!content) return
  sendingComment.value = true
  const ok = await reservationStore.addComment(reservation.value.id, { content })
  if (ok) {
    comment.value = ''
    await load()
  }
  sendingComment.value = false
}

function reload() {
  load()
}

function goBack() {
  router.back()
}

function goEdit() {
  if (reservation.value) {
    router.push(`/reservations/${reservation.value.id}/edit`)
  }
}

function openDeleteDialog() {
  deleteError.value = null
  deleteDialog.value?.showModal()
}

function closeDeleteDialog() {
  if (!deleting.value) {
    deleteDialog.value?.close()
  }
}

async function doDelete() {
  if (!reservation.value) return
  deleting.value = true
  deleteError.value = null
  const ok = await reservationStore.deleteReservation(reservation.value.id)
  if (!ok) {
    deleteError.value = 'Erreur lors de la suppression.'
  } else {
    closeDeleteDialog()
    router.push('/dashboard')
  }
  deleting.value = false
}
</script>

<style scoped>
.reservation-detail-page {
  min-height: 100vh;
  padding: var(--space-8) 0 var(--space-10);
}

.detail-layout {
  display: grid;
  gap: var(--space-6);
}

.detail-header {
  padding: var(--space-6);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-6);
}

.eyebrow {
  margin: 0 0 var(--space-2);
  text-transform: uppercase;
  font-size: 0.72rem;
  letter-spacing: 0.08em;
  color: var(--color-text-soft);
  font-weight: 700;
}

.detail-header h1 {
  margin: 0;
  font-family: var(--font-display);
  font-size: clamp(1.35rem, 1.8vw, 1.8rem);
}

.subtitle {
  margin: var(--space-2) 0 0;
  color: var(--color-text-soft);
}

.header-actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.grid-panels {
  display: grid;
  gap: var(--space-4);
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
}

.panel {
  padding: var(--space-4);
}

.comments-panel {
  grid-column: 1 / -1;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-3);
}

.panel-head h2 {
  margin: 0;
  font-size: 1.1rem;
}

.count-pill {
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: 999px;
  padding: 0.2rem 0.55rem;
  font-size: 0.8rem;
  color: var(--color-text-soft);
  font-weight: 700;
}

.metadata-grid {
  margin: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-2);
}

.metadata-grid div {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
  background: var(--color-surface-2);
}

.metadata-grid dt {
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-soft);
}

.metadata-grid dd {
  margin: var(--space-1) 0 0;
  font-weight: 600;
}

.player-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.player-list li {
  border: 1px solid var(--color-border);
  background: var(--color-surface-2);
  padding: 0.3rem 0.6rem;
  border-radius: 999px;
  font-size: 0.82rem;
}

.panel-actions {
  margin-top: var(--space-3);
  display: flex;
  gap: var(--space-2);
}

.state-text {
  color: var(--color-text-soft);
  margin: 0;
}

.comment-list {
  list-style: none;
  margin: 0 0 var(--space-3);
  padding: 0;
  display: grid;
  gap: var(--space-2);
}

.comment-list li {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
  background: var(--color-surface-2);
  display: grid;
  gap: var(--space-1);
  font-size: 0.85rem;
}

.comment-list strong {
  font-size: 0.75rem;
  color: var(--color-text-soft);
}

.comment-form {
  display: flex;
  gap: var(--space-2);
  align-items: center;
}

.comment-form .form-input {
  flex: 1;
}

.confirm-dialog {
  border: none;
  background: transparent;
  padding: 0;
}

.confirm-box {
  width: min(26rem, 90vw);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  padding: var(--space-5);
  display: grid;
  gap: var(--space-3);
}

.confirm-box h3,
.confirm-box p {
  margin: 0;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
}

.fallback-panel {
  padding: var(--space-6);
  display: grid;
  gap: var(--space-3);
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
  .reservation-detail-page {
    padding-top: var(--space-6);
  }

  .detail-header {
    flex-direction: column;
  }
}

@media (max-width: 640px) {
  .metadata-grid {
    grid-template-columns: 1fr;
  }

  .comment-form {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>