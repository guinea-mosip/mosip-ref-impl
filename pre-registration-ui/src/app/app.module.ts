import {HttpClientModule} from '@angular/common/http';
import {APP_INITIALIZER, NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgxPrintModule} from 'ngx-print';
import {ConfigService} from 'src/app/core/services/config.service';
import {AppConfigService} from './app-config.service';

import {AppRoutingModule} from './app-routing.module';

import {AppComponent} from './app.component';
import {AuthModule} from './auth/auth.module';
import {CoreModule} from './core/core.module';
import {BookingService} from './feature/booking/booking.service';
import {SharedModule} from './shared/shared.module';

const appInitialization = (appConfig: AppConfigService) => {
  return () => {
    return appConfig.loadAppConfig();
  };
};

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    HttpClientModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    CoreModule,
    AuthModule,
    SharedModule,
    NgxPrintModule,
  ],
  providers: [
    BookingService,
    AppConfigService,
    {
      provide: APP_INITIALIZER,
      useFactory: appInitialization,
      multi: true,
      deps: [AppConfigService]
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
  config: ConfigService;
}
