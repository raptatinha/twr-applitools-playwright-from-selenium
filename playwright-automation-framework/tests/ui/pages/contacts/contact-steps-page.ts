import { type Page } from "@playwright/test";
import { IDateAttribute, IGeoAttribute } from "@interfaces/contact-interface";
import { ContactCreatePage } from "@pages/contacts/contact-create-page";
import { ContactProfileAttributesPage } from "@pages/contacts/contact-profile-attributes-page";
import { ContactProfileChannelsPage } from "@pages/contacts/contact-profile-channels-page";
import { ContactProfileDashboardPage } from "@pages/contacts/contact-profile-dashboard-page";
import { ContactProfileListPage } from "@pages/contacts/contact-profile-lists-page";
import { ContactProfileModalPage } from "@pages/contacts/contact-profile-modal-page";

export class ContactsStepsPage {
  readonly page: Page;
  readonly contactCreatePage: ContactCreatePage;
  readonly contactProfileModalPage: ContactProfileModalPage;
  readonly contactProfileListPage: ContactProfileListPage;
  readonly contactProfileAttributesPage: ContactProfileAttributesPage;
  readonly contactProfileChannelsPage: ContactProfileChannelsPage;
  readonly contactProfileDashboardPage: ContactProfileDashboardPage;

  constructor(page: Page) {
    this.page = page;
    this.contactCreatePage = new ContactCreatePage(this.page);
    this.contactProfileModalPage = new ContactProfileModalPage(this.page);
    this.contactProfileListPage = new ContactProfileListPage(this.page);
    this.contactProfileAttributesPage = new ContactProfileAttributesPage(this.page);
    this.contactProfileChannelsPage = new ContactProfileChannelsPage(this.page);
    this.contactProfileDashboardPage = new ContactProfileDashboardPage(this.page);
  }

  async openFormToCreateNewContact() {
    await this.contactCreatePage.openCreateContactForm();
    await this.contactCreatePage.assertTitleIsPresent();
  }

  async openExistingContactProfilePage(email: string) {
    console.log("Opening the existing contact profile page");
    await this.contactProfileModalPage.openContactProfileModal();
    await this.contactProfileModalPage.assertModalTitleIsPresent();
    await this.contactProfileModalPage.selectContactFromResultList(email);
    await this.contactProfileModalPage.assertProfileTitleIsPresent();
  }

  async fillChannelInformation(contactEmail: string, subscribeStatus: string) {
    await this.contactCreatePage.fillEmail(contactEmail);
    await this.contactCreatePage.selectSubscribeStatus(subscribeStatus);
  }

  async saveContact() {
    await this.contactCreatePage.clickSaveButton();
    await this.contactCreatePage.assertConfirmationMessagePopupIsPresent();
  }

  async assertSavedChannelsInformation(contactEmail: string, channelKey: string, subscribeStatus: string) {
    await this.contactProfileChannelsPage.openContactProfileChannelsTab();
  }

  async assertSavedStringAttribute(key: string, value: string) {
    await this.contactProfileAttributesPage.openContactProfileAttributesTab();
    await this.contactProfileAttributesPage.assertStringAttributeValueIsPresent(key, value);
  }

  async assertSavedNumberAttribute(key: string, value: number) {
    await this.contactProfileAttributesPage.openContactProfileAttributesTab();
    await this.contactProfileAttributesPage.assertNumberAttributeValueIsPresent(key, value);
  }

  async assertSavedArrayAttribute(key: string, values: string[]) {
    await this.contactProfileAttributesPage.openContactProfileAttributesTab();
    await this.contactProfileAttributesPage.assertArrayAttributeValueIsPresent(key, values);
  }

  async assertSavedDateAttribute(key: string, values: IDateAttribute) {
    await this.contactProfileAttributesPage.openContactProfileAttributesTab();
    await this.contactProfileAttributesPage.assertDateAttributeValueIsPresent(key, values);
  }

  async assertSavedGeoAttribute(key: string, values: IGeoAttribute) {
    await this.contactProfileAttributesPage.openContactProfileAttributesTab();
    await this.contactProfileAttributesPage.assertGeoAttributeValuesArePresent(key, values);
  }

  async assertSavedListInformation(list: string) {
    await this.contactProfileListPage.openContactProfileListsTab();
  }

  async assertDashboardEventInformation(event: string) {
    await this.contactProfileDashboardPage.openContactProfileDashboardTab();
    await this.contactProfileDashboardPage.assertEventItemIsPresent(event);
  }

  async assertDashboardCartItemInformation(items: { name: string }[]) {
    await this.contactProfileDashboardPage.openContactProfileDashboardTab();
    await this.contactProfileDashboardPage.assertCartItemIsPresent(items);
  }

  async assertDashboardOrderInformation(order: { orderID: string }) {
    await this.contactProfileDashboardPage.openContactProfileDashboardTab();
    await this.contactProfileDashboardPage.assertOrderItemIsPresent(order);
  }

  async editStringAttribute(key: string, value: string) {
    await this.contactProfileAttributesPage.openContactProfileAttributesTab();
    await this.contactProfileAttributesPage.clickEditAttributesButton(key);
    await this.contactCreatePage.clickSaveButton();
    await this.contactProfileAttributesPage.assertSavedToastMessageIsPresent();
    await this.contactProfileAttributesPage.assertStringAttributeValueIsPresent(key, value);
  }

  async editNumberAttribute(key: string, value: number) {
    await this.contactProfileAttributesPage.openContactProfileAttributesTab();
    await this.contactProfileAttributesPage.clickEditAttributesButton(key);
    await this.contactCreatePage.clickSaveButton();
    await this.contactProfileAttributesPage.assertSavedToastMessageIsPresent();
    await this.contactProfileAttributesPage.assertNumberAttributeValueIsPresent(key, value);
  }

  async editArrayAttribute(key: string, values: string[]) {
    await this.contactProfileAttributesPage.openContactProfileAttributesTab();
    await this.contactProfileAttributesPage.clickEditAttributesButton(key);
    await this.contactCreatePage.clickSaveButton();
    await this.contactProfileAttributesPage.assertSavedToastMessageIsPresent();
    await this.contactProfileAttributesPage.assertArrayAttributeValueIsPresent(key, values);
  }

  async editDateAttribute(key: string, values: IDateAttribute) {
    await this.contactProfileAttributesPage.openContactProfileAttributesTab();
    await this.contactProfileAttributesPage.clickEditAttributesButton(key);
    await this.contactCreatePage.clickSaveButton();
    await this.contactProfileAttributesPage.assertSavedToastMessageIsPresent();
    await this.contactProfileAttributesPage.assertDateAttributeValueIsPresent(key, values);
  }

  async editGeoAttribute(key: string, values: IGeoAttribute) {
    await this.contactProfileAttributesPage.openContactProfileAttributesTab();
    await this.contactProfileAttributesPage.clickEditAttributesButton(key);
    await this.contactCreatePage.clickSaveButton();
    await this.contactProfileAttributesPage.assertSavedToastMessageIsPresent();
    await this.contactProfileAttributesPage.assertGeoAttributeValuesArePresent(key, values);
  }

  async editList(list: string) {
    await this.contactProfileListPage.openContactProfileListsTab();
    await this.contactProfileListPage.clickEditListsButton();
    await this.contactCreatePage.clickSaveButton();
    await this.contactProfileListPage.assertSavedToastMessageIsPresent();
  }
}
