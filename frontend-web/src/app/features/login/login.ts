import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService, LoginRequest } from '../../core/auth/auth';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-login',
  imports: [FormsModule, CommonModule, MatInputModule, MatButtonModule, MatCardModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class LoginComponent implements OnInit {
  credentials: LoginRequest = { username: '', password: ''}
  showErrorMessage = false;

  constructor(private router: Router, private titleService: Title, private authService: AuthService) {}

  ngOnInit() {
    this.titleService.setTitle('ShopIt | Login');
    // Check if user is already logged in
    if (localStorage.getItem('auth_token')) {
      this.router.navigate(['/overview'], { replaceUrl: true });
    }
  }

  login() {
    this.authService.login(this.credentials).subscribe({
      next:() => {
        this.router.navigate(['/overview']);
      },
      error: () => {
        this.showErrorMessage = true;
      }
    });
  }  
}
