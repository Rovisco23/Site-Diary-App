import {Component, inject} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {MatButton, MatFabButton, MatIconButton} from "@angular/material/button";
import {Invite, InviteCreation, Role} from "../utils/classes";
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderCellDef, MatHeaderRow,
  MatHeaderRowDef, MatRow, MatRowDef,
  MatTable, MatTableDataSource
} from "@angular/material/table";
import {MatIconModule} from "@angular/material/icon";
import {MatError, MatFormField, MatLabel} from "@angular/material/form-field";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatInput} from "@angular/material/input";
import {MatOption, MatSelect} from "@angular/material/select";
import {NgForOf, NgIf} from "@angular/common";
import {HttpService} from "../utils/http.service";
import {catchError, throwError} from "rxjs";
import {ErrorHandler} from "../utils/errorHandle";
import {NavigationService} from "../utils/navService";
import {WorkDetailsComponent} from "../work-details/work-details.component";
import {MatCheckbox} from "@angular/material/checkbox";
import {MatMenu, MatMenuItem} from "@angular/material/menu";
import {ConfirmDialogComponent} from "../utils/dialogComponent";
import {MatDialog} from "@angular/material/dialog";
import {SnackBar} from "../utils/snackBarComponent";

@Component({
  selector: 'app-work-invite',
  standalone: true,
  imports: [
    MatButton,
    MatTable,
    MatColumnDef,
    MatHeaderCell,
    MatIconModule,
    MatLabel,
    MatError,
    MatCell,
    MatCellDef,
    MatHeaderCellDef,
    MatHeaderRowDef,
    MatHeaderRow,
    MatRow,
    MatRowDef,
    MatFabButton,
    MatFormField,
    ReactiveFormsModule,
    MatInput,
    MatSelect,
    MatOption,
    NgIf,
    MatCheckbox,
    MatIconButton,
    MatMenu,
    MatMenuItem,
    FormsModule,
    NgForOf
  ],
  templateUrl: './work-invite.component.html',
  styleUrl: './work-invite.component.css'
})
export class WorkInviteComponent {

  roles = ['ESPECTADOR', 'MEMBRO', 'ARQUITETURA', 'ESTABILIDADE', 'ELETRICIDADE', 'GÁS', 'CANALIZAÇÃO', 'TELECOMUNICAÇÕES', 'TERMICO', 'ACUSTICO', 'TRANSPORTES'];

  workId: string = '';
  inviteCreation: InviteCreation[] = [];
  invites: Invite[] = [];
  dataSource: MatTableDataSource<InviteCreation> = new MatTableDataSource<InviteCreation>(this.inviteCreation);
  displayedColumns: string[] = ['email', 'role', 'actions'];
  httpService = inject(HttpService)

  constructor(
    private workComponent: WorkDetailsComponent,
    private route: ActivatedRoute,
    private navService: NavigationService,
    private errorHandle: ErrorHandler,
    private dialog: MatDialog,
    private snackBar: SnackBar
  ) {
    const parentRoute = this.route.parent;
    if (parentRoute) {
      const parentId = parentRoute.snapshot.paramMap.get('id');
      this.workId = parentId ?? '';
    }
    this.roles = this.roles.map(role => Role.composeRole(role));
    this.workComponent.work?.members.forEach( member => this.roles = this.roles.filter(role => role !== member.role || role === 'Membro' || role === 'Espectador'))
  }

  addRowInvite() {
    const invite = {
      position: this.inviteCreation.length + 1,
      email: '',
      role: '',
      submitted: false
    }
    this.inviteCreation.push(invite);
    this.dataSource.data = this.inviteCreation;
  }

  removeInvite(id: number) {
    this.roles.push(Role.composeRole(this.inviteCreation.find(invite => invite.position === id)!.role))
    this.inviteCreation = this.inviteCreation.filter(invite => invite.position !== id);
    this.invites = this.invites.filter(invite => invite.position !== id);
    this.dataSource.data = this.inviteCreation;
  }

  submitInvite(id: number){
    const inviteCreation = this.inviteCreation.find(invite => invite.position === id);
    if (inviteCreation){
      if (this.checkInvite(inviteCreation)){
        const invite = {
          position: inviteCreation.position,
          email: inviteCreation.email,
          role: Role.decomposeRole(inviteCreation.role)
        }
        this.invites.push(invite);
        this.inviteCreation.find(invite => invite.position === id)!.submitted = true;
        this.roles = this.roles.filter(role => role !== inviteCreation.role || role === 'Membro' || role === 'Espectador')
      }
    }
  }

  checkInvite(invite: InviteCreation): boolean {
    let errorMessage = '';
    if (invite.role === '' || invite.email === '') {
      errorMessage = 'É necessário preencher os campos de papel e email para adicionar um convite.'
    } else  if (invite.role !== 'Membro' && invite.role !== 'Espectador' && this.roles.find(role => role === invite.role) === undefined){
      errorMessage = 'Já existe um membro da obra ou convite com esse papel.'
    } else if (this.invites.find(i => i.email === invite.email) !== undefined){
      errorMessage = 'Já existe um convite para esse email.'
    }
    if (errorMessage.length === 0) {
      return true
    } else {
      const dialogRef = this.dialog.open(ConfirmDialogComponent, {
        data: {
          title: 'Erro',
          message: errorMessage,
        },
      });
      dialogRef.afterClosed().subscribe(() => {});
      return false
    }
  }

  sendInvites() {
    this.httpService.inviteMembers(this.workId, this.invites).pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe(() => {
      this.workComponent.loadWork(this.workId);
      this.workComponent.showLayout = true;
      this.navService.navWorkDetails(this.workId);
      this.snackBar.openSnackBar('Convites enviados com sucesso.');
    });
  }

  onBackCall() {
    this.workComponent.loadWork(this.workId);
    this.workComponent.showLayout = true;
    this.navService.navWorkDetails(this.workId);
  }
}
