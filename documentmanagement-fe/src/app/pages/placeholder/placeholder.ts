import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-placeholder',
  standalone: true,
  imports: [],
  templateUrl: './placeholder.html',
  styleUrl: './placeholder.css',
})
export class Placeholder {
  readonly title: string;

  constructor(route: ActivatedRoute) {
    this.title = route.snapshot.data['title'] || 'Nội dung';
  }
}
