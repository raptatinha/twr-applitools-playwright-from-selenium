import { EAttributeType } from "@enums/contact-attribute-enum";

export interface IContact {
  channels?: IChannel;
  attribute?: IAttribute[];
  list?: string;
}
export interface IChannel {
  channelKey: string;
  address: string;
}
export interface IPushValues {
  token: string;
  os: string;
  pushEnabled: boolean;
}
export interface ISmsValues {
  programName: string;
  keyword: string;
  suppressDOI: boolean;
}
export interface IAttribute {
  key: string;
  value: string | number | string[] | IDateAttribute | IGeoAttribute;
  type: EAttributeType;
}
export interface IDateAttribute {
  month: string;
  day: string;
  year: string;
  hour: string;
  minute: string;
  second: string;
}
export interface IGeoAttribute {
  address1: string;
  address2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  timezone: string;
}
