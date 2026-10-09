import { createParser, Safe, type InferParser } from '@mbccs/core/parse'

export const representativeInfoSchema = {
  boss_birthday: Safe.string,
  boss_current_address: Safe.string,
  boss_ethnic: Safe.string,
  boss_id_issue_date: Safe.string,
  boss_id_issue_place: Safe.string,
  boss_id_number: Safe.string,
  boss_id_type: Safe.string,
  boss_name: Safe.string,
  boss_name_conf: Safe.string,
  boss_nationality: Safe.string,
  boss_permanent_address: Safe.string,
  boss_position: Safe.string,
  boss_sex: Safe.string,
}

export const representativeInfoParser = createParser(representativeInfoSchema)

export type RepresentativeInfo = InferParser<typeof representativeInfoSchema>

export const businessLicenseResponseSchema = {
  address: Safe.string,
  addressConf: Safe.string,
  date: Safe.string,
  dateConf: Safe.string,
  district: Safe.string,
  districtName: Safe.string,
  elapsedTime: Safe.integer,
  hoKinhDoanh: Safe.integer,
  id: Safe.string,
  idConf: Safe.string,
  name: Safe.string,
  nameConf: Safe.string,
  precinct: Safe.string,
  precinctName: Safe.string,
  province: Safe.string,
  provinceName: Safe.string,
  quality: Safe.string,
  qualityMsg: Safe.string,
  representativeInfo: (val: unknown) => representativeInfoParser.safe(val),
  street: Safe.string,
  streetName: Safe.string,
}

export const businessLicenseResponseParser = createParser(businessLicenseResponseSchema)

export type BusinessLicenseResponse = InferParser<typeof businessLicenseResponseSchema>
