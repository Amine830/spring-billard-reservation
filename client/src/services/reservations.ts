import api from './api'
import type { Reservation, ReservationRequest, CommentRequest, Link } from '@/types'

export const reservationService = {
  // Récupérer toutes les réservations
  async getReservations(): Promise<Array<Reservation & { id: string }>> {
    const response = await api.get('/reservations')
    // L'API retourne une liste de liens, nous devons ensuite récupérer chaque réservation
    const links: { link: string }[] = response.data.reservations || []
    
    //console.log('Liens de réservations disponibles:', links)
    
    // Récupérer les détails de chaque réservation avec son ID
    const reservations: Array<Reservation & { id: string }> = []
    for (const linkObj of links) {
      try {
        // Extraire l'ID de la réservation depuis le lien (ex: "reservations/eb2a7899" -> "eb2a7899")
        const reservationId = linkObj.link.split('/').pop()
        if (reservationId) {
          const reservation = await this.getReservation(reservationId)
          // Ajouter l'ID à l'objet réservation
          reservations.push({ ...reservation, id: reservationId })
        }
      } catch (err) {
        console.warn(`Erreur lors du chargement de la réservation ${linkObj.link}:`, err)
      }
    }
    
    //console.log('Réservations chargées:', reservations)
    return reservations
  },

  // Récupérer une réservation par ID
  async getReservation(id: string): Promise<Reservation> {
    const response = await api.get(`/reservations/${id}`)
    return response.data
  },

  // Créer une nouvelle réservation
  async createReservation(reservation: ReservationRequest): Promise<Reservation> {
    const response = await api.post('/reservations', reservation)
    // L'API retourne un code 201 avec un header Location
    const location = response.headers.location
    if (location) {
      const id = location.split('/').pop()
      return this.getReservation(id!)
    }
    throw new Error('Erreur lors de la création de la réservation')
  },

  // Modifier une réservation
  async updateReservation(id: string, reservation: Partial<ReservationRequest>): Promise<Reservation> {
    await api.put(`/reservations/${id}`, reservation)
    return this.getReservation(id)
  },

  // Supprimer une réservation
  async deleteReservation(id: string): Promise<void> {
    await api.delete(`/reservations/${id}`)
  },

  // S'inscrire à une réservation
  async registerToReservation(id: string): Promise<void> {
  // Certains serveurs renvoient 415 si aucun Content-Type n'est fourni.
  // On envoie un corps JSON vide pour forcer 'application/json'.
  await api.post(`/reservations/${id}/register`, {})
  },

  // Se désinscrire d'une réservation
  async unregisterFromReservation(id: string): Promise<void> {
    await api.delete(`/reservations/${id}/unregister`)
  },

  // Récupérer les joueurs d'une réservation
  async getReservationPlayers(id: string): Promise<Link[]> {
    const response = await api.get(`/reservations/${id}/players`)
    return response.data
  },

  // Ajouter un commentaire
  async addComment(reservationId: string, comment: CommentRequest): Promise<void> {
    await api.post(`/reservations/${reservationId}/comment`, comment)
  }
}

export default reservationService