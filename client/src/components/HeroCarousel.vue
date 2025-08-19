<template>
  <div class="carousel" @mouseenter="pause" @mouseleave="resume" :style="{'--slide-count': slides.length}">
    <div class="slides" :style="trackStyle" role="list" aria-live="polite">
      <div
        v-for="(s, i) in slides"
        :key="s.src + i"
        class="slide"
        role="listitem"
        :aria-hidden="currentIndex !== i"
      >
        <img :src="s.src" :alt="s.alt || 'Hero image '+(i+1)" loading="lazy" />
      </div>
    </div>

    <!-- Overlay content (slot) -->
    <div class="overlay">
      <slot />
    </div>

    <!-- Controls -->
    <button class="nav prev" @click="prev" aria-label="Image précédente">‹</button>
    <button class="nav next" @click="next" aria-label="Image suivante">›</button>

    <div class="indicators" role="tablist">
      <button
        v-for="(s,i) in slides"
        :key="'dot-'+i"
        class="dot"
        :class="{active: currentIndex===i}"
        @click="goTo(i)"
        :aria-label="'Aller à l\'image '+(i+1)"
        :aria-selected="currentIndex===i"
        role="tab"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

// Chargement dynamique des images du dossier hero
// Place tes images dans src/assets/hero/ (ex: 01.jpg, 02.jpg ...)
const modules = import.meta.glob('../assets/hero/*.{png,jpg,jpeg,webp,avif}', { eager: true, import: 'default' }) as Record<string,string>

// Construire la liste triée pour cohérence
let slides = Object.keys(modules)
  .sort()
  .map(path => ({ src: modules[path] as string, alt: path.split('/').pop() }))

// Fallback si aucune image
if (slides.length === 0) {
  slides = [{
    src: 'https://via.placeholder.com/1600x900/111827/FFFFFF?text=Billard+Book',
    alt: 'Placeholder'
  }]
}

const currentIndex = ref(0)
const intervalMs = 6000
let timer: number | null = null

function start() {
  stop()
  timer = window.setInterval(() => next(), intervalMs)
}
function stop() {
  if (timer) { clearInterval(timer); timer = null }
}
function pause() { stop() }
function resume() { start() }

function next() {
  currentIndex.value = (currentIndex.value + 1) % slides.length
}
function prev() {
  currentIndex.value = (currentIndex.value - 1 + slides.length) % slides.length
}
function goTo(i: number) { currentIndex.value = i }

const trackStyle = computed(() => ({
  transform: `translateX(-${currentIndex.value * 100}%)`
}))

onMounted(() => { if (slides.length > 1) start() })
onBeforeUnmount(() => stop())
</script>

<style scoped>
.carousel {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  isolation: isolate;
  background: #000;
}
.slides {
  display: flex;
  width: 100%;
  height: 100%;
  transition: transform 1s cubic-bezier(.65,.05,.36,1);
}
.slide {
  flex: 0 0 100%;
  width: 100%;
  height: 100%;
  position: relative;
}
.slide img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  filter: brightness(0.55) saturate(1.1);
  transition: transform 10s linear;
  /* animation: slow-zoom 18s linear forwards; */
}
@keyframes slow-zoom {
  from { transform: scale(1); }
  to { transform: scale(1.08); }
}
.overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 2rem 1rem 3rem;
  color: #fff;
  pointer-events: none; /* laisse cliquer sur boutons nav */
  z-index: 2;
}
.overlay::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.60) 0%, rgba(0,0,0,0.35) 45%, rgba(0,0,0,0.70) 100%);
  backdrop-filter: saturate(150%) brightness(0.85);
  z-index: -1;
}
.overlay :deep(a),
.overlay :deep(button) {
  pointer-events: auto; /* réactive interactions */
}
.nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0,0,0,0.35);
  border: none;
  color: #fff;
  font-size: 2.2rem;
  width: 3rem;
  height: 3rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  backdrop-filter: blur(4px);
  transition: background .3s;
}
.nav:hover { background: rgba(0,0,0,0.55); }
.prev { left: 1rem; }
.next { right: 1rem; }

.indicators {
  position: absolute;
  bottom: 1.1rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: .6rem;
  z-index: 10;
}
.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid rgba(255,255,255,.7);
  background: rgba(255,255,255,.25);
  cursor: pointer;
  padding: 0;
  transition: background .3s, transform .3s;
}
.dot.active { background: #fff; transform: scale(1.15); }
.dot:hover { background: rgba(255,255,255,.85); }

@media (max-width: 768px) {
  .overlay h1 { font-size: 2.2rem; }
  .overlay p { font-size: 1rem; }
  .nav { font-size: 1.6rem; width: 2.5rem; height: 2.5rem; }
}
</style>
