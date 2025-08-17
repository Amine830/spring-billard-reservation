<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="login-title">{{ isRegistering ? 'Inscription' : 'Connexion' }}</h1>
      
      <form @submit.prevent="handleSubmit" class="login-form">
        <div class="form-group">
          <label for="login">Login</label>
          <input
            id="login"
            v-model="form.login"
            type="text"
            required
            class="form-input"
            placeholder="Votre login"
          />
        </div>

        <div v-if="isRegistering" class="form-group">
          <label for="name">Mot de passe *</label>
          <input
            id="name"
            v-model="form.name"
            type="password"
            required
            class="form-input"
            placeholder="Choisissez un mot de passe"
          />
        </div>

        <div class="form-group">
          <label for="password">{{ isRegistering ? 'Confirmer le mot de passe' : 'Mot de passe' }}</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            required
            class="form-input"
            :placeholder="isRegistering ? 'Retapez le mot de passe' : 'Votre mot de passe'"
          />
        </div>

        <div v-if="authStore.error" class="error-message">
          {{ authStore.error }}
        </div>

        <button 
          type="submit" 
          class="submit-button"
          :disabled="authStore.loading"
        >
          {{ authStore.loading ? 'Chargement...' : (isRegistering ? 'S\'inscrire' : 'Se connecter') }}
        </button>
      </form>

      <div class="toggle-mode">
        <p>
          {{ isRegistering ? 'Déjà un compte ?' : 'Pas encore de compte ?' }}
          <button @click="toggleMode" type="button" class="toggle-button">
            {{ isRegistering ? 'Se connecter' : 'S\'inscrire' }}
          </button>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const isRegistering = ref(false)

const form = reactive({
  login: '',
  name: '',
  password: ''
})

function toggleMode() {
  isRegistering.value = !isRegistering.value
  // Réinitialiser les erreurs quand on change de mode
  authStore.error = null
}

async function handleSubmit() {
  let success = false
  
  if (isRegistering.value) {
    // Validation pour l'inscription
    if (form.name !== form.password) {
      authStore.error = 'Le nom et sa confirmation doivent être identiques'
      return
    }
    
    success = await authStore.register({
      login: form.login,
      name: form.name, // Le nom sera utilisé à la fois comme nom d'affichage et mot de passe
      password: form.name // On passe le nom comme password aussi pour cohérence
    })
  } else {
    // Pour la connexion, le password va dans le champ "name" de l'API
    success = await authStore.login({
      login: form.login,
      name: form.password // Le password devient le "name" pour l'API
    })
  }
  
  if (success) {
    // Rediriger vers le dashboard après connexion/inscription réussie
    router.push('/dashboard')
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-accent) 0%, #764ba2 100%);
  padding: 1rem;
}

.login-card {
  background: var(--color-surface);
  border-radius: 12px;
  padding: 2rem;
  box-shadow: var(--shadow-elev);
  width: 100%;
  max-width: 420px; /* légère marge plus large */
  margin: 0 auto;
  position: relative;
  border:1px solid var(--color-border);
}

.login-title {
  text-align: center;
  margin-bottom: 2rem;
  color: var(--color-text);
  font-size: 1.8rem;
  font-weight: 600;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 500;
  color: var(--color-text-soft);
}

.form-help {
  color: #718096;
  font-size: 0.8rem;
  font-style: italic;
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

.error-message {
  background: #7f1d1d;
  color: #fecaca;
  padding: 0.75rem;
  border-radius: 6px;
  border: 1px solid #991b1b;
  font-size: 0.9rem;
}

.submit-button {
  background: linear-gradient(135deg, var(--color-accent) 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 0.875rem;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.submit-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.submit-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.toggle-mode {
  text-align: center;
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--color-border);
}

.toggle-mode p {
  color: var(--color-text-soft);
  margin: 0;
}

.toggle-button {
  background: none;
  border: none;
  color: var(--color-accent);
  font-weight: 500;
  cursor: pointer;
  text-decoration: underline;
}

.toggle-button:hover {
  color: var(--color-accent-hover);
}

/* Grande largeur : garder le centrage et éviter l'effet "collé à gauche" si parent extérieur change */
@media (min-width: 1024px) {
  .login-container {
    justify-content: center;
    padding: 2rem;
  }
  .login-card {
    margin-left: auto;
    margin-right: auto;
  }
}

/* Ultra large écrans : possibilité d'ajouter un léger scaling pour ne pas paraître minuscule */
@media (min-width: 1600px) {
  .login-card {
    transform: scale(1.05);
  }
}
</style>
