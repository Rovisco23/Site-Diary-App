import {Component, ElementRef, inject, ViewChild} from '@angular/core';
import {ActivatedRoute, Router, RouterLink, RouterOutlet} from '@angular/router';
import {
  Address, Company, EditWorkInputModel,
  Invite,
  LogEntrySimplified,
  Member,
  Role,
  Technician,
  TechnicianCreation,
  Work,
  WorkState, WorkTypes
} from "../utils/classes";
import {HttpService} from '../utils/http.service';
import {HttpClientModule} from "@angular/common/http";
import {MatTab, MatTabGroup} from "@angular/material/tabs";
import {DatePipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {MatCardModule} from "@angular/material/card";
import {MatListModule} from "@angular/material/list";
import {MatIcon} from "@angular/material/icon";
import {MatButton, MatFabButton, MatIconButton} from "@angular/material/button";
import {MatLabel} from "@angular/material/form-field";
import {FormsModule} from "@angular/forms";
import {MatBadge} from "@angular/material/badge";
import {catchError, throwError} from "rxjs";
import {ErrorHandler} from "../utils/errorHandle";
import {MatDialog} from "@angular/material/dialog";
import {ConfirmDialogComponent} from "../utils/dialogComponent";
import {NavigationService} from "../utils/navService";
import {OriginalUrlService} from "../utils/originalUrl.service";
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell, MatHeaderCellDef,
  MatHeaderRow, MatHeaderRowDef, MatRow, MatRowDef, MatTable,
  MatTableDataSource
} from "@angular/material/table";
import {concelhos, freguesias} from "../utils/utils";
import {SnackBar} from "../utils/snackBarComponent";

@Component({
  selector: 'app-work-details',
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
    RouterOutlet,
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatHeaderCell,
    MatHeaderRow,
    MatHeaderRowDef,
    MatRow,
    MatRowDef,
    MatTable,
    MatHeaderCellDef
  ],
  providers: [HttpService, DatePipe],
  templateUrl: './work-details.component.html',
  styleUrl: './work-details.component.css'
})
export class WorkDetailsComponent {
  route: ActivatedRoute = inject(ActivatedRoute);
  httpService = inject(HttpService);
  work: Work | undefined;
  filteredLogList: LogEntrySimplified[] = [];
  filteredMembers: Member[] = [];
  workSrc = '';
  form = new FormData();
  editSrc: string | undefined = undefined;
  newImageFile: any = ''
  searchLogValue = '';
  searchMemberValue = '';
  tabIndex = 0;
  showLayout: boolean = true;
  technicians: Technician[] = [];
  techCreationList: TechnicianCreation[] = [];
  dataSource: MatTableDataSource<Technician> = new MatTableDataSource<Technician>(this.technicians);
  dataSourceEdit: MatTableDataSource<TechnicianCreation> = new MatTableDataSource<TechnicianCreation>(this.techCreationList);
  displayedColumns: string[] = ['role', 'name', 'association', 'actions'];
  displayedColumnsEdit: string[] = ['name', 'email', 'role', 'association', 'actions'];
  editWork: boolean = false;
  editPicture: boolean = false;
  @ViewChild('fileInput') fileInput: ElementRef<HTMLInputElement> | undefined;
  verificationDoc: string | null = null;


  types = Object.values(WorkTypes);
  roles = ['Membro', 'Espectador', 'Técnico de Arquitetura', 'Técnico de Estabilidade',
    'Técnico de Alimentação e Destribuição de Energia Elétrica', 'Técnico de Instalações de Gás',
    'Técnico de Instalações de Água e Esgotos', 'Técnico de Instalações de Telecomunicações',
    'Técnico de Comportamento Térmico', 'Técnico de Condicionamento Acústico',
    'Técnico de Instalações de Eletromecânicas de Transporte']

  counties: string[] = [];
  parishes: string[] = [];
  districts: string[] = [];

  editWorkName: string = '';
  editWorkDescription: string = '';
  editWorkType: string = '';
  editBuilding: string = '';
  editHolder: string = '';
  editCompany: Company = {
    name: '',
    num: 0
  }
  editAddress: Address = {
    street: '',
    postalCode: '',
    location: {
      parish: '',
      county: '',
      district: ''
    }
  };

  constructor(
    private datePipe: DatePipe,
    private router: Router,
    private urlService: OriginalUrlService,
    private navService: NavigationService,
    private dialog: MatDialog,
    private snackBar: SnackBar,
    private errorHandle: ErrorHandler,
  ) {
    const workListingId = String(this.route.snapshot.params['id']);
    const uri = this.router.url.split('/')
    if (uri[3] === 'invite-members') this.tabIndex = 2
    this.loadWork(workListingId);
    this.work?.members.forEach(member => this.roles = this.roles.filter(role => role !== member.role || role === 'Membro' || role === 'Espectador'))
    if (this.router.url !== '/work-details/' + workListingId) {
      this.showLayout = false
    }
    concelhos.forEach((value, key) => {
      value.forEach((v: string) => this.counties.push(v));
      this.districts.push(key);
    })
    freguesias.forEach((value) => {
      value.forEach((x: string) => this.parishes.push(x));
    })
  }

