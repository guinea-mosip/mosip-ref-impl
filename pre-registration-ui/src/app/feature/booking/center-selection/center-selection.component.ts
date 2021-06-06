import { Component, OnInit, OnDestroy } from '@angular/core';
import { MatDialog } from '@angular/material';
import { DialougComponent } from '../../../shared/dialoug/dialoug.component';
import { DataStorageService } from 'src/app/core/services/data-storage.service';
import {NESTED_ERROR, RESPONSE} from "./../../../app.constants";
import {BookingInterface} from "./booking.model";
import { RegistrationCentre } from './registration-center-details.model';
import { Router, ActivatedRoute } from '@angular/router';

import { UserModel } from 'src/app/shared/models/demographic-model/user.modal';
import { BookingService } from '../booking.service';
import { RegistrationService } from 'src/app/core/services/registration.service';
import { TranslateService } from '@ngx-translate/core';
import Utils from 'src/app/app.util';
import { ConfigService } from 'src/app/core/services/config.service';
import * as appConstants from './../../../app.constants';
import { BookingDeactivateGuardService } from 'src/app/shared/can-deactivate-guard/booking-guard/booking-deactivate-guard.service';
import LanguageFactory from 'src/assets/i18n';
import { Subscription } from 'rxjs';
import { NameList } from "../../../shared/models/demographic-model/name-list.modal";

@Component({
  selector: 'app-center-selection',
  templateUrl: './center-selection.component.html',
  styleUrls: ['./center-selection.component.css']
})
export class CenterSelectionComponent extends BookingDeactivateGuardService implements OnInit, OnDestroy {
  REGISTRATION_CENTRES: RegistrationCentre[] = [];
  searchClick: boolean = true;
  isWorkingDaysAvailable = false;
  canDeactivateFlag = true;
  locationTypes = [];

  locationType = null;
  searchText = null;
  showTable = false;
  selectedCentre = null;
  showMap = false;
  showMessage = false;
  enableNextButton = false;
  bookingDataList = [];
  errorlabels: any;
  step = 0;
  showDescription = false;
  mapProvider = 'OSM';
  searchTextFlag = false;
  displayMessage = 'Showing nearby registration centers';
  users: NameList[];
  subscriptions: Subscription[] = [];
  primaryLang = localStorage.getItem('langCode');
  workingDays: string;
  centerSelectedOption: string = '';
  degreeTitleList = [];

  toShow = 'autre';
  communes = [];
  sousPrefecture = [];

  constructor(
    public dialog: MatDialog,
    private service: BookingService,
    private dataService: DataStorageService,
    private router: Router,
    private route: ActivatedRoute,
    private registrationService: RegistrationService,
    private translate: TranslateService,
    private configService: ConfigService
  ) {
    super(dialog);
    this.translate.use(this.primaryLang);
  }

  ngOnInit() {
    this.REGISTRATION_CENTRES = [];
    this.selectedCentre = null;
    const subs = this.dataService.getLocationTypeData().subscribe(response => {
      const locationItems = response[appConstants.RESPONSE]['locations'];
      this.filterLocations(locationItems);
    });
    this.subscriptions.push(subs);
    this.users = this.service.getNameList();
    this.getRecommendedCenters();
    this.getErrorLabels();
    this.centerSelectedOption = 'Recommanded';
  }
  educationLevelChangeAction(education) {
    this.degreeTitleList = education.degreeTitleList;
  }
  // educationList: any = [
  //   {
  //     'critere': 'REGION',
  //     degreeTitleList: [
  //       'BOKE', 'CONAKRY', 'KANKAN', 'KINDIA', 'LABE', 'MAMOU', 'NZEREKORE'
  //     ]
  //   },
  //   {
  //     'critere': 'PREFECTURE',
  //     degreeTitleList: [
  //       'BOKE', 'COYAH', 'DIXINN', 'DUBREKA', 'FORECARIAH', 'KALOUM', 'KANKAN', 'KOUROUSSA', 'LABE', 'MAMOU', 'NZEREKORE', 'SIGUIRI', 'TELIMELE'
  //     ]
  //   },
  //   {
  //     'critere': 'SOUS-PREFECTURE',
  //     degreeTitleList: [
  //       'CU-BOKE', 'CU-COYAH', 'DIXINN', 'CU-DUBREKA', 'CU-FORECARIAH', 'KALOUM', 'CU-KANKAN', 'CU-KOUROUSSA', 'CU-LABE', 'CU-MAMOU', 'CU-NZEREKORE', 'CU-SIGUIRI', 'CU-TELIMELE'
  //     ]
  //   },
  // {
  //     'critere': 'COMMMUNE',
  //     degreeTitleList: [
  //       'CU-BOKE', 'CU-COYAH', 'DIXINN', 'CU-DUBREKA', 'CU-FORECARIAH', 'KALOUM', 'CU-KANKAN', 'CU-KOUROUSSA', 'CU-LABE', 'CU-MAMOU', 'CU-NZEREKORE', 'CU-SIGUIRI', 'CU-TELIMELE'
  //     ]
  //   }
  // ];

