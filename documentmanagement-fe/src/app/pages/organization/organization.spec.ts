import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { Organization } from './organization';

describe('Organization', () => {
  let component: Organization;
  let fixture: ComponentFixture<Organization>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Organization],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Organization);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
