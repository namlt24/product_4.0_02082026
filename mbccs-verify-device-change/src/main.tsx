import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router-dom'
import '@mbccs/design-system/styles.css'
import './index.css'
import './i18n'
import { ErrorBoundary, mountFatalDialog } from '@mbccs/runtime/feedback'
import { t } from './i18n'
import router from './router'

const root = document.getElementById('root')
if (!root) {
  mountFatalDialog({
    title: t('rootNotFoundTitle'),
    message: t('rootNotFoundMessage'),
    actionText: t('close'),
  })
} else {
  createRoot(root).render(
    <StrictMode>
      <div className="relative h-full">
        <ErrorBoundary>
          <RouterProvider router={router} />
        </ErrorBoundary>
      </div>
    </StrictMode>,
  )
}
