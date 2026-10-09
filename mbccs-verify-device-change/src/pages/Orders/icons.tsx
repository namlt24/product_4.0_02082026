type IconProps = {
  size?: number
}

/** Search mark in the order lookup field. */
export function SearchIcon({ size = 24 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M19.4697 19.4697C19.7626 19.1768 20.2374 19.1768 20.5303 19.4697L22.5303 21.4697C22.8232 21.7626 22.8232 22.2374 22.5303 22.5303C22.2374 22.8232 21.7626 22.8232 21.4697 22.5303L19.4697 20.5303C19.1768 20.2374 19.1768 19.7626 19.4697 19.4697ZM11.5 1.25C17.1609 1.25 21.75 5.83908 21.75 11.5C21.75 17.1609 17.1609 21.75 11.5 21.75C5.83908 21.75 1.25 17.1609 1.25 11.5C1.25 5.83908 5.83908 1.25 11.5 1.25ZM11.5 2.75C6.66751 2.75 2.75 6.66751 2.75 11.5C2.75 16.3325 6.66751 20.25 11.5 20.25C16.3325 20.25 20.25 16.3325 20.25 11.5C20.25 6.66751 16.3325 2.75 11.5 2.75Z"
        fill="currentColor"
      />
    </svg>
  )
}

/** Filter mark on the "Lọc" chip. */
export function FilterIcon({ size = 20 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 20 20" fill="none" aria-hidden>
      <path
        d="M17.2416 3.35007V5.20007C17.2416 5.87507 16.8166 6.71675 16.4 7.14175L14.9333 8.43341C14.6583 8.36674 14.3666 8.33341 14.0666 8.33341C11.8833 8.33341 10.1083 10.1084 10.1083 12.2917C10.1083 13.0334 10.3166 13.7334 10.6833 14.3334C10.9916 14.8501 11.4166 15.2918 11.9333 15.6084V15.8918C11.9333 16.4001 11.5999 17.0751 11.1749 17.3251L9.99997 18.0834C8.9083 18.7584 7.39163 18.0001 7.39163 16.6501V12.1918C7.39163 11.6001 7.04997 10.8418 6.71663 10.4251L3.51661 7.05842C3.09995 6.63342 2.7583 5.87509 2.7583 5.37509V3.43341C2.7583 2.42508 3.51665 1.66675 4.44165 1.66675H15.5583C16.4833 1.66675 17.2416 2.42507 17.2416 3.35007Z"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeMiterlimit="10"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M18.0251 12.2916C18.0251 13.0333 17.8251 13.7333 17.4584 14.3333C16.7751 15.4749 15.5167 16.2499 14.0667 16.2499C13.2834 16.2499 12.5501 16.0166 11.9334 15.6083C11.4167 15.2916 10.9917 14.8499 10.6834 14.3333C10.3167 13.7333 10.1084 13.0333 10.1084 12.2916C10.1084 10.1083 11.8834 8.33325 14.0667 8.33325C14.3667 8.33325 14.6584 8.36658 14.9334 8.43325C16.7084 8.82491 18.0251 10.4083 18.0251 12.2916Z"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeMiterlimit="10"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M12.5249 12.2917L13.4999 13.2667L15.6082 11.3167"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

/** Trailing chevron on a filter chip. */
export function ChevronDownIcon({ size = 20 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 20 20" fill="none" aria-hidden>
      <path
        d="M14.4697 6.96973C14.7626 6.67683 15.2374 6.67683 15.5303 6.96973C15.8231 7.26263 15.8231 7.73742 15.5303 8.03027L10.5303 13.0303C10.2374 13.3231 9.76263 13.3231 9.46973 13.0303L4.46973 8.03027C4.17683 7.73738 4.17683 7.26262 4.46973 6.96973C4.76262 6.67683 5.23738 6.67683 5.53027 6.96973L10 11.4395L14.4697 6.96973Z"
        fill="currentColor"
      />
    </svg>
  )
}

/** Order-code mark on a card footer. */
export function DocumentIcon({ size = 16 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 16 16" fill="none" aria-hidden>
      <path
        d="M14 4.66659V11.3333C14 13.3333 13 14.6666 10.6667 14.6666H5.33333C3 14.6666 2 13.3333 2 11.3333V4.66659C2 2.66659 3 1.33325 5.33333 1.33325H10.6667C13 1.33325 14 2.66659 14 4.66659Z"
        stroke="currentColor"
        strokeWidth="1"
        strokeMiterlimit="10"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M9.6665 3V4.33333C9.6665 5.06667 10.2665 5.66667 10.9998 5.66667H12.3332"
        stroke="currentColor"
        strokeWidth="1"
        strokeMiterlimit="10"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M6.66683 8.66675L5.3335 10.0001L6.66683 11.3334"
        stroke="currentColor"
        strokeWidth="1"
        strokeMiterlimit="10"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M9.3335 8.66675L10.6668 10.0001L9.3335 11.3334"
        stroke="currentColor"
        strokeWidth="1"
        strokeMiterlimit="10"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

/** Timestamp mark on a card footer. */
export function CalendarIcon({ size = 16 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 16 16" fill="none" aria-hidden>
      <path
        d="M5.3335 1.33325V3.33325"
        stroke="currentColor"
        strokeWidth="1"
        strokeMiterlimit="10"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M10.6665 1.33325V3.33325"
        stroke="currentColor"
        strokeWidth="1"
        strokeMiterlimit="10"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M2.3335 6.06006H13.6668"
        stroke="currentColor"
        strokeWidth="1"
        strokeMiterlimit="10"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M14 5.66659V11.3333C14 13.3333 13 14.6666 10.6667 14.6666H5.33333C3 14.6666 2 13.3333 2 11.3333V5.66659C2 3.66659 3 2.33325 5.33333 2.33325H10.6667C13 2.33325 14 3.66659 14 5.66659Z"
        stroke="currentColor"
        strokeWidth="1"
        strokeMiterlimit="10"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path d="M10.463 9.13338H10.469" stroke="currentColor" strokeWidth="1.33333" strokeLinecap="round" />
      <path d="M10.463 11.1334H10.469" stroke="currentColor" strokeWidth="1.33333" strokeLinecap="round" />
      <path d="M7.99715 9.13338H8.00314" stroke="currentColor" strokeWidth="1.33333" strokeLinecap="round" />
      <path d="M7.99715 11.1334H8.00314" stroke="currentColor" strokeWidth="1.33333" strokeLinecap="round" />
      <path d="M5.52938 9.13338H5.53537" stroke="currentColor" strokeWidth="1.33333" strokeLinecap="round" />
      <path d="M5.52938 11.1334H5.53537" stroke="currentColor" strokeWidth="1.33333" strokeLinecap="round" />
    </svg>
  )
}
