<template>
  <section v-if="user" class="profile-page">
    <div class="app-container profile-layout">
      <header class="profile-header ui-card">
        <div class="identity">
          <div class="avatar" aria-hidden="true">{{ initials }}</div>
          <div>
            <p class="eyebrow">Mon espace</p>
            <h1>@{{ user.login }}</h1>
            <p class="subtitle">Gerez vos informations et vos preferences de compte.</p>
          </div>
        </div>

        <span class="ui-badge status-info">{{ roleLabel }}</span>
      </header>

      <section class="profile-content">
        <article class="ui-card panel">
          <h2>Informations</h2>
          <dl class="stats-grid">
            <div>
              <dt>Identifiant</dt>
              <dd>{{ user.login }}</dd>
            </div>
            <div>
              <dt>Reservations creees</dt>
              <dd>{{ user.ownedReservations?.length || 0 }}</dd>
            </div>
            <div>
              <dt>Participations</dt>
              <dd>{{ user.registeredReservations?.length || 0 }}</dd>
            </div>
          </dl>

          <div v-if="!editMode" class="actions-row">
            <button type="button" class="btn btn-secondary" @click="enableEdit">
              <KeyRound :size="16" aria-hidden="true" />
              <span>Changer le mot de passe</span>
            </button>
            <button type="button" class="btn btn-danger" @click="openDeleteDialog">
              <Trash2 :size="16" aria-hidden="true" />
              <span>Supprimer le compte</span>
            </button>
          </div>
        </article>

        <article v-if="editMode" class="ui-card panel">
          <h2>Modifier le mot de passe</h2>
          <form class="edit-form" @submit.prevent="save">
            <div class="field">
              <label for="password">Nouveau mot de passe</label>
              <input id="password" v-model="form.name" type="password" class="form-input" :disabled="saving" required autocomplete="new-password" />
            </div>

            <div class="actions-row">
              <button type="submit" class="btn btn-primary" :disabled="saving || !changed">{{ saving ? 'Enregistrement' : 'Enregistrer' }}</button>
              <button type="button" class="btn btn-secondary" @click="cancelEdit" :disabled="saving">Annuler</button>
            </div>

            <p v-if="error" class="error-message">{{ error }}</p>
            <p v-if="success" class="success-message">Mot de passe mis a jour.</p>
          </form>
        </article>
      </section>
    </div>

    <dialog ref="deleteDialog" class="confirm-dialog">
      <form method="dialog" class="confirm-box" @submit.prevent="confirmDelete">
        <h3>Supprimer le compte</h3>
        <p>Cette action est definitive. Voulez-vous supprimer votre compte ?</p>
        <div class="dialog-actions">
          <button type="button" class="btn btn-secondary" @click="closeDeleteDialog" :disabled="deleting">Annuler</button>
          <button type="submit" class="btn btn-danger" :disabled="deleting">{{ deleting ? 'Suppression' : 'Supprimer' }}</button>
        </div>
        <p v-if="deleteError" class="error-message">{{ deleteError }}</p>
      </form>
    </dialog>
  </section>

  <section v-else class="profile-page">
    <div class="app-container ui-card loading-panel">
      <p>Chargement du profil...</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { KeyRound, Trash2 } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { userService } from '@/services/users'

const authStore = useAuthStore()
const user = computed(() => authStore.user)

const editMode = ref(false)
const form = ref<{ name: string }>({ name: '' })
const saving = ref(false)
const success = ref(false)
const error = ref<string | null>(null)

const deleteDialog = ref<HTMLDialogElement | null>(null)
const deleting = ref(false)
const deleteError = ref<string | null>(null)

const changed = computed(() => !!(user.value && form.value.name.trim() && form.value.name.trim() !== user.value.name))
const initials = computed(() => (user.value?.login || 'me').slice(0, 2).toUpperCase())
const roleLabel = computed(() => (user.value?.login?.toLowerCase() === 'admin' ? 'Admin' : 'Membre'))

onMounted(async () => {
  if (!user.value) {
    await authStore.fetchCurrentUser()
  }
  form.value.name = ''
})

function enableEdit() {
  if (!user.value) return
  form.value.name = ''
  success.value = false
  error.value = null
  editMode.value = true
}

function cancelEdit() {
  if (saving.value) return
  editMode.value = false
  success.value = false
  error.value = null
  form.value.name = ''
}

async function save() {
  if (!user.value || !changed.value) return
  saving.value = true
  error.value = null
  success.value = false

  try {
    await userService.updateUser(user.value.login, { name: form.value.name.trim() })
    await authStore.fetchCurrentUser()
    success.value = true
    editMode.value = false
  } catch (requestError: unknown) {
    error.value = requestError instanceof Error ? requestError.message : 'Erreur lors de la mise a jour.'
  } finally {
    saving.value = false
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

async function confirmDelete() {
  if (!user.value) return
  deleting.value = true
  deleteError.value = null
  try {
    await userService.deleteUser(user.value.login)
    await authStore.logout()
    window.location.href = '/login'
  } catch (requestError: unknown) {
    deleteError.value = requestError instanceof Error ? requestError.message : 'Erreur lors de la suppression.'
  } finally {
    deleting.value = false
    closeDeleteDialog()
  }
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  padding: var(--space-8) 0 var(--space-10);
}

.profile-layout {
  display: grid;
  gap: var(--space-6);
}

.profile-header {
  padding: var(--space-6);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-4);
}

.identity {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.avatar {
  width: 3rem;
  height: 3rem;
  border-radius: 999px;
  display: grid;
  place-items: center;
  font-weight: 700;
  color: var(--color-primary-contrast);
  background: linear-gradient(140deg, var(--color-primary), var(--color-info));
}

.eyebrow {
  margin: 0 0 var(--space-2);
  text-transform: uppercase;
  font-size: 0.72rem;
  letter-spacing: 0.08em;
  color: var(--color-text-soft);
  font-weight: 700;
}

.profile-header h1 {
  margin: 0;
  font-family: var(--font-display);
  font-size: clamp(1.35rem, 1.8vw, 1.8rem);
}

.subtitle {
  margin: var(--space-2) 0 0;
  color: var(--color-text-soft);
}

.profile-content {
  display: grid;
  gap: var(--space-4);
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
}

.panel {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-4);
}

.panel h2 {
  margin: 0;
  font-size: 1.1rem;
}

.stats-grid {
  margin: 0;
  display: grid;
  gap: var(--space-2);
}

.stats-grid div {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
  background: var(--color-surface-2);
}

.stats-grid dt {
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-soft);
}

.stats-grid dd {
  margin: var(--space-1) 0 0;
  font-size: 0.95rem;
  font-weight: 700;
}

.field {
  display: grid;
  gap: var(--space-2);
}

.field label {
  font-size: 0.82rem;
  color: var(--color-text-soft);
  font-weight: 700;
}

.actions-row {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
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

.loading-panel {
  padding: var(--space-6);
}

@media (max-width: 900px) {
  .profile-page {
    padding-top: var(--space-6);
  }

  .profile-header {
    flex-direction: column;
  }
}

@media (max-width: 560px) {
  .identity {
    align-items: flex-start;
  }

  .actions-row {
    flex-direction: column;
  }

  .actions-row .btn {
    width: 100%;
  }
}
</style>