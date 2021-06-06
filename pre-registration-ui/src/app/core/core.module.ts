import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import {RecaptchaFormsModule, RecaptchaModule} from "ng-recaptcha";

import { AboutUsComponent } from './about-us/about-us.component';
import {ConfirmationDialogComponent} from "./contact-us/confirmation-dialog/confirmation-dialog.component";
import {ContactUsService} from "./contact-us/contact-us.service";
import { FaqComponent } from './faq/faq.component';
import { HeaderComponent } from './header/header.component';
import { FooterComponent } from './footer/footer.component';
import { ContactUsComponent } from './contact-us/contact-us.component';
import { AppRoutingModule } from '../app-routing.module';
import { SharedModule } from '../shared/shared.module';
import { AuthInterceptorService } from '../shared/auth-interceptor.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

 
@NgModule({
  imports: [
      CommonModule,
    AppRoutingModule,
    SharedModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    RecaptchaModule,  //this is the recaptcha main module
    RecaptchaFormsModule
  ],
  declarations: [
      HeaderComponent,
    FooterComponent,
    AboutUsComponent,
    FaqComponent,
    ContactUsComponent,
    ConfirmationDialogComponent
  ],
  exports: [
      HeaderComponent,
    FooterComponent,
    SharedModule
  ],
  entryComponents: [
    ConfirmationDialogComponent
  ],
  providers: [
      { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptorService, multi: true }, ContactUsService]
})
export class CoreModule {}
