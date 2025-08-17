import api from './api'
import type { User } from '@/types'

export const userService = {
  // Récupérer tous les utilisateurs (public)
  async getUsers(): Promise<{ users: { link: string }[] }> {
    const response = await api.get('/users')
    return response.data
  },

  // Récupérer un utilisateur par login
  async getUser(login: string): Promise<User> {
    const response = await api.get(`/users/${login}`)
    return response.data
  },

  // Créer un nouvel utilisateur
  async createUser(userData: { login: string; name: string }): Promise<string> {
    const response = await api.post('/users', userData)
    // L'API retourne l'URL dans le header Location
    return response.headers.location
  },

  // Mettre à jour un utilisateur
  async updateUser(login: string, userData: { name?: string }): Promise<void> {
    await api.put(`/users/${login}`, userData)
  },

  // Supprimer un utilisateur
  async deleteUser(login: string): Promise<void> {
    await api.delete(`/users/${login}`)
  },

  // Récupérer le nom d'un utilisateur
  async getUserName(login: string): Promise<{ name: string }> {
    const response = await api.get(`/users/${login}/name`)
    return response.data
  },

  // Récupérer les réservations créées par un utilisateur
  async getUserOwnedReservations(login: string): Promise<string[]> {
    const response = await api.get(`/users/${login}/ownedReservations`)
    return response.data
  },

  // Récupérer les réservations auxquelles un utilisateur est inscrit
  async getUserRegisteredReservations(login: string): Promise<string[]> {
    const response = await api.get(`/users/${login}/registeredReservations`)
    return response.data
  }
}

export default userService
