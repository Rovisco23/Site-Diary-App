import {Component, ElementRef, inject, ViewChild} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {HttpService} from "../utils/http.service";
import {LogEntry, SimpleFile} from "../utils/classes";
import {DatePipe, NgForOf, NgIf} from "@angular/common";
import {MatIcon} from "@angular/material/icon";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButton, MatFabButton, MatIconButton} from "@angular/material/button";
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
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {MatCheckbox} from "@angular/material/checkbox";
import {SelectionModel} from "@angular/cdk/collections";
import {ErrorHandler} from "../utils/errorHandle";
import {catchError, EMPTY, throwError} from "rxjs";
import {NavigationService} from "../utils/navService";
import {MatListOption, MatSelectionList} from "@angular/material/list";
import {MatDivider} from "@angular/material/divider";
import {ConfirmDialogComponent} from "../utils/dialogComponent";
import {MatDialog} from "@angular/material/dialog";
import {WorkDetailsComponent} from "../work-details/work-details.component";
import {MatMenu, MatMenuItem, MatMenuTrigger} from "@angular/material/menu";

@Component({
  selector: 'app-log-entry-details',
  standalone: true,
  imports: [
    NgIf,
    MatIcon,
    FormsModule,
    MatButton,
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatFabButton,
    MatFormField,
    MatHeaderCell,
    MatHeaderRow,
    MatHeaderRowDef,
    MatInput,
    MatLabel,
    MatRow,
    MatRowDef,
    MatTable,
    ReactiveFormsModule,
    MatHeaderCellDef,
    MatCheckbox,
    MatSelectionList,
    MatListOption,
    NgForOf,
    MatDivider,
    MatIconButton,
    MatMenuTrigger,
    MatMenu,
    MatMenuItem
  ],
  providers: [DatePipe],
  templateUrl: './log-entry-details.component.html',
  styleUrls: ['./log-entry-details.component.css']
})
export class LogEntryDetailsComponent {
  route: ActivatedRoute = inject(ActivatedRoute);
  httpService = inject(HttpService);
  form: FormData = new FormData()
  files: Map<string, File> = new Map<string, File>();
  log: LogEntry | undefined;
  logId: string = '';
  editDescription: boolean = false;

  displayedColumns: string[] = ['select', 'name', 'uploadDate'];
  dataSource = new MatTableDataSource<SimpleFile>();
  selection = new SelectionModel<SimpleFile>(true, []);
  newContent: string = '';
  @ViewChild('fileInput') fileInput: ElementRef<HTMLInputElement> | undefined;

  constructor(
    private workComponent: WorkDetailsComponent,
    private dialog: MatDialog,
    private errorHandle: ErrorHandler,
    private navService: NavigationService,
    private datePipe: DatePipe
  ) {
    this.workComponent.tabIndex = 0
    this.logId = String(this.route.snapshot.params['id']);
    this.loadLog();
  }

