/**
 * @description This is the data object defination for the applicant coming as response in the dashboard
 * @author Shashank Agrawal
 *
 * @export
 * @interface Applicant
 */
export interface Applicant {
  applicationID: string;
  name: string;
  firstName: string;
  lastName: string;
  appointmentDateTime: string;
  appointmentDate: string;
  appointmentTime: string;
  status: string;
  regDto: any;
  nameInSecondaryLanguage: string;
  location: string;
}
