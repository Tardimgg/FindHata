import {Component, CUSTOM_ELEMENTS_SCHEMA, Input} from '@angular/core';
import {Routes} from "@angular/router";
import {SearchComponent} from "./components/search/search.component";
import {HomeComponent} from "./components/home/home.component";
import {AuthService} from "./services/auth.service";
import {Location} from "@angular/common";



@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'findHataFrontend';

  constructor(private authService: AuthService, private location: Location) {
    this.authService.loginEmitter.subscribe((v) => {
      this.userLogin = v;
    })
  }

  userLogin: string = this.authService.getLogin();

  logout() {
    this.authService.logout()
    this.userLogin = "";

    window.location.reload()

  }

}
