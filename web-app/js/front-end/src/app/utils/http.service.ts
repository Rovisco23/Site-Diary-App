import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {AnswerInvite, Classes, EditWorkInputModel, InputWork, Password, SimpleFile, User} from "./classes";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root',
})
export class HttpService {

  private readonly server = 'http://127.0.0.1:8080/api'

  constructor(private http: HttpClient) {
  }

  workListingsList: Classes[] = [];

  getTokenHeader() {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  login(user: string, password: string): Observable<any> {
    return this.http.post<any>(this.server + '/login', {user, password})
  }

  logout(token: string): Observable<any> {
    return this.http.post<any>(this.server + '/logout', {token})
  }

  signup(email: string, username: string, password: string, firstName: string, lastName: string, nif: number,
         phone: string, parish: string, county: string, district: string, role: string, associationName: string, associationNum: number)
    : Observable<any> {
    return this.http.post<any>(this.server + '/signup', {
      email,
      username,
      password,
      firstName,
      lastName,
      nif,
      phone,
      parish,
      county,
      district,
      role,
      associationName,
      associationNum
    })
  }

  checkToken(): Observable<any> {
    return this.http.get<any>(this.server + '/checkToken')
  }

  getWorkListings(): Observable<any> {
    const headers = this.getTokenHeader();
    return this.http.get<Classes[]>(this.server + '/work?skip=0', {headers: headers})
  }

  getWorkById(id: string): Observable<any> {
    const headers = this.getTokenHeader();
    return this.http.get<Classes>(this.server + `/work/${id}`, {headers: headers})
  }

  getProfile(username: string): Observable<any> {
    const headers = this.getTokenHeader();
    return this.http.get<any>(this.server + `/users/username/${username}`, {headers: headers})
  }

  editProfile(user: User) {
    const id = localStorage.getItem('userId');
    const headers = this.getTokenHeader();
    return this.http.put<any>(this.server + `/users/${id}`, {
      username: user.username,
      firstName: user.firstName,
      lastName: user.lastName,
      phone: user.phone,
      location: user.location,
      association: user.association
    }, {headers: headers});
  }

  createWork(work: InputWork): Observable<any> {
    const headers = this.getTokenHeader();
    return this.http.post<any>(this.server + '/work', work, {headers: headers})
  }

  createLogEntry(logEntry: FormData): Observable<any> {
    const headers = this.getTokenHeader()
    return this.http.post<any>(this.server + '/logs', logEntry, {headers: headers, observe: 'response'})
  }

  getProfilePicture() {
    const headers = this.getTokenHeader();
    return this.http.get<any>(this.server + '/profile-picture', {
      headers: headers,
      responseType: 'blob' as 'json'
    })
  }

  getProfilePictureByUsername(username: string) {
    const headers = this.getTokenHeader();
    return this.http.get<any>(this.server + `/profile-picture-username/${username}`, {
      headers: headers,
      responseType: 'blob' as 'json'
    })
  }

  inviteMembers(workId: string, invites: any): Observable<any> {
    const headers = this.getTokenHeader();
    return this.http.post<any>(this.server + `/invite/${workId}`, invites, {headers: headers})
  }

  getInviteList(): Observable<any> {
    const headers = this.getTokenHeader();
    return this.http.get<any>(this.server + '/invite', {headers: headers})
  }

  getInvite(id: string): Observable<any> {
    const headers = this.getTokenHeader();
    return this.http.get<any>(this.server + `/invite/${id}`, {headers: headers})
  }

  answerInvite(answer: AnswerInvite): Observable<any> {
    const headers = this.getTokenHeader();
    return this.http.put<any>(this.server + '/invite', answer, {headers: headers})
  }

  changeProfilePicture(form: FormData) {
    const headers = this.getTokenHeader();
    return this.http.put<any>(this.server + '/profile-picture', form, {headers: headers})
  }

  getWorkImage(workId: string): Observable<any> {
    const headers = this.getTokenHeader();
    return this.http.get<any>(this.server + `/work-image/${workId}`, {
      headers: headers,
      responseType: 'blob' as 'json'
    })
  }

  getLogById(logId: string): Observable<any> {
    const headers = this.getTokenHeader();
    return this.http.get<any>(this.server + `/logs/${logId}`, {headers: headers})
  }

  editLog(log: FormData, logId: string): Observable<any> {
    const headers = this.getTokenHeader();
    return this.http.put<any>(this.server + `/logs/${logId}`, log, {headers: headers, observe: 'response'})
  }

  getPendingUsers() {
    const headers = this.getTokenHeader();
    return this.http.get<any>(this.server + '/pending', {headers: headers})
  }

  answerPending(userId: number, accepted: boolean) {
    const headers = this.getTokenHeader();
    return this.http.put<any>(this.server + '/pending', {userId, accepted}, {headers: headers})
  }

  getAllUsers() {
    const headers = this.getTokenHeader();
    return this.http.get<any>('http://localhost:8080/api/users', {headers: headers})
  }

  downloadFiles(logId: string, workId: string, downloadFiles: SimpleFile[]) {
    const headers = this.getTokenHeader();
    return this.http.post<any>(this.server + '/logs-files',
      {
        logId: logId,
        workId: workId,
        files: downloadFiles
      },
      {
        headers: headers,
        responseType: 'blob' as 'json'
      }
    )
  }

  getNumberOfInvites() {
    const headers = this.getTokenHeader();
    return this.http.get<any>(this.server + '/invite-number', {headers: headers})
  }

  deleteFiles(logId: string, workId: string, filesToDelete: SimpleFile[]) {
    const headers = this.getTokenHeader();
    return this.http.post<any>(this.server + '/delete-files',
      {
        logId: logId,
        workId: workId,
        files: filesToDelete
      },
      {
        headers: headers,
        responseType: 'blob' as 'json'
      })
  }

  getWorksPending() {
    const headers = this.getTokenHeader();
    return this.http.get<any>(this.server + '/work-pending', {headers: headers})
  }

  answerPendingWork(workId: string, answer: boolean) {
    const headers = this.getTokenHeader();
    return this.http.put<any>(this.server + `/work-pending/${workId}`, answer, {headers: headers})
  }

  finishWork(work: string) {
    const headers = this.getTokenHeader();
    return this.http.post<any>(this.server + `/finish-work?work=${work}`, {}, {headers: headers})
  }

  getMyLogs() {
    const headers = this.getTokenHeader();
    return this.http.get<any>(this.server + '/my-logs', {headers: headers})
  }

  getMemberWorkProfile(workId: string, username: string) {
    const headers = this.getTokenHeader();
    return this.http.get<any>(this.server + `/member-profile/${workId}/${username}`, {headers: headers})
  }

  editWork(workId: string, work: EditWorkInputModel) {
    const headers = this.getTokenHeader();
    return this.http.put<any>(this.server + `/work/edit/${workId}`, work, {headers: headers})
  }

  changeWorkImage(id: string, form: FormData) {
    const headers = this.getTokenHeader();
    return this.http.put<any>(this.server + `/work-image/${id}`, form, {headers: headers})
  }

  askVerification(id: string, doc: string) {
    const headers = this.getTokenHeader();
    return this.http.put<any>(this.server + `/work-verification`, {
      workId: id,
      verificationDoc: doc
    }, {headers: headers})
  }

  changePassword(password: Password) {
    const headers = this.getTokenHeader();
    return this.http.put<any>(this.server + '/change-password', password, {headers: headers})
  }

  getOpeningTerm(id: string) {
    const headers = this.getTokenHeader();
    return this.http.get<any>(this.server + `/opening-term/${id}`, {
      headers: headers,
      responseType: 'blob' as 'json'
    })
  }

  getSiteDiary(workId: string) {
    const headers = this.getTokenHeader();
    return this.http.get<any>(this.server + `/site-diary/${workId}`, {
      headers: headers,
      responseType: 'blob' as 'json'
    })
  }
}
