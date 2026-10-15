import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../core/auth.service';
import { errorMessage } from '../../shared/problem';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly mode = signal<'login' | 'register'>('login');
  readonly error = signal<string | null>(null);
  readonly busy = signal(false);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    displayName: [''],
  });

  toggleMode(): void {
    this.mode.update((m) => (m === 'login' ? 'register' : 'login'));
    this.error.set(null);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { email, password, displayName } = this.form.getRawValue();
    if (this.mode() === 'register' && !displayName.trim()) {
      this.error.set('Please enter your name.');
      return;
    }
    this.busy.set(true);
    this.error.set(null);

    const request =
      this.mode() === 'login'
        ? this.auth.login(email, password)
        : this.auth.register(email, password, displayName);

    request.subscribe({
      next: () => this.router.navigate(['/work-orders']),
      error: (err) => {
        this.error.set(errorMessage(err));
        this.busy.set(false);
      },
    });
  }
}
