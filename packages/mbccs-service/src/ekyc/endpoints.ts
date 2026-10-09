/** Internal use by api.ts. */
export const endpoints = {
  validateNfc: {
    method: 'POST',
    path: '/services/mbccsekyc/api/validate-nfc',
  },
  ocr: {
    method: 'POST',
    path: '/services/mbccsekyc/api/ocr',
  },
} as const
