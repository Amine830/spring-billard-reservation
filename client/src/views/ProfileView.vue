<template>
  <div class="profile-view" v-if="user">
    <header class="profile-header">
      <h1>Mon profil</h1>
      <p class="subtitle">Gérez vos informations personnelles</p>
    </header>

    <main class="profile-content">
      <section class="card info-card">
        <h2>Informations</h2>
        <ul class="info-list">
          <li><strong>Identifiant (login):</strong> {{ user.login }}</li>
          <li><strong>Réservations créées:</strong> {{ user.ownedReservations?.length || 0 }}</li>
          <li><strong>Participations:</strong> {{ user.registeredReservations?.length || 0 }}</li>
        </ul>
        <div class="actions" v-if="!editMode">
          <button class="btn primary" @click="enableEdit">✏️ Modifier le mot de passe</button>
          <button class="btn danger" @click="openDeleteDialog">🗑️ Supprimer mon compte</button>
        </div>
      </section>

      <section class="card edit-card" v-if="editMode">
        <h2>Changer le mot de passe</h2>
        <form @submit.prevent="save" class="edit-form">
          <div class="form-group">
            <label for="name">Nouveau mot de passe</label>
            <input id="name" v-model="form.name" type="password" :disabled="saving" required autocomplete="new-password" />
          </div>
          <div class="form-actions">
            <button type="submit" class="btn primary" :disabled="saving || !changed">{{ saving ? 'Enregistrement...' : 'Enregistrer' }}</button>
            <button type="button" class="btn" @click="cancelEdit" :disabled="saving">Annuler</button>
          </div>
          <p v-if="error" class="error-msg">{{ error }}</p>
          <p v-if="success" class="success-msg">Mot de passe mis à jour ✅</p>
        </form>
      </section>

      <dialog ref="deleteDialog" class="confirm-dialog">
        <form method="dialog" class="confirm-box" @submit.prevent="confirmDelete">
          <h3>Confirmer la suppression</h3>
          <p>Cette action est <strong>définitive</strong>. Voulez-vous vraiment supprimer votre compte ?</p>
          <div class="confirm-actions">
            <button type="button" class="btn" @click="closeDeleteDialog" :disabled="deleting">Annuler</button>
            <button type="submit" class="btn danger" :disabled="deleting">{{ deleting ? 'Suppression...' : 'Oui, supprimer' }}</button>
          </div>
          <p v-if="deleteError" class="error-msg">{{ deleteError }}</p>
        </form>
      </dialog>
    </main>
  </div>
  <div v-else class="profile-view loading-state">
    <p>Chargement du profil...</p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
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

const changed = computed(() => user.value && form.value.name.trim() && form.value.name.trim() !== user.value.name)

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
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : 'Erreur lors de la mise à jour'
    error.value = msg
  } finally {
    saving.value = false
  }
}

function openDeleteDialog() {
  deleteError.value = null
  deleteDialog.value?.showModal()
}

function closeDeleteDialog() {
  if (deleting.value) return
  deleteDialog.value?.close()
}

async function confirmDelete() {
  if (!user.value) return
  deleting.value = true
  deleteError.value = null
  try {
    await userService.deleteUser(user.value.login)
    await authStore.logout()
    window.location.href = '/login'
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : 'Erreur lors de la suppression'
    deleteError.value = msg
  } finally {
    deleting.value = false
    closeDeleteDialog()
  }
}
</script>

<style scoped>
.profile-view { max-width: 860px; margin: 0 auto; padding: 2rem 1rem 3rem; color: var(--color-text); }
.profile-header { text-align: center; margin-bottom: 2rem; }
.profile-header h1 { margin:0; font-size:2rem; }
.subtitle { margin:0.25rem 0 0; color: var(--color-text-soft); }
.profile-content { display:grid; gap:1.5rem; grid-template-columns: repeat(auto-fill,minmax(320px,1fr)); }
.card { background: var(--color-surface); border:1px solid var(--color-border); border-radius:12px; padding:1.25rem 1.25rem 1.5rem; box-shadow: var(--shadow-elev); position:relative; }
.card h2 { margin:0 0 0.75rem; font-size:1.1rem; }
.info-list { list-style:none; margin:0 0 1rem; padding:0; display:flex; flex-direction:column; gap:0.45rem; font-size:0.9rem; }
.actions { display:flex; gap:0.6rem; }
.btn { border:1px solid var(--color-border); background: var(--color-bg); padding:0.55rem 0.9rem; border-radius:6px; cursor:pointer; font-size:0.8rem; }
.btn.primary { background: var(--color-accent); color:#fff; border-color: var(--color-accent); }
.btn.danger { background:#e53e3e; color:#fff; border-color:#e53e3e; }
.btn:disabled { opacity:0.6; cursor:not-allowed; }
.edit-form { display:flex; flex-direction:column; gap:1rem; }
.form-group { display:flex; flex-direction:column; gap:0.35rem; }
.form-group input { border:1px solid var(--color-border); background: var(--color-bg); color: var(--color-text); padding:0.55rem 0.65rem; border-radius:6px; }
.form-actions { display:flex; gap:0.6rem; }
.error-msg { color: var(--color-danger); font-size:0.8rem; margin:0.25rem 0 0; }
.success-msg { color:#2f855a; font-size:0.8rem; margin:0.25rem 0 0; }
.loading-state { text-align:center; padding:2rem; }
.confirm-dialog { border:none; border-radius:12px; padding:0; background:transparent; }
.confirm-box { background: var(--color-surface); padding:1.5rem 1.5rem 1.25rem; border:1px solid var(--color-border); border-radius:12px; width: min(420px, 90vw); display:flex; flex-direction:column; gap:1rem; }
.confirm-box h3 { margin:0; font-size:1.1rem; }
.confirm-box p { margin:0; font-size:0.85rem; line-height:1.3; }
.confirm-actions { display:flex; justify-content:flex-end; gap:0.6rem; }
@media (max-width:600px){ .profile-content { grid-template-columns:1fr; } }
.user-avatar { width: 60px; height: 60px; background: linear-gradient(135deg, var(--color-accent) 0%, #764ba2 100%); color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.25rem; font-weight: 600; margin: 0 auto; }
.avatar-icon { font-size: 2rem; }
</style>

