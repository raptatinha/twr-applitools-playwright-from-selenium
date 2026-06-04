

export interface IContactChange {
  attributeName: string;
  conditionOperator?: string;
  expectedValue?: string;
}

export interface ISupplementChange {
  supplementID?: string;
  supplementName: string;
  supplementChangeFieldName: string;
  supplementChangeFieldOperator?: string;
  supplementChangeFieldValue?: string;
}

export interface IOrderCreateOrUpdate {
  orderID?: string;
  orderTriggerStrategy: string;
  orderChangeFieldName?: string;
  orderChangeFieldOperator?: string;
  orderChangeFieldValue?: string;
}

export interface IContactActivities {
  a: string;
  email: string;
  properties?: Record<string, string>;
}

export interface ICustomEvent {
  eventName: string;
  propertyName: string;
  propertyValue: string;
  triggerValues?: IContactActivities;
}

export interface IEventTriggerSettings {
  contactChange?: IContactChange;
  addToList?: string;
  supplementChange?: ISupplementChange;
  orderCreateOrUpdate?: IOrderCreateOrUpdate;
  customEvent?: ICustomEvent;
}
