import { test } from "@setup/base-fixture";
import { getEmailChannelKey } from "@utils/session-details";
import { getRandomEmail } from "@utils/util";
import { ESubscriptionStatus } from "@enums/contact-enum";
import { generateContactAttributeData } from "@data/contact-attribute-data";
import contact from "@data/contact-email-data";

test.describe("Contacts Suite", { tag: ["@contacts"] }, () => {
  test("Create new contact via UI subscribed to email", { tag: ["@smoke"] }, async ({ pm }) => {
    const contactEmail = await getRandomEmail(true);
    const subscribeStatus = ESubscriptionStatus.SUBSCRIBED;
    const channel = getEmailChannelKey();
    const attributes = generateContactAttributeData;

    await test.step("Open the create new contact form", async () => {
      await pm.getContactsStepsPage().openFormToCreateNewContact();
    });

    await test.step("Enter the channel email information", async () => {
      await pm.getContactsStepsPage().fillChannelInformation(contactEmail, subscribeStatus);
    });

    await test.step("Save the new contact", async () => {
      await pm.getContactsStepsPage().saveContact();
    });

    await test.step("Validate contact is created with attributes and list", async () => {
      await pm.getContactsStepsPage().assertSavedChannelsInformation(contactEmail, channel, subscribeStatus);
      await pm.getContactsStepsPage().assertSavedStringAttribute(attributes.stringKey, attributes.stringValue);
      console.log("New Contact created successfully via UI");
    });
  });

  test("Update existing contact via UI", async ({ pm }) => {
    const contactEmail = contact.subscribe_contact_1.email;
    const attributes = generateContactAttributeData;

    await test.step("Open the contact profile page of existing contact", async () => {
      await pm.getContactsStepsPage().openExistingContactProfilePage(contactEmail);
    });

    await test.step("Edit the Attributes and validate changes", async () => {
      await pm.getContactsStepsPage().editStringAttribute(attributes.stringKey, attributes.stringValue);
      await pm.getContactsStepsPage().editNumberAttribute(attributes.numberKey, attributes.numberValue);
    });

    await test.step("Edit the Lists and validate changes", async () => {
      await pm.getContactsStepsPage().editList(attributes.list);
      console.log("Existing Contact updated successfully via UI");
    });
  });
});
