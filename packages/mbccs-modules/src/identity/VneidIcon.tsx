import { vneidIconSrc } from './vneidIconSrc.js'

type VneidIconProps = {
  size?: number
}

/** VNeID mark used on the identity-method row. The row title carries the accessible name. */
export function VneidIcon({ size = 24 }: VneidIconProps) {
  return (
    <img
      src={vneidIconSrc}
      alt=""
      width={size}
      height={size}
      draggable={false}
    />
  )
}
