<template>
  <section class="auth-page">
    <div class="auth-ambient" aria-hidden="true"></div>
    <article class="auth-card ui-card" aria-labelledby="auth-title">
      <header class="auth-header">
        <h1 id="auth-title" class="auth-title">{{ isRegistering ? 'Creer un compte' : 'Connexion' }}</h1>
        <p class="auth-subtitle">{{ isRegistering ? 'Rejoignez la plateforme de reservation.' : 'Accedez a vos reservations en cours.' }}</p>
      </header>

      <form @submit.prevent="handleSubmit" class="auth-form" novalidate>
        <div class="form-group">
          <label for="login">Identifiant</label>
          <input
            id="login"
            v-model="form.login"
            type="text"
            required
            class="form-input"
            placeholder="Votre identifiant"
            autocomplete="username"
          />
        </div>

        <div v-if="isRegistering" class="form-group">
          <label for="passwordCreate">Mot de passe</label>
          <input
            id="passwordCreate"
            v-model="form.password"
            type="password"
            required
            class="form-input"
            placeholder="Choisissez un mot de passe"
            autocomplete="new-password"
          />
        </div>

        <div class="form-group">
          <label for="passwordConfirm">{{ isRegistering ? 'Confirmation' : 'Mot de passe' }}</label>
          <input
            id="passwordConfirm"
            v-model="form.confirmPassword"
            type="password"
            required
            class="form-input"
            :placeholder="isRegistering ? 'Retapez le mot de passe' : 'Votre mot de passe'"
            :autocomplete="isRegistering ? 'new-password' : 'current-password'"
          />
        </div>

        <div v-if="authStore.error" class="error-message">
          {{ authStore.error }}
        </div>

        <button
          type="submit"
          class="btn btn-primary submit-button"
          :disabled="authStore.loading"
        >
          {{ authStore.loading ? 'Chargement' : (isRegistering ? 'Creer le compte' : 'Se connecter') }}
        </button>
      </form>

      <footer class="toggle-mode">
        <p>
          {{ isRegistering ? 'Vous avez deja un compte ?' : 'Nouveau ici ?' }}
          <button @click="toggleMode" type="button" class="mode-link">
            {{ isRegistering ? 'Connexion' : 'Creer un compte' }}
          </button>
        </p>
      </footer>
    </article>
  </section>
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
  password: '',
  confirmPassword: ''
})

function toggleMode() {
  isRegistering.value = !isRegistering.value
  form.password = ''
  form.confirmPassword = ''
  authStore.error = null
}

async function handleSubmit() {
  let success = false
  
  if (isRegistering.value) {
    if (form.password !== form.confirmPassword) {
      authStore.error = 'La confirmation ne correspond pas au mot de passe.'
      return
    }
    
    success = await authStore.register({
      login: form.login,
      name: form.password,
      password: form.password
    })
  } else {
    success = await authStore.login({
      login: form.login,
      name: form.confirmPassword
    })
  }
  
  if (success) {
    router.push('/dashboard')
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
}

.auth-ambient {
  position: relative;
  width: min(920px, 100%);
  min-height: 560px;
  border-radius: var(--radius-xl);
  background:
    linear-gradient(150deg, color-mix(in srgb, var(--color-primary) 72%, #061626), #082035),
    radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.15), transparent 45%);
  box-shadow: var(--shadow-lg);
}

.auth-card {
  position: absolute;
  width: min(430px, calc(100% - 2 * var(--space-8)));
  right: clamp(1rem, 8vw, 4rem);
  top: 50%;
  transform: translateY(-50%);
  padding: var(--space-8);
  background: color-mix(in srgb, var(--color-surface) 88%, transparent);
  backdrop-filter: blur(4px);
}

.auth-header {
  margin-bottom: var(--space-6);
}

.auth-title {
  margin: 0;
  font-family: var(--font-display);
  color: var(--color-text);
  font-size: 1.8rem;
  font-weight: 800;
}

.auth-subtitle {
  color: var(--color-text-soft);
  margin: var(--space-2) 0 0;
  font-size: 0.9375rem;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.form-group label {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--color-text-soft);
}

.submit-button {
  width: 100%;
}

.toggle-mode {
  text-align: center;
  margin-top: var(--space-6);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}

.toggle-mode p {
  color: var(--color-text-soft);
  margin: 0;
  font-size: 0.875rem;
}

.mode-link {
  background: none;
  border: none;
  color: var(--color-primary);
  font-weight: 700;
  cursor: pointer;
}

.mode-link:hover {
  color: var(--color-primary-hover);
}

@media (max-width: 960px) {
  .auth-page {
    padding: var(--space-4);
  }

  .auth-ambient {
    min-height: 680px;
  }

  .auth-card {
    left: 50%;
    right: auto;
    width: min(440px, calc(100% - 2 * var(--space-4)));
    transform: translate(-50%, -50%);
  }
}

@media (max-width: 520px) {
  .auth-ambient {
    min-height: 620px;
  }

  .auth-card {
    padding: var(--space-6);
  }
}
</style>
