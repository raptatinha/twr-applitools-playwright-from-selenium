import { EmailMask2Options, JsonMask2Configs, PasswordMaskOptions, StringMaskV2Options } from "maskdata";

export const passwordOptions: PasswordMaskOptions = {
  maskWith: "*",
  maxMaskedCharacters: 100,
  unmaskedStartCharacters: 0,
  unmaskedEndCharacters: 0,
};

export const emailOptions: EmailMask2Options = {
  maskWith: "*",
  unmaskedStartCharactersBeforeAt: 2,
  unmaskedEndCharactersAfterAt: 2,
  maskAtTheRate: false,
};

export const jsonOptions: JsonMask2Configs = {
  emailMaskOptions: emailOptions,
  emailFields: ["data.address", "data.email"], //all the email keys in the JSON
  passwordMaskOptions: passwordOptions,
  passwordFields: ["data.password"],
};

export const stringMaskV2Options: StringMaskV2Options = {
  maskWith: "X",
  maxMaskedCharacters: 30, // To limit the output length to 30.
  unmaskedStartCharacters: 4,
  unmaskedEndCharacters: 10,
};
