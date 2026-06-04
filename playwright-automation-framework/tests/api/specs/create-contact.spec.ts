import { expect, test } from "@playwright/test";
import { getRandomEmail } from "@utils/util";
import { createContactViaSystemApi } from "@requests/contact-create";
import { createNewContact } from "@requests/contact-create";
import { deleteContactViaSwaggerApi, waitForContactToBeDeleted } from "@requests/contact-delete";
import { deleteContactByPrimaryKey } from "@requests/contact-delete";
import { updateContactViaSwaggerApi } from "@requests/contact-update";

test.describe("Contacts Suite", { tag: ["@contacts"] }, () => {
  test("Create new contact subscribed to email via system API", async () => {
    const contactEmail = await getRandomEmail();
    const responseBody = await createContactViaSystemApi(contactEmail);
    const json = await responseBody.json();
    expect(json, "Contact is not created").toHaveProperty("ID");
    expect(json).toHaveProperty("channels.email.address", contactEmail);
    expect(json).toHaveProperty("channels.email.ss", "s");
    expect(await deleteContactByPrimaryKey(json.ID)).toBeTruthy();
  });

  for (const subscribeStatus in ESubscriptionStatus) {
    test(`Create new contact ${subscribeStatus} to email via swagger API`, async () => {
      const contactEmail = await getRandomEmail();
      const channelKey = sessionDetails.getEmailChannelKey();
      const contact = {
        channels: { channelKey, address: contactEmail, subscriptionStatusValue: ESubscriptionStatus[subscribeStatus] },
      };
      const ID = await createNewContact(contact);
      const contactResponse = await getContactByEmailViaSwaggerApi(contactEmail);
      const contactJson = await contactResponse.json();
      expect(contactJson[0]).toHaveProperty("channels.email.address", contactEmail);
      expect(contactJson[0]).toHaveProperty("channels.email.subscribeStatus", ESubscriptionStatus[subscribeStatus]);
      expect(await deleteContactByPrimaryKey(ID)).toBeTruthy();
    });
  }

  test("Create new contact subscribed to email with attributes via swagger API", async () => {
    const contactEmail = await getRandomEmail();
    const channelKey = sessionDetails.getEmailChannelKey();
    const contact = {
      channels: { channelKey, address: contactEmail, subscriptionStatusValue: ESubscriptionStatus.SUBSCRIBED },
    };

    const ID = await createNewContact(contact);
    const contactResponse = await getContactByEmailViaSwaggerApi(contactEmail);
    const contactJson = await contactResponse.json();
    expect(contactJson[0]).toHaveProperty("channels.email.address", contactEmail);
    expect(contactJson[0]).toHaveProperty("channels.email.subscribeStatus", ESubscriptionStatus.SUBSCRIBED);
    expect(await deleteContactByPrimaryKey(ID)).toBeTruthy();
  });

  test("Update existing contact via swagger API", async () => {
    const contactEmail = await createNewContactSubscribedToEmailViaSwagger();
    const primaryKey = "email:" + contactEmail;
    const payload = {
      data: {
      },
    };
    const responseBody = await updateContactViaSwaggerApi(primaryKey, payload);
    const json = await responseBody.json();
    expect(json).toHaveProperty("success", true);

    const contactResponse = await getContactByEmailViaSwaggerApi(contactEmail);
    const contactJson = await contactResponse.json();
    expect(await deleteContactByPrimaryKey(contactJson[0]._id)).toBeTruthy();
  });

  test("Delete existing contact via swagger API", async () => {
    const contactEmail = await createNewContactSubscribedToEmailViaSwagger();
    const primaryKey = "email:" + contactEmail;
    const responseBody = await deleteContactViaSwaggerApi(primaryKey);
    const json = await responseBody.json();
    expect(json).toHaveProperty("success", true);
    expect(await waitForContactToBeDeleted(contactEmail)).toBeTruthy();
  });
});
