import {Component, inject} from '@angular/core';
import {MatIcon} from "@angular/material/icon";
import {NgForOf} from "@angular/common";
import {User} from "../utils/classes";
import {HttpService} from "../utils/http.service";
import {MatDivider} from "@angular/material/divider";
import {MatButton, MatIconButton} from "@angular/material/button";
import {MatList, MatListItem, MatListItemLine, MatListItemMeta, MatListItemTitle} from "@angular/material/list";
import {FormsModule} from "@angular/forms";
import {catchError, throwError} from "rxjs";
import {ErrorHandler} from "../utils/errorHandle";
import {NavigationService} from "../utils/navService";
import {Router} from "@angular/router";
import {OriginalUrlService} from "../utils/originalUrl.service";

@Component({
  selector: 'app-list-users',
  standalone: true,
    imports: [
        MatIcon,
        MatDivider,
        MatIconButton,
        MatList,
        MatListItem,
        MatListItemLine,
        MatListItemMeta,
        MatListItemTitle,
        NgForOf,
        FormsModule,
        MatButton
    ],
  templateUrl: './list-users.component.html',
  styleUrl: './list-users.component.css'
})
export class ListUsersComponent {

  httpService: HttpService = inject(HttpService)

  value: string = ''

  usersList: User[] = []

  filteredUsersList: User[] = []

  constructor(
    private router: Router,
    private errorHandle: ErrorHandler,
    private navService: NavigationService,
    private urlService: OriginalUrlService
  ) {
    this.httpService.getAllUsers().pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe((res) => {
      this.usersList = res
      this.filteredUsersList = this.usersList
    });
  }

  filterUsers(text: string) {
    if (!text) {
      this.filteredUsersList = this.usersList
      return
    }
    this.filteredUsersList = this.usersList.filter(
      user => user.username.toLowerCase().includes(text.toLowerCase()) ||
        user.firstName.toLowerCase().includes(text.toLowerCase()) ||
        user.lastName.toLowerCase().includes(text.toLowerCase()) ||
        user.email.toLowerCase().includes(text.toLowerCase())
    )
  }

  onBackCall() {
    const url = this.urlService.getOriginalUrl()
    if (url === undefined){
      this.navService.navWork()
    } else {
      this.urlService.resetOriginalUrl()
      this.navService.navUrl(url)
    }
  }

  onUserClick(username: string) {
    this.urlService.setOriginalUrl(this.router.url)
    this.navService.navProfile(username)
  }
}
