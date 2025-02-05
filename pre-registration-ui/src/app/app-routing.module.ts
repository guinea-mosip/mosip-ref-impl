import { NgModule } from '@angular/core';
import { Routes, RouterModule, PreloadAllModules } from '@angular/router';
import { FaqComponent } from './core/faq/faq.component';
import { AboutUsComponent } from './core/about-us/about-us.component';
import { ContactUsComponent } from './core/contact-us/contact-us.component';
import { ParentComponent } from './shared/parent/parent.component';
import { AuthGuardService } from './auth/auth-guard.service';

/**
 * @description These are the routes.
 */
const appRoutes: Routes = [
  { path: '', loadChildren: './feature/dashboard/dashboard.module#DashboardModule' },
  { path: 'about-us', component: AboutUsComponent },
  { path: 'faq', component: FaqComponent },
  { path: 'contact-us', component: ContactUsComponent },

  {
    path: 'pre-registration',
    component: ParentComponent,
    canActivate: [AuthGuardService],
    children: [
      { path: '', pathMatch: 'full', redirectTo: '/' },
      { path: 'demographic', loadChildren: './feature/demographic/demographic.module#DemographicModule' },
      { path: 'file-upload', loadChildren: './feature/file-upload/file-upload.module#FileUploadModule' },
      { path: 'summary', loadChildren: './feature/summary/summary.module#SummaryModule' },
      { path: 'booking', loadChildren: './feature/booking/booking.module#BookingModule' }      
    ]
  }
];

/**
 * @author Shashank Agrawal
 *
 * @export
 * @class AppRoutingModule
 */
@NgModule({
  imports: [RouterModule.forRoot(appRoutes, { useHash: true, preloadingStrategy: PreloadAllModules })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
