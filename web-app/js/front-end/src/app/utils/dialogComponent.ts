import { Component, Inject, ViewEncapsulation } from '@angular/core';
import {
  MatDialogRef,
  MAT_DIALOG_DATA,
  MatDialogContent,
  MatDialogActions,
  MatDialogClose,
  MatDialogTitle
} from '@angular/material/dialog';
import { MatButton } from "@angular/material/button";
import { NgIf } from "@angular/common";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";

@Component({
  selector: 'app-confirm-dialog',
  template: `
    <div class="error-dialog">
      <h2 mat-dialog-title style="color: #FF7A00">{{ data.title }}</h2>
      <mat-dialog-content style="color: white">
        <p>{{ data.message }}</p>
      </mat-dialog-content>
      <div class="input-container">
        <input *ngIf="data.title.includes('Pedir verificação')" type="text" id="inputValue" name="inputValue" placeholder="Doc. de verificação" [(ngModel)]="inputValue" required>
      </div>
      <mat-dialog-actions style="justify-content: center">
        <button *ngIf="!(data.title.includes('Erro'))" class="submit-button" mat-button mat-dialog-close>Cancelar
        </button>
        <button class="submit-button" mat-button (click)="confirm()">Ok</button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .error-dialog {
      min-width: 300px; /* Defina a largura mínima desejada */
      min-height: 200px; /* Defina a altura mínima desejada */
      max-width: 400px; /* Defina a largura máxima desejada */
      max-height: 300px; /* Defina a altura máxima desejada */
      overflow: auto; /* Adicione scrollbars se o conteúdo for maior que a altura máxima */
      background-color: #11113D;
    }

    .input-container {
      display: flex;
      justify-content: center;
      align-items: center;
      margin: 15px 0; /* Ajuste a margem conforme necessário */
    }

    input {
      height: 20px;
      width: 50%;
      max-width: 200px;
      border: 1px solid #11113D;
      border-radius: 5px;
      padding: 5px 5px;
    }

    .submit-button {
      color: #FF7A00 !important;
      border: 1px solid #FF7A00;
      background-color: #11113D;
    }

    .submit-button:hover {
      color: white !important;
      background-color: #FF7A00;
    }
  `],
  standalone: true,
  imports: [
    MatDialogActions,
    MatDialogContent,
    MatDialogTitle,
    NgIf,
    MatButton,
    MatDialogClose,
    ReactiveFormsModule,
    FormsModule
  ],
  encapsulation: ViewEncapsulation.None
})
export class ConfirmDialogComponent {
  inputValue: string;

  constructor(
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.inputValue = data.inputValue; // Inicializa inputValue com o valor passado
  }

  confirm() {
    if (this.data.title.includes('Pedir verificação')) {
      this.dialogRef.close(this.inputValue); // Return inputValue if title includes 'Pedir verificação'
    } else {
      this.dialogRef.close(true); // Return true otherwise
    }
  }
}
