import {
  NfcIcon,
  UserScanIcon,
  VneidIcon,
  type IdentityMethodOption,
} from '@mbccs/modules-bccs/identity'
import { lk } from '@/i18n/key'

/** Preview subscriber until the lookup API is wired. */
export const PREVIEW_SUBSCRIBER = {
  initials: 'TD',
  phone: '0364 123 456',
} as const

export const CustomerSegment = {
  DomesticIndividual: 'domestic-individual',
  ForeignIndividual: 'foreign-individual',
  DomesticOrganization: 'domestic-organization',
  ForeignOrganization: 'foreign-organization',
} as const

export type CustomerSegment =
  (typeof CustomerSegment)[keyof typeof CustomerSegment]

type Translate = (key: string) => string

const SEGMENTS = new Set<string>(Object.values(CustomerSegment))

export function parseCustomerSegment(value: string | null): CustomerSegment {
  if (value != null && SEGMENTS.has(value)) return value as CustomerSegment
  return CustomerSegment.DomesticIndividual
}

function buildMethods(
  segment: CustomerSegment,
  t: Translate,
): IdentityMethodOption[] {
  if (segment === CustomerSegment.ForeignIndividual) {
    return [
      {
        id: 'direct-capture',
        icon: <UserScanIcon />,
        title: t(lk.verifyDevice.captureTitle),
        description: t(lk.verifyDevice.capturePassport),
      },
    ]
  }

  if (segment === CustomerSegment.ForeignOrganization) {
    return [
      {
        id: 'direct-capture',
        icon: <UserScanIcon />,
        title: t(lk.verifyDevice.captureTitle),
        description: t(lk.verifyDevice.captureLicense),
      },
    ]
  }

  return [
    {
      id: 'nfc',
      icon: <NfcIcon />,
      title: t(lk.verifyDevice.nfcTitle),
      description: t(lk.verifyDevice.nfcDescription),
    },
    {
      id: 'vneid',
      icon: <VneidIcon />,
      title: t(lk.verifyDevice.vneidTitle),
      description: t(lk.verifyDevice.vneidDescription),
    },
  ]
}

const DESCRIPTION_KEY: Record<CustomerSegment, string> = {
  [CustomerSegment.DomesticIndividual]: lk.verifyDevice.domesticIndividual,
  [CustomerSegment.ForeignIndividual]: lk.verifyDevice.foreignIndividual,
  [CustomerSegment.DomesticOrganization]: lk.verifyDevice.domesticOrganization,
  [CustomerSegment.ForeignOrganization]: lk.verifyDevice.foreignOrganization,
}

export function resolveIdentityContent(segment: CustomerSegment, t: Translate) {
  return {
    description: t(DESCRIPTION_KEY[segment]),
    methods: buildMethods(segment, t),
  }
}
