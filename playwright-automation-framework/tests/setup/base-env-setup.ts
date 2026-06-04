import url from "@setup/base-url";
import { ETestLayer } from "@enums/test-layer-enum";
import { ICredentials } from "@interfaces/base-interface";

const accountId = process.env.ACCOUNT_ID || "0001";
const environment = (process.env.ENVIRONMENT || "TEST").toLowerCase();
const backendBranchName = (process.env.BE_BRANCH || "main").replace(/_/g, "-").toLowerCase();
const frontendBranchName = (process.env.FE_BRANCH || "main").replace(/_/g, "-").toLowerCase();
const appName = process.env.PROJECT_NAME || "";

export function getCredentials() {
  const envVariable = `${environment.toUpperCase()}_${accountId}_`;

  if (!process.env[envVariable + "USERNAME"]) {
    throw new Error(
      `The credentials for ${envVariable} are missing in the .env file. Please check the README.md file for instructions.`
    );
  }

  const credentials: ICredentials = {
    username: process.env[envVariable + "USERNAME"]!,
    password: process.env[envVariable + "PASSWORD"]!,
    apiKey: process.env[envVariable + "API_KEY"]!,
  };

  return credentials;
}

/**
 *
 * @param testLayer can be UI, API.
 * @returns the base url for the test layer.
 */
export function buildBaseUrl(testLayer?: ETestLayer) {
  testLayer = testLayer || ETestLayer.UI;
  let baseUrl = "";

  baseUrl = url[environment][testLayer] ;
  return baseUrl;
}