  // Hack: for displaying SOUS-Prefecture and COMMUNE separately and retrieving PAYS option
  filterLocations(locationItems: any) {
    locationItems.forEach((locationType) => {

      if (locationType.locationHierarchylevel === 0  // 0 => PAYS
        || locationType.locationHierarchylevel === 4 // 4 => DISTRICT
        || locationType.locationHierarchylevel === 5) // 5 => SECTEUR
        return;

      if (locationType.locationHierarchylevel === 3) // 3 => SOUS_PREFECTURE_OU_COMMUNE
        locationType.locationHierarchyDescription = 'SOUS-PREFECTURE/COMMUNE';
      else
        locationType.locationHierarchyDescription = locationType.locationHierarchyName;

      this.locationTypes.push(locationType);
    });

    this.locationTypes.sort((a, b) => (a.locationHierarchylevel > b.locationHierarchylevel) ? 1 : ((b.locationHierarchylevel > a.locationHierarchylevel) ? -1 : 0));
  }

  getErrorLabels() {
    let factory = new LanguageFactory(this.primaryLang);
    let response = factory.getCurrentlanguage();
    this.errorlabels = response['error'];
  }

  getRecommendedCenters() {
    this.searchClick = true;
    let locations = [];
    let locationNames = [];
    this.users.forEach((user) => {
      locations.push(user.location);
    });
    this.getPrefectures().then((res) => {
      if (res && Array.isArray(res)) {
        locationNames = res.reduce(function (acc, v) {
          if (locations.indexOf(v.code) !== -1) {
            acc.push(v.name);
          }
          return acc
        }, []);

        this.subscriptions.push(
          this.dataService
            /* leave it commented */
            .recommendedCenters(
              this.primaryLang,
              this.configService.getConfigByKey(
                appConstants.CONFIG_KEYS.preregistration_recommended_centers_locCode
              ),
              locationNames
            )
            // .getCenter()
            .subscribe((response) => {
              if (response[appConstants.RESPONSE]) {
                console.log(response["response"]);
                this.displayResults(response["response"]);
              } else {
                if (response["errors"] && response["errors"].length > 0) {
                  let errStr = response["errors"].reduce(function (c, e) {
                    return c + "\n" + e.message
                  }, "");
                  console.error(errStr);
                } else {
                  alert("error occured while getting recommended centers")
                }
              }
            })
        );
      }
      else {
        alert("No prefectures found")
      }
    });
  }

  private getPrefectures() {
    return new Promise((resolve, reject) => {
      this.subscriptions.push(
        this.dataService.getLocationByHiererchy(
          "prefecture"
        ).subscribe(
          response => {
            if (response[appConstants.RESPONSE]) {
              resolve(response[appConstants.RESPONSE][appConstants.DEMOGRAPHIC_RESPONSE_KEYS.locations]);
            } else {
              if (response["errors"] && response["errors"].length > 0) {
                let errStr = response["errors"].reduce(function (c, e) {
                  return c + "\n" + e.message
                }, "");
                alert(errStr)
              } else {
                alert("error occured while getting recommended centers")
              }
              reject()
            }
          },
          error => {
            reject();
          }
        )
      );
    });
  }

  setSearchClick(flag: boolean) {
    this.searchClick = flag;
    this.setCenters();
  }

  setCenters() {
    this.dataService.getCenterByLocattionType(this.locationType.locationHierarchylevel).subscribe(ret => {
      const dtl = ret[this.locationType.locationHierarchylevel];
      this.degreeTitleList = dtl.degreeTitleList;
    })

    console.log(this.degreeTitleList === this.communes);
  }

  onSubmit() {
    this.searchTextFlag = true;
    if (this.searchText.length !== 0 || this.searchText !== null) {
      this.displayMessage = `Searching results for ${this.searchText} ....`;
    } else {
      this.displayMessage = '';
    }
  }
  setStep(index: number) {
    this.step = index;
  }

  nextStep() {
    this.step++;
    this.showDescription = true;
  }

  prevStep() {
    this.step--;
  }

  showResults() {
    this.REGISTRATION_CENTRES = [];
    if (this.locationType !== null && this.searchText !== null) {
      this.showMap = false;
      const subs = this.dataService
        .getRegistrationCentersByName(this.locationType.locationHierarchylevel, this.searchText)
        .subscribe(
          response => {
            if (response[appConstants.RESPONSE]) {
              this.displayResults(response[appConstants.RESPONSE]);
            } else {
              this.showMessage = true;
              this.selectedCentre = null;
            }
          },
          error => {
            this.showMessage = true;
            this.displayMessageError('Error', this.errorlabels.error, error);
          }
        );
      this.subscriptions.push(subs);
    }
  }

  plotOnMap() {
    this.showMap = true;
    this.service.changeCoordinates([Number(this.selectedCentre.longitude), Number(this.selectedCentre.latitude)]);
  }

