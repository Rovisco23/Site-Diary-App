import {Component, inject} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {RouterLink, RouterLinkActive} from "@angular/router";
import {HttpService} from "../utils/http.service";
import {HttpClientModule} from "@angular/common/http";
import {MatButton} from "@angular/material/button";
import {NgForOf, NgIf} from "@angular/common";
import {concelhos, freguesias} from "../utils/utils";
import {catchError, throwError} from "rxjs";
import {ErrorHandler} from "../utils/errorHandle";
import {NavigationService} from "../utils/navService";
import {SnackBar} from "../utils/snackBarComponent";
import {MatIcon} from "@angular/material/icon";
import {MatDialog} from "@angular/material/dialog";
import {ConfirmDialogComponent} from "../utils/dialogComponent";

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    MatButton,
    RouterLink,
    RouterLinkActive,
    NgForOf,
    MatIcon,
    NgIf
  ],
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.css',
  providers: [HttpService]
})

export class SignUpComponent {
  email: string = '';
  username: string = '';
  password: string = '';
  firstName: string = '';
  lastName: string = '';
  nif: string = '';
  phone: string = '';
  parish: string = '';
  county: string = '';
  district: string = '';
  role: string = 'OPERÁRIO';
  association_name: string = '';
  association_num: string = '';
  passwordVisible: boolean = false;


  isChecked: boolean = false;

  districts: string[] = [];
  counties: string[] = [];
  parishes: string[] = [];

  httpService = inject(HttpService);

  constructor(
    private errorHandle: ErrorHandler,
    private navService: NavigationService,
    private snackBar: SnackBar,
    private dialog: MatDialog
  ) {
    concelhos.forEach((value, key) => {
      value.forEach((v: string) => this.counties.push(v));
      this.districts.push(key);
    })
    freguesias.forEach((value) => {
      value.forEach((x: string) => this.parishes.push(x));
    })
  }

  changeRole(): void {
    this.role = this.isChecked ? 'CÂMARA' : 'OPERÁRIO';
  }

  togglePasswordVisibility() {
    this.passwordVisible = !this.passwordVisible;
    const passwordInput = document.getElementById('password') as HTMLInputElement;
    if (this.passwordVisible) {
      passwordInput.type = 'text';
    } else {
      passwordInput.type = 'password';
    }
  }

  updateLocation(change: boolean) {
    const selectedParish = this.parish;
    const selectedCounty = this.county;
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
      this.county = cList[0];
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
    this.district = this.districts[0];
  }

  signUp(): void {
    if (this.fieldChecker()){
      this.httpService.signup(this.email, this.username, this.password, this.firstName, this.lastName, Number(this.nif),
        this.phone, this.parish, this.county, this.district, this.role, this.association_name, Number(this.association_num)).pipe(
        catchError(error => {
          this.errorHandle.handleError(error);
          return throwError(error);
        })
      ).subscribe(() => {
        console.log("Sign Up Finished");
        this.navService.navLogin()
        this.snackBar.openSnackBar('Conta criada com sucesso.');
      })
    }
  }

  fieldChecker(): boolean {
    let errorMessage: string | null = null;
    const numericPattern = /^\d+$/;
    const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(this.email)) {
      errorMessage = 'Email inválido.'
    } else if (!passwordPattern.test(this.password)) {
      errorMessage = 'Password inválida. A password deve ter pelo menos 8 caracteres, uma letra maiúscula, uma letra minúscula, um número e um símbolo.';
    } else if (this.password.indexOf(this.username) !== -1) {
      errorMessage = 'Password inválida. A password não pode conter o nome de utilizador.';
    } else if ((this.phone.length !== 9 || !numericPattern.test(this.phone)) && (this.phone.length !== 0)) {
      errorMessage = 'Número de telefone inválido. Número de telefone deve ter apenas 9 digitos.';
    } else if (this.nif.length !== 9 || !numericPattern.test(this.nif)) {
      errorMessage = 'NIF inválido. NIF deve ter apenas 9 digitos.';
    } else if (!numericPattern.test(this.association_num) || Number(this.association_num) < 1 ) {
      errorMessage = 'Número de associação inválido.';
    }
    if (errorMessage) {
      const dialogRef = this.dialog.open(ConfirmDialogComponent, {
        data: {
          title: 'Erro',
          message: errorMessage,
        },
      });
      dialogRef.afterClosed().subscribe(() => {});
      return false;
    } else{
      return true;
    }
  }

  onLoginClick(): void {
    this.navService.navLogin()
  }
}
