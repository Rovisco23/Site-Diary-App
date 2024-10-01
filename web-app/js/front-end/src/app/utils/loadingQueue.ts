import {Injectable} from "@angular/core";

@Injectable({
  providedIn: 'root',
})
export class LoadingQueue {
  loadingQueue = 0;
  workLoadingQueue = 0;

  isWorkEmpty() {
    return this.workLoadingQueue === 0;
  }

  isEmpty() {
    return this.loadingQueue === 0;
  }

  addToLoadingQueue() {
    setTimeout(() => this.loadingQueue++);
  }

  removeFromLoadingQueue() {
    setTimeout(() => this.loadingQueue--);
  }

  addToWorkLoadingQueue() {
    setTimeout(() => this.workLoadingQueue++);
  }

  removeFromWorkLoadingQueue() {
    setTimeout(() => this.workLoadingQueue--);
  }
}
