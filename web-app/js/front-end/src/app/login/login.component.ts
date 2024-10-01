import {Component, inject} from '@angular/core';
import {RouterLink} from "@angular/router";
import {HttpService} from "../utils/http.service";
import {FormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {MatButton} from "@angular/material/button";
import {OriginalUrlService} from "../utils/originalUrl.service";
import {catchError, throwError} from "rxjs";
import {ErrorHandler} from "../utils/errorHandle";
import {NavigationService} from "../utils/navService";
import {SnackBar} from "../utils/snackBarComponent";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    HttpClientModule,
    FormsModule,
    MatButton,
    RouterLink
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
  providers: [HttpService]
})
export class LoginComponent {
  username: string = '';
  password: string = '';

  httpService = inject(HttpService);

  constructor(
    private originalUrlService: OriginalUrlService,
    private errorHandle: ErrorHandler,
    private navService: NavigationService,
    private snackBar: SnackBar
  ) {}

  login(): void {
    this.httpService.login(this.username, this.password).pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe(res => {
      console.log("Done")
      const token = res.token
      const userId = res.userId
      const bytes = res.pfp
      const role = res.role
      const username = res.username

      localStorage.setItem('userId', userId)
      localStorage.setItem('username', username)
      localStorage.setItem('token', token)
      localStorage.setItem('role', role)
      if (bytes) {
        localStorage.setItem('pfp', window.btoa(bytes))
      }
      const redirect = this.originalUrlService.getOriginalUrl()
      if (redirect) {
        this.originalUrlService.resetOriginalUrl()
        this.navService.navUrl(redirect)
        this.snackBar.openSnackBar('Login efetuado com sucesso.');
      } else {
        this.navService.navWork()
        this.snackBar.openSnackBar('Login efetuado com sucesso.');
      }
    })
  }

  onSignUpClick() {
    this.navService.navSignUp()
  }
}
