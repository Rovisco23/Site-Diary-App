import {Injectable} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {ConfirmDialogComponent} from "./dialogComponent";
import {NavigationService} from "./navService";

@Injectable({
  providedIn: 'root',
})
export class ErrorHandler {
  loginRequired = false;

  constructor(private dialog: MatDialog, private navService: NavigationService) {
  }

  handleError(error: any) {
    if (error.status === 401) {
      if (!this.loginRequired){
        this.loginRequired = true;
        this.handleErrorInternal(error, () => {
          this.loginRequired = false;
          localStorage.removeItem('userId');
          localStorage.removeItem('username');
          localStorage.removeItem('token');
          localStorage.removeItem('role');
          localStorage.removeItem('profilePicture');
          this.navService.navLogin()
        })
      }
    } else if (error.status === 404 || error.status === 400 || error.status === 403) {
      const nothing = () => {
      }
      this.handleErrorInternal(error, nothing)
    } else {
      this.handleErrorInternal({
        status: error.status,
        error: "Erro inesperado. Tente mais tarde."
      }, () => this.navService.navWork())
    }
  }

  private handleErrorInternal(error: any, onClose: () => any) {
    if (error.error instanceof Blob) {
      error.error.text().then((errorMessage: string) => {
        this.showErrorMessage(errorMessage, onClose)
      });
    } else {
      this.showErrorMessage(error.error, onClose)
    }
  }

  private showErrorMessage(message: string, onClose: () => void) {
    console.log('Showing error message:', message, status);  // Add this line
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '20%',
      height: 'auto',
      data: {
        title: 'Erro',
        message: message,
      },
    });

    dialogRef.afterClosed().subscribe(() => {
      onClose()
    });
  }

}
