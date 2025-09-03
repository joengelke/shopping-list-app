import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TokenManagerService {

  getToken(): string | null {
    return localStorage.getItem('auth_token')
  }

  getPayload(): any | null {
    const token = this.getToken();
    if (!token) return null;
    const payload = token.split('.')[1];
    try {
      return JSON.parse(atob(payload));
    } catch {
      return null;
    }
  }

  getUsername(): string | null {
    const payload = this.getPayload();
    return payload?.sub ?? null;
  }

  getUserId(): string | null {
    const payload = this.getPayload();
    return payload?.userId ?? null; 
  }
}
