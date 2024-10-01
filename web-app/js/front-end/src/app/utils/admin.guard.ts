import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {NavigationService} from "./navService";

@Injectable({
  providedIn: 'root'
})
export class AdminGuard {
  constructor(private router: Router, private navService: NavigationService) {
  }

  canActivate() {
    if (localStorage.getItem('role') === 'ADMIN') {
      return true;
    }
    this.navService.navWork()
    return false;
  }
}
