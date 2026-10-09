# mbccs-verify-device-change

Vite + React 19 + TypeScript starter for BCCS H5 miniapps (Alibaba SuperApp / EMAS). Clone this folder for every new app.

Lives in **mbccs-miniapp-platform**; domain repos (`bccs` / `gboc`) clone from here. SDKs are **separate sibling repos**, linked via `pnpm-workspace.yaml` overrides.

## Scripts

From the miniapp workspace root (platform or domain repo):

```bash
pnpm dev:mbccs-verify-device-change
pnpm --filter mbccs-verify-device-change build
pnpm --filter mbccs-verify-device-change preview

pnpm build:miniapp -- --miniapp mbccs-verify-device-change
```

| Script | What it does |
|--------|----------------|
| `dev` | Vite (`host: 0.0.0.0`) — `mbccs-sdk-dev` builds/watches sibling `@mbccs/*`, full-reload on SDK `dist` changes |
| `build` | `oxlint` + `tsc --noEmit` + Vite → `dist/` |
| `preview` | Serve production `dist/` |

`build:miniapp` force-rebuilds SDKs, syncs `manifest.json`, writes `dist/manifest.json`, zips to `scripts/release/<app>-v<version>.zip`.

## Stack

Vite 8 · React 19 · TypeScript · Tailwind 4 · hash router · i18next · Zustand · Oxlint

Import **chỉ subpath** `@mbccs/*` — oxlint cấm package root (`@mbccs/core`, `@mbccs/design-system`, `@mbccs/runtime`, team `@mbccs/service-{team}` / `@mbccs/modules-{team}`) và `@mbccs/*/src/**`. Catalog đầy đủ: README từng SDK.

```ts
import { BccsButton } from '@mbccs/design-system/components'
import '@mbccs/design-system/styles.css'
import { Core } from '@mbccs/core/core'
import { BccsError, CoreErrorCode } from '@mbccs/core/common'
import { wvBccsApp, wvMiniApp, wvNavigationBar } from '@mbccs/core/jsapi/bindings'
import { ErrorBoundary } from '@mbccs/runtime/feedback'
import { LoadingHost } from '@mbccs/runtime/feedback/loading'
import { DialogHost } from '@mbccs/runtime/feedback/dialog'
import { PageLayout } from '@mbccs/runtime/chrome'
import { setAppLanguage, useTranslation } from '@/i18n'
```

| Package | Subpath thường dùng |
|---------|---------------------|
| `@mbccs/core` | `/core`, `/common`, `/network`, `/jsapi`, `/jsapi/bindings`, `/parse`, `/context` |
| `@mbccs/design-system` | `/components`, `/foundation`, `/assets/icons`, `/styles.css` |
| `@mbccs/service-bccs` | `/ekyc` (domain HTTP) — team monorepo only |
| `@mbccs/modules-bccs` | `/otp`, `/address` — reusable flows |
| `@mbccs/runtime` | `/chrome`, `/bootstrap`, `/shell`, `/feedback/*`, `/i18n`, `/events` |

Platform SDKs: `@mbccs/core` · `@mbccs/design-system` · `@mbccs/runtime`.

## Layout

```text
src/
├── main.tsx          # ErrorBoundary → RouterProvider
├── App.tsx           # feedback hosts + BootstrapGate + PageLayout + Outlet
├── index.css         # Tailwind + --safe-area-bottom
├── mocks/            # JSAPI mock (Vite DEV only)
├── router/           # createHashRouter
├── pages/            # Screens (Home)
├── components/       # App-local UI — add when needed
├── i18n/             # vi / en — public API only
├── store/            # optional Zustand — add when needed
└── assets/
```

## Boot

On open: loading → JSAPI → `Core.init` → language → `BootstrapGate` binds auth bridge → routes.

1. Vite `DEV` → mock JSAPI; production → WindVane
2. `wvBccsApp.getBccsAppContext()`
3. Success → `Core.init` → `setAppLanguage` → render
4. Failure → dialog → `wvMiniApp.close()`

`VITE_CONSOLE=open` keeps `console.*` in production minify.

### Safe area

Template uses `viewport-fit=cover` and CSS `--safe-area-bottom`. Host `getSafeAreaInsets` is imperfect — prefer host chrome; runtime `PageLayout` is full-screen for now.

### Navigation bar

```ts
import { wvNavigationBar } from '@mbccs/core/jsapi/bindings'

void wvNavigationBar.update({ title: 'Order detail' })
```

## i18n

Owned by the miniapp (`src/i18n/`). Core only stores `language` for `Accept-Language`.

| Locales | Codes |
|---------|-------|
| Vietnamese, English | `vi`, `en` (exact) |

Host `language` applied via runtime `setAppLanguage` (empty → `vi`). Do not import `i18next` / `react-i18next` outside `src/i18n/` (oxlint).

```ts
import { initI18n, VI, EN, useTranslation } from '@mbccs/runtime/i18n'
import { vi } from './vi'
import { en } from './en'

initI18n({ [VI]: vi, [EN]: en })
```

Add a string: `lk` in `key.ts` plus both `locale/vi.ts` and `locale/en.ts`.

## Store (Zustand)

No store base. One feature → `src/store/<name>.ts` with `create`. Select fields: `useXxxStore((s) => s.field)`. Do not mirror Core fields. Use `persist` only for local drafts — never tokens.

## Manifest (EMAS)

Edit [`manifest.json`](./manifest.json) before upload.

| Field | Must set | Rule |
|-------|----------|------|
| `appId` | yes | SuperApp / EMAS id |
| `name` / `version` | auto | From `package.json` on clone / `build:miniapp` |
| `runtime.adapterId` | yes | `alibaba` (host) or `mock` (local only) |

Bump version in `package.json` only.

## Clone

```bash
pnpm clone:miniapp -- mbccs-order
pnpm install
pnpm dev:mbccs-order
```

Set `appId` / `adapterId`, then `pnpm build:miniapp -- --miniapp mbccs-order`.
