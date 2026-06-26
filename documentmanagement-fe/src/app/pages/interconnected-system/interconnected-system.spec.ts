import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { InterconnectedSystem } from './interconnected-system';

describe('InterconnectedSystem', () => {
  let component: InterconnectedSystem;
  let fixture: ComponentFixture<InterconnectedSystem>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InterconnectedSystem],
      providers: [provideHttpClient(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(InterconnectedSystem);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
