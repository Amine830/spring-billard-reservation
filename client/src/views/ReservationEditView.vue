<template>
  <div class="reservation-edit" v-if="loaded && reservation">
    <header class="header">
      <h1>Modifier la réservation #{{ reservation.id }}</h1>
      <button class="back-btn" @click="goBack">← Retour</button>
    </header>

    <form class="edit-form" @submit.prevent="save">
      <div class="field">
        <label>Table</label>
        <input type="number" v-model.number="tableId" min="1" required />
      </div>
      <div class="field">
        <label>Date & heure de début</label>
        <input type="datetime-local" v-model="startLocal" required />
      </div>
      <div class="field">
        <label>Durée (minutes)</label>
        <input type="number" v-model.number="duration" min="15" step="15" required />
      </div>
      <div class="actions">
        <button type="submit" class="btn primary" :disabled="saving || !dirty">{{ saving ? 'Enregistrement...' : 'Enregistrer' }}</button>
        <button type="button" class="btn" @click="reset" :disabled="saving">Réinitialiser</button>
        <button type="button" class="btn danger" @click="confirmDelete" :disabled="deleting">{{ deleting ? 'Suppression...' : 'Supprimer' }}</button>
      </div>
      <p v-if="error" class="error">{{ error }}</p>
      <p v-if="success" class="success">Modifications enregistrées ✅</p>
    </form>

    <dialog ref="deleteDialog" class="confirm-dialog">
      <form method="dialog" class="confirm-box" @submit.prevent>
        <h3>Confirmer la suppression</h3>
        <p>Cette action est <strong>définitive</strong>. Continuer ?</p>
        <div class="dialog-actions">
          <button type="button" @click="closeDeleteDialog" :disabled="deleting">Annuler</button>
          <button type="button" class="danger" :disabled="deleting" @click="doDelete">Supprimer</button>
        </div>
        <p v-if="deleteError" class="error">{{ deleteError }}</p>
      </form>
    </dialog>
  </div>
  <div v-else-if="error" class="reservation-edit error-state">
    <h2>Erreur</h2>
    <p>{{ error }}</p>
    <button class="btn primary" @click="load">Réessayer</button>
  </div>
  <div v-else class="reservation-edit loading">
    <p>Chargement...</p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useReservationStore } from '@/stores/reservations'
import { useAuthStore } from '@/stores/auth'
import type { ReservationCreateRequest } from '@/types'

const route = useRoute()
const router = useRouter()
const reservationStore = useReservationStore()
const authStore = useAuthStore()

const id = route.params.id as string

const loaded = ref(false)
const error = ref<string | null>(null)
const success = ref(false)
const saving = ref(false)
const deleting = ref(false)
const deleteError = ref<string | null>(null)
const deleteDialog = ref<HTMLDialogElement | null>(null)

const reservation = computed(() => reservationStore.currentReservation)

const tableId = ref<number>(1)
const startLocal = ref('')
const duration = ref<number>(60)

const original = ref<{ tableId: number; startISO: string; endISO: string } | null>(null)
const dirty = computed(() => {
  if (!original.value) return false
  return (
    tableId.value !== original.value.tableId ||
    toIso(startLocal.value) !== original.value.startISO ||
    recomputeEndISO() !== original.value.endISO
  )
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
    loaded.value = true
    return
  }
  // Vérifier ownership
  const me = authStore.user?.login
  if (res.ownerId !== me) {
    error.value = 'Accès refusé (pas propriétaire)'
    loaded.value = true
    return
  }
  tableId.value = parseInt(res.tableId, 10)
  startLocal.value = toLocalInput(res.startTime)
  const startDate = new Date(res.startTime)
  const endDate = new Date(res.endTime)
  duration.value = Math.max(15, Math.round((endDate.getTime() - startDate.getTime()) / 60000))
  original.value = { tableId: tableId.value, startISO: res.startTime, endISO: res.endTime }
  loaded.value = true
}

