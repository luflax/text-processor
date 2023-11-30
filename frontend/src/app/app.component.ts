import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';

import { HttpClient, HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { GraphRender } from './graph';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, HttpClientModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, AfterViewInit {
  title = 'graphvisualizer';

  @ViewChild("mysvg") svg: any;

  fileName: string = "";

  width = 1366;
  height = 768;

  constructor(private http: HttpClient) {

  }

  ngOnInit(): void {
  }


  ngAfterViewInit(): void {
    
  }

  loadGraph(visualizationType: string) {
    switch (visualizationType) {
      case "TOP_3":
        this.http.get<any>("http://localhost:8081/api/graphs/topWords?fileName=" + this.fileName + "&numberOfWords=3").subscribe(data => {
          GraphRender.loadGraph(this.svg.nativeElement, data);
        });
        break;
      case "TOP_10":
        this.http.get<any>("http://localhost:8081/api/graphs/topWords?fileName=" + this.fileName + "&numberOfWords=10").subscribe(data => {
          GraphRender.loadGraph(this.svg.nativeElement, data);
        });
        break;
      case "ALL_WORDS":
        this.http.get<any>("http://localhost:8081/api/graphs?fileName=" + this.fileName).subscribe(data => {
          GraphRender.loadGraph(this.svg.nativeElement, data);
        });
        break;
      case "COAUTORIA":
        this.http.get<any>("http://localhost:8081/api/graphs/coautoria").subscribe(data => {
          GraphRender.loadGraph(this.svg.nativeElement, data);
        });
        break;
      default:
        break;
    }
  }
}

