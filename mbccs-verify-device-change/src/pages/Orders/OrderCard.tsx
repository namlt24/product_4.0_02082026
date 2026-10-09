import { BccsText } from '@mbccs/design-system/components'
import { color } from '@mbccs/design-system/foundation'
import { CalendarIcon, DocumentIcon } from './icons'
import {
  resolveOrderCardBodyStyle,
  resolveOrderCardFooterStyle,
  resolveOrderCardHeaderStyle,
  resolveOrderCardRowStyle,
  resolveOrderCardStyle,
  resolveOrderMetaStyle,
  resolveOrderTagStyle,
} from './orders.styles'

export const OrderStatus = {
  InProgress: 'inProgress',
  Completed: 'completed',
  Cancelled: 'cancelled',
} as const

export type OrderStatus = (typeof OrderStatus)[keyof typeof OrderStatus]

export type OrderCardModel = {
  id: string
  serviceName: string
  status: OrderStatus
  statusLabel: string
  phone: string
  customerName: string
  orderCode: string
  createdAt: string
  phoneLabel: string
  customerLabel: string
}

const STATUS_COLOR: Record<OrderStatus, string> = {
  [OrderStatus.InProgress]: color.icon.semantics.warning.primary,
  [OrderStatus.Completed]: color.icon.semantics.success.primary,
  [OrderStatus.Cancelled]: color.icon.semantics.error.primary,
}

/** One order in Quản lý đơn hàng. */
export function OrderCard({ order }: { order: OrderCardModel }) {
  return (
    <article style={resolveOrderCardStyle()}>
      <header style={resolveOrderCardHeaderStyle()}>
        <BccsText
          as="h2"
          role="headingSm"
          text={order.serviceName}
          style={{ minWidth: 0, flex: '1 1 auto' }}
        />
        <span style={resolveOrderTagStyle(STATUS_COLOR[order.status])}>
          <BccsText
            role="textXs"
            weight="medium"
            color={color.text.neutrals.white}
            text={order.statusLabel}
          />
        </span>
      </header>
      <div style={resolveOrderCardBodyStyle()}>
        <div style={resolveOrderCardRowStyle()}>
          <BccsText
            role="textSm"
            color={color.text.neutrals.quaternary}
            text={order.phoneLabel}
            style={{ minWidth: 0, flex: '1 1 auto' }}
          />
          <BccsText
            role="textSm"
            weight="semibold"
            text={order.phone}
            textAlign="right"
            style={{ minWidth: 0, flex: '1 1 auto' }}
          />
        </div>
        <div style={resolveOrderCardRowStyle()}>
          <BccsText
            role="textSm"
            color={color.text.neutrals.quaternary}
            text={order.customerLabel}
            style={{ minWidth: 0, flex: '1 1 auto' }}
          />
          <BccsText
            role="textSm"
            weight="semibold"
            text={order.customerName}
            textAlign="right"
            style={{ minWidth: 0, flex: '1 1 auto' }}
          />
        </div>
      </div>
      <footer style={resolveOrderCardFooterStyle()}>
        <span style={resolveOrderMetaStyle()}>
          <DocumentIcon />
          <BccsText
            role="textXs"
            color={color.text.neutrals.secondary}
            text={order.orderCode}
          />
        </span>
        <span style={resolveOrderMetaStyle()}>
          <CalendarIcon />
          <BccsText
            role="textXs"
            color={color.text.neutrals.secondary}
            text={order.createdAt}
          />
        </span>
      </footer>
    </article>
  )
}
