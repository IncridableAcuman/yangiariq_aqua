# Multi-stage Dockerfile for React Vite Frontend (YANGIARIQ AQUA AI)

# Stage 1: Build stage
FROM node:22-alpine AS builder
WORKDIR /app

# Cache dependencies
COPY package*.json ./
RUN npm ci

# Copy source code and build production assets
COPY . .

ARG VITE_API_BASE_URL=/api/v1
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

# Stage 2: Production Nginx serving stage
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Clean default html
RUN rm -rf ./*

# Copy built production assets
COPY --from=builder /app/dist .

# Copy hardened Nginx configuration
COPY ./nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:80/index.html > /dev/null || exit 1

STOPSIGNAL SIGQUIT

CMD ["nginx", "-g", "daemon off;"]