  loadWork(id: string) {
    this.httpService.getWorkById(id).pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe((work: Work) => {
      work.members = this.composeMemberRoles(work.members);
      work.technicians = this.composeTechnicianRoles(work.technicians)
      this.technicians = work.technicians
      this.techCreationList = this.convertFromTech(work.technicians)
      this.dataSource.data = this.technicians
      this.dataSourceEdit.data = this.techCreationList
      this.work = work;
      this.work.state = WorkState.composeState(work.state);
      this.verificationDoc = work.verificationDoc
      this.work.verificationDoc = work.verificationDoc
      this.work.type = WorkState.composeType(work.type);
      this.filteredLogList = work.log;
      this.filteredMembers = work.members;
      this.filteredLogList.map(log => {
          const date = new Date(log.createdAt)
          log.createdAt = this.datePipe.transform(date, 'longDate') ?? log.createdAt
        }
      )
      this.editWorkName = work.name;
      this.editWorkDescription = work.description;
      this.editWorkType = work.type;
      this.editBuilding = work.building;
      this.editHolder = work.licenseHolder;
      this.editAddress = work.address;
      this.editCompany = work.company;
    });
    this.httpService.getWorkImage(id).pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe((data) => {
      if (data.size === 0) {
        this.workSrc = './assets/work.png'
      } else {
        this.workSrc = URL.createObjectURL(data)
      }
    })
  }

  toggleEditWork() {
    this.editWork = !this.editWork;
  }

  checkCanAskVerification(){
    return (this.work?.state === 'Rejeitada' || this.work?.state === 'Em Progresso' && !this.work.verification) &&
      this.work?.members.find(member => member.role === 'Dono da Obra')?.id === Number(localStorage.getItem('userId'))
  }

  onImageChange(event: any) {
    this.newImageFile = event.target.files[0];
    const reader = new FileReader();

    reader.onload = () => {
      this.editSrc = reader.result as string;
    };

    if (!this.editPicture) this.editPicture = true
    reader.readAsDataURL(this.newImageFile);
    this.fileInput!.nativeElement.value = '';
  }

  changeTab(id: number) {
    this.showLayout = true
    this.navService.navWorkDetails(this.work!!.id)
    if (this.editWork) this.cancelEdit()
    this.tabIndex = id
  }

  onInviteClick() {
    this.showLayout = false
    this.navService.navInviteMembers(this.work?.id ?? '')
  }

  createNewEntry() {
    this.showLayout = false
    this.navService.navCreateLogEntry(this.work!!.id);
  }

  composeTechnicianRoles(tecs: Technician[]): Technician[] {
    return tecs.map(technician => {
      technician.role = Role.composeRole(technician.role);
      return technician;
    });
  }

  private composeMemberRoles(members: Member[]) {
    return members.map(m => {
      m.role = Role.composeRole(m.role);
      return m;
    });
  }

  onLogEntryClick(id: number) {
    this.showLayout = false
    this.navService.navLogEntry(this.work?.id ?? '', id)
  }

  onMemberClick(username: string) {
    this.showLayout = false
    this.urlService.setOriginalUrl(this.router.url)
    this.navService.navWorkMemberProfile(this.work?.id ?? '', username)
  }

  filterResults(text: string) {
    if (!text) {
      this.filteredLogList = this.work!!.log;
      return;
    }
    this.filteredLogList = this.work!!.log.filter(
      entry => entry.createdAt.toLowerCase().includes(text.toLowerCase()) ||
        entry.author.name.toLowerCase().includes(text.toLowerCase())
    );
  }

  filterMembers(text: string) {
    if (!text) {
      this.filteredMembers = this.work!!.members;
      return;
    }
    this.filteredMembers = this.work!!.members.filter(
      entry => entry.name.toLowerCase().includes(text.toLowerCase())
    );
  }

  onBackCall() {
    this.navService.navWork()
  }

