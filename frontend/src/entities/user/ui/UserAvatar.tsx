import React from 'react'
import { UserProfile, UserRole } from '../types'
import { Avatar, AvatarFallback } from '@/shared/ui/avatar'
import { User } from 'lucide-react'
import { cn } from '@/shared/lib/utils'

interface UserAvatarProps {
  user?: Partial<UserProfile> | null
  size?: 'sm' | 'md' | 'lg'
  className?: string
}

export const UserAvatar: React.FC<UserAvatarProps> = ({
  user,
  size = 'md',
  className = '',
}) => {
  const sizeClasses = {
    sm: 'h-8 w-8 text-xs',
    md: 'h-10 w-10 text-sm',
    lg: 'h-14 w-14 text-base font-semibold',
  }[size]

  if (!user) {
    return (
      <Avatar className={cn(sizeClasses, className)}>
        <AvatarFallback>
          <User className={size === 'sm' ? 'h-4 w-4' : size === 'md' ? 'h-5 w-5' : 'h-7 w-7'} />
        </AvatarFallback>
      </Avatar>
    )
  }

  const initials =
    user.firstName && user.lastName
      ? `${user.firstName[0]}${user.lastName[0]}`.toUpperCase()
      : user.firstName
      ? user.firstName.slice(0, 2).toUpperCase()
      : user.phoneNumber
      ? user.phoneNumber.slice(-2)
      : 'U'

  const roleColors: Record<UserRole, string> = {
    FARMER: 'bg-emerald-600 text-white',
    OPERATOR: 'bg-sky-600 text-white',
    ADMIN: 'bg-purple-600 text-white',
    SUPER_ADMIN: 'bg-rose-700 text-white',
  }

  const colorClass = user.role ? roleColors[user.role] : 'bg-primary text-primary-foreground'

  return (
    <Avatar className={cn(sizeClasses, 'border-2 border-background shadow-xs', className)} title={user.fullName || user.phoneNumber}>
      <AvatarFallback className={cn('font-bold select-none', colorClass)}>
        {initials}
      </AvatarFallback>
    </Avatar>
  )
}
