import {EventEmitter, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {LoginResponse} from "../entities/responses/login_response";
import {Role, roleFrom} from "../entities/role";

@Injectable({
  providedIn: 'root'
})

export class AuthService {

  // private url = 'http://127.0.0.1:8080/auth/api/';
  // private url = document.location.hostname + '/api/auth/api/';
  private url = '/api/auth/api/';

  constructor(private httpClient: HttpClient) { }

  loginEmitter = new EventEmitter<string>();

  saveToken(token: string) {
    localStorage.setItem("accessToken", token);
  }

  // not tested
  parseJwt(token: string) {
    let base64Url = token.split('.')[1];
    let base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    let jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));

    return JSON.parse(jsonPayload);
  }

  // not tested
  checkToken() {
    let token = localStorage.getItem("accessToken");
    if (token != null) {
      let payload = this.parseJwt(token);
      let exp = payload.exp;

      if ((Date.now() / 1000) >= exp) {
        this.logout();
      }
    }
  }

  getToken() {
    this.checkToken();
    return localStorage.getItem("accessToken");
  }

  saveUserId(userId: number) {
    localStorage.setItem("userId", userId.toString());
  }


  getUserId() {
    this.checkToken();
    // @ts-ignore
    return +localStorage.getItem("userId");
  }

  saveRole(role: Role) {
    localStorage.setItem("role", role.toString());
  }

  getRole() {
    this.checkToken();
    let role = localStorage.getItem("role");

    if (role != null) {
      return roleFrom(role);
    }

    return Role.ANONYMOUS;
  }

  emitLogin(login: string) {
    this.loginEmitter.emit(login);
    localStorage.setItem("login", login);
  }

  getLogin() {
    this.checkToken();
    let val = localStorage.getItem("login");
    return val == null ? "" : val;
  }

  logout() {
    localStorage.removeItem("login");
    localStorage.removeItem("role");
    localStorage.removeItem("accessToken");
    localStorage.removeItem("userId")
  }


  login(login: string, password: string) {
    return this.httpClient.post<LoginResponse>(this.url + "login", {
      login: login,
      password: password
    });
  }

  registration(login: string, password: string, email: string){
    return this.httpClient.post<LoginResponse>(this.url + "registration", {
      login: login,
      password: password,
      url: email,
      typeCommunication: "EMAIL"
    });
  }

}
