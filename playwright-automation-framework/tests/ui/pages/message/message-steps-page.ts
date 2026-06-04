/* eslint-disable max-lines */

/** This will be refactored as part of Q4 */
import { type Page } from "@playwright/test";
import apiPaths from "@utils/api-paths";
import { NetworkInterceptor, shouldMock } from "@utils/network-interceptor";
import { ESendingMethod, ETriggerEvents } from "@enums/automation-template-enum";
import { EChannelKey } from "@enums/contact-enum";
import { EMessageType, ESendTime } from "@enums/message-enum";
import { ENavigationMenu } from "@enums/navigation-menu-enum";
import { IEventTriggerSettings } from "@interfaces/automation-template-interface";
import { IWaitForExpectedValueOptions } from "@interfaces/base-interface";
import {
  IIntervalOptions,
  IMessage,
  IMessageStats,
  IScheduleTime,
  IThrottledBatchBy,
} from "@interfaces/message-interface";
import { MessageContentPage } from "@pages/message/message-content-page";
import { MessageCreateNewPage } from "@pages/message/message-create-new-page";
import { MessageEventTriggerSendingPage } from "@pages/message/message-event-trigger-sending-page";
import { MessageHeaderPage } from "@pages/message/message-header-page";
import { MessageHubPage } from "@pages/message/message-hub-page";
import { MessagePerformancePage } from "@pages/message/message-performance-page";
import { MessageSchedulePage } from "@pages/message/message-schedule-page";
import { MessageSendingMethodsPage } from "@pages/message/message-sending-methods-page";
import { waitForSentStatusForBatchMessage } from "@requests/batch-message";
import {
  triggerAutomationByCreateContactActivity,
  triggerAutomationByCreateOrUpdateOrder,
  triggerAutomationByUpdateAttribute,
  triggerAutomationByUpdateList,
  triggerAutomationByUpdateSupplementRecords,
} from "@requests/contact-triggers";

export class MessageStepsPage {
  readonly page: Page;
  readonly messageCreateNewPage: MessageCreateNewPage;
  readonly messageHubPage: MessageHubPage;
  readonly messageHeaderPage: MessageHeaderPage;
  readonly messageContentPage: MessageContentPage;
  readonly messageSchedulePage: MessageSchedulePage;
  readonly messagePerformancePage: MessagePerformancePage;
  readonly messageEventTriggerSendingPage: MessageEventTriggerSendingPage;
  readonly messageSendingMethodsPage: MessageSendingMethodsPage;

  constructor(page: Page) {
    this.page = page;
    this.messageCreateNewPage = new MessageCreateNewPage(page);
    this.messageHubPage = new MessageHubPage(page);
    this.messageHeaderPage = new MessageHeaderPage(page);
    this.messageContentPage = new MessageContentPage(page);
    this.messageSchedulePage = new MessageSchedulePage(page);
    this.messagePerformancePage = new MessagePerformancePage(page);
    this.messageEventTriggerSendingPage = new MessageEventTriggerSendingPage(page);
    this.messageSendingMethodsPage = new MessageSendingMethodsPage(page);
  }

  async createNewMessage(message: IMessage, messageType: EMessageType = EMessageType.BATCH) {
    const messageTypeKey = Object.keys(EMessageType).find((key) => EMessageType[key] === messageType);
    console.log(`Creating new ${messageTypeKey} message ${message.messageName}`);
    await this.messageCreateNewPage.openCreateMessageModal(messageType as unknown as ENavigationMenu);
    await this.messageCreateNewPage.assertModalTitleIsPresent();
    await this.messageCreateNewPage.fillMessageName(message.messageName);
    await this.messageCreateNewPage.selectChannelHubOptions(message);
    await this.messageCreateNewPage.clickContinue();
    await this.messageCreateNewPage.assertSuccessMessagePopupIsPresent();
    await this.messageHubPage.assertMessageHubTitleIsPresent();
    console.log(`${messageTypeKey} message created successfully, message hub page is opened`);
  }

  async fillMessageHeader(subject: string, fromEmail?: string, replyEmail?: string, fromDescription?: string) {
    console.log("Opening message header");
    await this.messageHeaderPage.openMessageHeader();
    await this.messageHeaderPage.fillSubject(subject);
    if (fromEmail) {
      await this.messageHeaderPage.fillFromEmail(fromEmail!);
    }
    if (replyEmail) {
      await this.messageHeaderPage.fillReplyEmail(replyEmail!);
    }
    if (fromDescription) {
      await this.messageHeaderPage.fillFromDescription(fromDescription!);
    }
    await this.messageHeaderPage.clickSaveMessageHeader();
    await this.messageHeaderPage.assertSuccessMessageHeaderUpdatedIsPresent();
    console.log("Message header information has been saved successfully");
  }