  selectedRow(row) {
    this.selectedCentre = row;
    this.enableNextButton = true;

    if (Object.keys(this.selectedCentre).length !== 0) {
      this.plotOnMap();
    }
  }
  pos:number;

  getLocation() {
    this.searchClick = true;
    this.REGISTRATION_CENTRES = [];
    if (navigator.geolocation) {
      this.showMap = false;
      navigator.geolocation.getCurrentPosition(position => {
        console.log(position);
        const subs = this.dataService.getNearbyRegistrationCenters(position.coords).subscribe(
            (resp: BookingInterface) => {

            if (
                resp.errors === null &&
                resp.response.registrationCenters.length !== 0
            ) {
              console.log("Inside condition", resp);
              this.displayResults(resp.response);
            } else {
              this.showMessage = true;
            }
          },
          error => {
            console.log(error);
            this.showMessage = true;
            this.displayMessageError('Error', this.errorlabels.error, error);
          }
        );
        this.subscriptions.push(subs);
      });
    } else {
    }
  }

  changeTimeFormat(time: string): string | Number {
    let inputTime = time.split(':');
    let formattedTime: any;
    if (Number(inputTime[0]) < 12) {
      formattedTime = inputTime[0];
      formattedTime += ':' + inputTime[1] + ' am';
    } else {
      formattedTime = Number(inputTime[0]) - 12;
      formattedTime += ':' + inputTime[1] + ' pm';
    }

    return formattedTime;
  }

  dispatchCenterCoordinatesList() {
    const coords = [];
    this.REGISTRATION_CENTRES.forEach(centre => {
      const data = {
        id: centre.id,
        latitude: Number(centre.latitude),
        longitude: Number(centre.longitude)
      };
      coords.push(data);
    });

    this.service.listOfCenters(coords);
  }

  routeNext() {
    this.registrationService.setRegCenterId(this.selectedCentre.id);
    this.users.forEach(user => {
      this.service.updateRegistrationCenterData(user.preRegId, this.selectedCentre);
    });
    this.canDeactivateFlag = false;
    setTimeout(f => {
      this.router.navigate(['../pick-time'], { relativeTo: this.route });
    }, 500);
  }

  routeDashboard() {
    this.canDeactivateFlag = false;
    const url = Utils.getURL(this.router.url, '', 3);
    setTimeout(f => {
      this.router.navigateByUrl(url);
    }, 500)
  }

  routeBack() {
    let url = '';
    if (this.registrationService.getUsers().length === 0) {
      url = Utils.getURL(this.router.url, '', 3);
    } else {
      url = Utils.getURL(this.router.url, 'summary/preview', 2);
    }
    this.canDeactivateFlag = false;
    setTimeout(f => {
      this.router.navigateByUrl(url);
    }, 500)
  }

  async displayResults(response: any) {
    this.REGISTRATION_CENTRES = response['registrationCenters'];
    console.log(this.REGISTRATION_CENTRES);
    await this.getWorkingDays();
    this.showTable = true;
    if (this.REGISTRATION_CENTRES) {
      this.selectedRow(this.REGISTRATION_CENTRES[0]);
      this.dispatchCenterCoordinatesList();
    }
  }

  getWorkingDays() {
    return new Promise(resolve => {
      this.REGISTRATION_CENTRES.forEach(center => {
        this.dataService.getWorkingDays(center.id, this.primaryLang).subscribe(response => {
          response[appConstants.RESPONSE]['workingdays'].forEach(day => {
            center.workingDays = center.workingDays === undefined ? '' : center.workingDays + day.name + ', ';
          });
          center.workingDays = center.workingDays.substring(0, center.workingDays.length - 2);
          this.isWorkingDaysAvailable = true;
          resolve(true);
        });
      });
    });
  }

  displayMessageError(title: string, message: string, error: any) {
    if (
      error &&
      error[appConstants.ERROR] &&
      error[appConstants.ERROR][appConstants.NESTED_ERROR][0].errorCode === appConstants.ERROR_CODES.tokenExpired
    ) {
      message = this.errorlabels.tokenExpiredLogout;
      title = '';
    }
    const messageObj = {
      case: 'MESSAGE',
      title: title,
      message: message
    };
    this.openDialog(messageObj, '250px');
  }
  openDialog(data, width) {
    const dialogRef = this.dialog.open(DialougComponent, {
      width: width,
      data: data
    });
    return dialogRef;
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  //This function takes in latitude and longitude of two location and returns the distance between them as the crow flies (in km)
  calcCrow(lat1P, lon1P, lat2P, lon2P) {
    let R = 6371; // km
    let dLat = this.toRad(lat2P-lat1P);
    let dLon = this.toRad(lon2P-lon1P);
    let lat1 = this.toRad(lat1P);
    let lat2 = this.toRad(lat2P);

    let a = Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
    let c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    let d = R * c;
    return d;
  }

  // Converts numeric degrees to radians
  toRad(Value)
  {
    return Value * Math.PI / 180;
  }
}
