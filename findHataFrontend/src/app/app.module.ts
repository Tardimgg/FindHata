import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { ShortInfoProposalComponent } from "./components/proposal/short-info-proposal.component";
import { SearchComponent } from "./components/search/search.component";
import { HomeComponent } from "./components/home/home.component";

import { SwiperModule } from 'swiper/angular';
import { RouterModule, Routes } from "@angular/router";
import { ProposalDetailComponent } from './components/proposal-detail/proposal-detail.component';
import { ChatComponent } from './components/chat/chat.component';
import { EntranceComponent } from './components/entrance/entrance.component';
import { RegistrationComponent } from './components/registration/registration.component';
import { FormsModule } from "@angular/forms";
import { TokenInterceptor } from "./services/token.interceptor";
import { CreateProposalComponent } from './components/create-proposal/create-proposal.component';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DialogsComponent } from './components/dialogs/dialogs.component';



// import 'swiper/swiper-bundle.css';
// import 'swiper/swiper.less';

const appRoutes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'proposal/:id', component: ProposalDetailComponent },
  { path: 'login', component: EntranceComponent },
  { path: 'registration', component: RegistrationComponent },
  { path: 'create-proposal', component: CreateProposalComponent },
  { path: 'dialogs', component: DialogsComponent },
  { path: '**', redirectTo: "/" }
];

@NgModule({
  declarations: [
    AppComponent,
    ShortInfoProposalComponent,
    SearchComponent,
    HomeComponent,
    ProposalDetailComponent,
    ChatComponent,
    EntranceComponent,
    RegistrationComponent,
    CreateProposalComponent,
    DialogsComponent
  ],
  imports: [
    RouterModule.forRoot(appRoutes, {scrollPositionRestoration: 'enabled'}),
    BrowserModule,
    HttpClientModule,
    SwiperModule,
    FormsModule,
    // IgxListModule,
    BrowserAnimationsModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
