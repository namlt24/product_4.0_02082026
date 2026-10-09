type IconProps = {
  size?: number
}

/** Customer section mark. */
export function DetailUserIcon({ size = 24 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M17 7C17 9.76142 14.7614 12 12 12C9.23858 12 7 9.76142 7 7C7 4.23858 9.23858 2 12 2C14.7614 2 17 4.23858 17 7Z"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M20.5901 22C20.5901 18.13 16.7402 15 12.0002 15C7.26015 15 3.41016 18.13 3.41016 22"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

/** Order section mark. */
export function DetailBagIcon({ size = 24 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M8.80994 2L5.18994 5.63"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeMiterlimit="10"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M15.1899 2L18.8099 5.63"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeMiterlimit="10"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M2 7.84998C2 5.99998 2.99 5.84998 4.22 5.84998H19.78C21.01 5.84998 22 5.99998 22 7.84998C22 9.99998 21.01 9.84998 19.78 9.84998H4.22C2.99 9.84998 2 9.99998 2 7.84998Z"
        stroke="currentColor"
        strokeWidth="1.5"
      />
      <path d="M9.75977 14V17.55" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
      <path d="M14.3599 14V17.55" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
      <path
        d="M3.5 10L4.91 18.64C5.23 20.58 6 22 8.86 22H14.89C18 22 18.46 20.64 18.82 18.76L20.5 10"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
    </svg>
  )
}

/** Customer-photo section mark. */
export function DetailScanIcon({ size = 24 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M2 8.99963V6.49963C2 4.00963 4.01 1.99963 6.5 1.99963H9M15 1.99963H17.5C19.99 1.99963 22 4.00963 22 6.49963V8.99963M22 15.9996V17.4996C22 19.9896 19.99 21.9996 17.5 21.9996H16M9 21.9996H6.5C4.01 21.9996 2 19.9896 2 17.4996V14.9996M12.0073 10.4448C11.9496 10.4448 11.8776 10.4448 11.8199 10.4448C10.4652 10.4016 9.38427 9.2774 9.38427 7.90825C9.38427 6.51027 10.5228 5.3717 11.9208 5.3717C13.3188 5.3717 14.4574 6.51027 14.4574 7.90825C14.4429 9.29181 13.362 10.4016 12.0073 10.4448ZM9.13924 14.4804C7.75568 15.4028 7.75568 16.9161 9.13924 17.8384C10.7102 18.8905 13.2899 18.8905 14.8608 17.8384C16.2444 16.9161 16.2444 15.4028 14.8608 14.4804C13.3043 13.4284 10.7246 13.4284 9.13924 14.4804Z"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

/** Identity-document section mark. */
export function DetailIdCardIcon({ size = 24 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M17 21H7C3 21 2 20 2 16V8C2 4 3 3 7 3H17C21 3 22 4 22 8V16C22 20 21 21 17 21Z"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path d="M14 8H19" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M15 12H19" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M17 16H19" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
      <path
        d="M10.3099 9.47998C10.3099 10.4796 9.49958 11.29 8.49994 11.29C7.50031 11.29 6.68994 10.4796 6.68994 9.47998C6.68994 8.48035 7.50031 7.66998 8.49994 7.66998C9.49958 7.66998 10.3099 8.48035 10.3099 9.47998Z"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M12 16.33C11.86 14.88 10.71 13.74 9.26 13.61C8.76 13.56 8.25 13.56 7.74 13.61C6.29 13.75 5.14 14.88 5 16.33"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}
