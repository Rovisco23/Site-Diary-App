import {Component, inject} from '@angular/core';
import {HttpService} from "../utils/http.service";
import {InviteSimplified, Role} from "../utils/classes";
import {DatePipe, NgForOf} from "@angular/common";
import {MatDivider} from "@angular/material/divider";
import {MatButton, MatFabButton} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";
import {MatList, MatListItem, MatListItemIcon, MatListItemLine, MatListItemTitle} from "@angular/material/list";
import {catchError, throwError} from "rxjs";
import {ErrorHandler} from "../utils/errorHandle";
import {FormsModule} from "@angular/forms";
import {NavigationService} from "../utils/navService";
import {OriginalUrlService} from "../utils/originalUrl.service";

@Component({
  selector: 'app-invite-list',
  standalone: true,
    imports: [
        DatePipe,
        MatDivider,
        MatFabButton,
        MatIcon,
        MatList,
        MatListItem,
        MatListItemLine,
        MatListItemTitle,
        NgForOf,
        FormsModule,
        MatButton,
        MatListItemIcon
    ],
  templateUrl: './invite-list.component.html',
  styleUrl: './invite-list.component.css'
})
export class InviteListComponent {

  invites: InviteSimplified[] = [];

  filteredInvites: InviteSimplified[] = [];

  inputValue: string = '';

  httpService: HttpService = inject(HttpService);

  constructor(
    private navService: NavigationService,
    private errorHandle: ErrorHandler,
    private originalUrlService: OriginalUrlService
  ) {
    this.httpService.getInviteList().pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe(res => {
      this.invites = this.composeInvites(res);
      this.filteredInvites = this.invites;
    });
  }

  filterInvites(text: string) {
    if (!text) {
      this.filteredInvites = this.invites;
      return;
    }
    this.filteredInvites = this.invites.filter(
      entry => entry.workTitle.toLowerCase().includes(text.toLowerCase())
    );
  }

  composeInvites(invites: InviteSimplified[]): InviteSimplified[] {
    return invites.map(invite => {
      invite.role = Role.composeRole(invite.role);
      return invite;
    });
  }

  onInviteClick(id: string) {
    this.navService.navInvite(id)
  }

  onBackCall(){
    const url = this.originalUrlService.getOriginalUrl()
    if (url === undefined) {
      this.navService.navWork()
    } else {
      this.navService.navUrl(url)
    }
  }
}
