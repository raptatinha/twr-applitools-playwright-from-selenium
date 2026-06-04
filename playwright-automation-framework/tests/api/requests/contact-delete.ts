import { request } from "@playwright/test";
import { buildBaseUrl } from "@setup/base-env-setup";
import apiMethods from "@utils/api-methods";
import apiPaths from "@utils/api-paths";
import { executeRequest } from "@utils/api-request";
import times from "@utils/times";
import { waitForItemToBeFound } from "@utils/util";
import { ETestLayer } from "@enums/test-layer-enum";
import { getContactByChannelAddress, getContactByIDViaSwaggerApi } from "@requests/contact-create";
import { getRuleChannelAddressEmail } from "@payloads/audience-payload";

const baseUrl = buildBaseUrl(ETestLayer.API);

/**
 * Delete contact via swagger API v2.
 * @param primaryKey  the contact primary key, email address or ID
 * example of primaryKey param for email address: "email:mymail@sample.com"
 */
export async function deleteContactViaSwaggerApi(primaryKey: string) {
  const queryParams = `/${encodeURIComponent(primaryKey)}`;
  const requestUrl = `${baseUrl}${apiPaths.contacts}${queryParams}`;
  const method = apiMethods.delete;
  const headers = { extraHTTPHeaders: { "Content-Type": "application/json" } };
  const apiContext = await request.newContext(headers);
  const requestOptions = {};
  return await executeRequest(requestUrl, method, requestOptions, apiContext);
}

/**
 * Search if a contact is deleted from account
 * supports email and ID primary keys.
 * @param primaryKey  the contact primary key, email address or ID - if primaryKey includes "@" means it's an email address, otherwise is a ID.
 */
export async function isContactDeleted(primaryKey: string) {
  if (primaryKey.includes("@")) {
    const requestUrl = `${baseUrl}${apiPaths.contactsSearch}`;
    const method = apiMethods.post;
    const payload = getRuleChannelAddressEmail(primaryKey);
    const requestOptions = { data: payload };
    const searchContactResponse = await executeRequest(requestUrl, method, requestOptions);
    const searchContactResponseBody = await searchContactResponse.json();
    return searchContactResponseBody.total_entries === 0;
  } else {
    const contactResponse = await getContactByIDViaSwaggerApi(primaryKey);
    return !contactResponse.ok();
  }
}

export async function waitForContactToBeDeleted(primaryKey: string) {
  return await waitForItemToBeFound(isContactDeleted, primaryKey, times.fiveSeconds, times.fiveMinutes);
}

/**
 * Delete the contact using primary key
 * Supports (email or ID) only.
 * @param primaryKey  the primary key
 * @param wait      wait for the contact to be deleted (Does not wait by default)
 */
export async function deleteContactByPrimaryKey(primaryKey: string, wait = false) {
  const deleteResponse = await deleteContactViaSwaggerApi(primaryKey);
  if (!deleteResponse.ok()) {
    throw new Error(`Failed to delete contact via API. Status: ${deleteResponse.status}`);
  } else {
    const deleteJson = await deleteResponse.json();
    if (wait) {
      return deleteJson.success ? await waitForContactToBeDeleted(primaryKey) : false;
    } else {
      return deleteJson.success;
    }
  }
}

/**
 * Delete the contact using channel and address if is found.
 * Supports all channels.
 * @param channel   the channel key
 * @param address   the address value
 * @param wait      wait for the contact to be deleted by default
 */
export async function deleteContactByAddress(channel: string, address: string, wait = true) {
  const searchResponse = await getContactByChannelAddress(channel, address);
  if (!searchResponse.ok()) {
    throw new Error(`Failed to search contact via API. Status: ${searchResponse.status}`);
  } else {
    const searchJson = await searchResponse.json();
    if (searchJson.total_entries === 0) {
      console.log(`Contact not found with ${address}`);
    } else {
      for (const record of searchJson.records) {
        await deleteContactByPrimaryKey(record.ID, wait);
        console.log(`Contact ${address} deleted via API`);
      }
    }
  }
}
