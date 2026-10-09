type ShareIconProps = {
  size?: number
}

/** Share mark on the order-code field. */
export function ShareIcon({ size = 24 }: ShareIconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M14.7073 17.6429L8.72754 14.1905M8.72754 10.8486L14.7073 7.39624"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeMiterlimit="2.6131"
      />
      <path
        d="M19.9641 3.36286C21.269 4.66776 21.269 6.78341 19.9641 8.08831C18.6592 9.3932 16.5436 9.3932 15.2387 8.08831C13.9338 6.78341 13.9338 4.66776 15.2387 3.36286C16.5436 2.05797 18.6592 2.05797 19.9641 3.36286Z"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeMiterlimit="2.6131"
      />
      <path
        d="M17.965 15.9928C19.7994 16.1941 21.1232 17.8444 20.9219 19.6787C20.7205 21.5131 19.0703 22.8369 17.2359 22.6355C15.4016 22.4342 14.0778 20.784 14.2791 18.9496C14.4804 17.1153 16.1307 15.7915 17.965 15.9928Z"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeMiterlimit="2.6131"
      />
      <path
        d="M6.36892 9.22137C8.19053 9.517 9.42758 11.2334 9.13194 13.055C8.83631 14.8766 7.11994 16.1136 5.29832 15.818C3.47671 15.5224 2.23966 13.806 2.5353 11.9844C2.83093 10.1628 4.5473 8.92573 6.36892 9.22137Z"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeMiterlimit="2.6131"
      />
    </svg>
  )
}
