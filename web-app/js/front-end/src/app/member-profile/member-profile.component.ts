import {Component, ErrorHandler} from '@angular/core';
import {HttpService} from "../utils/http.service";
import {NavigationService} from "../utils/navService";
import {ActivatedRoute} from "@angular/router";
import {catchError, throwError} from "rxjs";
import {MemberProfile, Role} from "../utils/classes";
import {NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {MatButton, MatIconButton} from "@angular/material/button";
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow, MatRowDef, MatTable
} from "@angular/material/table";
import {MatCheckbox} from "@angular/material/checkbox";
import {MatDivider} from "@angular/material/divider";
import {MatIcon} from "@angular/material/icon";
import {MatMenu, MatMenuItem} from "@angular/material/menu";
import {WorkDetailsComponent} from "../work-details/work-details.component";

@Component({
  selector: 'app-member-profile',
  standalone: true,
  imports: [
    NgIf,
    FormsModule,
    MatButton,
    MatCell,
    MatCellDef,
    MatCheckbox,
    MatColumnDef,
    MatDivider,
    MatHeaderCell,
    MatHeaderRow,
    MatHeaderRowDef,
    MatIcon,
    MatIconButton,
    MatMenu,
    MatMenuItem,
    MatRow,
    MatRowDef,
    MatTable
  ],
  templateUrl: './member-profile.component.html',
  styleUrl: './member-profile.component.css'
})
export class MemberProfileComponent {
  workId: string = '';
  username
  memberSrc: string = '';
  member: MemberProfile | undefined = undefined;

  constructor(private workComponent: WorkDetailsComponent, private route: ActivatedRoute, private navService: NavigationService, private httpService: HttpService,private errorHandle: ErrorHandler) {
    this.username = String(this.route.snapshot.params['username']);
    const parentRoute = this.route.parent;
    if (parentRoute) {
      const parentId = parentRoute.snapshot.paramMap.get('id');
      this.workId = parentId ?? '';
    }
    this.loadMember(this.workId, this.username)
  }

  loadMember(workId: string, member: string) {
    this.httpService.getProfilePictureByUsername(member).pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe((data) => {
      if (data.size === 0) {
        this.memberSrc = './assets/profile.png'
      } else {
        localStorage.setItem('profilePicture', URL.createObjectURL(data))
        this.memberSrc = URL.createObjectURL(data)
      }
    })
    this.httpService.getMemberWorkProfile(workId, member).pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe( res => {
      res.role = Role.composeRole(res.role)
      this.member = res;
    })
  }

  onBackCall() {
    this.workComponent.loadWork(this.workId)
    this.workComponent.showLayout = true;
    this.navService.navWorkDetails(this.workId);
  }
}
