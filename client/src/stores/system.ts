import { defineStore } from 'pinia'
import { ref } from 'vue'
import { systemService } from '@/services/system'
import type { HealthStatus, ApiError } from '@/types'

export const useSystemStore = defineStore('system', () => {
  const health = ref<HealthStatus | null>(null)
  const backendUp = ref<boolean | null>(null)
  const lastCheck = ref<Date | null>(null)
  const checking = ref(false)
  const error = ref<string | null>(null)

  // Backoff exponentiel simple pour services cold-start
  const nextDelay = ref(30000)

  async function checkHealth() {
    try {
      checking.value = true
      error.value = null
      const data = await systemService.getHealth()
      health.value = data
      backendUp.value = data.status === 'UP'
      lastCheck.value = new Date()
      // Reset backoff si UP
      if (backendUp.value) nextDelay.value = 30000
    } catch (err: unknown) {
      const apiError = err as ApiError
      backendUp.value = false
      error.value = apiError.message || 'Health check échoué'
      lastCheck.value = new Date()
      // Augmenter le délai progressivement jusqu'à 5 min max
      nextDelay.value = Math.min(nextDelay.value * 1.6, 300000)
    } finally {
      checking.value = false
    }
  }

  return { health, backendUp, lastCheck, checking, error, checkHealth, nextDelay }
})
