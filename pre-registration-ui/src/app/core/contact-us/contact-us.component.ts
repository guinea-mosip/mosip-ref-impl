import { HttpClient } from '@angular/common/http';
import {Component, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { MatButtonToggleChange } from '@angular/material';
import {MatDialog} from "@angular/material/dialog";
import {MatIcon} from "@angular/material/icon";
import {MatInput} from "@angular/material/input";
import {el} from "@angular/platform-browser/testing/src/browser_util";
import {DialogData} from "../../shared/dialoug/dialoug.component";
import {ConfirmationDialogComponent} from "./confirmation-dialog/confirmation-dialog.component";
import { ContactUs, ContactUsFormControlModal } from './contact-us';
import {ContactUsService} from "./contact-us.service";
declare var grecaptcha: any;



@Component({
  selector: 'app-contact-us',
  templateUrl: './contact-us.component.html',
  styleUrls: ['./contact-us.component.css']
})
export class ContactUsComponent implements OnInit {

  BASE_URL = "https://guinea-sandbox.mosip.net/pre-registration-contactus";
  reasons: object[] = ContactUs.Reasons;
  userForm: FormGroup;
  displayOtherReason: boolean = false;
  formControlValues: ContactUsFormControlModal;
  formControlNames: ContactUsFormControlModal = {
    name: 'name',
    email: 'email',
    reason: 'reason',
    otherReason: 'otherReason',
    objet: 'object',
    message: 'message',
    captcha: ''
  };

  invalidLogin: boolean = false;
  captchaError = '';
  loginResponse: string;
  captchaValidated = false;
  dialogType: DialogData = {case: 0};
  otherReasonError = false;

  constructor(
      private httpClient: HttpClient,
      private contactUsService: ContactUsService,
      public dialog: MatDialog
  ) { }

  ngOnInit() {
    this.setFormControlValues();
    this.userForm = new FormGroup({
      [this.formControlNames.name]: new FormControl(this.formControlValues.name.trim(), [
        Validators.required
      ]),
      [this.formControlNames.email]: new FormControl(this.formControlValues.email, [
        Validators.pattern(/^[\w-\+]+(\.[\w]+)*@[\w-]+(\.[\w]+)*(\.[a-zA-Z]{2,})$/),
        Validators.required
      ]),
      [this.formControlNames.reason]: new FormControl(this.formControlValues.reason.trim(), [
        Validators.required
      ]),
      [this.formControlNames.otherReason]: new FormControl(this.formControlValues.otherReason.trim(), []),
      [this.formControlNames.message]: new FormControl(this.formControlValues.message.trim(), [
        Validators.required
      ]),
      [this.formControlNames.captcha]: new FormControl(this.formControlValues.captcha, [])
    });
  }

  openDialog(dialogType: DialogData): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      width: '310px',
      disableClose: true,
      data: dialogType
    });

    dialogRef.afterClosed().subscribe(result => {
      this.userForm.reset();
    });
  }

  setFormControlValues() {
    this.formControlValues = {
      name: '',
      email: '',
      message: '',
      otherReason: '',
      objet: '',
      reason: '',
      captcha: ''
    };
  }

  onSubmit() {
    this.markFormGroupTouched(this.userForm);
    if (this.userForm.valid) {
      if (this.userForm.getRawValue().reason.toLocaleLowerCase() == "autre") {
        if (this.userForm.getRawValue().otherReason.length == 0) {
          this.otherReasonError = true;
        }
        else {
          this.sendValidatedForm();
        }
      }
      else {
        this.sendValidatedForm();
      }
    }
    else {
      if (this.userForm.getRawValue().reason.toLocaleLowerCase() == "autre") {
        if (this.userForm.getRawValue().otherReason.length == 0) {
          this.otherReasonError = true;
        }
      }
    }
  }


  onReasonChange(entity: any, event?: MatButtonToggleChange) {
    this.displayOtherReason = "AUTRES" === event.value.toString().toLocaleUpperCase();
  }

  private markFormGroupTouched(formGroup: FormGroup) {
    (<any>Object).values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if (control.controls) {
        this.markFormGroupTouched(control);
      }
    });
  }


  resolved(token: any) {
    console.log(token);
    this.contactUsService.verifyMyCaptcha(token).subscribe(response => {
      this.captchaValidated = true;
      console.log(response);
    }, error => {
      console.log(error);
      this.captchaError = 'invalid';
    });
  }

  onBlurOtherReason() {
    if (this.userForm.getRawValue().otherReason.length == 0) {
      this.otherReasonError = true;
    }
  }

  private sendValidatedForm() {
    this.contactUsService.sendForm(this.userForm.getRawValue()).subscribe(
        response => {
          const r = response;

          this.dialogType.case = 1;

          this.openDialog(this.dialogType);
        },
        error => {
          console.log(error);
          this.invalidLogin = true;
          this.dialogType.case = 0;

          this.openDialog(this.dialogType);
        });
    grecaptcha.reset();
    this.captchaValidated = false;
  }

  onWrittingReason() {
    this.otherReasonError = this.userForm.getRawValue().otherReason.length == 0;
  }
}
