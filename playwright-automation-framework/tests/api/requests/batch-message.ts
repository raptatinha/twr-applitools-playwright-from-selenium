import { expect, request } from "@playwright/test";
import { buildBaseUrl } from "@setup/base-env-setup";
import apiMethods from "@utils/api-methods";
import apiPaths from "@utils/api-paths";
import { executeRequest } from "@utils/api-request";
import times from "@utils/times";
import { convertMillisecondsToMinutes } from "@utils/util";
import { ETestLayer } from "@enums/test-layer-enum";

const baseUrl = buildBaseUrl(ETestLayer.API);

export async function getBatchMessageById(batchMessageId: string) {
  const queryParams = `/${batchMessageId}`;
  const requestUrl = `${baseUrl}${apiPaths.messages}${queryParams}`;
  const method = apiMethods.get;
  const requestOptions = {};
  const headers = { extraHTTPHeaders: { "Content-Type": "application/json" } };
  const apiContext = await request.newContext(headers);
  const response = await executeRequest(requestUrl, method, requestOptions, apiContext);
  const responseJson = await response.json();
  return await responseJson;
}

export async function waitForSentStatusForBatchMessage(batchMessageId: string, timeout: number) {
  console.log("Waiting for sent status for batch message via API");
  const minutes = convertMillisecondsToMinutes(timeout);
  await expect(async () => {
    const body = await getBatchMessageById(batchMessageId);
    await expect(body.status).toMatch("sent");
  })
    .toPass({ intervals: [times.tenSeconds], timeout: timeout })
    .catch(async () => {
      console.error(`Sent status not found in API request within ${minutes} minutes`);
    });
}
