import {Injectable} from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import {AuthService} from "./auth.service";
import {Observable} from "rxjs";

@Injectable()
export class TokenInterceptor implements HttpInterceptor {

  constructor(public auth: AuthService) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    let token = this.auth.getToken();

    if (token == null) {
      return next.handle(request);
    }

    // let newRequest = request.clone();
    //
    // newRequest.headers.set("accessToken", token)
    // console.log(newRequest);
    const clonedRequest = request.clone({ headers: request.headers.append('AccessToken', token) });


    return next.handle(clonedRequest);
  }
}
