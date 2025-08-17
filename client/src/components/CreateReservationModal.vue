<template>
  <div class="modal-overlay" @click="handleOverlayClick">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h2>Nouvelle réservation</h2>
        <button @click="$emit('close')" class="close-button">✕</button>
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
            class="submit-button"
            :disabled="loading"
          >
            {{ loading ? 'Création...' : 'Créer la réservation' }}
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
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
}
.modal-content {
  background: var(--color-surface);
  border-radius: 12px;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
  border:1px solid var(--color-border);
  color: var(--color-text);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--color-border);
}

.modal-header h2 {
  margin: 0;
  color: #1a202c;
  font-size: 1.25rem;
  font-weight: 600;
}
.modal-header h2 {
  margin: 0;
  color: var(--color-text);
  font-size: 1.25rem;
  font-weight: 600;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #718096;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: background-color 0.2s ease;
}
.close-button {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--color-text-soft);
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: background-color 0.2s ease;
}
.close-button:hover {
  background: var(--color-bg);
  color: var(--color-text);
}

.close-button:hover {
  background: #f7fafc;
  color: #4a5568;
}

.modal-form {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group label {
  font-weight: 500;
  color: #4a5568;
}
.form-group label {
  font-weight: 500;
  color: var(--color-text-soft);
}

.form-input {
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}
.form-input {
  padding: 0.75rem;
  border: 2px solid var(--color-border);
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
  background: var(--color-bg);
  color: var(--color-text);
}
.form-input:focus {
  outline: none;
  border-color: var(--color-accent);
}

.form-input:focus {
  outline: none;
  border-color: #4299e1;
}

textarea.form-input {
  resize: vertical;
  min-height: 80px;
}

.error-message {
  background: #fed7d7;
  color: #c53030;
  padding: 0.75rem;
  border-radius: 6px;
  border: 1px solid #feb2b2;
  font-size: 0.9rem;
}
.error-message {
  background: #7f1d1d;
  color: #fecaca;
  padding: 0.75rem;
  border-radius: 6px;
  border: 1px solid #991b1b;
  font-size: 0.9rem;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}
.modal-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  padding-top: 1rem;
  border-top: 1px solid var(--color-border);
}

.cancel-button {
  background: white;
  color: #4a5568;
  border: 2px solid #e2e8f0;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
}
.cancel-button {
  background: var(--color-surface);
  color: var(--color-text-soft);
  border: 2px solid var(--color-border);
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
}
.cancel-button:hover {
  background: var(--color-bg);
  border-color: var(--color-border);
}

.cancel-button:hover {
  background: #f7fafc;
  border-color: #cbd5e0;
}

.submit-button {
  background: #4299e1;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s ease;
}
.submit-button {
  background: var(--color-accent);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s ease;
}
.submit-button:hover:not(:disabled) {
  background: var(--color-accent-hover);
}

.submit-button:hover:not(:disabled) {
  background: #3182ce;
}

.submit-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 600px) {
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .modal-actions {
    flex-direction: column;
  }
}
</style>
