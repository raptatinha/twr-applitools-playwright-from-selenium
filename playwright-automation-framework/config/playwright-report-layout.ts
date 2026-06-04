import { Block, KnownBlock } from "@slack/types";
import { SummaryResults } from "playwright-slack-report/dist/src";
import { buildBaseUrl } from "@setup/base-env-setup";
import { ETestLayer } from "@enums/test-layer-enum";

const LOCALHOST = "http://localhost:9323/";

interface EnvVariables {
  ENVIRONMENT: string;
  ACCOUNT_ID: string;
  RELEASE_TAG?: string;
  CI_JOB_URL: string;
  CI_JOB_ID: string;
  CI_JOB_STARTED_AT: string;
  GITLAB_USER_NAME?: string;
  GITLAB_USER_LOGIN?: string;
  CI_PROJECT_URL?: string;
  CI_COMMIT_SHA?: string;
  BE_BRANCH?: string;
  FE_BRANCH?: string;
}

function getEnvVariables(): EnvVariables {
  return {
    ENVIRONMENT: process.env.ENVIRONMENT || "",
    ACCOUNT_ID: process.env.ACCOUNT_ID || "",
    RELEASE_TAG: process.env.RELEASE_TAG,
    CI_JOB_URL: process.env.CI_JOB_URL || LOCALHOST,
    CI_JOB_ID: process.env.CI_JOB_ID || "",
    CI_JOB_STARTED_AT: process.env.CI_JOB_STARTED_AT || "0",
    GITLAB_USER_NAME: process.env.GITLAB_USER_NAME,
    GITLAB_USER_LOGIN: process.env.GITLAB_USER_LOGIN,
    CI_PROJECT_URL: process.env.CI_PROJECT_URL,
    CI_COMMIT_SHA: process.env.CI_COMMIT_SHA,
    BE_BRANCH: process.env.BE_BRANCH,
    FE_BRANCH: process.env.FE_BRANCH,
  };
}

function createHeaderBlock(env: EnvVariables, baseUrl: string): Block {
  const releaseTag = env.RELEASE_TAG ? `*${env.RELEASE_TAG}*` : "";
  return {
    type: "section",
    text: {
      type: "mrkdwn",
      text: `Playwright Results finished for *${env.ENVIRONMENT}* account *${env.ACCOUNT_ID}* on ${releaseTag} ${baseUrl}`,
    },
  };
}

function createSummaryBlocks(summaryResults: SummaryResults, reportUrl: string, ciJobUrl: string): Block[] {
  const totalTests =
    summaryResults.passed + summaryResults.failed + (summaryResults.flaky || 0) + summaryResults.skipped;

  return [
    {
      type: "section",
      text: {
        type: "mrkdwn",
        text: `✅ Passed: ${summaryResults.passed}  |  ❌ Failed: ${summaryResults.failed}  |  🟡 Flaky: ${summaryResults.flaky}  |  ⏩ Skipped: ${summaryResults.skipped}  |  🔋 Total:  ${totalTests}`,
      },
    },
    {
      type: "actions",
      elements: [createButton(":playwright: Test Report", reportUrl), createButton(":trex: Job Log", ciJobUrl)],
    },
  ];
}

function createButton(text: string, url: string): KnownBlock {
  return {
    type: "button",
    text: {
      type: "plain_text",
      text,
      emoji: true,
    },
    style: "primary",
    value: url,
    url,
  };
}

function getDefaultCommitLink(env: EnvVariables): string {
  return `<${env.CI_PROJECT_URL}/-/commit/${env.CI_COMMIT_SHA}|this change>`;
}

function getBranchLink(branch: string, repo: string, defaultBranch: string): string | null {
  return branch !== defaultBranch
    ? `<https://gitlab.com/acme/apps/${repo}/-/commits/${branch}/|this change>`
    : null;
}

function getCommitLink(env: EnvVariables): string {
  if (!env.FE_BRANCH || !env.BE_BRANCH) {
    return getDefaultCommitLink(env);
  }

  const feLink = getBranchLink(env.FE_BRANCH, "core-frontend", "main");
  const beLink = getBranchLink(env.BE_BRANCH, "core", "master");

  if (!feLink && !beLink) {
    return getDefaultCommitLink(env);
  }

  return [feLink, beLink].filter(Boolean).join(" and ");
}

function createMonitoringBlocks(env: EnvVariables, durationInMilliseconds: number): Block[] {
  if (env.CI_JOB_URL === LOCALHOST) {
    return [
      {
        type: "section",
        text: {
          type: "mrkdwn",
          text: `Tests triggered locally. No CI data available.`,
        },
      },
    ];
  }

  const commitLink = getCommitLink(env);
  const userText = `${env.GITLAB_USER_NAME} (${env.GITLAB_USER_LOGIN}) triggered by ${commitLink}.`;
  const durationText = `Duration: ${new Date(durationInMilliseconds).toLocaleTimeString().substring(3, 8)} mins`;

  return [
    { type: "section", text: { type: "mrkdwn", text: userText } },
    { type: "section", text: { type: "mrkdwn", text: durationText } },
  ];
}

export default function generateReport(summaryResults: SummaryResults): (Block | KnownBlock)[] {
  const env = getEnvVariables();
  const baseUrl = buildBaseUrl(ETestLayer.UI);
  const reportUrl =
    env.CI_JOB_URL !== LOCALHOST
      ? `https://acme.gitlab.com/-/qa/playwright-automation-framework/-/jobs/${env.CI_JOB_ID}/artifacts/playwright-report/index.html`
      : LOCALHOST;
  const durationInMilliseconds = Date.now() - new Date(env.CI_JOB_STARTED_AT).getTime();

  const header = createHeaderBlock(env, baseUrl);
  const summary = createSummaryBlocks(summaryResults, reportUrl, env.CI_JOB_URL);
  const monitoring = createMonitoringBlocks(env, durationInMilliseconds);

  return [header, ...summary, ...monitoring];
}
