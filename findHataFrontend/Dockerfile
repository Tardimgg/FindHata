FROM node:14-alpine AS build

WORKDIR /app-angular
COPY . .

RUN npm install
RUN npm run build

FROM nginx:alpine

COPY --from=build /app-angular/dist/find-hata-frontend /usr/share/nginx/html
COPY --from=build /app-angular/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
