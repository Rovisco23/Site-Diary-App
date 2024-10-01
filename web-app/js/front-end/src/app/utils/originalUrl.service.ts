import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class OriginalUrlService {
  private originalUrl: string | undefined = undefined;

  setOriginalUrl(url: string) {
    this.originalUrl = url;
  }

  resetOriginalUrl() {
    this.originalUrl = undefined;
  }

  getOriginalUrl(): string | undefined {
    const url = this.originalUrl;
    this.originalUrl = undefined;
    return url;
  }
}
