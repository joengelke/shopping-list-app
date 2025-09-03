import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface ShoppingList {
  id: string;
  name: string;
  userIds: string[];
}

@Injectable({
  providedIn: 'root'
})
export class ShoppingListService {
  private baseUrl ='https://shopit-oracle.mooo.com:8443/api/shoppinglist'//'https://shopit.ddnss.de:8443/api/shoppinglist';// 'https://192.168.1.38:8443/api/shoppinglist'

  constructor(private http: HttpClient) { }

  getShoppingLists(): Observable<ShoppingList[]> {
    const token = localStorage.getItem('auth_token');
    const headers = { 'Authorization': `Bearer ${token}` };

    return this.http.get<ShoppingList[]>(this.baseUrl, { headers });
  }
}
