import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Title } from '@angular/platform-browser';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';

import { AuthService } from '../../core/auth/auth';
import { RecipesComponent } from "../recipes/recipes";
import { AnalyticsComponent } from "../analytics/analytics";

@Component({
  standalone: true,
  selector: 'app-overview',
  templateUrl: './overview.html',
  styleUrl: './overview.scss',
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatCardModule,
    RecipesComponent,
    AnalyticsComponent
  ]
})
export class OverviewComponent implements OnInit {
  activeSection: string = 'home';

  constructor(
    private router: Router,
    private titleService: Title,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.titleService.setTitle('ShopIt | Start');
  }

  setSection(section: string) {
    this.activeSection = section;
  }

  goToOverview() {
    this.activeSection = 'home';
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login'], { replaceUrl: true });
  }
}