  async fillMessageSculptMessageContent(templateSculpt: string) {
    console.log("Creating Sculpt content");
    await this.messageContentPage.fillMessageContentSculptNameAndClick(templateSculpt);
    await this.messageContentPage.openSculptMessageEditorAndSaveDraft();
  }

  async fillHtmlContent(messageBody: string) {
    console.log("Opening message content");
    await this.messageContentPage.openEmailMessageContent();
    await this.messageContentPage.fillBodyContent(messageBody);
    await this.messageContentPage.clickSaveMessageContent();
    await this.messageContentPage.assertSuccessMessageContentUpdatedIsPresent();
    await this.messageContentPage.clickBackMessageContent();
    console.log("Message content information has been saved successfully");
  }

  async fillSMSContent(messageBody: string) {
    console.log("Opening message content");
    await this.messageContentPage.openSMSMessageContent();
    await this.messageContentPage.fillSMSBodyContent(messageBody);
    await this.messageContentPage.clickSaveMessageContent();
    await this.messageContentPage.assertSuccessMessageContentUpdatedIsPresent();
    console.log("Message content information has been saved successfully", messageBody);
  }

  async sendMessage() {
    await this.messageHubPage.clickSendMessage();
    await this.messageHubPage.clickConfirmSendModal();
    console.log("Message has been sent successfully");
  }

  async getBatchMessageIdFromUrl() {
    const url = this.page.url();
    return url.split("/messagehub/")[1].split("/manual")[0];
  }

  async getAutomationTemplateIdFromUrl() {
    const url = this.page.url();
    return url.split("/messagehub/")[1].split("/automated")[0];
  }
  /**
   * Selects the send time for the message from the schedule options.
   * @param sendTime ESendTime enum value. Default is SEND_IMMEDIATELY
   */
  async scheduleMessage(
    sendTime: ESendTime = ESendTime.SEND_IMMEDIATELY,
    messageInfo?: { messageId: string; messageName: string }
  ) {
    await this.messageSchedulePage.selectScheduleSendTime(sendTime);
    if (sendTime === ESendTime.SEND_IMMEDIATELY) {
      await this.messageSchedulePage.unselectQuietHoursCheckbox();
      await this.messageSchedulePage.saveScheduleSettings();
    }
    await this.mockSendMessage(sendTime, messageInfo);
  }

  private async mockSendMessage(
    sendTimeType: ESendTime | string,
    messageInfo?: { messageId: string; messageName: string }
  ) {
    if (await shouldMock(sendTimeType)) {
      await new NetworkInterceptor(this.page).interceptResponse(apiPaths.batch_messages, sendTimeType, messageInfo);
      await new NetworkInterceptor(this.page).interceptResponse(
        apiPaths.client_log_aggregations,
        sendTimeType,
        messageInfo
      );
    }
  }

  private async mockSentStats(
    sendTimeType: ESendTime | string,
    messageInfo?: { messageId: string; messageName: string }
  ) {
    if (await shouldMock(sendTimeType)) {
      await new NetworkInterceptor(this.page).interceptResponse(apiPaths.batch_message, sendTimeType, messageInfo);
      await new NetworkInterceptor(this.page).interceptResponse(apiPaths.dashboard, sendTimeType, messageInfo);
      await new NetworkInterceptor(this.page).interceptResponse(apiPaths.stats, sendTimeType, messageInfo);
    }
  }

  async assertBatchSentStats(message: IMessageStats) {
    const { channelKey, messageName, messageId, emailStats, smsStats, timeout, sendTimeType } = message;
    let statistics: IWaitForExpectedValueOptions[];
    const targetPage = [this.messagePerformancePage.performanceButton];
    const proxyPage = this.messagePerformancePage.contentButton;

    if (channelKey === EChannelKey.EMAIL) {
      statistics = await this.messagePerformancePage.prepareStats(emailStats, targetPage, proxyPage);
      if (!(await shouldMock(sendTimeType))) {
        await waitForSentStatusForBatchMessage(messageId, timeout);
      }
      await this.messagePerformancePage.openEmailSentListPage();
      await this.mockSentStats(sendTimeType, { messageId, messageName });
      await this.messagePerformancePage.openTheMessageSentFromList(messageName);
      await this.messagePerformancePage.waitForStats(statistics);
      await this.messagePerformancePage.openEmailSentListPage();
    }
    if (isSmsChannelKey(channelKey)) {
      statistics = await this.messagePerformancePage.prepareStats(smsStats, targetPage, proxyPage);
      if (!(await shouldMock(sendTimeType))) {
        await waitForSentStatusForBatchMessage(messageId, timeout);
      }
      await this.messagePerformancePage.openSMSSentListPage();
      await this.mockSentStats(sendTimeType, { messageId, messageName });
      await this.messagePerformancePage.openTheMessageSentFromList(messageName);
      await this.messagePerformancePage.waitForStats(statistics);
      await this.messagePerformancePage.openSMSSentListPage();
    }

    await this.messagePerformancePage.openTheMessageSentFromList(messageName);
    await this.messagePerformancePage.waitForStats(this.messagePerformancePage.sentStatus);
  }

