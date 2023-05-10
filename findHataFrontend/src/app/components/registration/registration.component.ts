import { Component } from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {Location} from "@angular/common";

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.css']
})
export class RegistrationComponent {

  loginForm = {
    login: '',
    email: '',
    password: '',
  }

  text_error: string

  refresh() {
    this.text_error = ""
  }


  registration(){
    this.service.registration(this.loginForm.login, this.loginForm.password, this.loginForm.email)
      .subscribe(response => {
        if (response.status == "ok") {
          this.service.saveToken(response.accessToken);
          this.service.saveRole(response.roles[0]);
          this.service.saveUserId(response.userId)
          this.location.back();
          this.service.emitLogin(this.loginForm.login);
          window.alert("Для подтверждения аккаунта мы отправили вам письмо")
        } else {
          this.text_error = response.error;
        }
      })
  }

  constructor(private service: AuthService, private location: Location) {
  }

}