function toLocalInput(iso: string) {
  const d = new Date(iso)
  const pad = (n: number) => n.toString().padStart(2,'0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}
function toIso(local: string) {
  // local est en timezone locale; créer ISO
  const d = new Date(local)
  return d.toISOString()
}
function recomputeEndISO() {
  const startIso = toIso(startLocal.value)
  const start = new Date(startIso)
  const end = new Date(start.getTime() + duration.value * 60000)
  return end.toISOString()
}

async function save() {
  if (!dirty.value || !reservation.value) return
  saving.value = true
  error.value = null
  success.value = false
  try {
    await reservationStore.updateReservation(reservation.value.id, {
      tableId: tableId.value,
      startTime: toIso(startLocal.value),
      duration: duration.value
    } as ReservationCreateRequest)
    success.value = true
    setTimeout(() => success.value = false, 3000)
    await load()
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : 'Erreur lors de la sauvegarde'
    error.value = msg
  } finally {
    saving.value = false
  }
}

function reset() {
  if (!original.value || !reservation.value) return
  tableId.value = original.value.tableId
  startLocal.value = toLocalInput(original.value.startISO)
  const startDate = new Date(original.value.startISO)
  const endDate = new Date(original.value.endISO)
  duration.value = Math.round((endDate.getTime() - startDate.getTime()) / 60000)
  success.value = false
  error.value = null
}

function confirmDelete() {
  deleteError.value = null
  deleteDialog.value?.showModal()
}
function closeDeleteDialog() { if (!deleting.value) deleteDialog.value?.close() }
async function doDelete() {
  if (!reservation.value) return
  deleting.value = true
  deleteError.value = null
  try {
    const ok = await reservationStore.deleteReservation(reservation.value.id)
    if (ok) {
      closeDeleteDialog()
      router.push('/dashboard')
    }
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : 'Erreur lors de la suppression'
    deleteError.value = msg
  } finally {
    deleting.value = false
  }
}

function goBack() { router.back() }
</script>

<style scoped>
.reservation-edit { max-width: 760px; margin:0 auto; padding:2rem 1.25rem 3rem; color: var(--color-text); }
.header { display:flex; justify-content:space-between; align-items:center; margin-bottom:1.5rem; }
.header h1 { margin:0; font-size:1.4rem; }
.back-btn { background:none; border:1px solid var(--color-border); padding:0.4rem 0.75rem; border-radius:6px; cursor:pointer; color: var(--color-text-soft); }
.edit-form { display:flex; flex-direction:column; gap:1.1rem; background: var(--color-surface); border:1px solid var(--color-border); padding:1.3rem 1.3rem 1.6rem; border-radius:10px; box-shadow: var(--shadow-elev); }
.field { display:flex; flex-direction:column; gap:0.4rem; }
.field label { font-size:0.75rem; text-transform:uppercase; letter-spacing:0.6px; font-weight:600; color: var(--color-text-soft); }
.field input { border:1px solid var(--color-border); background: var(--color-bg); color: var(--color-text); padding:0.55rem 0.65rem; border-radius:6px; font-size:0.85rem; }
.actions { display:flex; gap:0.75rem; flex-wrap:wrap; }
.btn { border:none; padding:0.55rem 0.9rem; border-radius:6px; cursor:pointer; font-size:0.8rem; font-weight:500; }
.btn.primary { background: var(--color-accent); color:#fff; }
.btn.danger { background:#e53e3e; color:#fff; }
.btn:disabled { opacity:0.55; cursor:not-allowed; }
.error { color: var(--color-danger); font-size:0.75rem; }
.success { color:#2f855a; font-size:0.75rem; }
.confirm-dialog { border:none; background:transparent; padding:0; }
.confirm-box { background: var(--color-surface); border:1px solid var(--color-border); padding:1.25rem 1.25rem 1.4rem; border-radius:10px; display:flex; flex-direction:column; gap:0.9rem; min-width:320px; }
.confirm-box h3 { margin:0; font-size:1.05rem; }
.dialog-actions { display:flex; justify-content:flex-end; gap:0.7rem; }
.dialog-actions button { border:none; background: var(--color-bg); color: var(--color-text); padding:0.5rem 0.8rem; border-radius:6px; cursor:pointer; font-size:0.75rem; }
.dialog-actions button.danger, .danger { background:#e53e3e; color:#fff; }
.loading, .error-state { text-align:center; padding:3rem 1rem; }
</style>
