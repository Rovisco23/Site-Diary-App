import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {NavigationService} from "./navService";

@Injectable({
  providedIn: 'root'
})
export class CouncilGuard {
  constructor(private router: Router, private navService: NavigationService) {
  }

  canActivate() {
    if (localStorage.getItem('role') === 'ADMIN' || localStorage.getItem('role') === 'CÂMARA') {
      return true;
    }
    this.navService.navWork()
    return false;
  }
}
