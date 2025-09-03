import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ItemSetItem {
  id: string;
  tmpId: string;
  name: string;
  amount: number;
  unit: string;
}

export interface ItemSet {
  id: string;
  name: string;
  itemList: ItemSetItem[];
  receiptFileId: string;
}

export interface Recipe {
  id: string;
  name: string;
  creatorId: string;
  createdAt: string;
  itemSet: ItemSet;
  description: string;
  instructions: string[];
  categories: string[];
  visibility: 'PUBLIC' | 'SHARED' | 'PRIVATE';
  sharedWithUserIds: string[];
  receiptFileId: string;
}

@Injectable({
  providedIn: 'root'
})
export class RecipesService {
  private baseUrl = 'https://shopit-oracle.mooo.com:8443/api/recipe';

  constructor(private http: HttpClient) {}

  getRecipes(): Observable<Recipe[]> {
    const token = localStorage.getItem('auth_token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<Recipe[]>(this.baseUrl, { headers });
  }

  createRecipe(recipe: Recipe): Observable<Recipe> {
    const token = localStorage.getItem('auth_token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.post<Recipe>(this.baseUrl, recipe, {headers})
  }

  updateRecipe(recipe: Recipe): Observable<Recipe> {
    const token = localStorage.getItem('auth_token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.put<Recipe>(this.baseUrl+'/update', recipe, {headers})
  }

}