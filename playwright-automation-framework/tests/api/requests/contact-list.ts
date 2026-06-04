import { request } from "@playwright/test";
import { buildBaseUrl } from "@setup/base-env-setup";
import apiMethods from "@utils/api-methods";
import apiPaths from "@utils/api-paths";
import { executeRequest } from "@utils/api-request";
import times from "@utils/times";
import { waitForItemToBeFound } from "@utils/util";
import { ETestLayer } from "@enums/test-layer-enum";
import { getListPayload } from "@payloads/contact-list-payload";

const baseUrl = buildBaseUrl(ETestLayer.API);

export async function getContactList(listName: string) {
  const queryParams = `?name=${listName}`;
  const requestUrl = `${baseUrl}${apiPaths.lists}${queryParams}`;
  const method = apiMethods.get;
  const headers = { extraHTTPHeaders: { "Content-Type": "application/json" } };
  const apiContext = await request.newContext(headers);
  const requestOptions = {};
  return await executeRequest(requestUrl, method, requestOptions, apiContext, false);
}

export async function createContactList(listName: string, tracked = false) {
  const requestUrl = `${baseUrl}${apiPaths.lists}`;
  const method = apiMethods.post;
  const headers = { extraHTTPHeaders: { "Content-Type": "application/json" } };
  const requestOptions = getListPayload(listName, tracked);
  const apiContext = await request.newContext(headers);
  return await executeRequest(requestUrl, method, requestOptions, apiContext, false);
}

export async function deleteContactList(listId: number) {
  const queryParams = `/${listId}`;
  const requestUrl = `${baseUrl}${apiPaths.lists}${queryParams}`;
  const method = apiMethods.delete;
  const headers = { extraHTTPHeaders: { "Content-Type": "application/json" } };
  const apiContext = await request.newContext(headers);
  const requestOptions = {};
  return await executeRequest(requestUrl, method, requestOptions, apiContext);
}

export async function isListFound(listName: string) {
  const response = await getContactList(listName);
  return response.ok() ? true : false;
}

export async function getListId(listName: string) {
  const response = await getContactList(listName);
  if (response.ok()) {
    const json = await response.json();
    return json.id;
  } else {
    console.error(`List ${listName} not found`);
    return false;
  }
}

async function waitForListReplication(listName: string) {
  if (!(await waitForItemToBeFound(isListFound, listName, times.fiveSeconds, times.twoMinutes))) {
    throw new Error(`New Contact list '${listName}' was not found.`);
  }
}

export async function createListIfNotExists(listName: string, wait = false) {
  if (!(await isListFound(listName))) {
    await createContactList(listName);
    if (wait) {
      await waitForListReplication(listName);
    }
  }
}
