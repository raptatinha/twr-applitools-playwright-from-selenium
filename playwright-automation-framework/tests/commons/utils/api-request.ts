/* eslint-disable max-params */

/** This will be refactored as part of Q4 */
import { type APIRequestContext, type APIResponse, request } from "@playwright/test";
import { maskJSON2, maskStringV2 } from "maskdata";
import { jsonOptions, stringMaskV2Options } from "@config/mask-config";

interface PrintOptions {
  printRequest?: boolean;
  printResponseBody?: boolean;
  printPayload?: boolean;
}

/**
 * Perform a request to the API and return the response.
 *
 * @param requestUrl       the URL to perform the request.
 * @param method           the request method GET, POST, PUT, DELETE.
 * @param requestOptions   contains the payload.
 * @param requestContext   optional context to perform the request, like headers, cookies, etc.
 * @param failStatus       if true the test will fail throwing the error; false test continue.
 * @param printOptions     optional object to print request, response body or payload in the logs.
 * @returns                APIResponse object from the API.
 */
export async function executeRequest(
  requestUrl: string,
  method: string,
  requestOptions: object,
  requestContext?: APIRequestContext,
  failStatus = true,
  printOptions: PrintOptions = {}
) {
  const maskedRequestOptions = maskJSON2(requestOptions, jsonOptions);
  const maskedRequestUrl = maskStringV2(requestUrl, stringMaskV2Options);

  try {
    const context = requestContext || (await request.newContext({ storageState: "./session.json" }));
    const response = await context[method](requestUrl, requestOptions);
    const responseCode = await response.status();
    const responseOk = await response.ok();

    if (!responseOk) {
      const errorStatus = `Code: ${responseCode} \r\n`;
      const responseStatus = `Status: ${responseOk} \r\n`;
      const errorResponse = `Response: ${await response.text()} \r\n`;
      throw `${errorStatus} ${errorResponse} ${responseStatus} `;
    }

    if (printOptions.printRequest) {
      console.log(`Request ${method} to ${maskedRequestUrl}`);
    }

    if (printOptions.printPayload) {
      console.log(`Payload is ${JSON.stringify(requestOptions)}`);
    }

    if (printOptions.printResponseBody) {
      console.log(`Response body ${await response.text()}`);
    }

    return response;
  } catch (error) {
    if (failStatus) {
      const errorRequestUrl = `Request url: ${maskedRequestUrl} \r\n`;
      const errorRequestMethod = `Method: ${method} \r\n`;
      const errorRequestOptions = `Request options: ${JSON.stringify(maskedRequestOptions)} \r\n`;
      const errorMessage = `Error: ${error} \r\n`;
      throw new Error(
        `Invalid request! Failed on 'executeRequest' method. \r\n ${errorRequestUrl} ${errorRequestMethod} ${errorRequestOptions} ${errorMessage}`
      );
    } else {
      return createErrorResponse(requestUrl, error);
    }
  }

  function createErrorResponse(requestUrl: string, error: Error): APIResponse {
    return {
      ok: () => false,
      body: () => Promise.reject(error),
      dispose: async () => {
        console.log("Disposing response");
      },
      headers: () => ({ error: "error" }),
      headersArray: () => [{ name: "Content-Type", value: "application/json" }],
      json: async () => {
        return error;
      },
      status: () => 404,
      statusText: () => error.message,
      text: async () => {
        return error.message;
      },
      url: () => requestUrl,
      [Symbol.asyncDispose]: async () => {
        console.log("Async disposing response");
      },
    };
  }
}
