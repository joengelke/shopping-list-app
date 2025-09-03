import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}


@Injectable({
  providedIn: 'root'
})
export class AuthService {
private baseUrl = 'https://shopit-oracle.mooo.com:8443/api/'//'https://shopit.ddnss.de:8443/api/';
                  //'https://192.168.1.38:8443/api/'

private tokenKey = 'auth_token'

  constructor(private http: HttpClient) { }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}auth/login`, credentials)
      .pipe(
        tap(response => {
          localStorage.setItem(this.tokenKey, response.token);
        })
      );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
  }
}
