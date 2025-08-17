<template>
  <div class="about">
    <div class="about-inner">
      <h1>À propos</h1>
      <p class="lead">Billard‑Book est une application de démonstration pour gérer des réservations de tables de billard.</p>
      <ul class="bullets">
        <li>Réservation et inscription multi‑joueurs</li>
        <li>Commentaires en temps réel (rafraîchis)</li>
        <li>Mode sombre / clair</li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.about { min-height: 60vh; padding:2rem 1rem; background: var(--color-bg); color: var(--color-text); }
.about-inner { max-width: 900px; margin:0 auto; background: var(--color-surface); padding:2rem 2.5rem; border:1px solid var(--color-border); border-radius:14px; box-shadow: var(--shadow-elev); }
.about-inner h1 { margin-top:0; font-size:2rem; }
.lead { font-size:1.05rem; color: var(--color-text-soft); margin-bottom:1rem; }
.bullets { margin:0; padding-left:1.2rem; display:flex; flex-direction:column; gap:.35rem; }
.bullets li { color: var(--color-text-soft); }
@media (min-width: 1024px) { .about { min-height: 100vh; display:flex; align-items:center; } }
</style>
