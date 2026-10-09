import { BccsError, CoreErrorCode, bytesToBase64 } from '@mbccs/core/common'
import type { IJsApiProvider } from '@mbccs/core/jsapi'
import {
  startNfcResultParser,
  WV_BCCS_SOCKET_ERRORS,
  WV_BCCS_SOCKET_EVENTS,
  WV_BCCS_SOCKET_STATES,
  type EkycCaptureType,
  type StartNfcResult,
} from '@mbccs/core/jsapi/bindings'

const MOCK_DELAY_MS = 1_000

const MOCK_APP_CONTEXT = {
  environment: 'DEV' as const,
  baseUrl: 'http://10.207.252.17:8002/mbccs-gateway/',
  timeoutMs: 30_000,
  imei: 'mock-imei',
  osType: 'MiniApp-Android',
  deviceName: 'playground',
  version: '1.0.0',
  language: 'vi',
  lat: '21.0285',
  long: '105.8542',
  flowId: 'mock-flow',
  username: 'admin',
  accessToken: 'mock-token',
  staffInfoToken: 'mock-staff-info-token',
}

/**
 * Public URL paths to mock image, e.g. `/mocks/cccd_front.jpeg`.
 * Leave `''` until the file exists — mock returns empty base64 like host without capture.
 * Needs to be in current project, avoid committing to git.
 */
export const MOCK_IMAGE_PATHS = {
  ID_CARD_FRONT: '',
  ID_CARD_BACK: '',
  PORTRAIT: '',
  PASSPORT: '',
} as const

export type MockImageKey = keyof typeof MOCK_IMAGE_PATHS

const base64Cache = new Map<string, string>()

function delay(ms: number): Promise<void> {
  return new Promise((resolve) => {
    setTimeout(resolve, ms)
  })
}

function ekycStepToImageKey(step: string): MockImageKey {
  switch (step as EkycCaptureType) {
    case 'ID_CARD_FRONT':
      return 'ID_CARD_FRONT'
    case 'ID_CARD_BACK':
      return 'ID_CARD_BACK'
    case 'PORTRAIT':
      return 'PORTRAIT'
    case 'PASSPORT':
      return 'PASSPORT'
    default:
      return 'ID_CARD_FRONT'
  }
}

/**
 * File path → raw base64 (no data-URL prefix), same shape as JSAPI image fields.
 * Empty path → `''`.
 */
async function loadMockImageBase64(path: string): Promise<string> {
  if (!path) return ''

  const cached = base64Cache.get(path)
  if (cached) return cached

  const response = await fetch(path)
  if (!response.ok) {
    throw new BccsError({
      code: CoreErrorCode.Platform.JSAPI_FAILED,
      message: `Mock image fetch failed (${response.status}): ${path}`,
    })
  }

  const bytes = new Uint8Array(await response.arrayBuffer())
  const base64 = bytesToBase64(bytes)
  base64Cache.set(path, base64)
  return base64
}

async function mockStartEkyc(params?: Record<string, any>) {
  const steps: string[] = Array.isArray(params?.steps) ? params.steps : []
  const images = await Promise.all(
    steps.map(async (step) => ({
      step,
      base64Data: await loadMockImageBase64(
        MOCK_IMAGE_PATHS[ekycStepToImageKey(step)],
      ),
    })),
  )
  return { provider: 'mock', images }
}

/** 
 * Public path to NFC payload JSON, e.g: '/mocks/mock_nfc.json'
 * Needs to be in current project, avoid committing to git.
*/
export const MOCK_NFC_PATH = ''

/** Fetch `MOCK_NFC_PATH` → parse as `StartNfcResult`. */
async function mockStartNfc(): Promise<StartNfcResult> {
  const response = await fetch(MOCK_NFC_PATH)
  if (!response.ok) {
    throw new BccsError({
      code: CoreErrorCode.Platform.JSAPI_FAILED,
      message: `Mock NFC fetch failed (${response.status}): ${MOCK_NFC_PATH}`,
    })
  }

  const raw: unknown = await response.json()
  const parsed = startNfcResultParser.safeParse(raw)
  if (!parsed.success) {
    throw new BccsError({
      code: CoreErrorCode.Platform.JSAPI_FAILED,
      message: parsed.error.message || 'Mock NFC JSON failed StartNfcResult parse',
    })
  }
  return parsed.data
}

type MockHandler = (params?: Record<string, any>) => Promise<unknown> | unknown

const mockAuthSettings: Record<string, boolean> = {
  location: true,
  camera: true,
  bluetooth: true,
  album: true,
  contacts: true,
  microphone: true,
  file: true,
  call: true,
  vibrate: true,
  screen: true,
}

const MAX_SOCKET_SUBSCRIPTIONS = 10

/**
 * Playground mock for `WVBccsSocket` host methods and DOM events.
 * Prefer `wvBccsSocket.startSession` over calling these raw handlers in order.
 */

type MockSocketSub = {
  subscriptionId: string
  destination: string
}

