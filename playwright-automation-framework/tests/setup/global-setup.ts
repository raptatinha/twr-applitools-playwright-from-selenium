import { expect, request, test } from "@playwright/test";
import { buildBaseUrl, getCredentials } from "@setup/base-env-setup";
import apiMethods from "@utils/api-methods";
import apiPaths from "@utils/api-paths";
import { executeRequest } from "@utils/api-request";
import { ETestLayer } from "@enums/test-layer-enum";

test.describe.configure({ mode: "serial" });

test.describe("Global setup", () => {
  test("login", async () => {
    const credentials = getCredentials();

    const authFile = "./session.json";
    const baseUrl = buildBaseUrl(ETestLayer.API);
    console.log("=========Environment", baseUrl);
    let requestUrl = baseUrl + apiPaths.authentication;
    let method = apiMethods.post;
    const payload = {
      data: {
        email: credentials.username,
        password: credentials.password,
      },
    };

    const requestOptions = payload;
    const apiContext = await request.newContext({ baseURL: baseUrl });
    const response = await executeRequest(requestUrl, method, requestOptions, apiContext, false, {
      printRequest: true,
    });

    if (!response.ok()) {
      const errorDetails = (await response.statusText()) ? await response.statusText() : await response.json();
      throw new Error(`Fail to login via API, The URL: ${baseUrl} is not accessible. \r\n and/or ${errorDetails}`);
    }

    expect(response.ok()).toBeTruthy();
    await apiContext.storageState({ path: authFile });

    console.log("=========Login via API successful.");
  });
});