  loadLog() {
    this.httpService.getLogById(this.logId).pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return EMPTY;
      })
    ).subscribe((log: LogEntry) => {
      this.log = log;
      this.newContent = log.content;
      this.log.createdAt = this.datePipe.transform(log.createdAt, 'd MMMM y, HH:mm') ?? log.createdAt;
      this.log.modifiedAt = this.datePipe.transform(log.modifiedAt, 'd MMMM y, HH:mm') ?? log.modifiedAt
      this.dataSource.data = log.files;
    })
  }

  representFileDate(date: string) {
    return this.datePipe.transform(date, 'd MMMM y, HH:mm') ?? date
  }

  isEditable() {
    return this.log?.editable && this.log?.author.name == localStorage.getItem('username');
  }

  downloadFiles() {
    const downloadFiles = this.log?.files.filter(file => this.selection.isSelected(file));
    this.httpService.downloadFiles(this.logId, this.log!!.workId, downloadFiles!!).pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe((res) => {
      const link = document.createElement('a');
      link.href = URL.createObjectURL(res);
      link.download = 'files.zip';
      link.click();
    });
  }

  deleteFiles() {
    const filesToDelete = this.log?.files.filter(file => this.selection.isSelected(file));
    if (filesToDelete){
      this.httpService.deleteFiles(this.logId, this.log!!.workId, filesToDelete!!).pipe(
        catchError(error => {
          this.errorHandle.handleError(error);
          return throwError(error);
        })
      ).subscribe(() => {
        this.dataSource.data = this.dataSource.data.filter(file => !this.selection.isSelected(file));
        filesToDelete.forEach(file => this.files.delete(file.fileName))
        this.selection.clear();
      });
    }
  }

  onBackCall() {
    this.workComponent.loadWork(this.log?.workId || '');
    this.workComponent.showLayout = true;
    this.navService.navWorkDetails(this.log?.workId || '');
  }

  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.dataSource.data.length;
    return numSelected === numRows;
  }

  toggleAllRows() {
    if (this.isAllSelected()) {
      this.selection.clear();
      return;
    }
    this.selection.select(...this.dataSource.data);
  }

  checkboxLabel(row?: SimpleFile): string {
    if (!row) {
      return `${this.isAllSelected() ? 'deselect' : 'select'} all`;
    }
    return `${this.selection.isSelected(row) ? 'deselect' : 'select'} row ${row.id + 1}`;
  }

  toggleEditDescription(cancel: boolean = false) {
    if (cancel) {
      this.newContent = this.log?.content || '';
    }
    this.editDescription = !this.editDescription;
  }

  onEditLogCall(field: string) {
    if (this.log) {
      this.form.append("log", new Blob([JSON.stringify({
        description: this.newContent,
        workId: this.log.workId
      })], {type: 'application/json'}))
      this.files.forEach((file) => {
        if (!this.dataSource.data.find(f => f.fileName == file.name)) {
        this.form.append('files', file)
        }
      })
      this.httpService.editLog(this.form, this.logId).pipe(
        catchError(error => {
          this.errorHandle.handleError(error);
          return throwError(error);
        })
      ).subscribe(() => {
        this.loadLog()
        this.form = new FormData();
        if (field === 'Content') {
          this.log!.content = this.newContent;
          this.toggleEditDescription();
        }
      });
    }
  }

  onChangeValues(event: any, field: string) {
    if (field == 'Content') {
      this.newContent = event;
    }
  }

  onFileUpload(event: any) {
    if (event.target.files.length > 0) {
      const file: File = event.target.files[0];
      if (!this.files.has(file.name) && !this.dataSource.data.find(f => f.fileName == file.name)) {
        this.files.set(file.name, file);
        this.onEditLogCall('Files');
      } else {
        const dialogRef = this.dialog.open(ConfirmDialogComponent, {
          data: {
            title: 'Erro 400',
            message: 'Ficheiros não podem ter o mesmo nome no registo',
          },
        });
        dialogRef.afterClosed().subscribe(() => {});
      }
    }
    this.fileInput!.nativeElement.value = '';
  }

  openFileInput() {
    // Trigger click on file input element to open file selection dialog
    this.fileInput!.nativeElement.click();
  }

  onDeleteFile(id: number, name : string) {
    const fileRemoved = this.log?.files.filter(file => file.id == id && file.fileName == name)!;
    if (fileRemoved){
      this.httpService.deleteFiles(this.logId, this.log!!.workId, fileRemoved).pipe(
        catchError(error => {
          this.errorHandle.handleError(error);
          return throwError(error);
        })
      ).subscribe(() => {
        this.dataSource.data = this.dataSource.data.filter(file => fileRemoved[0].id != file.id);
        this.files.delete(fileRemoved[0].fileName)
      });
    }
  }

  onDownloadFile(id: number) {
    const fileDownloaded = this.log?.files.filter(file => file.id == id)!;
    this.httpService.downloadFiles(this.logId, this.log!!.workId, fileDownloaded).pipe(
      catchError(error => {
        this.errorHandle.handleError(error);
        return throwError(error);
      })
    ).subscribe((res) => {
      const link = document.createElement('a');
      link.href = URL.createObjectURL(res);
      link.download = 'file.zip';
      link.click();
    });
  }
}
