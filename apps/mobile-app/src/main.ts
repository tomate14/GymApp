import 'zone.js';
import * as echarts from 'echarts/core';
import { LineChart } from 'echarts/charts';
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideIonicAngular } from '@ionic/angular/standalone';
import { provideEchartsCore } from 'ngx-echarts';
import { AppComponent } from './app/app.component';
import { APP_ROUTES } from './app/app.routes';
import { authTokenInterceptor } from './app/interceptors/auth-token.interceptor';

echarts.use([LineChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer]);

bootstrapApplication(AppComponent, {
  providers: [
    provideIonicAngular(),
    provideRouter(APP_ROUTES),
    provideHttpClient(withInterceptors([authTokenInterceptor])),
    provideEchartsCore({ echarts })
  ]
}).catch((err) => console.error(err));
