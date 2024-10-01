import { Component, Input } from '@angular/core';
import { Classes } from '../utils/classes';
import {RouterLink} from "@angular/router";
import {MatButton} from "@angular/material/button";
import {
  MatCard,
  MatCardActions,
  MatCardContent,
  MatCardHeader,
  MatCardSubtitle,
  MatCardTitle
} from "@angular/material/card";
import {MatDivider} from "@angular/material/divider";
import {MatIcon} from "@angular/material/icon";
import {NgClass, NgIf} from "@angular/common";

@Component({
  selector: 'app-work-listings',
  standalone: true,
  imports: [
    RouterLink,
    MatButton,
    MatCard,
    MatCardHeader,
    MatCardContent,
    MatDivider,
    MatCardActions,
    MatCardTitle,
    MatCardSubtitle,
    MatIcon,
    NgClass,
    NgIf
  ],
  templateUrl: './work-listings.component.html',
  styleUrl: './work-listings.component.css'
})
export class WorkListingsComponent {
  @Input() workListing!: Classes;
}
