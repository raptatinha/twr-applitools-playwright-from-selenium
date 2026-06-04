import { ENavigationMenu } from "./navigation-menu-enum";

export enum EMessageType {
  BATCH = ENavigationMenu.MESSAGES_CREATE_NEW_MESSAGE,
}

export enum EEditor {
  HTML = "HTML",
}

export enum ESendTime {
  NOT_SCHEDULED = "Not scheduled",
  SEND_IMMEDIATELY = "Send immediately",
}

export enum ESendIntervals {
  SEND_EVERY_DAY = "Send every day",
  SEND_EVERY_HOUR = "Send every hour",
  SEND_EVERY_WEEK = "Send every week",
  SEND_EVERY_MONTH = "Send every month",
}