let mockSocketState: string = WV_BCCS_SOCKET_STATES.IDLE
let mockSocketSubs = new Map<string, MockSocketSub>()
let mockSocketSubSeq = 0

function emitSocketState(extra?: Record<string, unknown>): void {
  if (typeof document === 'undefined') return
  document.dispatchEvent(
    new CustomEvent(WV_BCCS_SOCKET_EVENTS.STATE, {
      detail: {
        state: mockSocketState,
        ...extra,
      },
    }),
  )
}

function emitSocketMessage(sub: MockSocketSub, body: string): void {
  if (typeof document === 'undefined') return
  document.dispatchEvent(
    new CustomEvent(WV_BCCS_SOCKET_EVENTS.MESSAGE, {
      detail: {
        subscriptionId: sub.subscriptionId,
        destination: sub.destination,
        headers: {},
        body,
        receivedAt: Date.now(),
      },
    }),
  )
}

function assertDestination(destination: unknown): string {
  if (typeof destination !== 'string' || !destination.trim()) {
    throw new BccsError({
      code: WV_BCCS_SOCKET_ERRORS.INVALID_DESTINATION,
      message: 'destination must be a non-empty string',
    })
  }
  return destination
}

function mockSocketConnect(params?: Record<string, any>) {
  const destinations = Array.isArray(params?.destinations)
    ? (params.destinations as unknown[])
    : []
  for (const d of destinations) {
    assertDestination(d)
  }
  if (destinations.length > MAX_SOCKET_SUBSCRIPTIONS) {
    throw new BccsError({
      code: WV_BCCS_SOCKET_ERRORS.LIMIT,
      message: `Max ${MAX_SOCKET_SUBSCRIPTIONS} subscriptions per session`,
    })
  }

  mockSocketSubs.clear()
  mockSocketState = WV_BCCS_SOCKET_STATES.CONNECTING
  emitSocketState()

  mockSocketState = WV_BCCS_SOCKET_STATES.CONNECTED
  const subscriptionIds: string[] = []
  for (const destination of destinations as string[]) {
    mockSocketSubSeq += 1
    const subscriptionId = `mock-sub-${mockSocketSubSeq}`
    mockSocketSubs.set(subscriptionId, { subscriptionId, destination })
    subscriptionIds.push(subscriptionId)
  }
  emitSocketState({ resubscribed: false })

  // After connect, emit a sample `VIDEO_CALL` body for `/topic/orders/{id}`.
  if (subscriptionIds.length > 0) {
    const first = mockSocketSubs.get(subscriptionIds[0]!)
    if (first) {
      setTimeout(() => {
        const orderMatch = /^\/topic\/orders\/(.+)$/.exec(first.destination)
        const body = orderMatch
          ? JSON.stringify({
              code: 'VIDEO_CALL',
              message: 'Ready for video call',
              data: { orderId: orderMatch[1], type: 'MOBILE' },
              requestId: `mock-req-${Date.now()}`,
              shortTraceId: 'mock-trace',
              timestamp: new Date().toISOString(),
            })
          : JSON.stringify({ mock: true, at: Date.now() })
        emitSocketMessage(first, body)
      }, 300)
    }
  }

  return subscriptionIds.length > 0
    ? { state: mockSocketState, subscriptionIds }
    : { state: mockSocketState }
}

function mockSocketSubscribe(params?: Record<string, any>) {
  if (mockSocketSubs.size >= MAX_SOCKET_SUBSCRIPTIONS) {
    throw new BccsError({
      code: WV_BCCS_SOCKET_ERRORS.LIMIT,
      message: `Max ${MAX_SOCKET_SUBSCRIPTIONS} subscriptions per session`,
    })
  }
  const destination = assertDestination(params?.destination)
  mockSocketSubSeq += 1
  const subscriptionId = `mock-sub-${mockSocketSubSeq}`
  mockSocketSubs.set(subscriptionId, { subscriptionId, destination })
  return { subscriptionId, state: mockSocketState }
}