  finishWorkCall() {
    if (this.editWork) this.cancelEdit()
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Terminar Obra',
        message: 'Tem a certeza que deseja terminar a obra?',
      },
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.httpService.finishWork(this.work!!.id).pipe(
          catchError(error => {
            this.errorHandle.handleError(error);
            return throwError(error);
          })
        ).subscribe(() => {
          this.navService.navWork();
        })
      }
    });
  }

  checkWorkCanFinish() {
    const isOwner = this.work?.members.find(member => member.role === 'Dono da Obra')?.id === Number(localStorage.getItem('userId'))
    return this.work?.state === 'Em Progresso' && isOwner
  }

  checkAskVerification() {
    return (this.work?.state === 'Rejeitada' || this.work?.state === 'Em Progresso' && !this.work.verification) && this.checkActionPermissions('invite')
  }

  checkActionPermissions(action: string) {
    const role = this.work?.members.find(member => member.id === Number(localStorage.getItem('userId')))?.role
    if (!role) {
      return false
    } else if (action === 'log') {
      return role !== 'Membro' && role !== 'Espectador'
    } else {
      return role === 'Dono da Obra'
    }
  }

  checkWorkRejected() {
    return this.work?.state === 'Rejeitada'
  }

  checkNotMember(role: string) {
    return this.work?.members.find(member => member.role === role) === undefined
  }

  inviteTech(role: string) {
    const tech = this.technicians.find(tech => tech.role === role)
    if (tech) {
      const invite: Invite[] = [{
        position: 0,
        email: tech.email,
        role: Role.decomposeRole(tech.role)
      }]
      this.httpService.inviteMembers(this.work!.id, invite).pipe(
        catchError(error => {
          this.errorHandle.handleError(error);
          return throwError(error);
        })
      ).subscribe(() => {
        this.loadWork(this.work!.id)
        this.snackBar.openSnackBar("Técnico convidado com sucesso")
      })
    }
  }

  onEditCall() {
    const work: EditWorkInputModel = {
      name: this.editWorkName,
      description: this.editWorkDescription,
      type: this.editWorkType.toUpperCase(),
      building: this.editBuilding,
      licenseHolder: this.editHolder,
      company: this.editCompany,
      address: this.editAddress,
      technicians: this.convertToTech(this.techCreationList)
    }
    this.httpService.editWork(this.work!.id, work).pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe(() => {
        this.loadWork(this.work!.id)
        this.toggleEditWork()
        this.snackBar.openSnackBar("Obra editada com sucesso")
      }
    )
  }

  cancelEdit() {
    this.resetEditValues()
    this.toggleEditWork()
  }

  resetEditValues() {
    this.techCreationList = this.convertFromTech(this.work!.technicians)
    this.dataSourceEdit.data = this.techCreationList;
    this.editWorkName = this.work!.name;
    this.editWorkDescription = this.work!.description;
    this.editWorkType = this.work!.type;
    this.editBuilding = this.work!.building;
    this.editHolder = this.work!.licenseHolder;
    this.editAddress = this.work!.address;
    this.editCompany = this.work!.company;
  }

  addRowTech() {
    const tech = {
      position: this.techCreationList.length + 1,
      name: '',
      email: '',
      role: '',
      association: {name: '', number: 0},
      submitted: false
    }
    this.techCreationList.push(tech);
    this.dataSourceEdit.data = this.techCreationList;
  }

  removeTech(id: number) {
    const tech = this.techCreationList.find(t => t.position === id);
    const role = tech!.role
    if (this.checkResponsability(tech!.position)) {
      this.techCreationList.find(t => t.position === id)!.submitted = false;
      this.technicians = this.technicians.filter(t => t.email !== tech?.email);
      this.dataSourceEdit.data = this.techCreationList;
    } else {
      if (role !== 'Membro' && role !== 'Espectador') {
        this.roles.push(Role.composeRole(role))
      }
      this.techCreationList = this.techCreationList.filter(t => t.position !== id);
      this.work!.technicians = this.work!.technicians.filter(t => t.email !== tech?.email);
      this.dataSourceEdit.data = this.techCreationList;
    }
  }

  checkResponsability(id: number) {
    const role = this.techCreationList.find(t => t.position === id)!.role;
    return role === 'Diretor de Obra' || role === 'Coordenador' || role === 'Responsável de Fiscalização';
  }

  submitTech(id: number) {
    const techCreation = this.techCreationList.find(t => t.position === id);
    if (techCreation) {
      if (this.checkTech(techCreation)) {
        const tech = {
          name: techCreation.name,
          email: techCreation.email,
          role: Role.decomposeRole(techCreation.role),
          association: techCreation.association
        }
        this.technicians.push(tech);
        this.techCreationList.find(t => t.position === id)!.submitted = true;
        this.roles = this.roles.filter(role => role !== techCreation.role || role === 'Membro' || role === 'Espectador')
      }
    }
  }

  checkTech(tech: TechnicianCreation): boolean {
    let errorMessage = '';
    if (tech.role === '' || tech.email === '' || tech.name === '' || tech.association.name === '') {
      errorMessage = 'É necessário preencher todos os campos.'
    } else if (isNaN(tech.association.number) || tech.association.number <= 0) {
      errorMessage = 'Número de associado inválido.'
    } else if (tech.role !== 'Membro' && tech.role !== 'Espectador' &&
      tech.role !== 'Diretor de Obra' && tech.role !== 'Coordenador' &&
      tech.role !== 'Responsável de Fiscalização' && this.roles.find(role => role === tech.role) === undefined) {
      errorMessage = 'Já existe um técnico com esse papel.'
    } else if (this.technicians.find(t => t.email === tech.email) !== undefined) {
      errorMessage = 'Já existe um técnico para esse email.'
    } else if (this.technicians.find(t => t.association.name === tech.association.name && t.association.number === tech.association.number) !== undefined) {
      errorMessage = 'Já existe um técnico com nessa associação com o mesmo número de associado.'
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
      dialogRef.afterClosed().subscribe(() => {
      });
      return false
    }
  }

  formatZipCode() {
    let value = this.work!.address.postalCode.replace(/\D/g, '');
    if (value.length > 4) {
      value = `${value.slice(0, 4)}-${value.slice(4, 7)}`;
    }
    this.work!.address.postalCode = value;
  }

  updateLocation(change: boolean) {
    const selectedParish = this.work!.address.location.parish;
    const selectedCounty = this.work!.address.location.county;
    if (!change) {
      const cList: string[] = [];
      const dList: string[] = [];
      for (const c of freguesias.keys()) {
        const pList = freguesias.get(c);
        if (pList.includes(selectedParish)) {
          cList.push(c);
        }
      }
      for (const d of concelhos.keys()) {
        const conList = concelhos.get(d);
        for (const c of cList) {
          if (conList.includes(c)) {
            dList.push(d);
          }
        }
      }
      this.counties = cList;
      this.districts = dList;
      this.editAddress.location.county = cList[0];
    }
    if (change) {
      const dList: string[] = [];
      for (const d of concelhos.keys()) {
        const cList = concelhos.get(d);
        if (cList.includes(selectedCounty)) {
          dList.push(d);
        }
      }
      this.districts = dList;
    }
    this.editAddress.location.district = this.districts[0];
  }

  private convertFromTech(technicians: Technician[]) {
    let techList: TechnicianCreation[] = [];
    technicians.forEach(tech => {
      techList.push({
        position: techList.length + 1,
        name: tech.name,
        email: tech.email,
        role: tech.role,
        association: tech.association,
        submitted: true
      })
    })
    return techList;
  }

  checkCanRemoveTech(role: string) {
    return this.work?.members.find(member => member.role === role) === undefined
  }

  convertToTech(techCreation: TechnicianCreation[]) {
    let techList: Technician[] = [];
    techCreation.forEach(tech => {
      techList.push({
        name: tech.name,
        email: tech.email,
        role: Role.decomposeRole(tech.role),
        association: tech.association
      })
    })
    return techList;
  }

  onSubmitImageChange() {
    this.form.append('file', this.newImageFile);
    this.httpService.changeWorkImage(this.work!.id, this.form).pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe(() => {
      this.editPicture = false
      this.workSrc = this.editSrc ?? '';
      this.editSrc = undefined;
      this.snackBar.openSnackBar('Imagem de obra alterada com sucesso.')
    });
  }

  onCancelImageChange() {
    this.editPicture = false;
    this.editSrc = undefined;
  }

  askVerification() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Pedir verificação à câmara municipal de ' + this.work?.address.location.county,
        message: 'Tem a certeza que deseja pedir verificação da obra?',
        inputValue: this.verificationDoc // Passa o valor atual
      },
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.verificationDoc = result; // Atualiza o valor de verificationDoc
        this.httpService.askVerification(this.work!.id, this.verificationDoc ?? '').pipe(
          catchError(error => {
            this.errorHandle.handleError(error);
            return throwError(error);
          })
        ).subscribe(() => {
          this.loadWork(this.work!.id);
          this.snackBar.openSnackBar("Pedido de verificação efetuado com sucesso");
        });
      } else {
        this.snackBar.openSnackBar("Pedido de verificação não enviado. Documento inválido.");
      }
    });
  }

  getOpeningTerm() {
    this.httpService.getOpeningTerm(this.work!.id).pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe((res) => {
      const link = document.createElement('a');
      link.href = URL.createObjectURL(res);
      link.download = 'termo_abertura_'+ this.work!.id +'.pdf';
      link.click();
    })
  }

  getSiteDiary() {
    this.httpService.getSiteDiary(this.work!.id).pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe((res) => {
      const link = document.createElement('a');
      link.href = URL.createObjectURL(res);
      link.download = 'diario_obra_'+ this.work!.id +'.pdf';
      link.click();
    })
  }
}
