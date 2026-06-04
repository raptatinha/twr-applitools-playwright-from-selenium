import { expect, type Locator, type Page } from "@playwright/test";
import times from "@utils/times";
import { convertMillisecondsToMinutes, convertMillisecondsToSeconds } from "@utils/util";
import { IWaitForExpectedValueOptions } from "@interfaces/base-interface";

export class BasePage {
  readonly page: Page;
  readonly successToast: Locator;
  readonly closeToast: Locator;
  readonly alertModalCloseIcon: Locator;
  readonly alertModalConfirmButton: Locator;
  readonly alertModalCancelButton: Locator;
  readonly MAX_RETRIES = 6;

  constructor(page: Page) {
    this.page = page;
    this.successToast = page.locator('[class="toast toast-success"]').first();
    this.closeToast = page.locator('[class="toast-close-button"]').first();
    this.alertModalCloseIcon = page.getByTestId("alert-modal-button-close");
    this.alertModalConfirmButton = page.getByTestId("alert-action-confirm");
    this.alertModalCancelButton = page.getByTestId("alert-action-cancel");
  }

  async openURL(url?: string) {
    const URL = url ? await this.page.goto(url) : await this.page.goto("");
    console.log("========= Opening ", URL?.url());
  }

  async waitForSuccessToast() {
    await expect(this.successToast).toBeVisible();
    await this.successToast.click();
    await expect(this.successToast).toBeHidden();
  }

  async confirmAlertModal() {
    await expect(this.alertModalConfirmButton).toBeVisible();
    await this.alertModalConfirmButton.click();
  }

  /**
   * Use this method to wait for a value to be displayed in a locator.
   * For example to wait for stats to be displayed in performance page.
   * It uses navigation back and forward to refresh the same page and retry the assertion (optional)
   * If no navigation locators are provided, it will retry the assertion in the same page.
   * @param expectedElement  the element locator you want to wait on the value
   * @param expectedValue    the value expected to be displayed in the locator
   * @param maxRetries       iterations for checking the value is displayed in a locator, 6 by default total 2 minutes
   * @param targetPage       the button locator you want to click to open the expected page if needed (optional)
   * @param proxyPage        the button locator you want to click to open the alternative page if needed (optional)
   * @returns                true if the value is displayed in the locator, false otherwise
   */
  async waitForExpectedValue(options: IWaitForExpectedValueOptions) {
    const { expectedElement, expectedValue, maxRetries = this.MAX_RETRIES, targetPage, proxyPage } = options;
    const startTime = Date.now();
    let retryCount = 0;

    while (retryCount < maxRetries) {
      try {
        await expect(expectedElement).toHaveText(expectedValue.toString());
        console.log(`Expected value "${expectedValue}" found after ${retryCount} attempts.`);
        return true;
      } catch {
        console.warn(`Element not found on attempt ${retryCount + 1}. Retrying...`);
      }

      if (targetPage && proxyPage) {
        await proxyPage.click({ delay: times.halfSecond });
        for (const target of targetPage) {
          await target.click({ delay: times.halfSecond });
        }
      } else {
        await this.page.reload();
      }

      retryCount++;
    }

    const endTime = convertMillisecondsToSeconds(Date.now() - startTime);
    console.log(`Failed to find expected value: "${expectedValue}". Time elapsed: ${endTime} seconds.`);
    return false;
  }

  /**
   * Wait for a response in the browser network tab
   * @param urlRegex  the url requested in a regex format
   * @param maxTimeout  the maximum timeout to waitfor the response, 2 minutes by default
   * ex: /\/batchmessages\/[^/]+\/dashboard/
   * @returns the response json if the response status is 200, false otherwise
   */
  async waitForNetworkResponse(urlRegex: RegExp, maxTimeout = times.twoMinutes) {
    try {
      const response = await this.page.waitForResponse(
        (response) => !!response.url().match(urlRegex) && response.status() === 200,
        { timeout: maxTimeout }
      );
      return response.status() === 200 ? await response.json() : false;
    } catch (error) {
      console.error(
        `Network response for "${urlRegex}" not found in ${convertMillisecondsToMinutes(maxTimeout)} minutes. Error: ${error}`
      );
      return false;
    }
  }
}
