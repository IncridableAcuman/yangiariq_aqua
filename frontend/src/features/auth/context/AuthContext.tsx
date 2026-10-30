import React, { createContext, useContext, useEffect, useState, useCallback } from 'react'
import { UserProfile, UpdateUserProfilePayload } from '@/entities/user/types'
import { getCurrentUserProfile, updateCurrentUserProfile } from '@/entities/user/api'
import {
  LoginCredentials,
  RegisterCredentials,
  ChangePasswordPayload,
} from '../types'
import {
  loginUser,
  registerUser,
  logoutUser,
  changeUserPassword,
  requestPasswordReset,
  confirmPasswordReset,
} from '../api'
import { authStorage } from '../storage'

interface AuthContextType {
  user: UserProfile | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (credentials: LoginCredentials) => Promise<void>
  register: (credentials: RegisterCredentials) => Promise<void>
  logout: () => Promise<void>
  updateProfile: (payload: UpdateUserProfilePayload) => Promise<UserProfile>
  changePassword: (payload: ChangePasswordPayload) => Promise<void>
  requestReset: (identifier: string) => Promise<void>
  confirmReset: (token: string, newPassword: string) => Promise<void>
  refreshUser: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserProfile | null>(null)
  const [isLoading, setIsLoading] = useState<boolean>(true)

  const refreshUser = useCallback(async () => {
    if (!authStorage.getAccessToken()) {
      setUser(null)
      setIsLoading(false)
      return
    }

    try {
      const profile = await getCurrentUserProfile()
      setUser(profile)
    } catch {
      // If fetching profile fails (e.g., token revoked or expired), clear state
      authStorage.clearTokens()
      setUser(null)
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    refreshUser()

    const handleUnauthorized = () => {
      setUser(null)
    }

    const handleAuthChange = () => {
      if (!authStorage.getAccessToken()) {
        setUser(null)
      }
    }

    window.addEventListener('aqua:unauthorized', handleUnauthorized)
    window.addEventListener('aqua:auth-change', handleAuthChange)

    return () => {
      window.removeEventListener('aqua:unauthorized', handleUnauthorized)
      window.removeEventListener('aqua:auth-change', handleAuthChange)
    }
  }, [refreshUser])

  const login = async (credentials: LoginCredentials) => {
    setIsLoading(true)
    try {
      const authResponse = await loginUser(credentials)
      authStorage.setTokens(authResponse.accessToken, authResponse.refreshToken)
      setUser(authResponse.user)
    } finally {
      setIsLoading(false)
    }
  }

  const register = async (credentials: RegisterCredentials) => {
    setIsLoading(true)
    try {
      const authResponse = await registerUser(credentials)
      authStorage.setTokens(authResponse.accessToken, authResponse.refreshToken)
      setUser(authResponse.user)
    } finally {
      setIsLoading(false)
    }
  }

  const logout = async () => {
    const refreshToken = authStorage.getRefreshToken()
    authStorage.clearTokens()
    setUser(null)
    if (refreshToken) {
      await logoutUser(refreshToken)
    }
  }

  const updateProfile = async (payload: UpdateUserProfilePayload): Promise<UserProfile> => {
    const updated = await updateCurrentUserProfile(payload)
    setUser(updated)
    return updated
  }

  const changePassword = async (payload: ChangePasswordPayload) => {
    await changeUserPassword(payload)
    // Changing password revokes sessions on backend; re-fetch or keep current JWT
  }

  const requestReset = async (identifier: string) => {
    await requestPasswordReset({ identifier })
  }

  const confirmReset = async (token: string, newPassword: string) => {
    await confirmPasswordReset({ token, newPassword })
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: Boolean(user),
        isLoading,
        login,
        register,
        logout,
        updateProfile,
        changePassword,
        requestReset,
        confirmReset,
        refreshUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
