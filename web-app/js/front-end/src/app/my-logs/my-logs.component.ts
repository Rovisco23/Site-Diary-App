import {Component, ErrorHandler} from '@angular/core';
import {HttpService} from '../utils/http.service';
import {DatePipe, NgClass, NgForOf, NgIf} from '@angular/common';
import {catchError} from 'rxjs/operators';
import {MyLog} from '../utils/classes';
import {NavigationService} from '../utils/navService';
import {FormsModule} from "@angular/forms";
import {MatListModule,} from "@angular/material/list";
import {MatIcon} from "@angular/material/icon";
import {HttpClientModule} from "@angular/common/http";
import {MatTab, MatTabGroup} from "@angular/material/tabs";
import {MatCardModule} from "@angular/material/card";
import {RouterLink, RouterOutlet} from "@angular/router";
import {MatButton, MatFabButton, MatIconButton} from "@angular/material/button";
import {MatLabel} from "@angular/material/form-field";
import {MatBadge} from "@angular/material/badge";
import {OriginalUrlService} from "../utils/originalUrl.service";

@Component({
  selector: 'app-my-logs',
  standalone: true,
  imports: [
    HttpClientModule,
    MatTabGroup,
    MatTab,
    NgForOf,
    NgClass,
    MatCardModule,
    MatListModule,
    RouterLink,
    DatePipe,
    MatIcon,
    MatFabButton,
    NgIf,
    MatButton,
    MatLabel,
    FormsModule,
    MatBadge,
    MatIconButton,
    RouterOutlet
  ],
  providers: [HttpService, DatePipe],
  templateUrl: './my-logs.component.html',
  styleUrl: './my-logs.component.css'
})
export class MyLogsComponent {
  logList: MyLog[] = [];
  filteredLogList: MyLog[] = [];
  searchLogValue = '';

  constructor(
    private navService: NavigationService,
    private errorHandle: ErrorHandler,
    private httpService: HttpService,
    private datePipe: DatePipe,
    private urlService: OriginalUrlService
  ) {
    this.fetchLogs();
  }

  fetchLogs() {
    this.httpService.getMyLogs().pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return []; // Return empty array if request fails
      })
    ).subscribe(res => {
      this.logList = res;
      this.transformDates();
      this.filteredLogList = this.logList;
    })
  }

  transformDates() {
    this.logList.forEach(log => {
      const date = new Date(log.createdAt);
      log.createdAt = this.datePipe.transform(date, 'longDate') ?? log.createdAt;
    });
  }

  onLogClick(id: number, workId: string) {
    this.navService.navLogEntry(workId, id);
  }

  filterResults(text: string) {
    if (!text) {
      this.filteredLogList = this.logList;
      return;
    }
    text = text.toLowerCase();
    this.filteredLogList = this.logList.filter(
      entry => entry.createdAt.toLowerCase().includes(text) ||
        entry.workName.toLowerCase().includes(text)
    );
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
}
