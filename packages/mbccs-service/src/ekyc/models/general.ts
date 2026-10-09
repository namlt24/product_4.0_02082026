import { createParser, Safe, type InferParser } from '@mbccs/core/parse'

export const generalResponseSchema = {
  status: Safe.string,
  description: Safe.string,
}

export const generalResponseParser = createParser(generalResponseSchema)

export type GeneralResponse = InferParser<typeof generalResponseSchema>
