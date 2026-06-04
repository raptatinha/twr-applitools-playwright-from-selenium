import { test } from "@setup/base-fixture";
import { getDateTime, getSendTime } from "@utils/time-utils";
import times from "@utils/times";
import {
  emailBatchPromotionalData,
  emailBatchTransactionalData,
  emailThrottledBatchData,
} from "@data/message-data";

test.describe.parallel("Batch Message Suite", { tag: ["@batch", "@email"] }, () => {
  const batchMessageData = [emailBatchPromotionalData, emailBatchTransactionalData];
  for (const batch of batchMessageData) {
    test(`Create new EMAIL batch ${batch.messageInfo.classification} message and send immediately`, async ({ pm }) => {
      const { messageInfo, content, audience, emailStats } = batch;
      let messageId = "";

      await test.step("Create new batch message", async () => {
        await pm.getMessageStepsPage().createNewMessage(messageInfo);
        messageId = await pm.getMessageStepsPage().getBatchMessageIdFromUrl();
      });

      await test.step("Fill message header and content", async () => {
        await pm.getMessageStepsPage().fillMessageHeader(content.emailSubject);
        await pm.getMessageStepsPage().fillHtmlContent(content.emailBody);
      });

      await test.step("Schedule message", async () => {
        await pm.getMessageStepsPage().scheduleMessage();
      });

      await test.step("Select audience", async () => {
        await pm
          .getAudiencePage()
          .selectAudienceByAddressRule(audience.contactEmail, messageInfo.channelKey, audience.rulesOperator);
      });

      await test.step("Send the message", async () => {
        await pm.getMessageStepsPage().sendMessage();
      });

      await test.step("Validate performance statistics", async () => {
        const messageStats = {
          channelKey: messageInfo.channelKey,
          messageName: messageInfo.messageName,
          messageId,
          emailStats,
          timeout: times.fourMinutes,
        };
        await pm.getMessageStepsPage().assertBatchSentStats(messageStats);
      });
    });
  }

  test("Create new EMAIL throttled batch message and send daily", async ({ pm }) => {
    const { messageInfo, content, audience, emailStats, schedule } = emailThrottledBatchData;
    let messageId = "";

    await test.step("Create new batch message", async () => {
      await pm.getMessageStepsPage().createNewMessage(messageInfo);
      messageId = await pm.getMessageStepsPage().getBatchMessageIdFromUrl();
    });

    await test.step("Fill message header and content", async () => {
      await pm.getMessageStepsPage().fillMessageHeader(content.emailSubject);
      await pm.getMessageStepsPage().fillHtmlContent(content.emailBody);
    });

    await test.step("Select audience", async () => {
      await pm
        .getAudiencePage()
        .selectAudienceByAddressRule(audience.contactEmail, messageInfo.channelKey, audience.rulesOperator);
    });

    await test.step("Set up schedule throttled settings", async () => {
      schedule.intervalOptions = { ...schedule.intervalOptions, sendTime: getSendTime(), startDate: getDateTime() };
      await pm
        .getMessageStepsPage()
        .scheduleMessage(schedule.sendTime, { messageId, messageName: messageInfo.messageName });
      await pm
        .getMessageStepsPage()
        .setupThrottledBatchSending(schedule.throttledBatchBySettings, schedule.intervalOptions);
    });

    await test.step("Send the message", async () => {
      await pm.getMessageStepsPage().sendMessage();
    });

    await test.step("Validate performance statistics", async () => {
      const messageStats = {
        channelKey: messageInfo.channelKey,
        messageName: messageInfo.messageName,
        messageId,
        emailStats,
        timeout: times.eightMinutes,
        sendTimeType: schedule.sendTime,
      };
      await pm.getMessageStepsPage().assertBatchSentStats(messageStats);
    });
  });
});
