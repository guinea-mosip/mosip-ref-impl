export class BookingModel {
  constructor(
    private preRegistrationId: string, 
    private registration_center_id: string,
    private appointment_date: string,
    private time_slot_from: string,
    private time_slot_to: string
  ) {}
}

export interface BookingInterface {
  id?: any;
  version?: any;
  responsetime: string;
  metadata?: any;
  response: Response;
  errors?: any;
}

export interface Response {
  registrationCenters: RegistrationCenter[];
}

export interface RegistrationCenter {
  id: string;
  name: string;
  centerTypeCode: string;
  addressLine1: string;
  addressLine2: string;
  addressLine3: string;
  latitude: string;
  longitude: string;
  locationCode: string;
  holidayLocationCode: string;
  contactPhone: string;
  workingHours: string;
  langCode: string;
  numberOfKiosks: number;
  perKioskProcessTime: string;
  centerStartTime: string;
  centerEndTime: string;
  timeZone: string;
  contactPerson: string;
  lunchStartTime: string;
  lunchEndTime: string;
  isActive: boolean;
  zoneCode: string;
  workingDays: string;
}
