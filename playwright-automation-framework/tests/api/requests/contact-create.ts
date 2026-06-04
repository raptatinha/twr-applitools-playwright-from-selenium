import { request } from "@playwright/test";
import { buildBaseUrl } from "@setup/base-env-setup";
import apiMethods from "@utils/api-methods";
import apiPaths from "@utils/api-paths";
import { executeRequest } from "@utils/api-request";
import times from "@utils/times";
import { getRandomEmail, waitForItemToBeFound } from "@utils/util";
import { ETestLayer } from "@enums/test-layer-enum";
import { IContact } from "@interfaces/contact-interface";
import { createContactPayload } from "@payloads/contact-payload";

const baseUrl = buildBaseUrl(ETestLayer.API);

export async function createContactViaSystemApi(
  contactEmail: string,
  subscribeStatus = ESubscriptionStatus.SUBSCRIBED
) {
  const requestUrl = `${baseUrl}${apiPaths.contacts}`; //RENATA: Update this URL
  const method = apiMethods.post;
  const channelKey = EChannelKey.EMAIL;
  const contact = { channels: { channelKey, address: contactEmail, subscriptionStatusValue: subscribeStatus } };
  const requestOptions = createContactPayload(contact);
  return await executeRequest(requestUrl, method, requestOptions, undefined, false);
}

/**
 * Creates a contact with channel, attributes and list,via swagger API v2.
 * @param contact   the contact object IContact
 */
export async function createContactViaSwaggerApi(contact: IContact) {
  const requestUrl = `${baseUrl}${apiPaths.contacts}`;
  const method = apiMethods.post;
  const headers = { extraHTTPHeaders: { "Content-Type": "application/json" } };
  const requestOptions = createContactPayload(contact);
  const apiContext = await request.newContext(headers);
  return await executeRequest(requestUrl, method, requestOptions, apiContext, false);
}

/**
 * Search for the contact by ID via swagger API v2.
 */
export async function getContactByIDViaSwaggerApi(ID: string) {
  const queryParams = `?ID=${encodeURIComponent(ID)}&return_count=false`;
  const requestUrl = `${baseUrl}${apiPaths.contacts}${queryParams}`;
  const method = apiMethods.get;
  const headers = { extraHTTPHeaders: { "Content-Type": "application/json" } };
  const apiContext = await request.newContext(headers);
  const requestOptions = {};
  return await executeRequest(requestUrl, method, requestOptions, apiContext, false);
}


/***
 * Create a new contact and return the ID.
 */
export async function createNewContact(contact: IContact) {
  const responseBody = await createContactViaSwaggerApi(contact);
  if (!responseBody.ok()) {
    throw new Error(`Failed to create contact via API. Status: ${responseBody.status}`);
  } else {
    const json = await responseBody.json();
    return json.ID;
  }
}
