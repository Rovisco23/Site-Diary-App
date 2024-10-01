import {Component, inject} from '@angular/core';
import {HttpService} from "../utils/http.service";
import {Password, User} from "../utils/classes";
import {ActivatedRoute, RouterLink, RouterLinkActive, RouterOutlet} from "@angular/router";
import {NgForOf, NgIf} from "@angular/common";
import {MatIcon} from "@angular/material/icon";
import {catchError, throwError} from "rxjs";
import {ErrorHandler} from "../utils/errorHandle";
import {NavigationService} from "../utils/navService";
import {OriginalUrlService} from "../utils/originalUrl.service";
import {concelhos, freguesias} from "../utils/utils";
import {AppComponent} from "../app.component";
import {FormsModule} from "@angular/forms";
import {ConfirmDialogComponent} from "../utils/dialogComponent";
import {MatDialog} from "@angular/material/dialog";
import {MatButton} from "@angular/material/button";

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    NgIf,
    MatIcon,
    FormsModule,
    NgForOf,
    MatButton
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent {
  httpService = inject(HttpService);
  user: User | undefined;

  profileSrc: string;
  form: FormData = new FormData()
  newFile: any = ''
  newSrc: string | undefined = undefined

  counties: string[] = [];
  parishes: string[] = [];
  districts: string[] = [];

  newName = '';
  newLastName = '';
  newUsername = '';
  newPhone: string | null = '';
  newPassword: Password = {
    passwordValue: '',
  };
  newAssociationName = '';
  newAssociationNumber = 0;
  newDistrict = '';
  newCounty = '';
  newParish = '';

  editPicture: boolean = false;
  editName: boolean = false;
  editPhone: boolean = false;
  editAssociation: boolean = false;
  editUsername: boolean = false;
  editLocation: boolean = false;
  editPassword: boolean = false;

  oldValue: any = undefined;

  constructor(
    private app: AppComponent,
    private route: ActivatedRoute,
    private urlService: OriginalUrlService,
    private errorHandle: ErrorHandler,
    private navService: NavigationService,
    private dialog: MatDialog
  ) {
    this.profileSrc = './assets/profile.png';
    const username = String(this.route.snapshot.params['name']);
    if (username !== localStorage.getItem('username')) this.navService.navProfile(localStorage.getItem('username') ?? '')
    this.loadUser(username);
    concelhos.forEach((value, key) => {
      value.forEach((v: string) => this.counties.push(v));
      this.districts.push(key);
    })
    freguesias.forEach((value) => {
      value.forEach((x: string) => this.parishes.push(x));
    })
  }

  loadUser(username: string) {
    this.httpService.getProfile(username).pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe((user: User) => {
      this.user = user;
      this.newName = user.firstName;
      this.newLastName = user.lastName;
      this.newUsername = user.username;
      this.newPhone = user.phone;
      this.newAssociationName = user.association.name;
      this.newAssociationNumber = user.association.number;
      this.newDistrict = user.location.district;
      this.newCounty = user.location.county;
      this.newParish = user.location.parish;
      if (user.role === 'ADMIN') {
        this.user.role = 'Admin';
      } else if (user.role === 'OPERÁRIO') {
        this.user.role = 'Operário';
      } else if (user.role === 'CÂMARA') {
        this.user.role = 'Câmara Municipal';
      }
    })
    this.httpService.getProfilePictureByUsername(username).pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe((data) => {
      if (data.size === 0) {
        this.profileSrc = './assets/profile.png'
      } else {
        localStorage.setItem('profilePicture', URL.createObjectURL(data))
        this.profileSrc = URL.createObjectURL(data)
      }
    })
  }

  updateLocation(change: boolean) {
    const selectedParish = this.newParish;
    const selectedCounty = this.newCounty;
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
      this.newCounty = cList[0];
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
    this.newDistrict = this.districts[0];
  }

  onImageChange(event: any) {
    this.newFile = event.target.files[0];
    const reader = new FileReader();

    reader.onload = () => {
      this.newSrc = reader.result as string;
    };

    if (!this.editPicture) this.toggleEdit('picture')
    reader.readAsDataURL(this.newFile);
  }

  onSubmitPicture() {
    this.form.append('file', this.newFile);
    this.httpService.changeProfilePicture(this.form).pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe(() => {
      this.toggleEdit('picture')
      this.profileSrc = this.newSrc ?? '';
      this.app.updatePicture(this.newSrc ?? '')
      this.newSrc = undefined;
    });
  }

  onRemovePicture() {
    this.httpService.changeProfilePicture(this.form).pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe(() => {
      this.toggleEdit('picture')
      this.profileSrc = './assets/profile.png';
      this.app.updatePicture('./assets/profile.png')
    });
  }

  isOwner() {
    return String(this.user?.id) === localStorage.getItem('userId');
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

  toggleEdit(field: string) {
    if (field === 'name') {
      this.editName = !this.editName;
    } else if (field === 'phone') {
      this.editPhone = !this.editPhone;
    } else if (field === 'picture') {
      this.editPicture = !this.editPicture;
    } else if (field === 'association') {
      this.editAssociation = !this.editAssociation;
    } else if (field === 'location') {
      this.editLocation = !this.editLocation;
    } else if (field === 'username') {
      this.editUsername = !this.editUsername;
    } else if (field === 'password') {
      this.editPassword = !this.editPassword;
    }
  }

  onCancelEdit(field: string) {
    if (field === 'picture') {
      this.toggleEdit(field);
      this.newSrc = undefined;
    } else if (field === 'name') {
      this.newName = this.user?.firstName ?? '';
      this.newLastName = this.user?.lastName ?? '';
      this.toggleEdit(field);
    } else if (field === 'phone') {
      this.newPhone = this.user?.phone ?? null;
      this.toggleEdit(field);
    } else if (field === 'association') {
      this.newAssociationName = this.user?.association.name ?? '';
      this.newAssociationNumber = this.user?.association.number ?? 0;
      this.toggleEdit(field);
    } else if (field === 'location') {
      this.newDistrict = this.user?.location.district ?? '';
      this.newCounty = this.user?.location.county ?? '';
      this.newParish = this.user?.location.parish ?? '';
      this.toggleEdit(field);
    } else if (field === 'username') {
      this.newUsername = this.user?.username ?? '';
      this.toggleEdit(field);
    } else if (field === 'password') {
      this.newPassword.passwordValue = '';
      this.toggleEdit(field);
    }
  }

  checkSameParams(field: string) {
    switch (field) {
      case 'name':
        return this.newName === this.user?.firstName && this.newLastName === this.user?.lastName;
      case 'phone':
        return this.newPhone === this.user?.phone;
      case 'association':
        return this.newAssociationName === this.user?.association.name && this.newAssociationNumber === this.user?.association.number;
      case 'location':
        return this.newDistrict === this.user?.location.district && this.newCounty === this.user?.location.county && this.newParish === this.user?.location.parish;
      case 'username':
        return this.newUsername === this.user?.username;
      default:
        return false;
    }
  }

  resetValues(field: string) {
    if (this.user) {
      if (field === 'name') {
        this.user.firstName = this.oldValue[0];
        this.user.lastName = this.oldValue[1];
      } else if (field === 'phone') {
        this.user.phone = this.oldValue;
      } else if (field === 'association') {
        this.user.association.name = this.oldValue[0];
        this.user.association.number = this.oldValue[1];
      } else if (field === 'location') {
        this.user.location.district = this.oldValue[0];
        this.user.location.county = this.oldValue[1];
        this.user.location.parish = this.oldValue[2];
      } else if (field === 'username') {
        this.user.username = this.oldValue;
      }
    }
  }

  onUpdateProfile(field: string) {
    if (this.user && !this.checkSameParams(field)) {
      this.updateField(field)
      this.httpService.editProfile(this.user).pipe(
        catchError(error => {
          this.resetValues(field);
          this.errorHandle.handleError(error);
          return throwError(error);
        })
      ).subscribe(() => {
        console.log("Edit Profile Finished");
        if (this.newUsername !== localStorage.getItem('username')) {
          localStorage.setItem('username', this.newUsername);
          this.navService.navProfile(this.newUsername)
        }
        this.toggleEdit(field)
        this.loadUser(this.newUsername)
      });
    } else {
      this.toggleEdit(field)
    }
  }

  private updateField(field: string) {
    if (this.user) {
      if (field === 'name') {
        this.oldValue = [this.user.firstName, this.user.lastName]
        this.user.firstName = this.newName
        this.user.lastName = this.newLastName
      } else if (field === 'phone') {
        this.oldValue = this.user.phone
        this.user.phone = this.newPhone
      } else if (field === 'association') {
        this.oldValue = [this.user.association.name, this.user.association.number]
        this.user.association.name = this.newAssociationName
        this.user.association.number = this.newAssociationNumber
      } else if (field === 'location') {
        this.oldValue = [this.user.location.district, this.user.location.county, this.user.location.parish]
        this.user.location.district = this.newDistrict
        this.user.location.county = this.newCounty
        this.user.location.parish = this.newParish
      } else if (field === 'username') {
        this.oldValue = this.user.username
        this.user.username = this.newUsername
      }
    }
  }

  onChangePassword() {
    if (this.checkPassword()){
      this.httpService.changePassword(this.newPassword).pipe(
        catchError(error => {
          this.errorHandle.handleError(error);
          return throwError(error);
        })
      ).subscribe(() => {
        this.toggleEdit('password')
      });
    }
  }

  private checkPassword() {
    let errorMessage: string | null = null;
    const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    if (!passwordPattern.test(this.newPassword.passwordValue)) {
      errorMessage = 'Password inválida. A password deve ter pelo menos 8 caracteres, uma letra maiúscula, uma letra minúscula, um número e um símbolo.';
    } else if (this.newPassword.passwordValue?.indexOf(this.user?.username ?? "") !== -1) {
      errorMessage = 'Password inválida. A password não pode conter o nome de utilizador.';
    }
    if (errorMessage === null){
      return true
    } else {
      const dialogRef = this.dialog.open(ConfirmDialogComponent, {
        data: {
          title: 'Erro',
          message: errorMessage,
        },
      });
      dialogRef.afterClosed().subscribe(() => {});
      return false;
    }
  }
}
