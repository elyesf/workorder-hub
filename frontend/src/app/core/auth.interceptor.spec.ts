import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  let auth: AuthService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
  });

  afterEach(() => backend.verify());

  function logIn(): void {
    auth.login('a@b.de', 'secret123').subscribe();
    backend.expectOne('/api/auth/login').flush({
      token: 'abc',
      expiresAt: new Date(Date.now() + 60_000).toISOString(),
      email: 'a@b.de',
      displayName: 'A',
      role: 'USER',
    });
  }

  it('adds the bearer token to API calls after login', () => {
    logIn();

    http.get('/api/work-orders').subscribe();
    const req = backend.expectOne('/api/work-orders');
    expect(req.request.headers.get('Authorization')).toBe('Bearer abc');
    req.flush({});
  });

  it('sends no token before login', () => {
    http.get('/api/work-orders').subscribe({ error: () => undefined });
    const req = backend.expectOne('/api/work-orders');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('logs the user out when the API answers 401', () => {
    logIn();
    expect(auth.isLoggedIn()).toBeTrue();

    http.get('/api/work-orders').subscribe({ error: () => undefined });
    backend.expectOne('/api/work-orders').flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(auth.isLoggedIn()).toBeFalse();
  });
});
