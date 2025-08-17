import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { reservationService } from '@/services/reservations'
import type { Reservation, ReservationUI, ReservationCreateRequest, CommentRequest, ApiError } from '@/types'

export const useReservationStore = defineStore('reservations', () => {
  const reservations = ref<ReservationUI[]>([])
  const currentReservation = ref<ReservationUI | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Computed
  const activeReservations = computed(() => 
    reservations.value.filter(r => new Date(r.endTime) > new Date())
  )

  const completedReservations = computed(() => 
    reservations.value.filter(r => new Date(r.endTime) <= new Date())
  )

  // Utility functions
  function convertToUI(reservation: Reservation, id: string): ReservationUI {
    const start = new Date(reservation.startTime)
    const end = new Date(reservation.endTime)
    const duration = Math.round((end.getTime() - start.getTime()) / (1000 * 60)) // en minutes
    // Mapper les joueurs depuis les liens fournis par l'API
    const players = (reservation.players || []).map(l => l.link.split('/').pop() || '')
    
    return {
      id,
      tableId: reservation.tableId,
      ownerId: reservation.ownerId,
      startTime: reservation.startTime,
      endTime: reservation.endTime,
      duration,
      players,
      comments: reservation.comments,
      title: `Table ${reservation.tableId} - ${start.toLocaleDateString('fr-FR')}`
    }
  }

  function convertCreateRequest(request: ReservationCreateRequest): { tableId: string; startTime: string; endTime: string } {
    const start = new Date(request.startTime)
    const end = new Date(start.getTime() + request.duration * 60 * 1000)
    
    return {
      tableId: request.tableId.toString(),
      startTime: start.toISOString(),
      endTime: end.toISOString()
    }
  }

  // Actions
  async function fetchReservations() {
    try {
      loading.value = true
      error.value = null
      
      // Récupérer d'abord les liens des réservations
      const response = await reservationService.getReservations()
      //console.log('Réservations de l\'API:', response)
      
      // Convertir les réservations pour l'UI en utilisant les vrais IDs
      reservations.value = response.map((reservationWithId) => {
        // Utiliser le vrai ID de la réservation
        return convertToUI(reservationWithId, reservationWithId.id)
      })
      
      //console.log('Réservations converties pour l\'UI:', reservations.value)
    } catch (err: unknown) {
      const apiError = err as ApiError
      error.value = apiError.message || 'Erreur lors du chargement des réservations'
      console.error('Erreur lors du chargement des réservations:', err)
    } finally {
      loading.value = false
    }
  }

  async function fetchReservation(id: string) {
    try {
      loading.value = true
      error.value = null
  const apiReservation = await reservationService.getReservation(id)
  const uiReservation = convertToUI(apiReservation, id)
      
      currentReservation.value = uiReservation
      return currentReservation.value
    } catch (err: unknown) {
      const apiError = err as ApiError
      error.value = apiError.message || 'Erreur lors du chargement de la réservation'
      console.error('Erreur lors du chargement de la réservation:', err)
      return null
    } finally {
      loading.value = false
    }
  }

  async function createReservation(reservationData: ReservationCreateRequest) {
    try {
      loading.value = true
      error.value = null
      const apiRequest = convertCreateRequest(reservationData)
      const newReservation = await reservationService.createReservation(apiRequest)
      
      // Créer l'ID à partir de l'URL retournée ou générer un ID temporaire
      const newId = Date.now().toString()
      const uiReservation = convertToUI(newReservation, newId)
      
      reservations.value.push(uiReservation)
      return uiReservation
    } catch (err: unknown) {
      const apiError = err as ApiError
      error.value = apiError.message || 'Erreur lors de la création de la réservation'
      console.error('Erreur lors de la création de la réservation:', err)
      return null
    } finally {
      loading.value = false
    }
  }

  async function updateReservation(id: string, reservationData: ReservationCreateRequest) {
    try {
      loading.value = true
      error.value = null
      const apiRequest = convertCreateRequest(reservationData)
      const updatedReservation = await reservationService.updateReservation(id, apiRequest)
      const uiReservation = convertToUI(updatedReservation, id)
      
      const index = reservations.value.findIndex(r => r.id === id)
      if (index !== -1) {
        reservations.value[index] = uiReservation
      }
      
      if (currentReservation.value?.id === id) {
        currentReservation.value = uiReservation
      }
      
      return uiReservation
    } catch (err: unknown) {
      const apiError = err as ApiError
      error.value = apiError.message || 'Erreur lors de la mise à jour de la réservation'
      console.error('Erreur lors de la mise à jour de la réservation:', err)
      return null
    } finally {
      loading.value = false
    }
  }

  async function deleteReservation(id: string) {
    try {
      loading.value = true
      error.value = null
      await reservationService.deleteReservation(id)
      
      reservations.value = reservations.value.filter(r => r.id !== id)
      
      if (currentReservation.value?.id === id) {
        currentReservation.value = null
      }
      
      return true
    } catch (err: unknown) {
      const apiError = err as ApiError
      error.value = apiError.message || 'Erreur lors de la suppression de la réservation'
      console.error('Erreur lors de la suppression de la réservation:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  async function registerToReservation(id: string) {
    try {
      loading.value = true
      error.value = null
      await reservationService.registerToReservation(id)
      
  // Optimisation: rafraîchir uniquement la liste des joueurs
  await refreshPlayers(id)
      
      return true
    } catch (err: unknown) {
      const apiError = err as ApiError
      error.value = apiError.message || 'Erreur lors de l\'inscription à la réservation'
      console.error('Erreur lors de l\'inscription à la réservation:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  async function unregisterFromReservation(id: string) {
    try {
      loading.value = true
      error.value = null
      await reservationService.unregisterFromReservation(id)
      
  // Optimisation: rafraîchir uniquement la liste des joueurs
  await refreshPlayers(id)
      
      return true
    } catch (err: unknown) {
      const apiError = err as ApiError
      error.value = apiError.message || 'Erreur lors de la désinscription de la réservation'
      console.error('Erreur lors de la désinscription de la réservation:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  async function addComment(reservationId: string, comment: CommentRequest) {
    try {
      loading.value = true
      error.value = null
      await reservationService.addComment(reservationId, comment)
      
      // Recharger la réservation pour avoir les commentaires mis à jour
      await fetchReservation(reservationId)
      
      return true
    } catch (err: unknown) {
      const apiError = err as ApiError
      error.value = apiError.message || 'Erreur lors de l\'ajout du commentaire'
      console.error('Erreur lors de l\'ajout du commentaire:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  async function getReservationPlayers(id: string): Promise<string[]> {
    try {
      const playersResponse: unknown = await reservationService.getReservationPlayers(id)
      // Normaliser différents formats potentiels de réponse:
      // 1. Array<Link>
      // 2. { players: Array<Link> }
      // 3. Array<string>
      // 4. { players: Array<string> }
      let linkObjects: Array<{ link: string }> = []
      if (Array.isArray(playersResponse)) {
        if (playersResponse.length > 0 && typeof playersResponse[0] === 'string') {
          linkObjects = (playersResponse as string[]).map(s => ({ link: s }))
        } else {
          linkObjects = playersResponse as Array<{ link: string }>
        }
      } else if (playersResponse && typeof playersResponse === 'object') {
        interface PlayersWrapper { players?: Array<{ link: string }> | string[] }
        const pr = playersResponse as PlayersWrapper
        if (Array.isArray(pr.players)) {
          if (pr.players.length > 0 && typeof pr.players[0] === 'string') {
            linkObjects = (pr.players as string[]).map((s) => ({ link: s }))
          } else {
            linkObjects = pr.players as Array<{ link: string }>
          }
        }
      }

      const ids = linkObjects
        .filter(l => typeof l?.link === 'string')
        .map(l => l.link.split('/').pop() || '')
        .filter(v => v)

      if (linkObjects.length === 0) {
        console.warn('Format inattendu pour les joueurs de la réservation', id, playersResponse)
      }
      return ids
    } catch (err: unknown) {
      const apiError = err as ApiError
      error.value = apiError.message || 'Erreur lors du chargement des joueurs'
      console.error('Erreur lors du chargement des joueurs:', err)
      return []
    }
  }

  // Rafraîchir uniquement la liste des joueurs pour une réservation (sans recharger tout l'objet)
  async function refreshPlayers(id: string): Promise<string[]> {
    try {
      const playerIds = await getReservationPlayers(id)
      // Mettre à jour la réservation dans la liste
      const index = reservations.value.findIndex(r => r.id === id)
      if (index !== -1) {
        reservations.value[index] = { ...reservations.value[index], players: playerIds }
      }
      // Mettre à jour la réservation courante si nécessaire
      if (currentReservation.value?.id === id) {
        currentReservation.value = { ...currentReservation.value, players: playerIds }
      }
      return playerIds
    } catch (err: unknown) {
      const apiError = err as ApiError
      error.value = apiError.message || 'Erreur lors de l\'actualisation des joueurs'
      console.error('Erreur lors de l\'actualisation des joueurs:', err)
      return []
    }
  }

  function clearError() {
    error.value = null
  }

  function clearCurrentReservation() {
    currentReservation.value = null
  }

  return {
    // State
    reservations,
    currentReservation,
    loading,
    error,
    
    // Computed
    activeReservations,
    completedReservations,
    
    // Actions
    fetchReservations,
    fetchReservation,
    createReservation,
    updateReservation,
    deleteReservation,
    registerToReservation,
    unregisterFromReservation,
    addComment,
    getReservationPlayers,
  refreshPlayers,
    clearError,
    clearCurrentReservation
  }
})
