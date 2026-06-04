import { EChannelKey } from "@enums/contact-enum";
import { IContact } from "@interfaces/contact-interface";

const CHANNELS = "channels";
const ADDRESS = "address";
const SUBSCRIBE_STATUS = "subscribeStatus";

export function createContactPayload(contact: IContact) {
  let payload = {};

  if (contact.channels) {
    const channelKey = contact.channels.channelKey;
    switch (channelKey) {
      case EChannelKey.EMAIL:
        payload = getEmailChannelPayload(contact);
        break;
      case EChannelKey.PUSH:
      case EChannelKey.SDK:
        payload = getPushChannelPayload(contact);
        break;
      case EChannelKey.SMS:
      case EChannelKey.CLX:
      case EChannelKey.PLIVO:
        payload = getSmsChannelPayload(contact);
        break;
    }
  }

  if (contact.list) {
    payload[contact.list] = true;
  }
  if (contact.attribute) {
    contact.attribute.forEach((attr) => (payload[attr.key] = attr.value));
  }

  return { data: payload };
}

function getEmailChannelPayload(contact: IContact) {
  const { channelKey, address, subscriptionStatusValue } = contact.channels;
  return {
    [CHANNELS]: {
      [channelKey]: {
        [ADDRESS]: address,
        [SUBSCRIBE_STATUS]: subscriptionStatusValue,
      },
    },
  };
}

function getPushChannelPayload(contact: IContact) {
  const { channelKey, address, subscriptionStatusValue, pushValues } = contact.channels;
  return {
    [CHANNELS]: {
      [channelKey]: {
        [SUBSCRIBE_STATUS]: subscriptionStatusValue,
        [ADDRESS]: {
          [address]: {
            pushEnabled: pushValues.pushEnabled,
            token: pushValues.token,
            os: pushValues.os,
          },
        },
      },
    },
  };
}

function getSmsChannelPayload(contact: IContact) {
  const { channelKey, address, subscriptionStatusValue, smsValues } = contact.channels;
  return {
    [CHANNELS]: {
      [channelKey]: {
        [ADDRESS]: address,
        programs: {
          [smsValues.programName]: {
            keyword: smsValues.keyword,
            [SUBSCRIBE_STATUS]: subscriptionStatusValue,
            suppressDOI: smsValues.suppressDOI,
          },
        },
      },
    },
  };
}
