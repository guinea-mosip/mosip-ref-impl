import { Component, OnInit, OnDestroy } from '@angular/core';

import { Router } from '@angular/router';
import { MatDialog, MatCheckboxChange } from '@angular/material';

import { TranslateService } from '@ngx-translate/core';
import { DataStorageService } from 'src/app/core/services/data-storage.service';
import { RegistrationService } from 'src/app/core/services/registration.service';
import { BookingService } from '../../booking/booking.service';
import { AutoLogoutService } from 'src/app/core/services/auto-logout.service';

import { DialougComponent } from 'src/app/shared/dialoug/dialoug.component';

import { FileModel } from 'src/app/shared/models/demographic-model/file.model';
import { Applicant } from 'src/app/shared/models/dashboard-model/dashboard.modal';
import { UserModel } from 'src/app/shared/models/demographic-model/user.modal';
import * as appConstants from '../../../app.constants';
import Utils from 'src/app/app.util';
import { ConfigService } from 'src/app/core/services/config.service';
import { RequestModel } from 'src/app/shared/models/request-model/RequestModel';
import { FilesModel } from 'src/app/shared/models/demographic-model/files.model';
import { LogService } from 'src/app/shared/logger/log.service';
import LanguageFactory from 'src/assets/i18n';
import { Subscription } from 'rxjs';
import {AuthService} from "../../../auth/auth.service";
/**
 * @description This is the dashbaord component which displays all the users linked to the login id
 *              and provide functionality like modifying the information, viewing the acknowledgement
 *              and modifying or booking an appointment.
 *
 * @export
 * @class DashBoardComponent
 * @implements {OnInit}
 * @implements {OnDestroy}
 */
