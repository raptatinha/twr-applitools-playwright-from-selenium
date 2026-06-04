import { parse } from "csv-parse";
import { readFileSync } from "fs";
import { EImportMapping } from "@enums/contact-enum";

type CsvRow<T extends string> = Record<T, string>;

export function readJsonFile(filePath: string) {
  const fileData = readFileSync(filePath, "utf-8");
  return JSON.parse(fileData);
}

export async function readCsvFile<T extends string>(filePath: string, fileHeader: string[]) {
  const headers = [...fileHeader];
  const fileContent = readFileSync(filePath, "utf-8");

  return await new Promise<CsvRow<T>[]>((resolve, reject) => {
    parse(fileContent, { delimiter: ",", columns: headers }, (error, result: CsvRow<T>[]) => {
      if (error) {
        reject(error);
      }
      resolve(result);
    });
  });
}

export async function extractEmailsFromCsv<T extends string>(csv: CsvRow<T>[]) {
  const emails: string[] = [];
  csv.slice(1).forEach((row) => {
    const email = row[EImportMapping.EMAIL_ADDRESS];
    if (email) {
      emails.push(email);
    }
  });
  return emails;
}

/**
Recursively updates specific fields in a JSON object or array of objects.
The use of any in this function is mainly due to the dynamic nature of the input. 
The function is designed to work with both objects and arrays, and the contents of these structures can be of any type (e.g., primitives, objects, arrays)
**/
/* eslint-disable @typescript-eslint/no-explicit-any */
export function updateFieldsInJson(obj: any, objUpdateValues: { key: string; newValue: any }[]) {
  function recursiveUpdate(item: any) {
    if (typeof item === "object" && item !== null) {
      objUpdateValues.forEach((update) => {
        if (update.key in item) {
          item[update.key] = update.newValue;
        }
      });

      for (const key in item) {
        if (Object.prototype.hasOwnProperty.call(item, key)) {
          recursiveUpdate(item[key]);
        }
      }
    } else if (Array.isArray(item)) {
      for (const element of item) {
        recursiveUpdate(element);
      }
    }
  }
  recursiveUpdate(obj);
  return obj;
}
