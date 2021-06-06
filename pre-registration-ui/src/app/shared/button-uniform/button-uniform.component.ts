import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-button-uniform',
  templateUrl: './button-uniform.component.html',
  styleUrls: ['./button-uniform.component.css']
})
export class ButtonUniformComponent implements OnInit {
  @Input()
  width: string;

  @Input()
  disable: boolean;

  @Input()
  text: string;

  @Input()
  disabled: boolean

  constructor() { }

  ngOnInit() {
  }

}
