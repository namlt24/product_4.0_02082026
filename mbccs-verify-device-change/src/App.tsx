import { Outlet } from 'react-router-dom'
import { PageLayout } from '@mbccs/runtime/chrome'
import { ErrorBoundary } from '@mbccs/runtime/feedback'
import { LoadingHost } from '@mbccs/runtime/feedback/loading'
import { DialogHost } from '@mbccs/runtime/feedback/dialog'
import { ToastHost } from '@mbccs/runtime/feedback/toast'
import { BottomSheetHost } from '@mbccs/runtime/feedback/bottomSheet'
import { BootstrapGate } from '@mbccs/runtime/bootstrap'
import { mockJsApiProvider } from '@/mocks/jsApiProvider'

const bootOptions = import.meta.env.DEV
  ? { jsApiProvider: mockJsApiProvider }
  : undefined

/**
 * Root layout inside RouterProvider — feedback hosts can use router context.
 * Page content is gated until Core.init boot succeeds.
 */
export default function App() {
  return (
    <ErrorBoundary>
      <LoadingHost />
      <DialogHost />
      <ToastHost />
      <BottomSheetHost />

      <BootstrapGate bootOptions={bootOptions}>
        <PageLayout>
          <Outlet />
        </PageLayout>
      </BootstrapGate>
    </ErrorBoundary>
  )
}
