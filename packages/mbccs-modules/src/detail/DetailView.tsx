import { BccsText } from '@mbccs/design-system/components'
import { color } from '@mbccs/design-system/foundation'
import {
  resolveDetailCardStyle,
  resolveDetailDividerStyle,
  resolveDetailFieldsStyle,
  resolveDetailHeadingStyle,
  resolveDetailLabelStyle,
  resolveDetailPhotoFrameStyle,
  resolveDetailPhotoStyle,
  resolveDetailRowStyle,
  resolveDetailStackStyle,
  resolveDetailTagStyle,
  resolveDetailValueCellStyle,
  resolveDetailValueStyle,
} from './detail.styles.js'
import {
  DetailStatus,
  DetailValueTone,
  type DetailField,
  type DetailMedia,
  type DetailSection,
  type DetailStatus as Status,
  type DetailViewProps,
} from './detail.types.js'

const STATUS_COLOR: Record<Status, string> = {
  [DetailStatus.InProgress]: color.icon.semantics.warning.primary,
  [DetailStatus.Completed]: color.icon.semantics.success.primary,
  [DetailStatus.Cancelled]: color.icon.semantics.error.primary,
}

function DetailRows({ fields }: { fields: readonly DetailField[] }) {
  return (
    <dl style={resolveDetailFieldsStyle()}>
      {fields.map((field, index) => (
        <div key={field.label}>
          <div style={resolveDetailRowStyle()}>
            <dt style={resolveDetailLabelStyle()}>
              <BccsText role="textSm" color={color.text.neutrals.quaternary} text={field.label} />
            </dt>
            <dd style={resolveDetailValueCellStyle()}>
              {field.status && field.statusLabel ? (
                <span style={resolveDetailTagStyle(STATUS_COLOR[field.status])}>
                  <BccsText
                    role="textXs"
                    weight="medium"
                    color={color.text.neutrals.white}
                    text={field.statusLabel}
                  />
                </span>
              ) : (
                <span style={resolveDetailValueStyle(field.tone ?? DetailValueTone.Default)}>
                  {field.value}
                </span>
              )}
            </dd>
          </div>
          {index < fields.length - 1 ? <hr style={resolveDetailDividerStyle()} /> : null}
        </div>
      ))}
    </dl>
  )
}

function DetailSectionCard({ section }: { section: DetailSection }) {
  return (
    <section style={resolveDetailCardStyle()} aria-labelledby={`detail-${section.id}`}>
      <h2 id={`detail-${section.id}`} style={resolveDetailHeadingStyle()}>
        {section.icon}
        <BccsText as="span" role="headingSm" text={section.title} />
      </h2>
      <DetailRows fields={section.fields} />
    </section>
  )
}

function DetailMediaCard({ item }: { item: DetailMedia }) {
  return (
    <section style={resolveDetailCardStyle()} aria-labelledby={`detail-${item.id}`}>
      <h2 id={`detail-${item.id}`} style={resolveDetailHeadingStyle()}>
        {item.icon}
        <BccsText as="span" role="headingSm" text={item.title} />
      </h2>
      <div style={resolveDetailPhotoFrameStyle(item.height)}>
        <img src={item.src} alt={item.alt} style={resolveDetailPhotoStyle()} />
      </div>
    </section>
  )
}

/**
 * Shared record detail: titled key-value cards, then optional photo cards.
 * Status, value tone, and the photo list select which Figma variant is shown.
 */
export function DetailView({ sections, media = [] }: DetailViewProps) {
  return (
    <div style={resolveDetailStackStyle()}>
      {sections.map((section) => (
        <DetailSectionCard key={section.id} section={section} />
      ))}
      {media.map((item) => (
        <DetailMediaCard key={item.id} item={item} />
      ))}
    </div>
  )
}
