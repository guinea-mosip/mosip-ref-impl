import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ButtonUniformComponent } from './button-uniform.component';

describe('ButtonUniformComponent', () => {
  let component: ButtonUniformComponent;
  let fixture: ComponentFixture<ButtonUniformComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ButtonUniformComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ButtonUniformComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
