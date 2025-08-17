// Types pour l'authentification
export interface User {
  login: string
  name: string
  ownedReservations?: string[]
  registeredReservations?: string[]
}

export interface LoginRequest {
  login: string
  name: string // Le "name" sert de mot de passe dans cette API
}

// Types pour les réservations de billard (API)
export interface Reservation {
  tableId: string
  ownerId: string
  startTime: string // ISO date string
  endTime: string   // ISO date string
  players: Link[]   // Links vers les utilisateurs inscrits
  comments: Comment[]
}

export interface ReservationRequest {
  tableId: string
  startTime: string // ISO date string
  endTime: string   // ISO date string
}

// Types pour l'interface utilisateur (adaptés pour le frontend)
export interface ReservationUI {
  id: string
  tableId: string
  ownerId: string
  startTime: string
  endTime: string
  duration: number // calculé en minutes
  players: string[] // noms des joueurs
  comments: Comment[]
  title?: string // généré pour l'UI
}

export interface ReservationCreateRequest {
  tableId: number
  startTime: string
  duration: number // en minutes, sera converti en endTime
}

export interface Comment {
  authorId: string
  content: string
}

export interface CommentRequest {
  content: string
}

export interface Link {
  link: string
}

// Types pour les réponses API
export interface ApiResponse<T> {
  data: T
  message?: string
}

export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}

// Types pour les erreurs
export interface ApiError {
  message: string
  status: number
  timestamp: string
}

// Santé système
export interface HealthStatus {
  status: string
  [key: string]: unknown
}