import { defineConfig, devices } from "@playwright/test";
import * as dotenv from "dotenv";
import generateReport from "@config/playwright-report-layout";
import { buildBaseUrl } from "@setup/base-env-setup";
import times from "@utils/times";

dotenv.config();

const { CI } = process.env;

const SLACK_CHANNELS = ["eng-playwright-pipe-notification-temp"];
const TEST_TIMEOUT = times.tenMinutes;
const EXPECT_TIMEOUT = times.twentySeconds;
const ACTION_TIMEOUT = times.fortySeconds;

export default defineConfig({
  fullyParallel: true,
  forbidOnly: !!CI,
  retries: CI ? 1 : 0,
  workers: undefined,
  reporter: CI
    ? [
        ["html"],
        ["junit", { outputFile: "./playwright-report/playwright-test-results.xml" }],
        [
          "./node_modules/playwright-slack-report/dist/src/SlackReporter.js",
          {
            channels: SLACK_CHANNELS, //one or more slack channels
            sendResults: "always",
            layout: generateReport,
            showInThread: true,
          },
        ],
      ]
    : [
        ["html"],
        // Uncomment the below lines to generate slack report when running tests locally. The lines below should be commented when running tests in CI. See README.md for more details.
        /*
        [
          "./node_modules/playwright-slack-report/dist/src/SlackReporter.js",
          {
            channels: ["eng-playwright-pipe-notification-temp"], //one or more slack channels
            sendResults: "always", // "always" , "on-failure", "off"
            layout: generateReport,
            showInThread: true,
          },
        ],
        */
      ],

  /* Shared settings for all the projects below.*/
  use: {
    baseURL: buildBaseUrl(),
    trace: "retain-on-failure",
    testIdAttribute: "data-id",
    screenshot: "only-on-failure",
    userAgent: "acme-playwright",
    actionTimeout: ACTION_TIMEOUT,
  },

  timeout: TEST_TIMEOUT,
  expect: {
    timeout: EXPECT_TIMEOUT,
  },

  /* Configure projects for major browsers */
  projects: [
    {
      // to run the global-setup file and store login state
      name: "setup",
      testDir: "./tests/setup",
      testMatch: "**/global-setup.ts",
    },
    {
      name: "chromium",
      dependencies: ["setup"],
      use: {
        ...devices["Desktop Chrome"],
        storageState: "./session.json", //to use the stored login state for all tests
      },
    },
  ],
});
