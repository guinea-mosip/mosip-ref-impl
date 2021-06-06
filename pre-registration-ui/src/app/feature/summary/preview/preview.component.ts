import { Component, OnInit } from '@angular/core';
import { DataStorageService } from 'src/app/core/services/data-storage.service';
import { Router } from '@angular/router';
import { UserModel } from 'src/app/shared/models/demographic-model/user.modal';
import { RegistrationService } from 'src/app/core/services/registration.service';
import { TranslateService } from '@ngx-translate/core';
import Utils from 'src/app/app.util';
import * as appConstants from '../../../app.constants';
import LanguageFactory from 'src/assets/i18n';

@Component({
  selector: 'app-preview',
  templateUrl: './preview.component.html',
  styleUrls: ['./preview.component.css']
})
export class PreviewComponent implements OnInit {
  previewData: any;
  secondaryLanguagelabels: any;
  primaryLanguage;
  secondaryLanguage;
  dateOfBirthPrimary: string = '';
  dateOfBirthSecondary: string = '';
  user: UserModel;
  files = [];
  documentTypes = [];
  documentMapObject = [];
  sameAs = '';
  residentTypeMapping = {
    primary: {},
    secondary: {}
  };

  constructor(
    private dataStorageService: DataStorageService,
    private router: Router,
    private registrationService: RegistrationService,
    private translate: TranslateService
  ) {
    this.translate.use(localStorage.getItem('langCode'));
    localStorage.setItem('modifyDocument', 'false');
  }

  ngOnInit() {
    this.primaryLanguage = localStorage.getItem('langCode');
    this.secondaryLanguage = localStorage.getItem('secondaryLangCode');
    this.user = { ...this.registrationService.getUser(this.registrationService.getUsers().length - 1) };
    this.documentTypes = this.registrationService.getDocumentCategories();
    this.previewData = this.user.request.demographicDetails.identity;
    this.calculateAge();
    this.previewData.primaryAddress = this.previewData.additionalAddressDetails;
    this.formatDob(this.previewData.dateOfBirth);
    this.setFieldValues();
    this.getSecondaryLanguageLabels();
    this.files = this.user.files ? this.user.files.documentsMetaData : [];
    this.documentsMapping();
  }

  formatDob(dob: string) {
    dob = dob.replace(/\//g, '-');
    this.dateOfBirthPrimary = Utils.getBookingDateTime(dob, '', localStorage.getItem('langCode'));
    this.dateOfBirthSecondary = Utils.getBookingDateTime(dob, '', localStorage.getItem('secondaryLangCode'));
  }

  setFieldValues() {
    let fields = appConstants.previewFields;
    fields.forEach(field => {
      if (field === 'gender') {
        this.previewData[field].forEach(element => {
          element.name = this.getGenderCodeToName(element.value, element.language);
        });
      } else if (field === 'residenceStatus') {
        this.previewData[field].forEach(element => {
          element.name = this.getresidenceStatusCodeToName(element.value, element.language);
        });
      } else {
        this.previewData[field].forEach(element => {
          element.name = this.locCodeToName(element.value, element.language);
        });
      }

    });
  }

  getGenderCodeToName(code: string, language: string) {
    const genders = this.registrationService.getGenderTypes();
    for (let i = 0; i < genders.length; i++) {
      if (genders[i]['code'] === code) {
        return genders[i]['genderName']
      }
    }
    return '';
  }

  getresidenceStatusCodeToName(code: string, language: string) {
    const ress = this.registrationService.getIndividualTypes();
    for (let i = 0; i < ress.length; i++) {
      if (ress[i]['code'] === code) {
        return ress[i]['name']
      }
    }
    return '';
  }

  documentsMapping() {
    this.documentMapObject = [];
    if (this.files && this.documentTypes.length !== 0) {
      this.documentTypes.forEach(type => {
        const file = this.files.filter(file => file.docCatCode === type.code);
        if (type.code === 'POA' && file.length === 0 && this.registrationService.getSameAs() !== '') {
          const obj = {
            docName: this.sameAs
          };
          file.push(obj);
        }
        if (type.code === 'POE') // Filter out Proof of Exception
          return;

        const obj = {
          code: type.code,
          name: type.name,
          fileName: file.length > 0 ? file[0].docName : undefined
        };
        this.documentMapObject.push(obj);
      });
    }
  }


  getSecondaryLanguageLabels() {
    let factory = new LanguageFactory(localStorage.getItem('secondaryLangCode'));
    let response = factory.getCurrentlanguage();
    this.secondaryLanguagelabels = response['preview'];
    this.residentTypeMapping.secondary = response['residentTypesMapping'];
  }

  getPrimaryLanguageData() {
    let factory = new LanguageFactory(localStorage.getItem('langCode'));
    let response = factory.getCurrentlanguage();
    this.sameAs = response['sameAs'];
    this.residentTypeMapping.primary = response['residentTypesMapping'];
  }

  calculateAge() {
    const now = new Date();
    const born = new Date(this.previewData.dateOfBirth);
    const years = Math.floor((now.getTime() - born.getTime()) / (365.25 * 24 * 60 * 60 * 1000));
    this.previewData.age = years;
  }

  modifyDemographic() {
    const url = Utils.getURL(this.router.url, 'demographic', 2);
    this.registrationService.changeMessage({ modifyUserFromPreview: 'true' });
    this.router.navigateByUrl(url);
  }

  modifyDocument() {
    localStorage.setItem('modifyDocument', 'true');
    this.navigateBack();
  }

  private locCodeToName(locationCode: string, language: string): string {
    const locations = this.user.location;
    const locationName = locations.filter(
      location => location.languageCode === language && location.valueCode === locationCode
    );
    return locationName[0] ? locationName[0].valueName : '';
  }

  enableContinue(): boolean {
    let flag = true;
    this.documentMapObject.forEach(object => {
      if (object.fileName === undefined) {
        if (object.code === 'POA' && this.registrationService.getSameAs() !== '') {
          flag = true;
        } else {
          flag = false;
        }
      }
    });
    return flag;
  }

  navigateDashboard() {
    localStorage.setItem('newApplicant', 'false');
    this.registrationService.setSameAs('');
    this.registrationService.changeMessage({ modifyUserFromPreview: 'false' });
    const url = Utils.getURL(this.router.url, 'demographic', 2);
    setTimeout(f => {
      this.router.navigateByUrl(url);
    }, 500);
  }

  navigateBack() {
    const url = Utils.getURL(this.router.url, 'file-upload', 2);
    setTimeout(f => {
      this.router.navigateByUrl(url);
    }, 500);
  }

  navigateNext() {
    const url = Utils.getURL(this.router.url, 'booking/pick-center', 2);
    setTimeout(f => {
      this.router.navigateByUrl(url);
    }, 500);
  }
}
