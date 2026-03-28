<template>
  <section class="home-page">
    <HeroCarousel>
      <div class="hero-overlay-content">
        <p class="hero-kicker">Application de reservation</p>
        <h1 class="hero-title">Billard Book</h1>
        <p class="hero-description">
          Organisez vos parties, trouvez des joueurs disponibles et gardez un suivi propre de chaque reservation.
        </p>
        <div class="hero-actions">
          <router-link v-if="!authStore.isAuthenticated" to="/login" class="btn btn-primary btn-large">
            Se connecter
          </router-link>
          <router-link v-else to="/dashboard" class="btn btn-primary btn-large">
            Ouvrir les reservations
          </router-link>
          <router-link to="/about" class="btn btn-secondary btn-large">
            En savoir plus
          </router-link>
        </div>
      </div>
    </HeroCarousel>

    <div class="app-container home-sections">
      <section class="ui-card panel">
        <header class="panel-header">
          <h2>Pourquoi utiliser Billard Book</h2>
          <p>Un flux simple, du choix du creneau a la participation des joueurs.</p>
        </header>

        <div class="features-grid">
          <article class="feature-card">
            <span class="icon-wrap" aria-hidden="true"><CalendarClock :size="20" /></span>
            <h3>Planning clair</h3>
            <p>Visualisez rapidement les reservations confirmees et les places restantes.</p>
          </article>

          <article class="feature-card">
            <span class="icon-wrap" aria-hidden="true"><UsersRound :size="20" /></span>
            <h3>Participation rapide</h3>
            <p>Rejoignez une partie ouverte ou creez la votre avec des regles explicites.</p>
          </article>

          <article class="feature-card">
            <span class="icon-wrap" aria-hidden="true"><Trophy :size="20" /></span>
            <h3>Sessions organisees</h3>
            <p>Gerez les horaires et capacites pour eviter les conflits et doublons.</p>
          </article>

          <article class="feature-card">
            <span class="icon-wrap" aria-hidden="true"><MessageSquareText :size="20" /></span>
            <h3>Communication utile</h3>
            <p>Ajoutez des commentaires contextuels pour preparer la session en avance.</p>
          </article>
        </div>
      </section>

      <section class="ui-card panel">
        <header class="panel-header">
          <h2>Demarrage en 3 etapes</h2>
          <p>Le parcours principal reste court et lisible sur desktop comme sur mobile.</p>
        </header>

        <ol class="steps-grid">
          <li class="step-card">
            <span class="step-number">1</span>
            <h3>Connectez votre compte</h3>
            <p>Authentifiez-vous pour acceder a votre espace de reservation.</p>
          </li>
          <li class="step-card">
            <span class="step-number">2</span>
            <h3>Choisissez un creneau</h3>
            <p>Selectionnez une reservation existante ou creez une nouvelle session.</p>
          </li>
          <li class="step-card">
            <span class="step-number">3</span>
            <h3>Invitez et jouez</h3>
            <p>Ajoutez les joueurs, laissez une note pratique, puis lancez la partie.</p>
          </li>
        </ol>
      </section>

      <section class="ui-card panel quick-links-panel">
        <header class="panel-header">
          <h2>Acces rapide</h2>
          <p>Raccourcis adaptes selon votre session et votre role.</p>
        </header>

        <div class="quick-links">
          <router-link v-if="authStore.isAuthenticated" to="/dashboard" class="btn btn-primary">
            Aller au dashboard
          </router-link>
          <router-link v-else to="/login" class="btn btn-primary">
            Ouvrir la connexion
          </router-link>
          <router-link to="/profile" v-if="authStore.isAuthenticated" class="btn btn-secondary">
            Mon profil
          </router-link>
          <router-link to="/users" v-if="isAdmin" class="btn btn-secondary">
            Gestion utilisateurs
          </router-link>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { CalendarClock, UsersRound, Trophy, MessageSquareText } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import HeroCarousel from '@/components/HeroCarousel.vue'

const authStore = useAuthStore()
const isAdmin = computed(() => authStore.user?.login?.toLowerCase() === 'admin')
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  display: grid;
  gap: var(--space-8);
  padding-bottom: var(--space-10);
}

.hero-overlay-content {
  max-width: 860px;
  margin: 0 auto;
  pointer-events: auto;
  padding: 0 var(--space-4);
  display: grid;
  gap: var(--space-4);
}

.hero-kicker {
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 0.75rem;
  font-weight: 700;
  color: color-mix(in srgb, #ffffff 90%, transparent);
}

.hero-title {
  margin: 0;
  font-family: var(--font-display);
  font-size: clamp(2.1rem, 5vw, 3.65rem);
  font-weight: 800;
  letter-spacing: 0.01em;
  text-shadow: 0 10px 36px rgba(0, 0, 0, 0.4);
}

.hero-description {
  margin: 0;
  max-width: 60ch;
  font-size: clamp(1rem, 1.7vw, 1.15rem);
  line-height: 1.65;
  color: color-mix(in srgb, #ffffff 88%, transparent);
}

.hero-actions {
  display: flex;
  gap: var(--space-3);
  justify-content: center;
  flex-wrap: wrap;
}

.btn-large {
  min-height: 2.8rem;
  padding-inline: var(--space-6);
}

.home-sections {
  display: grid;
  gap: var(--space-5);
}

.panel {
  padding: var(--space-6);
  display: grid;
  gap: var(--space-5);
}

.panel-header {
  display: grid;
  gap: var(--space-2);
}

.panel-header h2 {
  margin: 0;
  font-family: var(--font-display);
  font-size: clamp(1.25rem, 2.2vw, 1.7rem);
}

.panel-header p {
  margin: 0;
  color: var(--color-text-soft);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: var(--space-4);
}

.feature-card {
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-4);
  display: grid;
  gap: var(--space-2);
}

.feature-card h3 {
  margin: 0;
  font-size: 1rem;
}

.feature-card p {
  margin: 0;
  color: var(--color-text-soft);
  line-height: 1.55;
}

.icon-wrap {
  width: 2.1rem;
  height: 2.1rem;
  border-radius: var(--radius-md);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: color-mix(in srgb, var(--color-primary) 15%, transparent);
  color: var(--color-primary);
}

.steps-grid {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: var(--space-4);
}

.step-card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-4);
  background: var(--color-surface-2);
  display: grid;
  gap: var(--space-2);
}

.step-number {
  width: 2rem;
  height: 2rem;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
  font-weight: 700;
  background: var(--color-primary);
  color: var(--color-primary-contrast);
}

.step-card h3 {
  margin: 0;
  font-size: 1rem;
}

.step-card p {
  margin: 0;
  color: var(--color-text-soft);
  line-height: 1.55;
}

.quick-links-panel {
  margin-bottom: var(--space-2);
}

.quick-links {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
}

@media (max-width: 900px) {
  .home-page {
    gap: var(--space-6);
  }

  .panel {
    padding: var(--space-5);
  }
}

@media (max-width: 640px) {
  .btn-large {
    width: 100%;
  }

  .hero-actions {
    justify-content: stretch;
  }

  .quick-links .btn {
    width: 100%;
  }
}
</style>
