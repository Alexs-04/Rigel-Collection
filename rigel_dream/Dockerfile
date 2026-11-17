# ========== STAGE 1: Build ==========
FROM docker.io/library/node:22-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Base URL of the API as seen by the browser. The default is the same-origin path that
# nginx.conf proxies to the backend, so the bundle works from any host or LAN address.
ARG VITE_API_BASE=/api
ENV VITE_API_BASE=${VITE_API_BASE}

RUN npm run build

# ========== STAGE 2: Run ==========
FROM docker.io/library/nginx:stable-alpine

COPY nginx.conf /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html

# Where nginx forwards /api requests (resolved inside the Docker network).
ENV BACKEND_URL=http://backend:8080

EXPOSE 80
