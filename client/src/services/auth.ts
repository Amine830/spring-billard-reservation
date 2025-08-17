import api from './api'
import type { User, LoginRequest } from '@/types'

export const authService = {
  // Connexion
  async login(credentials: LoginRequest): Promise<User> {
    // console.log('Tentative de connexion avec les identifiants:', credentials)
    const response = await api.post('/users/login', credentials)
    
    // Le token JWT est automatiquement sauvegardé par l'intercepteur
    const token = response.headers.authorization
    if (token && token.startsWith('Bearer ')) {
      localStorage.setItem('jwt_token', token.substring(7))
    }
    
    // Sauvegarder le login pour pouvoir récupérer l'utilisateur plus tard
    localStorage.setItem('user_login', credentials.login)
    
    // Récupérer les infos utilisateur
    return this.getCurrentUser()
  },

  // Inscription
  async register(userData: { login: string; name: string; password: string }): Promise<User> {
    // Pour l'inscription, on utilise le name comme nom d'affichage dans l'API
    // (dans cette API, le "name" sert à la fois de nom d'affichage et de mot de passe)
    const userRequest = {
      login: userData.login,
      name: userData.name // On utilise directement le name fourni
    }
    
    const response = await api.post('/users', userRequest)
    
    // Le token JWT est automatiquement sauvegardé par l'intercepteur  
    const token = response.headers.authorization
    if (token && token.startsWith('Bearer ')) {
      localStorage.setItem('jwt_token', token.substring(7))
    }
    
    // Sauvegarder le login pour pouvoir récupérer l'utilisateur plus tard
    localStorage.setItem('user_login', userData.login)
    
    return this.getCurrentUser()
  },

  // Déconnexion
  async logout(): Promise<void> {
    try {
      await api.post('/users/logout')
    } finally {
      localStorage.removeItem('jwt_token')
      localStorage.removeItem('user_login')
    }
  },

  // Récupérer l'utilisateur actuel
  async getCurrentUser(): Promise<User> {
    // D'abord, nous devons récupérer le login depuis le token JWT
    // En attendant, nous stockons le login dans le localStorage après connexion
    const login = localStorage.getItem('user_login')
    if (!login) {
      throw new Error('Aucun utilisateur connecté')
    }
    
    const response = await api.get(`/users/${login}`)
    return response.data
  },

  // Vérifier si l'utilisateur est connecté
  isAuthenticated(): boolean {
    const token = localStorage.getItem('jwt_token')
    return !!token
  },

  // Récupérer le token
  getToken(): string | null {
    return localStorage.getItem('jwt_token')
  },

  // Nettoyer le localStorage (utile pour déboguer)
  clearStorage(): void {
    localStorage.removeItem('jwt_token')
    localStorage.removeItem('user_login')
  }
}

export default authService