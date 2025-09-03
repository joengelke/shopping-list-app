import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ShoppingItemActivity {
  id: string;
  listId: string;
  userId: string;
  itemId: string;
  name: string;
  amount: number;
  unit: string;
  timestamp: string; // or Date if you want to parse it
  actionType: string;
}

export interface ActivityByAmount {
  name: string,
  amount: number[],
  dates: string[]
}

@Injectable({
  providedIn: 'root'
})
export class AnalysisService {

  private baseUrl = 'https://shopit-oracle.mooo.com:8443/api/analytics'; //'https://shopit.ddnss.de:8443/api/analytics';

  constructor(private http: HttpClient) { }

  getActivityNames(
    shoppingListId: string
  ): Observable<string[]> {
    const params = new HttpParams().set('shoppingListId', shoppingListId);
    const token = localStorage.getItem('auth_token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.get<string[]>(`${this.baseUrl}/activity-names`, { params, headers });
  }

  getActivities(
    shoppingListId: string,
    userId?: string,
    name?: string,
    from?: Date,
    to?: Date
  ): Observable<ShoppingItemActivity[]> {
    let params = new HttpParams().set('shoppingListId', shoppingListId);
    if (userId) params = params.set('userId', userId);
    if (name) params = params.set('name', name)
    if (from) params = params.set('from', from.toISOString());
    if (to) params = params.set('to', to.toISOString());

    const token = localStorage.getItem('auth_token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.get<ShoppingItemActivity[]>(this.baseUrl, { params, headers });
  }

  getAverageAmount(
    activities: ShoppingItemActivity[],
    activityName: string,
    period: 'week' | 'month' | 'year'
  ): number | null {
    const filtered = activities.filter(a => a.name === activityName);
    if (!filtered.length) return null;
  
    const groups = new Map<string, number>();
    filtered.forEach(a => {
      const date = new Date(a.timestamp);
      let key = '';
      if (period === 'week') {
        key = `${date.getFullYear()}-W${this.getISOWeek(date)}`;
      } else if (period === 'month') {
        key = `${date.getFullYear()}-${date.getMonth() + 1}`;
      } else if (period === 'year') {
        key = `${date.getFullYear()}`;
      }
      groups.set(key, (groups.get(key) || 0) + a.amount);
    });
    return Array.from(groups.values()).reduce((a, b) => a + b, 0) / groups.size;
  }
  
  // Helper for ISO week number
  private getISOWeek(date: Date): number {
    const tmp = new Date(date.getTime());
    tmp.setHours(0, 0, 0, 0);
    tmp.setDate(tmp.getDate() + 4 - (tmp.getDay() || 7));
    const yearStart = new Date(tmp.getFullYear(), 0, 1);
    return Math.ceil((((tmp.getTime() - yearStart.getTime()) / 86400000) + 1) / 7);
  }

  getItemActivityByAmount(shoppingListId: string, activityName?: string, period?: string): Observable<ActivityByAmount[]> {
    let params = new HttpParams().set('shoppingListId', shoppingListId);
    if (activityName) params = params.set('name', activityName);
    if (period) params = params.set('period', period); // stacked up by daily, weekly, monthly ...
    const token = localStorage.getItem('auth_token');
    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });
    return this.http.get<ActivityByAmount[]>(`${this.baseUrl}/top-items`, { params, headers });
  }
}
