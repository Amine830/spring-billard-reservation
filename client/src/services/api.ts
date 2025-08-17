import axios, { type AxiosInstance, type AxiosResponse } from 'axios'
import type { ApiError } from '@/types'

// Détermination dynamique de l'URL de base API (dev: proxy /api, prod: URL distante)
interface ImportMetaEnvExt {
  VITE_API_BASE_URL?: string
}
const envVars = (import.meta as unknown as { env: ImportMetaEnvExt }).env
const rawBase = envVars.VITE_API_BASE_URL || '/api'
// Normaliser: enlever trailing slash
const baseURL = rawBase.replace(/\/$/, '')

const api: AxiosInstance = axios.create({
  baseURL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// Intercepteur pour ajouter le token JWT automatiquement
api.interceptors.request.use(
  (config) => {
    // Ne pas ajouter de token pour les endpoints publics
    const publicEndpoints = ['/users/login', '/users']
    const isPublicEndpoint = publicEndpoints.some(endpoint => {
      // Vérifier si l'URL correspond exactement ou se termine par l'endpoint
      return config.url === endpoint || 
             config.url?.endsWith(endpoint) ||
             (endpoint === '/users' && config.url === '/users') ||
             (endpoint === '/users/login' && config.url === '/users/login')
    })
    
  //console.log('🔍 Requête:', config.method?.toUpperCase(), config.baseURL + (config.url || ''), 'Public:', isPublicEndpoint)
    
    if (!isPublicEndpoint) {
      const token = localStorage.getItem('jwt_token')
      if (token && config.headers) {
        config.headers.Authorization = `Bearer ${token}`
        //console.log('🔑 Token ajouté à la requête')
      }
    } else {
      //console.log('🔓 Endpoint public - pas de token')
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Intercepteur pour gérer les réponses et les erreurs
api.interceptors.response.use(
  (response: AxiosResponse) => {
    // Sauvegarder le nouveau token s'il est présent dans les headers
    const newToken = response.headers.authorization
    if (newToken && newToken.startsWith('Bearer ')) {
      localStorage.setItem('jwt_token', newToken.substring(7))
    }
    return response
  },
  (error) => {
    // Gestion centralisée des erreurs
    if (error.response?.status === 401) {
      // Token expiré ou invalide
      localStorage.removeItem('jwt_token')
      window.location.href = '/login'
    }
    
    const apiError: ApiError = {
      message: error.response?.data?.message || 'Une erreur est survenue',
      status: error.response?.status || 500,
      timestamp: new Date().toISOString(),
    }
    
    return Promise.reject(apiError)
  }
)

export default api