const handlers: Record<string, MockHandler> = {
  'WVBccsApp.getBccsAppContext': () => MOCK_APP_CONTEXT,
  'WVBccsApp.checkNfcSupport': () => ({ supported: true, enabled: true }),
  'WVBccsApp.startEkyc': (params) => mockStartEkyc(params),
  'WVBccsApp.startNfc': () => mockStartNfc(),
  'WVBccsApp.chooseImages': (params) => {
    const maxCount = Math.min(Math.max(Number(params?.maxCount ?? 1), 1), 9)
    const needBase64 = Boolean(params?.needBase64)
    return {
      images: Array.from({ length: maxCount }).map((_, i) => ({
        url: `file:///media/external/images/mock_image_${i + 1}.jpg`,
        localPath: `/media/external/images/mock_image_${i + 1}.jpg`,
        name: `mock_image_${i + 1}.jpg`,
        size: 154200,
        ...(needBase64
          ? {
              base64Data:
                'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==',
            }
          : {}),
      })),
    }
  },
  'WVBccsApp.startVideoCall': (params) => ({
    provider: String(params?.provider ?? 'STRINGEE').toUpperCase(),
    orderId: String(params?.orderId ?? ''),
  }),

  'WVBccsSocket.connect': (params) => mockSocketConnect(params),
  'WVBccsSocket.disconnect': () => {
    mockSocketSubs.clear()
    mockSocketState = WV_BCCS_SOCKET_STATES.IDLE
    emitSocketState({ reason: 'idle' })
    return {}
  },
  'WVBccsSocket.subscribe': (params) => mockSocketSubscribe(params),
  'WVBccsSocket.unsubscribe': (params) => {
    const id = String(params?.subscriptionId ?? '').trim()
    if (!id) {
      throw new BccsError({
        code: WV_BCCS_SOCKET_ERRORS.INVALID_PARAM,
        message: 'subscriptionId must be a non-empty string',
      })
    }
    mockSocketSubs.delete(id)
    return {}
  },
  'WVBccsSocket.send': (params) => {
    if (mockSocketState !== WV_BCCS_SOCKET_STATES.CONNECTED) {
      throw new BccsError({
        code: WV_BCCS_SOCKET_ERRORS.NOT_CONNECTED,
        message: 'Socket is not connected',
      })
    }
    assertDestination(params?.destination)
    return {}
  },
  'WVBccsSocket.publish': (params) => {
    if (mockSocketState !== WV_BCCS_SOCKET_STATES.CONNECTED) {
      throw new BccsError({
        code: WV_BCCS_SOCKET_ERRORS.NOT_CONNECTED,
        message: 'Socket is not connected',
      })
    }
    assertDestination(params?.destination)
    return {}
  },
  'WVBccsSocket.getState': () => ({
    state: mockSocketState,
    ...(mockSocketState === WV_BCCS_SOCKET_STATES.IDLE
      ? { reason: 'idle' }
      : {}),
  }),

  'wv.getSetting': () => ({
    authSetting: { ...mockAuthSettings },
  }),
  'wv.authorize': (params) => {
    const scope = String(params?.scope ?? '')
    mockAuthSettings[scope] = true
    return {
      successScope: { [scope]: true },
    }
  },
  'wv.getAuthCode': (params) => {
    const scopes = Array.isArray(params?.scopes) ? params.scopes : ['USER_ID', 'USER_PHONE_NUMBER']
    return {
      authCode: `mock-auth-code-${Date.now()}`,
      authSuccessScopes: scopes,
      authErrorScopes: {},
    }
  },

  'WVMiniApp.close': () => ({}),
  'WVMiniApp.minimize': () => ({}),
  'WVMiniApp.navigateToMiniApp': (params) => ({
    opened: true,
    appId: params?.appId ?? '',
    path: params?.path,
    extraData: params?.extraData ?? {},
  }),
  'WVMiniApp.open': (params) => ({
    opened: true,
    appId: params?.appId ?? '',
    path: params?.path,
    extraData: params?.extraData ?? params?.params ?? {},
  }),

  'WVStandardEventCenter.postNotificationToNative': (params) => ({
    event: params?.event ?? '',
    param: params?.param ?? {},
  }),

  'WVNavigationBar.getHeight': () => ({ height: 56, statusBarHeight: 24 }),
  'WVNavigationBar.update': () => ({}),

  'WVNativeDetector.getSafeAreaInsets': () => ({
    top: 0,
    bottom: 0,
    left: 0,
    right: 0,
  }),

  'WVSystem.getSystemInfoSync': () => ({
    platform: 'android',
    appLanguage: 'vi',
    systemLanguage: 'vi',
    appVersion: '1.0.0',
    systemVersion: '14',
    model: 'playground',
    screenWidth: 390,
    screenHeight: 844,
    density: 2.75,
    userId: 'mock-user-123',
    deviceId: 'mock-device-uuid',
  }),

  'WVScan.scan': () => {
    const mockData = {
      content: 'https://viettel.vn/mock-qr-code-scan-result',
      format: 'QR_CODE',
    }
    if (typeof document !== 'undefined') {
      setTimeout(() => {
        document.dispatchEvent(
          new CustomEvent('WVScan.Event.scanSuccess', {
            detail: mockData,
          }),
        )
      }, 500)
    }
    return {}
  },
}

/** Dev/web mock when `window.WindVane` is absent. */
export const mockJsApiProvider: IJsApiProvider = {
  name: 'mock',

  async call<T = unknown>(
    service: string,
    method: string,
    params?: Record<string, any>,
  ): Promise<T> {
    const key = `${service}.${method}`
    const handler = handlers[key]
    if (!handler) {
      throw new BccsError({
        code: CoreErrorCode.Platform.JSAPI_FAILED,
        message: `JSAPI not mocked: ${key}`,
      })
    }

    await delay(MOCK_DELAY_MS)
    return (await handler(params)) as T
  },
}
