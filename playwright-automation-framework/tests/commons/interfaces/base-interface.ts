import { type Locator } from "@playwright/test";

export interface IWaitForExpectedValueOptions {
  expectedElement: Locator;
  expectedValue: number | string;
  maxRetries?: number;
  targetPage?: Locator[];
  proxyPage?: Locator;
}
export interface ICredentials {
  username: string;
  password: string;
  apiKey: string;
}
