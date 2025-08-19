<template>
  <div class="home">
    <HeroCarousel>
      <div class="hero-overlay-content">
        <h1 class="hero-title">Billard-Book</h1>
        <p class="hero-subtitle">Votre plateforme de réservation de tables de billard</p>
        <p class="hero-description">Réservez facilement une table, rejoignez des parties ou organisez vos propres tournois.</p>
        <div class="hero-actions">
          <router-link v-if="!authStore.isAuthenticated" to="/login" class="btn btn-primary btn-large">Se connecter</router-link>
          <router-link v-else to="/dashboard" class="btn btn-primary btn-large">Accéder au Dashboard</router-link>
        </div>
      </div>
    </HeroCarousel>

    <section class="features">
      <div class="container">
        <h2 class="section-title">Pourquoi choisir Billard-Book ?</h2>
        
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">📅</div>
            <h3>Réservation simple</h3>
            <p>Réservez votre table en quelques clics avec notre interface intuitive.</p>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon">👥</div>
            <h3>Rejoindre des parties</h3>
            <p>Trouvez d'autres joueurs et rejoignez des parties organisées par la communauté.</p>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon">🏆</div>
            <h3>Organiser des tournois</h3>
            <p>Créez vos propres événements et gérez les participants facilement.</p>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon">💬</div>
            <h3>Communauté active</h3>
            <p>Échangez avec d'autres passionnés et partagez vos expériences.</p>
          </div>
        </div>
      </div>
    </section>

    <section class="how-it-works">
      <div class="container">
        <h2 class="section-title">Comment ça marche ?</h2>
        
        <div class="steps">
          <div class="step">
            <div class="step-number">1</div>
            <h3>Créez votre compte</h3>
            <p>Inscrivez-vous gratuitement en quelques secondes</p>
          </div>
          
          <div class="step">
            <div class="step-number">2</div>
            <h3>Choisissez votre table</h3>
            <p>Sélectionnez la table et l'horaire qui vous conviennent</p>
          </div>
          
          <div class="step">
            <div class="step-number">3</div>
            <h3>Jouez !</h3>
            <p>Profitez de votre partie de billard</p>
          </div>
        </div>
      </div>
    </section>

    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-section">
            <h4>Billard-Book</h4>
            <p>La référence pour vos réservations de billard</p>
          </div>
          
          <div class="footer-section">
            <h4>Navigation</h4>
            <ul>
              <li><router-link to="/">Accueil</router-link></li>
              <li><router-link to="/users">Utilisateurs</router-link></li>
              <li><router-link to="/about">À propos</router-link></li>
              <li v-if="!authStore.isAuthenticated"><router-link to="/login">Connexion</router-link></li>
              <li v-else><router-link to="/dashboard">Dashboard</router-link></li>
            </ul>
          </div>
          
          <div class="footer-section">
            <h4>Contact</h4>
            <p>📧 amine.nedjar4716@gmail.com </p>
          </div>
        </div>
        
        <div class="footer-bottom">
          <p>&copy; 2025 Billard-Book. Tous droits réservés.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import HeroCarousel from '@/components/HeroCarousel.vue'

const authStore = useAuthStore()
</script>

<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Hero carousel overlay content */
.hero-overlay-content {
  max-width: 900px;
  margin: 0 auto;
  pointer-events: auto;
  padding: 0 1rem;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.hero-subtitle {
  font-size: 1.5rem;
  margin-bottom: 1rem;
  opacity: 0.9;
}

.hero-description {
  font-size: 1.1rem;
  margin-bottom: 2rem;
  opacity: 0.8;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-large {
  padding: 1rem 2rem;
  font-size: 1.1rem;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.features {
  padding: 4rem 1rem;
  background: var(--color-surface);
  color: var(--color-text);
  border-top:1px solid var(--color-border);
  border-bottom:1px solid var(--color-border);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  text-align: center;
  font-size: 2.5rem;
  color: var(--color-text);
  margin-bottom: 3rem;
  font-weight: 600;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.feature-card {
  text-align: center;
  padding: 2rem;
  border-radius: 12px;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  color: var(--color-text);
  margin-bottom: 1rem;
  font-size: 1.25rem;
}

.feature-card p {
  color: var(--color-text-soft);
  line-height: 1.6;
}

.how-it-works {
  padding: 4rem 1rem;
  background: var(--color-bg);
  color: var(--color-text);
}

.steps {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  max-width: 900px;
  margin: 0 auto;
}

.step {
  text-align: center;
  padding: 2rem 1rem;
}

.step-number {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, var(--color-accent) 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0 auto 1rem auto;
}

.step h3 {
  color: var(--color-text);
  margin-bottom: 0.5rem;
  font-size: 1.25rem;
}

.step p {
  color: var(--color-text-soft);
  line-height: 1.6;
}

.footer {
  background: #1a202c;
  color: white;
  padding: 3rem 1rem 1rem;
  margin-top: auto;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.footer-section h4 {
  color: #e2e8f0;
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.footer-section p {
  color: #a0aec0;
  line-height: 1.6;
}

.footer-section ul {
  list-style: none;
}

.footer-section ul li {
  margin-bottom: 0.5rem;
}

.footer-section ul li a {
  color: #a0aec0;
  text-decoration: none;
  transition: color 0.2s ease;
}

.footer-section ul li a:hover {
  color: #e2e8f0;
}

.footer-bottom {
  text-align: center;
  padding-top: 2rem;
  border-top: 1px solid #2d3748;
  color: #a0aec0;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }
  
  .hero-subtitle {
    font-size: 1.25rem;
  }
  
  .section-title {
    font-size: 2rem;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .steps {
    grid-template-columns: 1fr;
  }
}
</style>
