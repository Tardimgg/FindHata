import { Component } from '@angular/core';
import {GetAllProposalsResponse} from "../../entities/responses/get_all_proposals_response";
import {ProposalService} from "../../services/proposal.service";
import {AuthService} from "../../services/auth.service";
import {Location} from '@angular/common';
import {audit} from "rxjs";

@Component({
  selector: 'app-entrance',
  templateUrl: './entrance.component.html',
  styleUrls: ['./entrance.component.css']
})
export class EntranceComponent {

  loginForm: any = {
    login: '',
    password: '',
  }

  textError: string

  refresh() {
    this.textError = ""
  }

  login(){
    this.service.login(this.loginForm.login, this.loginForm.password)
      .subscribe(response => {
        if (response.status == "ok") {
          this.service.saveToken(response.accessToken)
          this.service.saveRole(response.roles[0]);
          this.service.saveUserId(response.userId)
          this.location.back();
          this.service.emitLogin(this.loginForm.login)
        } else {
          this.textError = "Неверный логин или пароль"
        }
      })
    return false;
  }

  constructor(private service: AuthService, private location: Location) {}

}
