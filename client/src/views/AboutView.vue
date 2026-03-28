<template>
  <section class="about-page">
    <div class="app-container about-layout">
      <header class="about-header ui-card">
        <p class="eyebrow">Produit</p>
        <h1>A propos de Billard Book</h1>
        <p class="lead">Billard Book simplifie la reservation de tables et la coordination entre joueurs dans une interface claire et rapide.</p>
      </header>

      <section class="pillars-grid">
        <article class="ui-card pillar">
          <h2>Planification</h2>
          <p>Creation et gestion des reservations en quelques champs, avec statut en temps reel.</p>
          <span class="ui-badge status-info">Reservations</span>
        </article>

        <article class="ui-card pillar">
          <h2>Participation</h2>
          <p>Inscription ou retrait d une partie selon les places disponibles et les horaires.</p>
          <span class="ui-badge status-success">Joueurs</span>
        </article>

        <article class="ui-card pillar">
          <h2>Suivi</h2>
          <p>Commentaires et details centralises pour garder une communication fluide entre participants.</p>
          <span class="ui-badge status-info">Notes</span>
        </article>
      </section>
    </div>
  </section>
</template>

<style scoped>
.about-page {
  min-height: 100vh;
  padding: var(--space-8) 0 var(--space-10);
}

.about-layout {
  display: grid;
  gap: var(--space-6);
}

.about-header {
  padding: var(--space-6);
  display: grid;
  gap: var(--space-3);
}

.eyebrow {
  margin: 0;
  text-transform: uppercase;
  font-size: 0.72rem;
  letter-spacing: 0.08em;
  color: var(--color-text-soft);
  font-weight: 700;
}

.about-header h1 {
  margin: 0;
  font-family: var(--font-display);
  font-size: clamp(1.45rem, 2.2vw, 2rem);
}

.lead {
  margin: 0;
  max-width: 56ch;
  color: var(--color-text-soft);
  line-height: 1.6;
}

.pillars-grid {
  display: grid;
  gap: var(--space-4);
  grid-template-columns: repeat(auto-fit, minmax(230px, 1fr));
}

.pillar {
  padding: var(--space-5);
  display: grid;
  gap: var(--space-3);
}

.pillar h2 {
  margin: 0;
  font-size: 1.1rem;
}

.pillar p {
  margin: 0;
  color: var(--color-text-soft);
  line-height: 1.55;
}

@media (max-width: 900px) {
  .about-page {
    padding-top: var(--space-6);
  }
}
</style>