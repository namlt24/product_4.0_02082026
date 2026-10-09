import { initI18n, VI, EN } from '@mbccs/runtime/i18n'
import { en } from './en'
import { vi } from './vi'

export { vi, type Messages } from './vi'
export { en } from './en'
export {
  VI,
  EN,
  t,
  useTranslation,
  setAppLanguage,
  getAppLanguage,
  supportedLanguages,
} from '@mbccs/runtime/i18n'

initI18n({
  [VI]: vi,
  [EN]: en,
})
