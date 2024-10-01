import {Component, ElementRef, ViewChild} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {NgIf} from "@angular/common";
import {MatIcon} from "@angular/material/icon";
import {MatButton} from "@angular/material/button";
import {ActivatedRoute} from "@angular/router";
import {HttpService} from "../utils/http.service";
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderCellDef,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow,
  MatRowDef,
  MatTable,
  MatTableDataSource
} from "@angular/material/table";
import {HttpResponse} from "@angular/common/http";
import {catchError, throwError} from "rxjs";
import {ErrorHandler} from "../utils/errorHandle";
import {NavigationService} from "../utils/navService";
import {MatCheckbox} from "@angular/material/checkbox";
import {MatDivider} from "@angular/material/divider";
import {SimpleFile} from "../utils/classes";
import {SelectionModel} from "@angular/cdk/collections";
import {ConfirmDialogComponent} from "../utils/dialogComponent";
import {MatDialog} from "@angular/material/dialog";
import {WorkDetailsComponent} from "../work-details/work-details.component";
import {SnackBar} from "../utils/snackBarComponent";

@Component({
  selector: 'app-create-log-entry',
  templateUrl: './create-log-entry.component.html',
  styleUrls: ['./create-log-entry.component.css'],
  standalone: true,
  imports: [
    MatColumnDef,
    MatHeaderCell,
    MatCell,
    MatHeaderRow,
    MatRow,
    MatRowDef,
    MatHeaderRowDef,
    MatCellDef,
    MatHeaderCellDef,
    MatCheckbox,
    MatTable,
    MatDivider,
    FormsModule,
    MatIcon,
    MatButton,
    NgIf
  ],
  providers: [HttpService, ErrorHandler, NavigationService] // Ensure providers are correctly set
})
export class CreateLogEntryComponent {
  @ViewChild('fileInput') fileInput: ElementRef<HTMLInputElement> | undefined;

  workId: string = ''; // Initialize to empty string
  form: FormData = new FormData();
  description: string = '';
  files: Map<string, File> = new Map<string, File>();
  displayedColumns: string[] = ['select', 'name'];
  dataSource = new MatTableDataSource<SimpleFile>(); // Use SimpleFile type for dataSource
  selection = new SelectionModel<SimpleFile>(true, []); // Use SimpleFile type for SelectionModel

  constructor(
    private route: ActivatedRoute,
    private httpService: HttpService,
    private errorHandle: ErrorHandler,
    private navService: NavigationService,
    private workComponent: WorkDetailsComponent,
    private dialog: MatDialog,
    private snackBar: SnackBar
  ) {
    this.workComponent.tabIndex = 0;
    const parentRoute = this.route.parent;
    if (parentRoute) {
      const parentId = parentRoute.snapshot.paramMap.get('id');
      this.workId = parentId || '';
    }
  }

  onSubmit() {
    this.form.append('log', new Blob([JSON.stringify({
      description: this.description,
      workId: this.workId
    })], { type: 'application/json' }));

    this.files.forEach((file) => {
      this.form.append('files', file);
    });

    this.httpService.createLogEntry(this.form).pipe(
      catchError(error => {
        this.form.delete('log');
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe((response: HttpResponse<any>) => {
      const headers = response.headers.get("Location") || '';
      this.navService.navUrl('work-details/'+ this.workId +'/'+headers);
      this.snackBar.openSnackBar('Registo criado com sucesso.');
    });
  }

  onFileUpload(event: any) {
    if (event.target.files.length > 0) {
      const file: File = event.target.files[0];
      if (!this.files.has(file.name) && !this.dataSource.data.find(f => f.fileName == file.name)) {
        this.files.set(file.name, file);
        this.updateDataSource();
      } else {
        const dialogRef = this.dialog.open(ConfirmDialogComponent, {
          data: {
            title: 'Erro',
            message: 'Ficheiros não podem ter o mesmo nome no registo',
          },
        });
        dialogRef.afterClosed().subscribe(() => {});
      }
    }
    this.fileInput!.nativeElement.value = '';

  }

  isAllSelected(): boolean {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.length;
    return numSelected === numRows;
  }

  toggleAllRows() {
    if (this.isAllSelected()) {
      this.selection.clear();
    } else {
      this.dataSource.data.forEach(row => this.selection.select(row));
    }
  }

  checkboxLabel(row?: SimpleFile): string {
    if (!row) {
      return `${this.isAllSelected() ? 'deselect' : 'select'} all`;
    }
    return `${this.selection.isSelected(row) ? 'deselect' : 'select'} row ${row.fileName}`;
  }

  onBackCall() {
    this.workComponent.loadWork(this.workId)
    this.workComponent.showLayout = true;
    this.navService.navWorkDetails(this.workId);
  }

  openFileInput() {
    if (this.fileInput) {
      this.fileInput.nativeElement.click();
    }
  }

  deleteFiles() {
    this.selection.selected.forEach(file => {
      this.files.delete(file.fileName);
    });
    this.updateDataSource();
    this.selection.clear();
  }

  private updateDataSource() {
    this.dataSource.data = Array.from(this.files.values()).map(file => ({
      fileName: file.name
    } as SimpleFile));
  }

  changeDescription(event: any) {
    this.description = event
  }
}
