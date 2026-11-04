import { Component, ErrorInfo, ReactNode } from 'react'
import { AlertTriangle, RotateCcw } from 'lucide-react'
import { Button } from './button'

interface Props {
  children: ReactNode
  fallbackTitle?: string
  fallbackMessage?: string
}

interface State {
  hasError: boolean
  error: Error | null
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
  }

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error }
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo)
  }

  public render() {
    if (this.state.hasError) {
      return (
        <div className="p-6 rounded-xl border border-destructive/30 bg-destructive/5 text-center space-y-3 my-4">
          <div className="flex justify-center">
            <div className="p-2.5 rounded-full bg-destructive/10 text-destructive">
              <AlertTriangle className="h-6 w-6" />
            </div>
          </div>
          <h3 className="text-sm font-bold text-foreground">
            {this.props.fallbackTitle || 'Xatolik yuz berdi'}
          </h3>
          <p className="text-xs text-muted-foreground max-w-md mx-auto">
            {this.props.fallbackMessage ||
              this.state.error?.message ||
              'Ushbu modulni yuklashda kutilmagan xatolik yuz berdi.'}
          </p>
          <div className="pt-1">
            <Button
              size="sm"
              variant="outline"
              onClick={() => this.setState({ hasError: false, error: null })}
              className="gap-1.5 text-xs"
            >
              <RotateCcw className="h-3.5 w-3.5" />
              <span>Qayta urinish</span>
            </Button>
          </div>
        </div>
      )
    }

    return this.props.children
  }
}
