type IdentityIconProps = {
  size?: number
}

/** NFC mark from the identity-method row (phone + signal). */
export function NfcIcon({ size = 24 }: IdentityIconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M14.75 1C19.16 1 20.5 2.34 20.5 6.75V16.75C20.5 21.16 19.16 22.5 14.75 22.5H8.75C4.34 22.5 3 21.16 3 16.75V6.75C3 2.34 4.34 1 8.75 1H14.75ZM8.75 2.5C5.17 2.5 4.5 3.18 4.5 6.75V16.75C4.5 20.32 5.17 21 8.75 21H14.75C18.33 21 19 20.32 19 16.75V6.75C19 3.18 18.33 2.5 14.75 2.5H8.75Z"
        fill="currentColor"
      />
      <path
        d="M8.51587 13.8174C10.6286 12.1862 13.3752 12.1862 15.4879 13.8174"
        stroke="currentColor"
        strokeWidth="0.737"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M7.08618 12.1075C10.0637 9.80807 13.9354 9.80807 16.9128 12.1075"
        stroke="currentColor"
        strokeWidth="0.737"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M9.4397 15.6107C10.9874 14.4118 13.0068 14.4118 14.5545 15.6107"
        stroke="currentColor"
        strokeWidth="0.737"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M10.7219 17.409C11.4982 16.8096 12.5055 16.8096 13.2818 17.409"
        stroke="currentColor"
        strokeWidth="0.737"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}
