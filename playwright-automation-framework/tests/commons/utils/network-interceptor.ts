import { Page } from "@playwright/test";
import apiPaths from "@utils/api-paths";
import { readJsonFile, updateFieldsInJson } from "@utils/file-handler";
import { getKeyByValue } from "@utils/util";
import { ESendTime } from "@enums/message-enum";

const MOCKS_FILE_PATH = "./tests/commons/response-mocks";
const BASE_API_URL = "**/api/";

const ALLOWED_SEND_TIMES = [
  ESendTime.NOT_SCHEDULED,
];

export class NetworkInterceptor {
  constructor(private page: Page) {}

  async interceptResponse(
    urlToIntercept: string,
    sendTimeType: ESendTime | string,
    message?: { messageId: string; messageName: string }
  ) {
    try {
      const sendTimeKey = getKeyByValue(ESendTime, sendTimeType);
      const urlToInterceptKey = getKeyByValue(apiPaths, urlToIntercept);
      let mockedResponse = this.getJsonFilePath(sendTimeKey, urlToInterceptKey).replace(/_/g, "-");
      mockedResponse = this.getMockedResponse(mockedResponse, message);
      urlToIntercept = `${BASE_API_URL}${urlToIntercept}`;

      await this.page.route(urlToIntercept, async (route) => {
        console.log("Intercepted response:", route.request().url());
        route.fulfill({
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(mockedResponse),
        });
      });
    } catch (error) {
      console.error("Error in intercepting response:", error);
      throw error;
    }
  }

  private getJsonFilePath(sendTimeKey: string, urlToInterceptKey: string) {
    return `${MOCKS_FILE_PATH}/${sendTimeKey}/${urlToInterceptKey}.json`.trim().toLowerCase();
  }

  private getMockedResponse(jsonFilepath: string, message?: { messageId: string; messageName: string }) {
    let mockedResponse = readJsonFile(jsonFilepath);

    if (message) {
      const newValues = [
        { key: "_id", newValue: message.messageId },
        { key: "name", newValue: message.messageName },
        { key: "messageName", newValue: message.messageName },
      ];
      mockedResponse = updateFieldsInJson(mockedResponse, newValues);
    }

    return mockedResponse;
  }
}

export async function shouldMock(sendTimeType: ESendTime | string) {
  const isEnumValue = Object.values(ESendTime).includes(sendTimeType as ESendTime);
  return process.env.MOCK === "true" && isEnumValue && ALLOWED_SEND_TIMES.includes(sendTimeType as ESendTime);
}
