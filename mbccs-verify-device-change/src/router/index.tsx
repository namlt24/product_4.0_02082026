import { createHashRouter } from 'react-router-dom'
import { RouteErrorFallback } from '@mbccs/runtime/feedback'
import App from '@/App'

const router = createHashRouter([
  {
    path: '/',
    element: <App />,
    errorElement: <RouteErrorFallback />,
    children: [
      {
        index: true,
        lazy: async () => {
          const mod = await import('@/pages/Home')
          return { Component: mod.default }
        },
      },
      {
        path: 'isdn',
        lazy: async () => {
          const mod = await import('@/pages/Isdn')
          return { Component: mod.default }
        },
      },
      {
        path: 'orders',
        lazy: async () => {
          const mod = await import('@/pages/Orders')
          return { Component: mod.default }
        },
      },
      {
        path: 'orders/detail',
        lazy: async () => {
          const mod = await import('@/pages/OrderDetail')
          return { Component: mod.default }
        },
      },
      {
        path: 'result',
        lazy: async () => {
          const mod = await import('@/pages/Result')
          return { Component: mod.default }
        },
      },
    ],
  },
])

export default router
