import { request } from "@playwright/test";
import { buildBaseUrl } from "@setup/base-env-setup";
import apiMethods from "@utils/api-methods";
import apiPaths from "@utils/api-paths";
import { executeRequest } from "@utils/api-request";
import { ETestLayer } from "@enums/test-layer-enum";

const baseUrl = buildBaseUrl(ETestLayer.API);

/**
 * Update contact via swagger API v2.
 * @param primaryKey  the contact primary key, email address or ID
 * @param payload     the payload to update the contact, attributes, list, channel, etc.
 */
export async function updateContactViaSwaggerApi(primaryKey: string, payload: object) {
  const queryParams = `/${encodeURIComponent(primaryKey)}`;
  const requestUrl = `${baseUrl}${apiPaths.contacts}${queryParams}`;
  const method = apiMethods.put;
  const headers = { extraHTTPHeaders: { "Content-Type": "application/json" } };
  const apiContext = await request.newContext(headers);
  const requestOptions = payload;
  return await executeRequest(requestUrl, method, requestOptions, apiContext);
}
