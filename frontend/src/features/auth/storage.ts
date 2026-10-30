const ACCESS_TOKEN_KEY = 'aqua_access_token'
const REFRESH_TOKEN_KEY = 'aqua_refresh_token'

export const authStorage = {
  getAccessToken(): string | null {
    try {
      return localStorage.getItem(ACCESS_TOKEN_KEY)
    } catch {
      return null
    }
  },

  getRefreshToken(): string | null {
    try {
      return localStorage.getItem(REFRESH_TOKEN_KEY)
    } catch {
      return null
    }
  },

  setTokens(accessToken: string, refreshToken: string): void {
    try {
      localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
      localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
      window.dispatchEvent(new Event('aqua:auth-change'))
    } catch {
      // Storage might be unavailable (e.g. private mode)
    }
  },

  clearTokens(): void {
    try {
      localStorage.removeItem(ACCESS_TOKEN_KEY)
      localStorage.removeItem(REFRESH_TOKEN_KEY)
      window.dispatchEvent(new Event('aqua:auth-change'))
    } catch {
      // Storage might be unavailable
    }
  },

  hasTokens(): boolean {
    return Boolean(this.getAccessToken())
  },
}
