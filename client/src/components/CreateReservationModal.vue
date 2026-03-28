<template>
  <div class="modal-overlay" @click="handleOverlayClick">
    <div class="modal-content" @click.stop role="dialog" aria-modal="true" aria-labelledby="create-reservation-title">
      <div class="modal-header">
        <h2 id="create-reservation-title">Nouvelle reservation</h2>
        <button @click="$emit('close')" class="close-button" type="button" aria-label="Fermer le formulaire de reservation">✕</button>
      </div>

      <form @submit.prevent="handleSubmit" class="modal-form">
        <div class="form-row">
          <div class="form-group">
            <label for="tableId">Table *</label>
            <select
              id="tableId"
              v-model="form.tableId"
              required
              class="form-input"
            >
              <option value="">Choisir une table</option>
              <option v-for="table in availableTables" :key="table" :value="table">
                Table {{ table }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="duration">Durée (minutes) *</label>
            <select
              id="duration"
              v-model="form.duration"
              required
              class="form-input"
            >
              <option value="">Durée</option>
              <option :value="30">30 minutes</option>
              <option :value="60">1 heure</option>
              <option :value="90">1h30</option>
              <option :value="120">2 heures</option>
              <option :value="180">3 heures</option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label for="startTime">Date et heure *</label>
          <input
            id="startTime"
            v-model="form.startTime"
            type="datetime-local"
            required
            class="form-input"
            :min="minDateTime"
          />
        </div>

        <div v-if="error" class="error-message">
          {{ error }}
        </div>

        <div class="modal-actions">
          <button 
            type="button" 
            @click="$emit('close')" 
            class="cancel-button"
          >
            Annuler
          </button>
          <button
            type="submit"
            class="btn btn-primary submit-button"
            :disabled="loading"
          >
            {{ loading ? 'Creation...' : 'Creer' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useReservationStore } from '@/stores/reservations'

const emit = defineEmits<{
  close: []
  created: []
}>()

const reservationStore = useReservationStore()

const loading = ref(false)
const error = ref<string | null>(null)

// Tables disponibles (simulé - dans un vrai projet, cela viendrait de l'API)
const availableTables = ref([1, 2, 3, 4, 5, 6])

const form = reactive({
  tableId: '',
  duration: '',
  startTime: ''
})

const minDateTime = computed(() => {
  const now = new Date()
  // Formatage pour datetime-local (YYYY-MM-DDTHH:MM)
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  
  return `${year}-${month}-${day}T${hours}:${minutes}`
})

onMounted(() => {
  // Définir une heure par défaut (maintenant + 1 heure)
  const defaultTime = new Date()
  defaultTime.setHours(defaultTime.getHours() + 1)
  defaultTime.setMinutes(0) // Arrondir aux heures pleines
  
  const year = defaultTime.getFullYear()
  const month = String(defaultTime.getMonth() + 1).padStart(2, '0')
  const day = String(defaultTime.getDate()).padStart(2, '0')
  const hours = String(defaultTime.getHours()).padStart(2, '0')
  const minutes = String(defaultTime.getMinutes()).padStart(2, '0')
  
  form.startTime = `${year}-${month}-${day}T${hours}:${minutes}`
})

async function handleSubmit() {
  try {
    loading.value = true
    error.value = null

    // Validation basique
    if (!form.tableId) {
      error.value = 'Veuillez sélectionner une table'
      return
    }

    if (!form.duration) {
      error.value = 'Veuillez sélectionner une durée'
      return
    }

    if (!form.startTime) {
      error.value = 'Veuillez sélectionner une date et heure'
      return
    }

    // Vérifier que la date est dans le futur
    const selectedDate = new Date(form.startTime)
    const now = new Date()
    if (selectedDate <= now) {
      error.value = 'La date doit être dans le futur'
      return
    }

    const reservationData = {
      tableId: parseInt(form.tableId),
      startTime: new Date(form.startTime).toISOString(),
      duration: parseInt(form.duration)
    }

    const result = await reservationStore.createReservation(reservationData)
    
    if (result) {
      emit('created')
    } else {
      error.value = reservationStore.error || 'Erreur lors de la création de la réservation'
    }
  } catch (err) {
    console.error('Erreur lors de la création:', err)
    error.value = 'Une erreur inattendue est survenue'
  } finally {
    loading.value = false
  }
}

function handleOverlayClick() {
  emit('close')
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(2, 6, 23, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: var(--space-4);
}

.modal-content {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 34rem;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border);
  color: var(--color-text);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-6);
  border-bottom: 1px solid var(--color-border);
}

.modal-header h2 {
  margin: 0;
  font-family: var(--font-display);
  color: var(--color-text);
  font-size: 1.125rem;
  font-weight: 700;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.25rem;
  color: var(--color-text-soft);
  cursor: pointer;
  padding: var(--space-1);
  border-radius: var(--radius-sm);
  transition: background-color var(--duration-fast) var(--easing-standard), color var(--duration-fast) var(--easing-standard);
}

.close-button:hover {
  background: var(--color-surface-2);
  color: var(--color-text);
}

.modal-form {
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}

.form-group label {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--color-text-soft);
}

.modal-actions {
  display: flex;
  gap: var(--space-3);
  justify-content: flex-end;
  border-top: 1px solid var(--color-border);
  padding-top: var(--space-4);
}

.cancel-button {
  background: var(--color-surface);
  color: var(--color-text-soft);
  border: 1px solid var(--color-border);
  min-height: 2.5rem;
  padding: 0.5rem 0.875rem;
  border-radius: var(--radius-md);
  font-weight: 700;
  cursor: pointer;
  transition: background-color var(--duration-fast) var(--easing-standard), color var(--duration-fast) var(--easing-standard);
}

.cancel-button:hover {
  background: var(--color-surface-2);
  color: var(--color-text);
}

.submit-button {
  min-width: 7.5rem;
}

@media (max-width: 600px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .modal-actions {
    flex-direction: column-reverse;
  }

  .submit-button,
  .cancel-button {
    width: 100%;
  }
}
</style>
