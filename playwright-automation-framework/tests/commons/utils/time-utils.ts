import { ETimezone } from "@enums/time-enum";
import { IScheduleTime } from "@interfaces/message-interface";

/*
 * Returns a Date object with the closest time to send in minutes
 * in the future in multiples of five; based on current time.
 */
export function getMinutesInFuture() {
  const now = new Date();
  const minutes = now.getMinutes();
  let closestMultipleOf5 = Math.ceil(minutes / 5) * 5;

  if (closestMultipleOf5 === minutes) {
    closestMultipleOf5 += 5;
  }

  if (closestMultipleOf5 === 60) {
    now.setHours(now.getHours() + 1);
    now.setMinutes(0);
  } else {
    now.setMinutes(closestMultipleOf5);
  }
  now.setSeconds(0);
  now.setMilliseconds(0);
  return now;
}

/*
 * Return a IScheduleTime object with the send time for future
 */
export function getSendTime(timezone: ETimezone = ETimezone.ACCOUNT_TIMEZONE) {
  const time = getMinutesInFuture();
  const sendTime: IScheduleTime = {
    hours: time.toLocaleString("us-us", { hour: "numeric", timeZone: timezone }).replace(/\D/g, ""),
    minutes: time.toLocaleString("us-us", { minute: "numeric", timeZone: timezone }).replace(/\D/g, ""),
    dayPeriod: time
      .toLocaleString("us-us", { hour: "2-digit", hour12: true, timeZone: timezone })
      .substring(3, 5)
      .toLowerCase(),
    timezone: timezone,
  };
  return sendTime;
}

/*
 * Return the current date formatted in IDateTime
 */
export function getDateTime(date: Date = new Date()) {
  const timezone = ETimezone.ACCOUNT_TIMEZONE;
  const month = (date.getMonth() + 1).toString().padStart(2, "0");
  const day = date.getDate().toString().padStart(2, "0");
  const year = date.getFullYear().toString();

  return {
    date: date,
    timezone: timezone,
    dateFormatted: `${month}/${day}/${year}`,
    longWeekday: date.toLocaleString("us-us", { weekday: "long", timeZone: timezone }),
    shortWeekday: date.toLocaleString("us-us", { weekday: "short", timeZone: timezone }),
    dayPeriod: date
      .toLocaleString("us-us", { hour: "2-digit", hour12: true, timeZone: timezone })
      .substring(3, 5)
      .toLowerCase(),
    hour: date.toLocaleString("us-us", { hour: "numeric", timeZone: timezone }).replace(/\D/g, ""),
    minutes: date.toLocaleString("us-us", { minute: "numeric", timeZone: timezone }).replace(/\D/g, ""),
  };
}
