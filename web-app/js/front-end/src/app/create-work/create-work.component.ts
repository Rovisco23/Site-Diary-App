import {Component, inject} from '@angular/core';
import {freguesias, concelhos} from '../utils/utils';
import {HttpService} from "../utils/http.service";
import {
  InputWork,
  Role,
  TechnicianCreation,
  WorkTypes
} from "../utils/classes";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButton, MatFabButton} from "@angular/material/button";
import {MatError, MatFormField, MatLabel, MatOption, MatSelect} from "@angular/material/select";
import {CommonModule, NgForOf, NgIf} from "@angular/common";
import {MatIcon} from "@angular/material/icon";
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell, MatHeaderCellDef,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow, MatRowDef, MatTable, MatTableDataSource
} from "@angular/material/table";
import {MatInput} from "@angular/material/input";
import {catchError, throwError} from "rxjs";
import {ErrorHandler} from "../utils/errorHandle";
import {NavigationService} from "../utils/navService";
import {MatDivider} from "@angular/material/divider";
import {ConfirmDialogComponent} from "../utils/dialogComponent";
import {MatDialog} from "@angular/material/dialog";
import {SnackBar} from "../utils/snackBarComponent";
import {OriginalUrlService} from "../utils/originalUrl.service";

@Component({
  selector: 'app-create-work',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButton,
    ReactiveFormsModule,
    MatSelect,
    MatOption,
    NgForOf,
    NgIf,
    MatFormField,
    MatIcon,
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatFabButton,
    MatHeaderCell,
    MatHeaderRow,
    MatHeaderRowDef,
    MatInput,
    MatLabel,
    MatRow,
    MatRowDef,
    MatTable,
    MatHeaderCellDef,
    MatError,
    MatDivider
  ],
  templateUrl: './create-work.component.html',
  styleUrl: './create-work.component.css'
})
export class CreateWorkComponent {

  techCreationList: TechnicianCreation[] = [{
    position: 0,
    name: '',
    email: '',
    role: 'Responsável de Fiscalização',
    association: {name: '', number: 0},
    submitted: false
  }, {
    position: 1,
    name: '',
    email: '',
    role: 'Coordenador',
    association: {name: '', number: 0},
    submitted: false
  }, {
    position: 2,
    name: '',
    email: '',
    role: 'Diretor de Obra',
    association: {name: '', number: 0},
    submitted: false
  }];
  work: InputWork;
  types = Object.values(WorkTypes);
  roles = ['Técnico de Arquitetura', 'Técnico de Estabilidade',
    'Técnico de Alimentação e Destribuição de Energia Elétrica', 'Técnico de Instalações de Gás',
    'Técnico de Instalações de Água e Esgotos', 'Técnico de Instalações de Telecomunicações',
    'Técnico de Comportamento Térmico', 'Técnico de Condicionamento Acústico',
    'Técnico de Instalações de Eletromecânicas de Transporte']

  httpService = inject(HttpService)

  counties: string[] = [];
  parishes: string[] = [];
  districts: string[] = [];

  dataSource: MatTableDataSource<TechnicianCreation> = new MatTableDataSource<TechnicianCreation>(this.techCreationList);
  displayedColumns: string[] = ['name', 'email', 'role', 'association', 'actions'];

  constructor(
    private dialog: MatDialog,
    private errorHandle: ErrorHandler,
    private navService: NavigationService,
    private snackBar: SnackBar,
    private urlService: OriginalUrlService
  ) {
    this.work = {
      name: '',
      type: '',
      description: '',
      holder: '',
      company: {
        name: '',
        num: 0
      },
      building: '',
      address: {
        location: {
          district: '',
          county: '',
          parish: ''
        },
        street: '',
        postalCode: ''
      },
      technicians: [],
      verification: null,
    }
    concelhos.forEach((value, key) => {
      value.forEach((v: string) => this.counties.push(v));
      this.districts.push(key);
    })
    freguesias.forEach((value) => {
      value.forEach((x: string) => this.parishes.push(x));
    })
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
    this.dataSource.data = this.techCreationList;
  }

  removeTech(id: number) {
    const tech = this.techCreationList.find(t => t.position === id);
    const role = tech!.role
    if (this.checkResponsability(tech!.position)){
      this.techCreationList.find(t => t.position === id)!.submitted = false;
      this.work.technicians = this.work.technicians.filter(t => t.email !== tech?.email);
      this.dataSource.data = this.techCreationList;
    } else {
      if (role !== 'Membro' && role !== 'Espectador'){
        this.roles.push(Role.composeRole(role))
      }
      this.techCreationList = this.techCreationList.filter(t => t.position !== id);
      this.work.technicians = this.work.technicians.filter(t => t.email !== tech?.email);
      this.dataSource.data = this.techCreationList;
    }
  }

  submitTech(id: number){
    const techCreation = this.techCreationList.find(t => t.position === id);
    if (techCreation){
      if (this.checkTech(techCreation)){
        const tech = {
          name: techCreation.name,
          email: techCreation.email,
          role: Role.decomposeRole(techCreation.role),
          association: techCreation.association
        }
        this.work.technicians.push(tech);
        this.techCreationList.find(t => t.position === id)!.submitted = true;
        this.roles = this.roles.filter(role => role !== techCreation.role || role === 'Membro' || role === 'Espectador')
      }
    }
  }

  checkTech(tech: TechnicianCreation): boolean {
    let errorMessage = '';
    if (tech.role === '' || tech.email === '' || tech.name === '' || tech.association.name === '') {
      errorMessage = 'É necessário preencher todos os campos.'
    } else if(isNaN(tech.association.number) || tech.association.number <= 0) {
      errorMessage = 'Número de associado inválido.'
    } else if (tech.role !== 'Membro' && tech.role !== 'Espectador' &&
      tech.role !== 'Diretor de Obra' && tech.role !== 'Coordenador' &&
      tech.role !== 'Responsável de Fiscalização' && this.roles.find(role => role === tech.role) === undefined){
      errorMessage = 'Já existe um técnico com esse papel.'
    } else if (this.work.technicians.find(t => t.email === tech.email) !== undefined){
      errorMessage = 'Já existe um técnico para esse email.'
    } else if(this.work.technicians.find(t => t.association.name === tech.association.name && t.association.number === tech.association.number) !== undefined){
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
      dialogRef.afterClosed().subscribe(() => {});
      return false
    }
  }

  formatZipCode() {
    let value = this.work.address.postalCode.replace(/\D/g, '');
    if (value.length > 4) {
      value = `${value.slice(0, 4)}-${value.slice(4, 7)}`;
    }
    this.work.address.postalCode = value;
  }

  updateLocation(change: boolean) {
    const selectedParish = this.work.address.location.parish;
    const selectedCounty = this.work.address.location.county;
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
      this.work.address.location.county = cList[0];
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
    this.work.address.location.district = this.districts[0];
  }

  create() {
    const type = this.work.type;
    this.work.type = this.work.type.toUpperCase();
    this.httpService.createWork(this.work).pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        this.work.type = type
        return throwError(error);
      })
    ).subscribe(() => {
      console.log("Work Created!");
      this.navService.navWork()
      this.snackBar.openSnackBar('Obra criada com sucesso.');
    });
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

  checkResponsability(id: number) {
    const role = this.techCreationList.find(t => t.position === id)!.role;
    return role === 'Diretor de Obra' || role === 'Coordenador' || role === 'Responsável de Fiscalização';
  }

}
