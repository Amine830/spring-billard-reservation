import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services/auth'
import type { User, LoginRequest, ApiError } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const isAuthenticated = computed(() => !!user.value)

  async function login(credentials: LoginRequest) {
    try {
      loading.value = true
      error.value = null
      user.value = await authService.login(credentials)
      return true
    } catch (err: unknown) {
      const apiError = err as ApiError
      error.value = apiError.message || 'Erreur de connexion'
      return false
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    try {
      await authService.logout()
    } catch (err) {
      console.error('Erreur lors de la déconnexion:', err)
    } finally {
      user.value = null
    }
  }

  async function fetchCurrentUser() {
    if (!authService.isAuthenticated()) return

    try {
      user.value = await authService.getCurrentUser()
    } catch (err) {
      console.error('Erreur lors de la récupération de l\'utilisateur:', err)
      await logout()
    }
  }

  async function register(userData: { login: string; name: string; password: string }) {
    try {
      loading.value = true
      error.value = null
      user.value = await authService.register(userData)
      return true
    } catch (err: unknown) {
      const apiError = err as ApiError
      error.value = apiError.message || 'Erreur lors de l\'inscription'
      return false
    } finally {
      loading.value = false
    }
  }

  // Initialisation au démarrage
  if (authService.isAuthenticated()) {
    fetchCurrentUser()
  }

  return {
    user,
    loading,
    error,
    isAuthenticated,
    login,
    logout,
    register,
    fetchCurrentUser
  }
})
