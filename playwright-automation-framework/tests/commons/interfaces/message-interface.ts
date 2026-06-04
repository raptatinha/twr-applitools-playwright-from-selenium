import { EEditor, ESendIntervals, ESendTime } from "@enums/message-enum";
import { ETimezone } from "@enums/time-enum";
import { IStatsEmail, IStatsSMS } from "@interfaces/performance-interface";

/**
 * Contains the basic message information and message type.
 */
export interface IMessage {
  messageName: string;
  channelKey: string;
  editor?: string | EEditor;
  tags?: string[];
}

/**
 * Contains the message stats details and additional information.
 */
export interface IMessageStats {
  messageName: string;
  messageId?: string;
  emailStats?: IStatsEmail;
  smsStats?: IStatsSMS;
  timeout?: number;
  additionalInfo?: string;
  sendTimeType?: string | ESendTime;
}

/**
 * Interface for the audience size per batch options.
 * @param isAuto - true to select auto, if false is custom
 * @param autoAudienceSize - the audience size number
 * @param customNumberOfBatches - the number of batches
 * @param customAudienceSizePercentage - the audience size percentage inputs fields, the amount will be based on the customNumberOfBatches
 */
export interface IAudienceSizePerBatchOptions {
  isAuto: boolean;
  autoAudienceSize?: string;
  customNumberOfBatches?: string;
  customAudienceSizePercentage?: string[];
}

/**
 * Interface for the batch interval options.
 * @param isDaily - true to select daily, if false is hourly
 * @param sendTime - IScheduleTime for the send time
 * @param startDate - IDateTime for the start or schedule batch sending time
 * @param endDate - IDateTime for the end date (optional)
 * @param sendIntervals - Enum for the send intervals recurring options
 */
export interface IIntervalOptions {
  isDaily: boolean;
  sendTime?: IScheduleTime;
  startDate?: IDateTime;
  endDate?: IDateTime;
  sendIntervals?: ESendIntervals;
}

/**
 * Interface for the send time.
 */
export interface IScheduleTime {
  hours: string;
  minutes: string;
  dayPeriod: string;
  timezone?: ETimezone;
}

/**
 * Interface for the start or schedule batch sending time
 */
export interface IDateTime {
  date: Date;
  timezone: ETimezone;
  dateFormatted: string;
  longWeekday: string;
  shortWeekday: string;
  dayPeriod: string;
  hour: string;
  minutes: string;
}

/**
 * Interface for the allowed hour.
 */
export interface IAllowedHour {
  hour: string;
  dayPeriod: string;
}
