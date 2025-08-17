<template>
  <div class="reservation-detail" v-if="loaded && reservation">
    <header class="header">
      <h1>Réservation #{{ reservation.id }}</h1>
      <div class="header-actions">
        <button class="back-btn" @click="goBack">← Retour</button>
        <template v-if="isOwner">
          <button class="owner-btn" @click="goEdit" :disabled="past">✏️ Modifier</button>
          <button class="owner-btn danger" @click="openDeleteDialog" :disabled="deleting">🗑️ Supprimer</button>
        </template>
      </div>
    </header>
    <dialog ref="deleteDialog" class="confirm-dialog">
      <form method="dialog" class="confirm-box" @submit.prevent>
        <h3>Confirmer la suppression</h3>
        <p>Cette action est <strong>définitive</strong>. Continuer ?</p>
        <div class="dialog-actions">
          <button type="button" @click="closeDeleteDialog" :disabled="deleting">Annuler</button>
          <button type="button" class="danger" :disabled="deleting" @click="doDelete">Supprimer</button>
        </div>
        <p v-if="deleteError" class="error-msg">{{ deleteError }}</p>
      </form>
    </dialog>
    <section class="info-grid">
      <div class="card">
        <h2>Informations</h2>
        <ul class="props">
          <li><strong>Table:</strong> {{ reservation.tableId }}</li>
          <li><strong>Créateur:</strong> {{ reservation.ownerId }}</li>
          <li><strong>Début:</strong> {{ formatDateTime(reservation.startTime) }}</li>
          <li><strong>Fin:</strong> {{ formatDateTime(reservation.endTime) }}</li>
          <li><strong>Durée:</strong> {{ reservation.duration }} min</li>
          <li><strong>Statut:</strong> <span :class="{ badge: true, past: isPast(reservation.endTime) }">{{ isPast(reservation.endTime) ? 'Terminée' : 'Active' }}</span></li>
          <li><strong>Joueurs:</strong> {{ reservation.players.length }} / 4
            <span v-if="full && !past" class="badge full">Complet</span>
          </li>
        </ul>
      </div>

      <div class="card">
        <h2>Joueurs inscrits</h2>
        <p v-if="reservation.players.length === 0">Aucun joueur inscrit.</p>
        <ul v-else class="players">
          <li v-for="p in reservation.players" :key="p">{{ p }}</li>
        </ul>
        <div class="actions">
          <button 
            v-if="!isAlreadyRegistered" 
            @click="register" 
            :disabled="loadingAction || full || past"
            class="btn primary">
            S'inscrire
          </button>
          <button 
            v-else 
            @click="unregister"
            :disabled="loadingAction || past"
            class="btn warn">
            Se désinscrire
          </button>
        </div>
      </div>

      <div class="card span-2">
        <h2>Commentaires ({{ reservation.comments.length }})</h2>
        <div v-if="reservation.comments.length === 0" class="empty">Aucun commentaire.</div>
        <ul v-else class="comments">
          <li v-for="(c, i) in reservation.comments" :key="i">
            <strong>{{ c.authorId }}</strong>
            <span class="content">{{ c.content }}</span>
          </li>
        </ul>
        <form class="comment-form" @submit.prevent="submitComment">
          <input 
            v-model="comment" 
            type="text" 
            placeholder="Votre commentaire..." 
            :disabled="past || sendingComment"
          />
          <button 
            type="submit" 
            :disabled="!comment.trim() || sendingComment || past"
            class="btn secondary">
            {{ sendingComment ? 'Envoi...' : 'Envoyer' }}
          </button>
        </form>
      </div>
    </section>
  </div>
  <div v-else-if="error" class="reservation-detail error-state">
    <h2>Erreur</h2>
    <p>{{ error }}</p>
    <button class="btn primary" @click="reload">Réessayer</button>
  </div>
  <div v-else class="reservation-detail loading">
    <p>Chargement...</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useReservationStore } from '@/stores/reservations'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const reservationStore = useReservationStore()
const authStore = useAuthStore()

const id = route.params.id as string
const loaded = ref(false)
const error = ref<string | null>(null)
const comment = ref("")
const sendingComment = ref(false)
const loadingAction = ref(false)
const deleting = ref(false)
const deleteError = ref<string | null>(null)
const deleteDialog = ref<HTMLDialogElement | null>(null)

const reservation = computed(() => reservationStore.currentReservation)
const past = computed(() => reservation.value ? new Date(reservation.value.endTime) <= new Date() : false)
const full = computed(() => reservation.value ? reservation.value.players.length >= 4 : false)
const isAlreadyRegistered = computed(() => {
  const me = authStore.user?.login
  if (!me || !reservation.value) return false
  return reservation.value.players.includes(me)
})
const isOwner = computed(() => {
  const me = authStore.user?.login
  return !!me && !!reservation.value && reservation.value.ownerId === me
})

onMounted(async () => {
  await load()
})

async function load() {
  error.value = null
  loaded.value = false
  const res = await reservationStore.fetchReservation(id)
  if (!res) {
    error.value = 'Réservation introuvable'
  }
  loaded.value = true
}

