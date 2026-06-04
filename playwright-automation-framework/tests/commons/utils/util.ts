import { faker } from "@faker-js/faker";
import { IGeoAttribute } from "@interfaces/contact-interface";
import { isContactFoundByEmail } from "@requests/contact-create";
import timeZoneData from "@data/time-zone-data";
import userData from "@data/user-data";

export async function getRandomEmail(isCloudflare = false) {
  let contactEmail: string;
  let isSearchable = true;
  do {
    const randomNumber = generateRandomNumber(5);
    const dateNow = Date.now();
    const domain = isCloudflare ? userData.email.cloudflare.domain : userData.email.gmail.domain;
    const suffix = isCloudflare ? userData.email.cloudflare.suffix : "";
    contactEmail = `${userData.email.prefix}${suffix}${dateNow}${randomNumber}${domain}`;
    isSearchable = await isContactFoundByEmail(contactEmail);
  } while (isSearchable);

  return contactEmail;
}

export function generateRandomNumber(length = 9) {
  return faker.string.numeric(length);
}

export function convertMillisecondsToMinutes(value: number) {
  return value / 1000 / 60;
}

export function convertMillisecondsToSeconds(value: number) {
  return (value / 1000) % 60;
}

/**
 * This function waits for a specific item to be found by repeatedly calling a check function.
 * It checks at regular intervals and stops when the item is found or when a timeout is reached.
 * checkFunction examples: isContactSearchable() , doesContactAttributeExist()
 * To be use to wait for replication.
 *
 * @param checkFunction    The function that checks if the item is found. Must returns a boolean.
 * @param item             The item to be found. Ex: contactEmail or attributeName.
 * @param intervalSeconds  The interval between checks in milliseconds.
 * @param timeoutMinutes   The timeout in minutes is the Maximum wait time.
 * @returns                Returns true if the item is found, false otherwise.
 */
export async function waitForItemToBeFound(
  checkFunction: (item: string) => Promise<boolean>,
  item: string,
  intervalSeconds: number,
  timeoutMinutes: number
) {
  const timeout = convertMillisecondsToMinutes(timeoutMinutes);
  const startTime = Date.now();

  try {
    while (!(await checkFunction(item))) {
      const currentTime = Date.now() - startTime;
      if (currentTime > timeoutMinutes) {
        console.error(`Item '${item}' is not found. Timeout of ${timeout} minutes reached.`);
        return false;
      }
      await new Promise((resolve) => {
        setTimeout(resolve, intervalSeconds);
      });
    }
    const endTime = convertMillisecondsToSeconds(Date.now() - startTime);
    console.info(`Item '${item}' found. Time elapsed waiting ${endTime} seconds.`);
    return true;
  } catch (error) {
    console.error(`An error occurred:`, error);
    return false;
  }
}

/**
 * @returns IDateAttribute object with random date values
 */
export function getRandomDate() {
  const date = faker.date.anytime();
  return {
    month: date.getMonth().toString().padStart(2, "0"),
    day: date.getDate().toString().padStart(2, "0"),
    year: date.getFullYear().toString(),
    hour: date.getHours().toString().padStart(2, "0"),
    minute: date.getMinutes().toString().padStart(2, "0"),
    second: date.getSeconds().toString().padStart(2, "0"),
  };
}

/**
 * @returns IGeoAttribute object with random geo values
 */
export function getRandomGeoLocationValues() {
  const timeZoneIndex = faker.number.int({ min: 0, max: timeZoneData.timeZone.length - 1 });
  const timeZone = timeZoneData.timeZone[timeZoneIndex];

  return {
    address1: faker.location.streetAddress(),
    address2: faker.location.secondaryAddress(),
    city: faker.location.city(),
    state: faker.location.state(),
    postalCode: faker.location.zipCode(),
    timezone: timeZone,
  };
}

/**
 * Return string with geo formatted like: 54134 Aufderhar Loop, Suite 460, East Shanny, Pennsylvania, 44413-8326, Guyana, Africa/Banjul
 */
export function formatGeoLocation(geo: IGeoAttribute) {
  return Object.values(geo).join(", ");
}

/**
 * Retrieves the key of an enum or an object based on its value.
 */
export function getKeyByValue<T extends Record<string, string | number>>(
  obj: T,
  value: string | number
): keyof T | undefined {
  const entries = Object.entries(obj);
  const foundEntry = entries.find(([, val]) => val === value);
  if (foundEntry) {
    const [key] = foundEntry;
    const isNumericKey = !isNaN(Number(key));
    return isNumericKey ? undefined : (key as keyof T);
  }

  return undefined;
}
