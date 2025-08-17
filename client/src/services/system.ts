import api from './api'

export interface HealthStatus {
  status: string
  [key: string]: unknown
}

export const systemService = {
  async getHealth(): Promise<HealthStatus> {
    const res = await api.get('/actuator/health')
    return res.data
  }
}
