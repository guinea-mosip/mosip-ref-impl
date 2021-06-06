FROM node:8-alpine as build-step
RUN mkdir -p /app
WORKDIR /app
COPY package.json /app
RUN npm install
COPY . /app
RUN npm run build --prod

FROM nginx
COPY default.conf /etc/nginx/conf.d/
COPY dist /usr/share/nginx/html
