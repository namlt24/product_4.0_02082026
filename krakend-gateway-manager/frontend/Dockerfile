# ---- Stage 1: build Angular app ----
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json ./
RUN npm install
COPY . .
RUN npm run build -- --configuration production

# ---- Stage 2: serve bang Nginx ----
FROM nginx:1.27-alpine
# nginx.conf la 1 TEMPLATE (co placeholder __DNS_RESOLVER__) - docker-entrypoint.sh
# thay the placeholder nay bang nameserver thuc cua moi trung ngay khi container
# khoi dong, TRUOC khi goi nginx that su (xem 2 file do de biet ly do - can thiet
# de anh frontend nay chay dung tren ca Docker Compose lan Kubernetes khong sua gi).
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh
COPY --from=build /app/dist/krakend-gateway-manager-ui/browser /usr/share/nginx/html
EXPOSE 80
ENTRYPOINT ["/docker-entrypoint.sh"]