  async publishDraft() {
    await this.messageHubPage.clickPublishDraft();
    await this.messageHubPage.assertDraftPublishedSuccessfully();
  }

  async setupEventTriggerAndEnable(settings: IEventTriggerSettings) {
    const {
      triggerEvent,
      contactChange,
      addToList,
      supplementChange,
      orderCreateOrUpdate,
      customEvent,
      howOften: howOftenSettings,
    } = settings;
    await this.messageSendingMethodsPage.openEventTriggerSendingMethodPage();
    switch (triggerEvent) {
      case ETriggerEvents.CONTACT_ATTRIBUTE_CHANGE:
        await this.messageEventTriggerSendingPage.fillContactAttributeChange(contactChange);
        break;
      case ETriggerEvents.CONTACT_ADD_TO_LIST:
        await this.messageEventTriggerSendingPage.fillAddToList(addToList);
        break;
      case ETriggerEvents.SUPPLEMENT_RECORD_CHANGE:
        await this.messageEventTriggerSendingPage.fillContactAttributeSupplementRecord(supplementChange);
        break;
      case ETriggerEvents.ORDER_CREATED_OR_UPDATED:
        await this.messageEventTriggerSendingPage.fillOrderCreateOrUpdateRecord(orderCreateOrUpdate);
        break;
      case ETriggerEvents.CUSTOM_EVENT:
        await this.messageEventTriggerSendingPage.fillCustomEvent(customEvent);
        break;
    }
    if (howOftenSettings) {
      await this.messageEventTriggerSendingPage.selectHowOftenToProcess(howOftenSettings);
    }
    await this.messageEventTriggerSendingPage.saveTriggerSettings();
    await this.messageSendingMethodsPage.enableSendingMethod(ESendingMethod.EVENT_TRIGGERED);
  }

  async setupApiTriggerAndEnable() {
    await this.messageSendingMethodsPage.openApiTriggerSendingMethodPage();
    await this.messageSendingMethodsPage.enableSendingMethod(ESendingMethod.API_TRIGGERED);
  }

  async setupRecurringTriggerAndEnable() {
    await this.messageSendingMethodsPage.openRecurringTriggerSendingMethodPage();
    //TODO - implement function
  }

  async enableSendingMethod(method: ESendingMethod) {
    await this.messageSendingMethodsPage.enableSendingMethod(method);
  }

  async disableSendingMethod(method: ESendingMethod) {
    await this.messageSendingMethodsPage.enableSendingMethod(method, false);
  }

  async triggerAutomationWithContact(settings: IEventTriggerSettings, contactEmail: string) {
    const { triggerEvent, contactChange, addToList, supplementChange, orderCreateOrUpdate, customEvent } = settings;
    switch (triggerEvent) {
      case ETriggerEvents.CONTACT_ATTRIBUTE_CHANGE:
        await triggerAutomationByUpdateAttribute(contactEmail, contactChange);
        break;
      case ETriggerEvents.CONTACT_ADD_TO_LIST:
        await triggerAutomationByUpdateList(contactEmail, addToList);
        break;
      case ETriggerEvents.SUPPLEMENT_RECORD_CHANGE:
        await triggerAutomationByUpdateSupplementRecords(contactEmail, supplementChange);
        break;
      case ETriggerEvents.CUSTOM_EVENT:
        await triggerAutomationByCreateContactActivity(contactEmail, customEvent.triggerValues);
        break;
      case ETriggerEvents.ORDER_CREATED_OR_UPDATED:
        await triggerAutomationByCreateOrUpdateOrder(contactEmail, orderCreateOrUpdate);
        break;
      default:
        throw new Error(`Unhandled trigger event: ${triggerEvent}`);
    }
  }
}
