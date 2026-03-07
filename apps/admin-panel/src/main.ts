import 'zone.js';
import * as echarts from 'echarts/core';
import { BarChart, GaugeChart, LineChart, PieChart } from 'echarts/charts';
import {
  GridComponent,
  LegendComponent,
  TooltipComponent,
  TitleComponent,
  DatasetComponent,
  ToolboxComponent
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { provideRouter } from '@angular/router';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { provideEchartsCore } from 'ngx-echarts';
import { AppComponent } from './app/app.component';
import { APP_ROUTES } from './app/app.routes';

echarts.use([
  BarChart,
  GaugeChart,
  LineChart,
  PieChart,
  GridComponent,
  LegendComponent,
  TooltipComponent,
  TitleComponent,
  DatasetComponent,
  ToolboxComponent,
  CanvasRenderer
]);

bootstrapApplication(AppComponent, {
  providers: [provideRouter(APP_ROUTES), provideHttpClient(), provideEchartsCore({ echarts })]
}).catch((err) => console.error(err));
