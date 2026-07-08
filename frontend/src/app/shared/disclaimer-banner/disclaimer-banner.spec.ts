import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DisclaimerBanner } from './disclaimer-banner';

describe('DisclaimerBanner', () => {
  let component: DisclaimerBanner;
  let fixture: ComponentFixture<DisclaimerBanner>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DisclaimerBanner],
    }).compileComponents();

    fixture = TestBed.createComponent(DisclaimerBanner);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
