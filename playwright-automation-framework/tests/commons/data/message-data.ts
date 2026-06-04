import { faker } from "@faker-js/faker";
import { getSendTime } from "@utils/time-utils";
import { generateRandomNumber } from "@utils/util";
import { ERulesOperator } from "@enums/audience-builder-enum";
import { EClassification, EEditor, ESendTime } from "@enums/message-enum";
import { IIntervalOptions, IThrottledBatchBy } from "@interfaces/message-interface";
import { IStatsEmail, IStatsSMS } from "@interfaces/performance-interface";
import emailContact from "@data/contact-email-data";

export const messageData = {
  batchName: "pw_automation_batch_",
  templateName: "pw_automation_template_",
  email: {
    subject: "automation subject line",
    body: "automation body sample",
    autoOpen: '<p>automation test to open data-id="open"</p>',
    autoClick: '<a data-id="click" href="https://example.com">test link</a>',
    autoClickAll: '<p>automation test to click all links data-id="click-all"</p>',
    autoClickAndAssignToList:
      '<a data-id="click" href="https://example.com" data-assign-list="list_1">add to list test</a>',
    autoClickNoTrack:
      '<a data-id="click" href="https://example.com/product" data-no-track="true" ' +
      'data-tags="no-track" id="%d">%s</a></p>',
    autoBounce: '<p>automation test to bounce data-id="reject"</p>',
    withLink: '<a href="https://example.com">test link</a>',
    noForward: '<span data-id="no-forward"></span> ',
    dynamicDate: " {{formatDate(now, 'America/Los_Angeles', 'MM/dd/yyyy')}}",
    dynamicContactEmail: " {{contact.email}}",
    templateDefault: "template-default",
  },
  sms: {
    simpleText: "automation content sample",
    dynamicText: `hello {{contact.first_name}} you have {{contact.points}} prizes.`,
    linkShorten: `For example click here {{"https://www.example.com/promo"|shorten:7}} text after`,
    shortLink: `New link example here {{shortLink href="https://example.com/search?q=home" key="testhome" shorten="6"}}`,
  },
};

export const emailBatchPromotionalData = {
  messageInfo: {
    messageName: `${messageData.batchName}${generateRandomNumber()}`,
    channelKey: "",
    editor: EEditor.HTML,
    classification: EClassification.PROMOTIONAL,
  },
  content: {
    emailSubject: `${messageData.email.subject} ${faker.word.words(2)}`,
    emailBody: `${messageData.email.body} ${messageData.email.withLink} ${faker.lorem.paragraph()}`,
  },
  audience: {
    contactEmail: [emailContact.subscribe_contact_1.email],
    rulesOperator: "",
  },
  emailStats: { totalSent: 1, totalDelivered: 1 } as IStatsEmail,
};

export const emailBatchTransactionalData = {
  messageInfo: {
    messageName: `${messageData.batchName}${generateRandomNumber()}`,
    channelKey: "",
    editor: EEditor.HTML,
    classification: EClassification.TRANSACTIONAL,
  },
  content: {
    emailSubject: `${messageData.email.subject} ${messageData.email.dynamicDate} ${faker.word.words(1)}`,
    emailBody: `${messageData.email.dynamicContactEmail} ${messageData.email.autoClickNoTrack} ${faker.lorem.paragraph()}`,
  },
  audience: {
    contactEmail: [emailContact.subscribe_contact_1.email, emailContact.unsubscribe_contact_11.email],
    rulesOperator: ERulesOperator.OR,
  },
  emailStats: { totalSent: 2, totalDelivered: 2 } as IStatsEmail,
};

export const emailThrottledBatchData = {
  messageInfo: {
    messageName: `${messageData.batchName}${generateRandomNumber()}`,
    channelKey: "",
    editor: EEditor.HTML,
    classification: EClassification.PROMOTIONAL,
  },
  content: {
    emailSubject: `${messageData.email.subject} ${faker.word.words(3)}`,
    emailBody: `${messageData.email.body} ${faker.lorem.paragraph()}`,
  },
  audience: {
    contactEmail: [emailContact.subscribe_contact_2.email, emailContact.subscribe_contact_3.email],
    rulesOperator: ERulesOperator.OR,
  },
  emailStats: { totalSent: 2, totalDelivered: 2 } as IStatsEmail,
  schedule: {
    sendTime: ESendTime.sCHEDULED,
    throttledBatchBySettings: { isMaxNumberOfBatches: true, maxNumberOfBatches: "1" } as IThrottledBatchBy,
    intervalOptions: { isDaily: true } as IIntervalOptions,
  },
};

export const smsBatchData = {
  messageInfo: {
    messageName: `${messageData.batchName}${generateRandomNumber()}`,
    channelKey: "",
  },
  content: `${messageData.sms.simpleText}${faker.lorem.paragraph()}`,
  audience: {
    contactPhone: [smsContact.googleVoiceNumberAdam.phoneNumber],
    rulesOperator: "",
  },
  smsStats: { totalSent: 1 } as IStatsSMS,
};

export const emailScheduledBatchData = {
  messageInfo: {
    messageName: `${messageData.batchName}${generateRandomNumber()}`,
    channelKey: "",
    editor: EEditor.HTML,
    classification: EClassification.PROMOTIONAL,
  },
  content: {
    emailSubject: `${messageData.email.subject} ${faker.word.words(2)}`,
    emailBody: `${messageData.email.body} ${messageData.email.dynamicContactEmail} ${faker.lorem.paragraph()}`,
  },
  audience: {
    contactEmail: [emailContact.subscribe_contact_1.email, emailContact.subscribe_contact_2.email],
    rulesOperator: ERulesOperator.OR,
  },
  emailStats: { totalSent: 2, totalDelivered: 2 } as IStatsEmail,
  schedule: {
    sendTime: ESendTime.SCHEDULED,
    scheduleTime: getSendTime(),
    startBatchSending: "",
  },
};