@Component({
  selector: 'app-registration',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashBoardComponent implements OnInit, OnDestroy {
  userFile: FileModel[] = [];
  file: FileModel = new FileModel();
  userFiles: FilesModel = new FilesModel(this.userFile);
  message = {};

  primaryLangCode = localStorage.getItem('langCode');
  secondaryLangCode = localStorage.getItem('secondaryLangCode');
  textDir = localStorage.getItem('dir');
  secondaryLanguagelabels: any;
  errorLanguagelabels: any;
  disableModifyDataButton = false;
  disableModifyAppointmentButton = true;
  fetchedDetails = true;
  modify = false;
  isNewApplication = false;
  isFetched = false;
  allApplicants: any[];
  users: Applicant[] = [];
  selectedUsers: Applicant[] = [];
  titleOnError = '';
  subscriptions: Subscription[] = [];

  /**
   * @description Creates an instance of DashBoardComponent.
   * @param {Router} router
   * @param {MatDialog} dialog
   * @param {DataStorageService} dataStorageService
   * @param {RegistrationService} regService
   * @param {BookingService} bookingService
   * @param {AutoLogoutService} autoLogout
   * @param {TranslateService} translate
   * @param {ConfigService} configService
   * @memberof DashBoardComponent
   */
  constructor(
    private router: Router,
    public dialog: MatDialog,
    private authService: AuthService,
    private dataService: DataStorageService,
    private dataStorageService: DataStorageService,
    private regService: RegistrationService,
    private bookingService: BookingService,
    private autoLogout: AutoLogoutService,
    private translate: TranslateService,
    private configService: ConfigService,
    private loggerService: LogService // private errorService: ErrorService
  ) {
    this.translate.use(this.primaryLangCode);
    localStorage.setItem('modifyDocument', 'false');
  }

  /**
   * @description Lifecycle hook ngOnInit
   *
   * @memberof DashBoardComponent
   */
  async ngOnInit() {
    await this.getConfig();
    const authenticated = await this.authService.getLogin();
    if (!authenticated){
      await this.router.navigate(['/login']);
    } else {
      this.initUsers();
      await this.getDynamicFields();
      /* Removed for 1.1.5 compatibility*/
      // await this.getGenderDetails();
      // await this.getResidentDetails();
      const subs = this.autoLogout.currentMessageAutoLogout.subscribe(message => (this.message = message));
      this.subscriptions.push(subs);
      if (!this.message['timerFired']) {
        this.autoLogout.getValues(this.primaryLangCode);
        this.autoLogout.setValues();
        this.autoLogout.keepWatching();
      } else {
        this.autoLogout.getValues(this.primaryLangCode);
        this.autoLogout.continueWatching();
      }
      let factory = new LanguageFactory(this.primaryLangCode);
      let response = factory.getCurrentlanguage();
      this.secondaryLanguagelabels = response['dashboard'].discard;
      this.regService.setSameAs('');
    }
  }

  async getConfig(){
    return new Promise(resolve => {
      this.dataService.getConfig().subscribe(
        response => {
          this.configService.setConfig(response);
          resolve();
        },
        error => {
          this.loggerService.error('dashboard', error);
          this.onError();
          resolve();
        });
    });
  }

  /**
   * @description This will get the dynamic fields from the master data.
   *
   * @private
   * @returns
   * @memberof DemographicComponent
   */
  private getDynamicFields() {
    return new Promise(resolve => {
      this.subscriptions.push(
          this.dataStorageService.getDynamicFields().subscribe(
              response => {
                if (response[appConstants.RESPONSE]) {
                  let that = this;
                  response[appConstants.RESPONSE]["data"].map(function(elem){
                    switch(elem.name){
                      case "gender":
                        that.regService.setGenderTypes(elem.fieldVal);
                        break;
                      case "residenceStatus":
                        that.regService.setIndividualTypes(elem.fieldVal);
                        break;
                    }
                  });
                  resolve(true);
                } else {
                  this.onError(new Error("not able to find response"));
                }
              },
              error => {
                this.loggerService.error('Unable to fetch dynamic fields');
                this.onError(error);
              }
          )
      );
    });
  }

  /* Removed for 1.1.5 compatibility*/
  // private getGenderDetails() {
  //   return new Promise(resolve => {
  //     this.subscriptions.push(
  //       this.dataStorageService.getGenderDetails().subscribe(
  //         response => {
  //           if (response[appConstants.RESPONSE]) {
  //             this.regService.setGenderTypes(response[appConstants.RESPONSE][appConstants.DEMOGRAPHIC_RESPONSE_KEYS.genderTypes]);
  //             resolve(true);
  //           } else {
  //             this.onError(new Error("not able to find response"));
  //             resolve(true);
  //           }
  //         },
  //         error => {
  //           this.loggerService.error('Unable to fetch gender');
  //           this.onError(error);
  //         }
  //       )
  //     );
  //   });
  // }

  /**
   * @description This will get the residenceStatus details from the master data.
   *
   * @private
   * @returns
   * @memberof DemographicComponent
   */
  /* Removed for 1.1.5 compatibility*/
  // private getResidentDetails() {
  //   return new Promise(resolve => {
  //     this.subscriptions.push(
  //       this.dataStorageService.getResidentDetails().subscribe(
  //         response => {
  //           if (response[appConstants.RESPONSE]) {
  //             this.regService.setIndividualTypes(response[appConstants.RESPONSE][appConstants.DEMOGRAPHIC_RESPONSE_KEYS.residentTypes]);
  //             resolve(true);
  //           } else {
  //             this.onError(new Error("not able to find response"));
  //             resolve(true);
  //           }
  //         },
  //         error => {
  //           this.loggerService.error('Unable to fetch Resident types');
  //           this.onError(error);
  //         }
  //       )
  //     );
  //   });
  // }

  /**
   * @description This is the intial set up for the dashboard component
   *
   * @memberof DashBoardComponent
   */
  initUsers() {
    this.getUsers();
  }

  flushArrays() {
    this.regService.flushUsers();
    this.bookingService.flushNameList();
  }

  /**
   * @description This is to get all the users assosiated to the login id.
   *
   * @memberof DashBoardComponent
   */
  getUsers() {
    const sub = this.dataStorageService.getUsers().subscribe(
      (applicants: any) => {
        this.loggerService.info('applicants in dashboard', applicants);
        if (
          applicants[appConstants.NESTED_ERROR] &&
          applicants[appConstants.NESTED_ERROR][0][appConstants.ERROR_CODE] ===
            appConstants.ERROR_CODES.noApplicantEnrolled
        )
        {
          localStorage.setItem('newApplicant', 'true');
          this.onNewApplication();
          return;
        }

        if (applicants[appConstants.RESPONSE] && applicants[appConstants.RESPONSE] !== null) {
          localStorage.setItem('newApplicant', 'false');

          this.allApplicants =
            applicants[appConstants.RESPONSE][appConstants.DASHBOARD_RESPONSE_KEYS.applicant.basicDetails];
          this.bookingService.addApplicants(
            applicants[appConstants.RESPONSE][appConstants.DASHBOARD_RESPONSE_KEYS.applicant.basicDetails]
          );
          for (
            let index = 0;
            index <
            applicants[appConstants.RESPONSE][appConstants.DASHBOARD_RESPONSE_KEYS.applicant.basicDetails].length;
            index++
          ) {
            const applicant = this.createApplicant(applicants, index);
            this.users.push(applicant);
          }
        } else {
          this.onError();
        }
      },
      error => {
        this.loggerService.error('dashboard', error);
        this.onError();
        this.isFetched = true;
      },
      () => {
        this.isFetched = true;
      }
    );
    this.subscriptions.push(sub);
  }

  /**
   * @description This method return the appointment date and time.
   *
   * @private
   * @param {*} applicant
   * @returns the appointment date and time
   * @memberof DashBoardComponent
   */
  private createAppointmentDateTime(applicant: any) {
    const bookingRegistrationDTO = applicant[appConstants.DASHBOARD_RESPONSE_KEYS.bookingRegistrationDTO.dto];
    const date = bookingRegistrationDTO[appConstants.DASHBOARD_RESPONSE_KEYS.bookingRegistrationDTO.regDate];
    const fromTime = bookingRegistrationDTO[appConstants.DASHBOARD_RESPONSE_KEYS.bookingRegistrationDTO.time_slot_from];
    const toTime = bookingRegistrationDTO[appConstants.DASHBOARD_RESPONSE_KEYS.bookingRegistrationDTO.time_slot_to];
    let appointmentDateTime = date + ' ( ' + fromTime + ' - ' + toTime + ' )';
    return appointmentDateTime;
  }

  /**
   * @description This method return the appointment date.
   *
   * @private
   * @param {*} applicant
   * @returns the appointment date
   * @memberof DashBoardComponent
   */
  private createAppointmentDate(applicant: any) {
    const bookingRegistrationDTO = applicant[appConstants.DASHBOARD_RESPONSE_KEYS.bookingRegistrationDTO.dto];
    const date = Utils.getBookingDateTime(
      bookingRegistrationDTO[appConstants.DASHBOARD_RESPONSE_KEYS.bookingRegistrationDTO.regDate],
      '',
      this.primaryLangCode
    );
    let appointmentDate = date;
    return appointmentDate;
  }

  /**
   * @description This method return the appointment time.
   *
   * @private
   * @param {*} applicant
   * @returns the appointment time
   * @memberof DashBoardComponent
   */
  private createAppointmentTime(applicant: any) {
    const bookingRegistrationDTO = applicant[appConstants.DASHBOARD_RESPONSE_KEYS.bookingRegistrationDTO.dto];
    const fromTime = bookingRegistrationDTO[appConstants.DASHBOARD_RESPONSE_KEYS.bookingRegistrationDTO.time_slot_from];
    const toTime = bookingRegistrationDTO[appConstants.DASHBOARD_RESPONSE_KEYS.bookingRegistrationDTO.time_slot_to];
    let appointmentTime = fromTime + ' - ' + toTime ;
    return appointmentTime;
  }

  /**
   * @description This method parse the applicants and return the individual applicant.
   *
   * @param {*} applicants
   * @param {number} index
   * @returns
   * @memberof DashBoardComponent
   */
  createApplicant(applicants: any, index: number) {
    const applicantResponse =
      applicants[appConstants.RESPONSE][appConstants.DASHBOARD_RESPONSE_KEYS.applicant.basicDetails][index];
    const demographicMetadata = applicantResponse[appConstants.DASHBOARD_RESPONSE_KEYS.applicant.demographicMetadata];

    let primaryIndex = 0;
    // let secondaryIndex = 1;
    let lang =
      applicantResponse['demographicMetadata'][appConstants.DASHBOARD_RESPONSE_KEYS.applicant.firstName][0]['language'];
    let locationCode = null;
    let locationData = applicantResponse['demographicMetadata'][appConstants.DASHBOARD_RESPONSE_KEYS.applicant.location];
    if (locationData.constructor === "test".constructor) {
      try {
        let obj = JSON.parse(locationData);
        if(obj[0]){
          locationCode = obj[0].value;
        }
      } catch(e) {
        console.error(e);
      }
    }
    const applicant: Applicant = {
      applicationID: applicantResponse[appConstants.DASHBOARD_RESPONSE_KEYS.applicant.preId],
      name:
        applicantResponse['demographicMetadata'][appConstants.DASHBOARD_RESPONSE_KEYS.applicant.firstName][primaryIndex][
          'value'
        ]+" "+applicantResponse['demographicMetadata'][appConstants.DASHBOARD_RESPONSE_KEYS.applicant.lastName][primaryIndex][
          'value'
        ],
      firstName: applicantResponse['demographicMetadata'][appConstants.DASHBOARD_RESPONSE_KEYS.applicant.firstName][primaryIndex][
        'value'
      ],
      lastName: applicantResponse['demographicMetadata'][appConstants.DASHBOARD_RESPONSE_KEYS.applicant.lastName][primaryIndex][
        'value'
      ],
      appointmentDateTime: applicantResponse[appConstants.DASHBOARD_RESPONSE_KEYS.bookingRegistrationDTO.dto]
        ? this.createAppointmentDateTime(applicantResponse)
        : '-',
      appointmentDate: applicantResponse[appConstants.DASHBOARD_RESPONSE_KEYS.bookingRegistrationDTO.dto]
        ? this.createAppointmentDate(applicantResponse)
        : '-',
      appointmentTime: applicantResponse[appConstants.DASHBOARD_RESPONSE_KEYS.bookingRegistrationDTO.dto]
        ? this.createAppointmentTime(applicantResponse)
        : '-',
      status: applicantResponse[appConstants.DASHBOARD_RESPONSE_KEYS.applicant.statusCode],
      regDto: applicantResponse[appConstants.DASHBOARD_RESPONSE_KEYS.bookingRegistrationDTO.dto],
      nameInSecondaryLanguage:"",
      location: locationCode
    };

    return applicant;
  }

  /**
   * @description This method navigate the user to demographic page if it is a new applicant.
   *
   * @memberof DashBoardComponent
   */
  onNewApplication() {
    this.flushArrays();
    this.regService.changeMessage({ modifyUser: 'false' });
    this.router.navigate(['pre-registration', 'demographic']);
    this.isNewApplication = true;
  }

  openDialog(data, width) {
    const dialogRef = this.dialog.open(DialougComponent, {
      width: width,
      data: data
    });
    return dialogRef;
  }

  radioButtonsStatus(status: string) {
    let data = {};
    if (status.toLowerCase() === 'booked') {
      data = {
        case: 'DISCARD',
        disabled: {
          radioButton1: false,
          radioButton2: false
        }
      };
    } else {
      data = {
        case: 'DISCARD',
        disabled: {
          radioButton1: false,
          radioButton2: true
        }
      };
    }
    return data;
  }

  confirmationDialog(selectedOption: number) {
    let body = {};
    if (Number(selectedOption) === 1) {
      body = {
        case: 'CONFIRMATION',
        title: this.secondaryLanguagelabels.title_confirm,
        message: this.secondaryLanguagelabels.deletePreregistration.msg_confirm,
        yesButtonText: this.secondaryLanguagelabels.button_confirm,
        noButtonText: this.secondaryLanguagelabels.button_cancel
      };
    } else {
      body = {
        case: 'CONFIRMATION',
        title: this.secondaryLanguagelabels.title_confirm,
        message: this.secondaryLanguagelabels.cancelAppointment.msg_confirm,
        yesButtonText: this.secondaryLanguagelabels.button_confirm,
        noButtonText: this.secondaryLanguagelabels.button_cancel
      };
    }
    const dialogRef = this.openDialog(body, '250px');
    return dialogRef;
  }

  removeApplicant(preRegId: string) {
    let x: number = -1;
    for (let i of this.allApplicants) {
      x++;
      if (i.preRegistrationId == preRegId) {
        this.allApplicants.splice(x, 1);
        break;
      }
    }
    this.bookingService.addApplicants(this.allApplicants);
  }

  deletePreregistration(element: any) {
    const subs = this.dataStorageService.deleteRegistration(element.applicationID).subscribe(
      response => {
        if (!response['errors']) {
          this.removeApplicant(element.applicationID);
          let index = this.users.indexOf(element);
          this.users.splice(index, 1);
          index = this.selectedUsers.indexOf(element);
          this.selectedUsers.splice(index, 1);
          if (this.users.length == 0) {
            this.onNewApplication();
            localStorage.setItem('newApplicant', 'true');
          } else {
            this.displayMessage(
              this.secondaryLanguagelabels.title_success,
              this.secondaryLanguagelabels.deletePreregistration.msg_deleted
            );
          }
        } else {
          this.displayMessage(
            this.secondaryLanguagelabels.title_error,
            this.secondaryLanguagelabels.deletePreregistration.msg_could_not_deleted
          );
        }
      },
      () => {
        this.displayMessage(
          this.secondaryLanguagelabels.title_error,
          this.secondaryLanguagelabels.deletePreregistration.msg_could_not_deleted
        );
      }
    );
    this.subscriptions.push(subs);
  }

  cancelAppointment(element: any) {
    element.regDto.pre_registration_id = element.applicationID;
    const subs = this.dataStorageService
      .cancelAppointment(new RequestModel(appConstants.IDS.booking, element.regDto), element.applicationID)
      .subscribe(
        response => {
          if (!response['errors']) {
            this.displayMessage(
              this.secondaryLanguagelabels.title_success,
              this.secondaryLanguagelabels.cancelAppointment.msg_deleted
            );
            const index = this.users.indexOf(element);
            this.users[index].status = appConstants.APPLICATION_STATUS_CODES.pending;
            this.users[index].appointmentDate = '-';
            this.users[index].appointmentTime = '';
          } else {
            this.displayMessage(
              this.secondaryLanguagelabels.title_error,
              this.secondaryLanguagelabels.cancelAppointment.msg_could_not_deleted
            );
          }
        },
        () => {
          this.displayMessage(
            this.secondaryLanguagelabels.title_error,
            this.secondaryLanguagelabels.cancelAppointment.msg_could_not_deleted
          );
        }
      );
    this.subscriptions.push(subs);
  }

  onDelete(element) {
    let data = this.radioButtonsStatus(element.status);
    let dialogRef = this.openDialog(data, `460px`);
    const subs = dialogRef.afterClosed().subscribe(selectedOption => {
      if (selectedOption && Number(selectedOption) === 1) {
        dialogRef = this.confirmationDialog(selectedOption);
        dialogRef.afterClosed().subscribe(confirm => {
          if (confirm) {
            this.deletePreregistration(element);
          }
        });
      } else if (selectedOption && Number(selectedOption) === 2) {
        dialogRef = this.confirmationDialog(selectedOption);
        dialogRef.afterClosed().subscribe(confirm => {
          if (confirm) {
            this.cancelAppointment(element);
          }
        });
      }
    });
    this.subscriptions.push(subs);
  }

  displayMessage(title: string, message: string) {
    const messageObj = {
      case: 'MESSAGE',
      title: title,
      message: message
    };
    this.openDialog(messageObj, '250px');
  }

  /**
   * @description This method navigate the user to demographic page to modify the existence data.
   *
   * @param {Applicant} user
   * @memberof DashBoardComponent
   */
  onModifyInformation(user: Applicant) {
    this.flushArrays();
    const preId = user.applicationID;
    this.regService.changeMessage({ modifyUser: 'true' });
    this.disableModifyDataButton = true;
    const subs = this.dataStorageService.getUserDocuments(preId).subscribe(
      response => this.setUserFiles(response),
      error => {
        this.loggerService.error('dashboard error', error);
        this.disableModifyDataButton = false;
        this.onError(error);
      },
      () => {
        this.addtoNameList(user);
        this.dataStorageService.getUser(preId).subscribe(
          response => {
            this.onModification(response, preId);
          },
          error => {
            this.onError(error);
          }
        );
      }
    );
    this.subscriptions.push(subs);
  }

  /**
   * @description This method navigate the user to demmographic page on selection of modification.
   *
   * @private
   * @param {*} response
   * @param {string} preId
   * @memberof DashBoardComponent
   */
  private onModification(response: any, preId: string) {
    const request = response[appConstants.RESPONSE];
    this.disableModifyDataButton = true;
    this.regService.addUser(new UserModel(preId, request, this.userFiles));
    this.fetchedDetails = true;
    this.router.navigate(['pre-registration', 'demographic']);
  }

  /**
   * @description This method is called when a check box is selected.
   *
   * @param {Applicant} user
   * @param {MatCheckboxChange} event
   * @memberof DashBoardComponent
   */
  onSelectUser(user: Applicant, event: MatCheckboxChange) {
    if (event && event.checked) {
      this.selectedUsers.push(user);
    } else {
      this.selectedUsers.splice(this.selectedUsers.indexOf(user), 1);
    }
    if (this.selectedUsers.length > 0) {
      this.disableModifyAppointmentButton = false;
    } else {
      this.disableModifyAppointmentButton = true;
    }
  }

  /**
   * @description This method navigates to center selection page to book/modify the apointment
   *
   * @memberof DashBoardComponent
   */
  onModifyMultipleAppointment() {
    this.flushArrays();
    for (let index = 0; index < this.selectedUsers.length; index++) {
      this.addtoNameList(this.selectedUsers[index]);
    }
    let url = '';
    url = Utils.getURL(this.router.url, 'pre-registration/booking/pick-center');
    this.router.navigateByUrl(url);
  }

  /**
   * @description This method is used to navigate to acknowledgement page to view the acknowledgment
   *
   * @param {Applicant} user
   * @memberof DashBoardComponent
   */
  onAcknowledgementView(user: Applicant) {
    this.flushArrays();
    this.addtoNameList(user);
    let url = '';
    url = Utils.getURL(this.router.url, 'pre-registration/summary/acknowledgement');
    this.router.navigateByUrl(url);
  }

  /**
   * @description This method add the user details to shared service name list array.
   *
   * @param {Applicant} user
   * @memberof DashBoardComponent
   */
  addtoNameList(user: Applicant) {
    const preId = user.applicationID;
    const fullName = user.name;
    const regDto = user.regDto;
    const status = user.status;
    this.bookingService.addNameList({
      fullName: fullName,
      firstName: user.firstName,
      lastName: user.lastName,
      location: user.location,
      preRegId: preId,
      regDto: regDto,
      status: status,
    });
  }

  setUserFiles(response) {
    if (!response['errors']) {
      this.userFile = response[appConstants.RESPONSE][appConstants.METADATA];
    } else {
      let fileModel: FileModel = new FileModel('', '', '', '', '', '', '');
      this.userFile.push(fileModel);
    }
    this.userFiles['documentsMetaData'] = this.userFile;
  }

  getColor(value: string) {
    if (value === appConstants.APPLICATION_STATUS_CODES.pending) return 'orange';
    if (value === appConstants.APPLICATION_STATUS_CODES.booked) return 'green';
    if (value === appConstants.APPLICATION_STATUS_CODES.expired) return 'red';
  }

  getMargin(name: string) {
    if (name.length > 25) return '0px';
    else return '27px';
  }

  isBookingAllowed(user: Applicant) {
    if (user.status !== 'Booked') return true;
    const appointmentDate = user.appointmentDateTime.split('(')[0];
    const dateform = new Date(appointmentDate);
    if (dateform.toDateString() !== 'Invalid Date') {
      const appointmentTimeInMilliseconds = this.getTimeInMilliseconds(user.appointmentTime);    
      const now = new Date();  
      let dateTimeUtcNow: string = new Date(now.getTime() + now.getTimezoneOffset() * 60000).toString();
      let diffInMilliseconds: number = (Date.parse(appointmentDate) + appointmentTimeInMilliseconds) - Date.parse(dateTimeUtcNow);
      let diffInHours: number = diffInMilliseconds / 1000 / 60 / 60;
      return diffInHours >= this.configService.getConfigByKey(appConstants.CONFIG_KEYS.preregistration_timespan_rebook);  
    }
    return false;
  }

  getTimeInMilliseconds(timeSlot: string) : number   {
    const hrs = timeSlot.split(":")[0];
    const mins = timeSlot.split(":")[1];

    return (parseInt(hrs) * 60 * 60 * 1000) + (parseInt(mins)* 60 * 1000);
  }

  /**
   * @description This will return the json object of label of demographic in the primary language.
   *
   * @private
   * @returns the `Promise`
   * @memberof DashBoardComponent
   */
  private getErrorLabels() {
    let factory = new LanguageFactory(this.primaryLangCode);
    let response = factory.getCurrentlanguage();
    this.errorLanguagelabels = response['error'];
  }

  /**
   * @description This is a dialoug box whenever an erroe comes from the server, it will appear.
   *
   * @private
   * @memberof DashBoardComponent
   */
  private async onError(error?: any) {
    await this.getErrorLabels();
    let message = this.errorLanguagelabels.error;
    this.titleOnError = this.errorLanguagelabels.errorLabel;
    if (
      error &&
      error[appConstants.ERROR] &&
      error[appConstants.ERROR][appConstants.NESTED_ERROR][0].errorCode === appConstants.ERROR_CODES.tokenExpired
    ) {
      message = this.errorLanguagelabels.tokenExpiredLogout;
      this.titleOnError = '';
    }
    if (this.errorLanguagelabels) {
      const body = {
        case: 'ERROR',
        title: this.titleOnError,
        message: message,
        yesButtonText: this.errorLanguagelabels.button_ok
      };
      this.dialog.open(DialougComponent, {
        width: '250px',
        data: body
      });
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }
}
