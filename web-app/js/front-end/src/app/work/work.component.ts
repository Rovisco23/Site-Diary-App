import {Component, inject, ViewChild} from '@angular/core';
import {WorkListingsComponent} from "../work-listings/work-listings.component";
import {CommonModule} from "@angular/common";
import {HttpService} from '../utils/http.service';
import {Classes} from "../utils/classes";
import {HttpClientModule} from "@angular/common/http";
import {Router, RouterLink} from "@angular/router";
import {MatIcon} from "@angular/material/icon";
import {MatButton, MatFabButton} from "@angular/material/button";
import {FormsModule} from "@angular/forms";
import {catchError, throwError} from "rxjs";
import {ErrorHandler} from "../utils/errorHandle";
import {MatBadge} from "@angular/material/badge";
import {MatPaginator, PageEvent} from "@angular/material/paginator";
import {NavigationService} from "../utils/navService";

@Component({
  selector: 'app-work',
  standalone: true,
  imports: [
    HttpClientModule,
    CommonModule,
    WorkListingsComponent,
    RouterLink,
    MatIcon,
    MatFabButton,
    FormsModule,
    MatButton,
    MatBadge,
    MatPaginator
  ],
  providers: [HttpService],
  templateUrl: './work.component.html',
  styleUrl: './work.component.css'
})
export class WorkComponent {
  workListingsList: Classes[] = [];
  filteredWorkList: Classes[] = [];
  inputValue: string = '';
  httpService: HttpService = inject(HttpService);
  workElements: number = 6;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private navService: NavigationService, private router: Router, private errorHandle: ErrorHandler) {
    this.httpService.getWorkListings().pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe(res => {
      this.workListingsList = res;
      this.workElements = res.length;
      this.filteredWorkList = this.workListingsList.slice(0, 6);
    });
    if (localStorage.getItem('role') === 'CÂMARA') {
    }
  }


  onPageChange(event: PageEvent) {
    const startIndex = event.pageIndex * event.pageSize;
    const endIndex = startIndex + event.pageSize;
    this.filteredWorkList = this.workListingsList.slice(startIndex, endIndex);
  }

  filterResults(text: string) {
    if (!text) {
      this.filteredWorkList = this.workListingsList.slice(0, 6);
      this.workElements = this.workListingsList.length;
      return;
    }
    this.filteredWorkList = this.workListingsList.filter(
      workListing => workListing?.name.toLowerCase().includes(text.toLowerCase())
    );
    this.workElements = this.filteredWorkList.length;
    this.paginator.pageIndex = 0; // Reset the paginator to the first page
  }

  checkCouncil() {
    return localStorage.getItem('role') === 'CÂMARA';
  }

  councilVerifications() {
    this.navService.navVerifications();
  }

  createWork() {
    this.navService.navCreateWork()
  }
}