function formatDateTime(d: string) {
  return new Date(d).toLocaleString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function isPast(d: string) {
  return new Date(d) <= new Date()
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

function reload() { load() }
function goBack() { router.back() }
function goEdit() { if (reservation.value) router.push(`/reservations/${reservation.value.id}/edit`) }

function openDeleteDialog() { deleteError.value = null; deleteDialog.value?.showModal() }
function closeDeleteDialog() { if (!deleting.value) deleteDialog.value?.close() }
async function doDelete() {
  if (!reservation.value) return
  deleting.value = true
  deleteError.value = null
  const ok = await reservationStore.deleteReservation(reservation.value.id)
  if (!ok) {
    deleteError.value = 'Erreur lors de la suppression'
  } else {
    closeDeleteDialog()
    router.push('/dashboard')
  }
  deleting.value = false
}
</script>

<style scoped>
.reservation-detail {
  max-width: 1100px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
  color: var(--color-text);
}
.loading, .error-state { text-align: center; }
.header { display:flex; justify-content: space-between; align-items: center; margin-bottom:1.5rem; }
.header h1 { margin:0; font-size:1.6rem; }
.back-btn { background:none; border:1px solid var(--color-border); padding:0.4rem 0.75rem; border-radius:6px; cursor:pointer; color: var(--color-text-soft); }
.header-actions { display:flex; gap:0.5rem; align-items:center; }
.owner-btn { background:#2d3748; color:#fff; border:none; padding:0.4rem 0.65rem; border-radius:6px; cursor:pointer; font-size:0.7rem; }
.owner-btn.danger { background:#e53e3e; }
.owner-btn:disabled { opacity:0.5; cursor:not-allowed; }
.info-grid { display:grid; gap:1.5rem; grid-template-columns: repeat(auto-fill,minmax(320px,1fr)); }
.card { background: var(--color-surface); border:1px solid var(--color-border); border-radius:10px; padding:1.25rem 1.25rem 1.5rem; box-shadow: var(--shadow-elev); position:relative; }
.card h2 { margin:0 0 0.75rem 0; font-size:1.1rem; }
.props { list-style:none; margin:0; padding:0; display:flex; flex-direction:column; gap:0.4rem; font-size:0.9rem; }
.players { list-style:none; margin:0.5rem 0 0 0; padding:0; display:flex; flex-wrap:wrap; gap:0.5rem; }
.players li { background:#33415522; padding:0.35rem 0.6rem; border-radius:4px; font-size:0.75rem; }
.badge { background:#3182ce; color:#fff; padding:0.2rem 0.5rem; border-radius:4px; font-size:0.65rem; text-transform:uppercase; letter-spacing:0.5px; }
.badge.past { background:#718096; }
.badge.full { background:#e53e3e; }
.comments { list-style:none; margin:0 0 1rem 0; padding:0; display:flex; flex-direction:column; gap:0.6rem; }
.comments li { background: var(--color-bg); padding:0.6rem 0.75rem; border-radius:6px; font-size:0.8rem; }
.comments li .content { margin-left:0.4rem; color: var(--color-text-soft); }
.comment-form { display:flex; gap:0.6rem; }
.comment-form input { flex:1; border:1px solid var(--color-border); padding:0.55rem 0.6rem; border-radius:6px; font-size:0.85rem; background: var(--color-bg); color: var(--color-text); }
.comment-form button { padding:0.55rem 0.9rem; border:none; background: var(--color-accent); color:#fff; border-radius:6px; cursor:pointer; font-size:0.8rem; }
.comment-form button:disabled { opacity:0.5; cursor:not-allowed; }
.actions { margin-top:0.75rem; display:flex; gap:0.75rem; }
.btn.primary { background:#3182ce; color:#fff; border:none; padding:0.5rem 0.9rem; border-radius:6px; cursor:pointer; font-size:0.8rem; }
.btn.warn { background:#f6ad55; color:#1a202c; border:none; padding:0.5rem 0.9rem; border-radius:6px; cursor:pointer; font-size:0.8rem; }
.btn.primary:disabled, .btn.warn:disabled { opacity:0.6; cursor:not-allowed; }
.span-2 { grid-column: 1 / -1; }
.empty { font-size:0.8rem; color:#718096; margin-bottom:0.5rem; }
.confirm-dialog { border:none; background:transparent; padding:0; }
.confirm-box { background: var(--color-surface); border:1px solid var(--color-border); padding:1.1rem 1.1rem 1.3rem; border-radius:10px; width:min(360px,90vw); display:flex; flex-direction:column; gap:0.85rem; }
.confirm-box h3 { margin:0; font-size:1rem; }
.dialog-actions { display:flex; justify-content:flex-end; gap:0.6rem; }
.dialog-actions button { border:none; background: var(--color-bg); color: var(--color-text); padding:0.45rem 0.75rem; border-radius:6px; cursor:pointer; font-size:0.7rem; }
.dialog-actions button.danger, .danger { background:#e53e3e; color:#fff; }
.error-msg { color: var(--color-danger); font-size:0.7rem; }
</style>
