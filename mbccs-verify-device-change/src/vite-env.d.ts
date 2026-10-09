/// <reference types="vite/client" />

declare module '@mbccs/design-system/styles.css'

/**
 * Custom Vite env for this miniapp.
 * Built-ins (`DEV` / `PROD` / `MODE`) come from `vite/client` — do not redeclare.
 *
 * - `VITE_CONSOLE=open` — keep `console.*` in production builds (vite.config).
 */
interface ImportMetaEnv {
  readonly VITE_CONSOLE?: 'open'
}
