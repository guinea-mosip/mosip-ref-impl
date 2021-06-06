import { Component, OnInit, OnChanges, Input } from '@angular/core';

@Component({
  selector: 'app-stepper',
  templateUrl: './stepper.component.html',
  styleUrls: ['./stepper.component.css']
})
export class StepperComponent implements OnInit, OnChanges {
  @Input() componentName: string;

  classes = {
    step0: {
      div: []
    },
    step1: {
      a: [],
      icon: [],
      line: []
    },
    step2: {
      a: [],
      icon: [],
      line: []
    },
    step3: {
      a: [],
      icon: [],
      line: []
    },
    step4: {
      a: [],
      icon: []
    }
  };

  constructor() {}

  ngOnInit() {}

  ngOnChanges() {
    if (this.componentName === 'DemographicComponent') {
      this.classes.step0.div = ['container'];
      this.classes.step1.a = ['active'];
      this.classes.step1.icon = ['inline-icon', 'inline-icon-background-active'];
      this.classes.step1.line = ['progress', 'progress-active'];
      this.classes.step2.a = ['incomplete'];
      this.classes.step2.icon = ['inline-icon', 'inline-icon-background-incomplete'];
      this.classes.step2.line = ['progress', 'progress-incomplete'];
      this.classes.step3.a = ['incomplete'];
      this.classes.step3.icon = ['inline-icon', 'inline-icon-background-incomplete'];
      this.classes.step3.line = ['progress', 'progrcompleteess-incomplete'];
      this.classes.step4.a = ['incomplete'];
      this.classes.step4.icon = ['inline-icon', 'inline-icon-background-incomplete'];
    } else if (this.componentName === 'FileUploadComponent' || this.componentName === 'PreviewComponent') {
      this.classes.step0.div = ['upload'];
      this.classes.step1.a = ['complete'];
      this.classes.step1.icon = ['inline-icon', 'inline-icon-background-complete'];
      this.classes.step1.line = ['progress', 'progress-complete'];
      this.classes.step2.a = ['active'];
      this.classes.step2.icon = ['inline-icon', 'inline-icon-background-active'];
      this.classes.step2.line = ['progress', 'progress-active'];
      this.classes.step3.a = ['incomplete'];
      this.classes.step3.icon = ['inline-icon', 'inline-icon-background-incomplete'];
      this.classes.step3.line = ['progress', 'progress-incomplete'];
      this.classes.step4.a = ['incomplete'];
      this.classes.step4.icon = ['inline-icon', 'inline-icon-background-incomplete'];
    } else if (this.componentName === 'CenterSelectionComponent' || this.componentName === 'TimeSelectionComponent') {
      this.classes.step0.div = ['rdv'];
      this.classes.step1.a = ['complete'];
      this.classes.step1.icon = ['inline-icon', 'inline-icon-background-complete'];
      this.classes.step1.line = ['progress', 'progress-complete'];
      this.classes.step2.a = ['complete'];
      this.classes.step2.icon = ['inline-icon', 'inline-icon-background-complete'];
      this.classes.step2.line = ['progress', 'progress-complete'];
      this.classes.step3.a = ['active'];
      this.classes.step3.icon = ['inline-icon', 'inline-icon-background-active'];
      this.classes.step3.line = ['progress', 'progress-active'];
      this.classes.step4.a = ['incomplete'];
      this.classes.step4.icon = ['inline-icon', 'inline-icon-background-incomplete'];
    } else if (this.componentName === 'AcknowledgementComponent') {
      this.classes.step0.div = ['confirmation-rdv'];
      this.classes.step1.a = ['complete'];
      this.classes.step1.icon = ['inline-icon', 'inline-icon-background-complete'];
      this.classes.step1.line = ['progress', 'progress-complete'];
      this.classes.step2.a = ['complete'];
      this.classes.step2.icon = ['inline-icon', 'inline-icon-background-complete'];
      this.classes.step2.line = ['progress', 'progress-complete'];
      this.classes.step3.a = ['complete'];
      this.classes.step3.icon = ['inline-icon', 'inline-icon-background-complete'];
      this.classes.step3.line = ['progress', 'progress-complete'];
      this.classes.step4.a = ['active'];
      this.classes.step4.icon = ['inline-icon', 'inline-icon-background-complete'];
    }
  }
}
