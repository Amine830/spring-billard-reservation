<template>
  <section v-if="loaded && reservation" class="reservation-edit-page">
    <div class="app-container edit-layout">
      <header class="edit-header ui-card">
        <div>
          <p class="eyebrow">Edition</p>
          <h1>Modifier la reservation</h1>
          <p class="subtitle">Ajustez les details de planification en quelques champs.</p>
        </div>

        <button type="button" class="btn btn-secondary" @click="goBack">
          <ArrowLeft :size="16" aria-hidden="true" />
          <span>Retour</span>
        </button>
      </header>

      <form class="edit-form ui-card" @submit.prevent="save">
        <div class="field-grid">
          <div class="field">
            <label for="tableId">Table</label>
            <input id="tableId" v-model.number="tableId" type="number" class="form-input" min="1" required />
          </div>

          <div class="field">
            <label for="startTime">Debut</label>
            <input id="startTime" v-model="startLocal" type="datetime-local" class="form-input" required />
          </div>

          <div class="field">
            <label for="duration">Duree (minutes)</label>
            <input id="duration" v-model.number="duration" type="number" class="form-input" min="15" step="15" required />
          </div>
        </div>

        <div class="actions-row">
          <button type="submit" class="btn btn-primary" :disabled="saving || !dirty">{{ saving ? 'Enregistrement' : 'Enregistrer' }}</button>
          <button type="button" class="btn btn-secondary" @click="reset" :disabled="saving">Annuler les changements</button>
          <button type="button" class="btn btn-danger" @click="confirmDelete" :disabled="deleting">{{ deleting ? 'Suppression' : 'Supprimer' }}</button>
        </div>

        <p v-if="error" class="error-message">{{ error }}</p>
        <p v-if="success" class="success-message">Modifications enregistrees.</p>
      </form>
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

  <section v-else-if="error" class="reservation-edit-page">
    <div class="app-container ui-card fallback-panel">
      <h2>Erreur</h2>
      <p>{{ error }}</p>
      <button type="button" class="btn btn-primary" @click="load">Reessayer</button>
    </div>
  </section>

  <section v-else class="reservation-edit-page">
    <div class="app-container ui-card fallback-panel">
      <p>Chargement...</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from 'lucide-vue-next'
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

const tableId = ref(1)
const startLocal = ref('')
const duration = ref(60)

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

  const response = await reservationStore.fetchReservation(id)
  if (!response) {
    error.value = 'Reservation introuvable.'
    loaded.value = true
    return
  }

  const currentLogin = authStore.user?.login
  if (response.ownerId !== currentLogin) {
    error.value = 'Acces refuse. Vous n etes pas proprietaire.'
    loaded.value = true
    return
  }

  tableId.value = parseInt(response.tableId, 10)
  startLocal.value = toLocalInput(response.startTime)
  const startDate = new Date(response.startTime)
  const endDate = new Date(response.endTime)
  duration.value = Math.max(15, Math.round((endDate.getTime() - startDate.getTime()) / 60000))

  original.value = {
    tableId: tableId.value,
    startISO: response.startTime,
    endISO: response.endTime
  }

  loaded.value = true
}

function toLocalInput(iso: string) {
  const date = new Date(iso)
  const pad = (value: number) => value.toString().padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function toIso(local: string) {
  return new Date(local).toISOString()
}

function recomputeEndISO() {
  const startIso = toIso(startLocal.value)
  const startDate = new Date(startIso)
  const endDate = new Date(startDate.getTime() + duration.value * 60000)
  return endDate.toISOString()
}

async function save() {
  if (!dirty.value || !reservation.value) return
  saving.value = true
  error.value = null
  success.value = false

  try {
    await reservationStore.updateReservation(
      reservation.value.id,
      {
        tableId: tableId.value,
        startTime: toIso(startLocal.value),
        duration: duration.value
      } as ReservationCreateRequest
    )

    success.value = true
    setTimeout(() => {
      success.value = false
    }, 3000)

    await load()
  } catch (requestError: unknown) {
    error.value = requestError instanceof Error ? requestError.message : 'Erreur lors de la sauvegarde.'
  } finally {
    saving.value = false
  }
}

function reset() {
  if (!original.value) return
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

function closeDeleteDialog() {
  if (!deleting.value) {
    deleteDialog.value?.close()
  }
}

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
  } catch (requestError: unknown) {
    deleteError.value = requestError instanceof Error ? requestError.message : 'Erreur lors de la suppression.'
  } finally {
    deleting.value = false
  }
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.reservation-edit-page {
  min-height: 100vh;
  padding: var(--space-8) 0 var(--space-10);
}

.edit-layout {
  display: grid;
  gap: var(--space-6);
}

.edit-header {
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

.edit-header h1 {
  margin: 0;
  font-family: var(--font-display);
  font-size: clamp(1.35rem, 1.8vw, 1.8rem);
}

.subtitle {
  margin: var(--space-2) 0 0;
  color: var(--color-text-soft);
}

.edit-form {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: var(--space-3);
}

.field {
  display: grid;
  gap: var(--space-2);
}

.field label {
  font-size: 0.82rem;
  color: var(--color-text-soft);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.actions-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
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

@media (max-width: 900px) {
  .reservation-edit-page {
    padding-top: var(--space-6);
  }

  .edit-header {
    flex-direction: column;
  }
}

@media (max-width: 640px) {
  .actions-row {
    flex-direction: column;
  }

  .actions-row .btn {
    width: 100%;
  }
}
</style>