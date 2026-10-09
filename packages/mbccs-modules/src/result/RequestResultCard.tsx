import { IcError, IcSuccess } from '@mbccs/design-system/assets/icons'
import { BccsText } from '@mbccs/design-system/components'
import { color, spacing } from '@mbccs/design-system/foundation'
import { BccsClickable } from '@mbccs/design-system/interaction'
import { loadingMarkSrc } from './loadingMarkSrc.js'
import { ShareIcon } from './ShareIcon.js'
import {
  resolveResultCardStyle,
  resolveResultCopyStyle,
  resolveResultEmphasisColor,
  resolveResultLabelRowStyle,
  resolveResultMessageStyle,
  resolveResultOrderStyle,
  resolveResultOrderTextStyle,
  resolveResultTitleColor,
} from './RequestResultCard.styles.js'
import {
  REQUEST_RESULT_MARK_SIZE,
  RequestResultStatus,
  type RequestResultCardProps,
} from './RequestResultCard.types.js'

export type { RequestResultCardProps } from './RequestResultCard.types.js'

const SHARE_ICON_SIZE = 24

function ResultMark({ status }: { status: RequestResultCardProps['status'] }) {
  if (status === RequestResultStatus.Processing) {
    return (
      <img
        src={loadingMarkSrc}
        alt=""
        width={REQUEST_RESULT_MARK_SIZE}
        height={REQUEST_RESULT_MARK_SIZE}
        draggable={false}
      />
    )
  }

  const Icon = status === RequestResultStatus.Success ? IcSuccess : IcError
  return (
    <Icon
      width={REQUEST_RESULT_MARK_SIZE}
      height={REQUEST_RESULT_MARK_SIZE}
      aria-hidden
    />
  )
}

/**
 * Shared request-result card: processing, success, or error.
 * The message is a list of text runs so a subscriber number can be emphasized.
 */
export function RequestResultCard({
  status,
  title,
  message,
  orderLabel,
  orderCode,
  orderRequired = false,
  copyLabel,
  onCopyOrderCode,
  mark,
}: RequestResultCardProps) {
  const emphasisColor = resolveResultEmphasisColor(status)
  const liveRole = status === RequestResultStatus.Error ? 'alert' : 'status'

  const copyOrderCode = () => {
    void navigator.clipboard?.writeText(orderCode).catch(() => undefined)
    onCopyOrderCode?.()
  }

  return (
    <section role={liveRole} style={resolveResultCardStyle()}>
      {mark ?? <ResultMark status={status} />}
      <div style={resolveResultCopyStyle()}>
        <BccsText
          as="h1"
          text={title}
          role="headingMd"
          textAlign="center"
          color={resolveResultTitleColor(status)}
        />
        <p style={resolveResultMessageStyle()}>
          {message.map((part, index) =>
            part.emphasized ? (
              <strong
                key={`${part.text}-${index}`}
                style={{ color: emphasisColor, fontWeight: 700 }}
              >
                {part.text}
              </strong>
            ) : (
              <span key={`${part.text}-${index}`}>{part.text}</span>
            ),
          )}
        </p>
      </div>
      <div style={resolveResultOrderStyle()}>
        <div style={resolveResultOrderTextStyle()}>
          <div style={resolveResultLabelRowStyle()}>
            <BccsText
              text={orderLabel}
              role="textXs"
              color={color.text.neutrals.quaternary}
            />
            {orderRequired ? (
              <span aria-hidden>
                <BccsText
                  text="*"
                  role="textXs"
                  weight="semibold"
                  color={color.text.brands.primary}
                />
              </span>
            ) : null}
          </div>
          <BccsText
            text={orderCode}
            role="textSm"
            color={color.text.neutrals.primary}
          />
        </div>
        <BccsClickable
          aria-label={copyLabel}
          onClick={copyOrderCode}
          style={{
            color: color.icon.neutrals.primary,
            flexShrink: 0,
            width: spacing.xl3,
            height: spacing.xl3,
          }}
        >
          <ShareIcon size={SHARE_ICON_SIZE} />
        </BccsClickable>
      </div>
    </section>
  )
}
