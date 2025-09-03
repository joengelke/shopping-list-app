import { Injectable } from '@angular/core';
import { TokenManagerService } from '../auth/token-manager';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  
  constructor(private tokenManagerService: TokenManagerService) { }

  getCurrentUserId(): string | null {
    return this.tokenManagerService.getUserId()
  }
}
