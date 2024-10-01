import {Component, inject, LOCALE_ID, ViewChild} from '@angular/core';
import {NavigationEnd, Router, RouterLink, RouterLinkActive, RouterModule} from '@angular/router';
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatDrawer, MatSidenavModule} from "@angular/material/sidenav";
import {MatIconModule} from "@angular/material/icon";
import {MatButton, MatIconButton} from "@angular/material/button";
import {NgClass, NgIf, NgOptimizedImage, registerLocaleData} from "@angular/common";
import {filter} from 'rxjs/operators';
import {HttpService} from "./utils/http.service";
import {HttpClientModule} from "@angular/common/http";
import { FormsModule } from '@angular/forms';
import {MatBadge} from "@angular/material/badge";
import {catchError, throwError} from "rxjs";
import {ErrorHandler} from "./utils/errorHandle";
import {OriginalUrlService} from "./utils/originalUrl.service";
import {NavigationService} from "./utils/navService";
import localePt from '@angular/common/locales/pt';
import {MatProgressSpinner} from "@angular/material/progress-spinner";
import {LoadingQueue} from "./utils/loadingQueue";

registerLocaleData(localePt);

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterModule,
    FormsModule,
    MatToolbarModule,
    MatSidenavModule,
    MatIconModule,
    RouterLink,
    RouterLinkActive,
    MatIconButton,
    MatButton,
    HttpClientModule,
    NgIf,
    NgOptimizedImage,
    MatBadge,
    NgClass,
    MatProgressSpinner
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
  providers: [HttpService,  { provide: LOCALE_ID, useValue: 'pt-PT' }],
})
export class AppComponent {
  title = 'livro-de-obra-eletronico';
  showLayout = true;
  src = ''
  notification = 0;
  @ViewChild('drawer') drawer: MatDrawer | undefined;
  loadingQueue = inject(LoadingQueue);

  logout(): void {
    const token = localStorage.getItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('profilePicture');
    this.urlService.setOriginalUrl(this.router.url)
    this.httpService.logout(token ?? '').pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe(() => {
      this.navService.navLogin()
    })
  }

  constructor(private router: Router, private httpService: HttpService, private errorHandle: ErrorHandler, private urlService: OriginalUrlService, private navService: NavigationService) {
    this.router.events
      .pipe(filter((events) => events instanceof NavigationEnd))
      .subscribe((event: any) => {
        const navigationEndEvent = event as NavigationEnd;
        this.showLayout = navigationEndEvent.urlAfterRedirects !== '/login' &&
          navigationEndEvent.urlAfterRedirects !== '/signup';
        if (localStorage.getItem('token')){
          this.httpService.getProfilePicture().pipe(
            catchError(error => {
              this.errorHandle.handleError(error);
              return throwError(error);
            })
          ).subscribe((data) => {
            if (data.size === 0) {
              this.src = './assets/profile.png'
            } else {
              localStorage.setItem('profilePicture', URL.createObjectURL(data))
              this.src = URL.createObjectURL(data)
            }
          })
          this.httpService.getNumberOfInvites().pipe(
            catchError(error => {
              this.errorHandle.handleError(error);
              return throwError(error);
            })
          ).subscribe(res => {
            this.notification = res;
          })
        }
      })
  }

  toggleDrawer() {
    if (this.drawer) {
      this.drawer.toggle();
    }
  }

  updatePicture(src: string){
    this.src = src
  }

  checkAdminRole() {
    return localStorage.getItem('role') === 'ADMIN'
  }

  onProfileCall() {
    const username = localStorage.getItem('username') ?? '';
    this.urlService.setOriginalUrl(this.router.url)
    this.navService.navProfile(username)
  }

  onWorkCall() {
    this.navService.navWork()
  }

  onUsersCall() {
    this.urlService.setOriginalUrl(this.router.url)
    this.navService.navUsers()
  }

  onPendingUsersCall() {
    this.urlService.setOriginalUrl(this.router.url)
    this.navService.navPendingUsers()
  }

  onAllLogsCall() {
    this.urlService.setOriginalUrl(this.router.url)
    this.navService.navMyLogs()
  }

  onVerificationsCall() {
    this.urlService.setOriginalUrl(this.router.url)
    this.navService.navVerifications()
  }

  onCreateWorkCall() {
    this.urlService.setOriginalUrl(this.router.url)
    this.navService.navCreateWork()
  }


  onInvitesClick() {
    this.urlService.setOriginalUrl(this.router.url)
    this.navService.navInviteList()
  }

  getUserName() {
    return localStorage.getItem('username') ?? '';
  }

  checkRole() {
    return localStorage.getItem('role') === 'ADMIN' || localStorage.getItem('role') === 'CÂMARA'
  }
}
