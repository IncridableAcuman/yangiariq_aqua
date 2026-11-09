# Ubuntu Server Deployment & Operational Runbook
## YANGIARIQ AQUA AI Platform — Milestone 08.18

This guide provides a comprehensive, reproducible step-by-step procedure for deploying YANGIARIQ AQUA AI on a clean **Ubuntu Server 22.04 LTS or 24.04 LTS** instance.

---

## 1. System Requirements & Hardware Sizing

| Sizing Profile | Target Scope | vCPUs | RAM | Storage | Network |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Small Pilot** | 1 District, Single Canal Zone, ~50 Farmers | 2 vCPUs | 4 GB | 40 GB SSD | 100 Mbps |
| **Standard Regional Prod** | Full Yangiariq District, Multiple WUAs, 100+ Sensors | 4 vCPUs | 8 GB | 100 GB NVMe | 1 Gbps |

---

## 2. Server Hardening & User Provisioning

### Step 2.1: Initial System Update
Log in as `root` and perform an initial system upgrade:
```bash
apt-get update && apt-get upgrade -y
apt-get install -y ufw curl wget git fail2ban jq ca-certificates gnupg
```

### Step 2.2: Create Dedicated Deployment User
Never operate directly as `root`. Create a non-root system user `aquaadmin`:
```bash
adduser --gecos "" aquaadmin
usermod -aG sudo aquaadmin
```

Set up key-based SSH authentication for `aquaadmin`:
```bash
mkdir -p /home/aquaadmin/.ssh
chmod 700 /home/aquaadmin/.ssh
cp /root/.ssh/authorized_keys /home/aquaadmin/.ssh/
chown -R aquaadmin:aquaadmin /home/aquaadmin/.ssh
chmod 600 /home/aquaadmin/.ssh/authorized_keys
```

> [!IMPORTANT]
> Test SSH access in a new terminal window with `ssh aquaadmin@<SERVER_IP>` **BEFORE** closing your existing root session.

### Step 2.3: SSH Hardening
Edit `/etc/ssh/sshd_config.d/50-cloud-init.conf` or `/etc/ssh/sshd_config`:
```text
PermitRootLogin no
PasswordAuthentication no
PubkeyAuthentication yes
X11Forwarding no
```
Reload SSH daemon:
```bash
systemctl reload ssh
```

### Step 2.4: Configure Host Firewall (UFW)
Enforce least-privilege ingress access:
```bash
# Set default policies
ufw default deny incoming
ufw default allow outgoing

# Allow SSH on management port (default 22)
ufw allow 22/tcp comment "SSH Management"

# Allow Public Web Traffic
ufw allow 80/tcp comment "HTTP Web & ACME Challenges"
ufw allow 443/tcp comment "HTTPS Secure Web"

# Enable UFW
ufw --force enable
ufw status verbose
```
*(Notice: Database port `5432` and internal backend port `8080` are strictly blocked from external ingress).*

---

## 3. Install Docker Engine from Official Repository

Follow official Docker instructions for Ubuntu:
```bash
# Add Docker official GPG key:
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc

# Add the repository to Apt sources:
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  tee /etc/apt/sources.list.d/docker.list > /dev/null

apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Grant docker access to aquaadmin:
usermod -aG docker aquaadmin
```
Log out and log back in as `aquaadmin` to refresh group permissions. Verify:
```bash
docker --version
docker compose version
```

---

## 4. Application Setup & Deployment

### Step 4.1: Clone Application Codebase
```bash
sudo mkdir -p /opt/yangiariq-aqua
sudo chown -R aquaadmin:aquaadmin /opt/yangiariq-aqua
cd /opt/yangiariq-aqua

git clone https://github.com/your-org/agronexsus.git .
```

### Step 4.2: Configure Environment File
```bash
cp .env.prod.example .env.prod
chmod 600 .env.prod
```
Generate secure production secrets:
```bash
# Generate 512-bit JWT Secret:
JWT_KEY=$(openssl rand -base64 64)
# Generate PostgreSQL Password:
DB_PASS=$(openssl rand -hex 24)

sed -i "s|REPLACE_WITH_CRYPTO_SECURE_64_BYTE_BASE64_SECRET|${JWT_KEY}|g" .env.prod
sed -i "s|REPLACE_WITH_STRONG_GENERATED_PRODUCTION_PASSWORD|${DB_PASS}|g" .env.prod
```
Update `DOMAIN_NAME` and `CORS_ALLOWED_ORIGINS` to your registered public domain (e.g. `aqua.yangiariq.uz`).

### Step 4.3: Execute Preflight Validation
```bash
./scripts/production/preflight-check.sh .env.prod
```

### Step 4.4: Deploy Initial Stack (HTTP Mode)
```bash
./scripts/production/deploy.sh
```

---

## 5. SSL / TLS Certificate Automation (Certbot)

To secure traffic with Let's Encrypt certificates:

### Step 5.1: Request Certificate via Certbot Webroot
Ensure your domain's DNS A-record points to the server IP.
```bash
# Request certificate using Docker Certbot:
docker run -it --rm --name certbot \
  -v yangiariq_aqua_certbot_www:/var/www/certbot \
  -v yangiariq_aqua_certbot_conf:/etc/letsencrypt \
  certbot/certbot certonly \
  --webroot \
  --webroot-path=/var/www/certbot \
  --email admin@yangiariq.uz \
  --agree-tos \
  --no-eff-email \
  -d aqua.yangiariq.uz
```

### Step 5.2: Activate HTTPS Configuration in NGINX
Generate the active SSL configuration from template:
```bash
export DOMAIN_NAME=aqua.yangiariq.uz
envsubst '${DOMAIN_NAME}' < docker/nginx/conf.d/aqua-ssl.conf.template > docker/nginx/conf.d/aqua-ssl.conf

# Reload NGINX to apply TLS termination:
docker exec aqua-prod-nginx nginx -s reload
```

### Step 5.3: Automated Certificate Renewal Cron
Certificates expire every 90 days. Set up a daily cron task:
```bash
(crontab -l 2>/dev/null; echo "0 3 * * * docker run --rm -v yangiariq_aqua_certbot_www:/var/www/certbot -v yangiariq_aqua_certbot_conf:/etc/letsencrypt certbot/certbot renew --quiet && docker exec aqua-prod-nginx nginx -s reload") | crontab -
```

---

## 6. Automated Backup Scheduling

Configure automated daily backups with retention:
```bash
(crontab -l 2>/dev/null; echo "0 2 * * * /opt/yangiariq-aqua/scripts/production/backup-postgres.sh >> /var/log/aqua-backup.log 2>&1") | crontab -
```

---

## 7. Application Update Procedure

When updating to a new release tag:
```bash
cd /opt/yangiariq-aqua
git pull origin main
./scripts/production/deploy.sh --tag=0.8.19
```

If an issue occurs post-deployment:
```bash
./scripts/production/rollback.sh
```

---

## 8. Operational Troubleshooting & Diagnostics

| Symptom | Diagnostic Command | Remediation Action |
| :--- | :--- | :--- |
| **Backend Fails Startup** | `docker compose -f docker-compose.prod.yml logs backend` | Check if `ProductionSecurityValidator` aborted due to default dev secrets. |
| **Database Unreachable** | `docker exec aqua-prod-postgres pg_isready -U aqua_admin` | Verify PostgreSQL health and volume mount permissions. |
| **Nginx 502 Bad Gateway** | `docker exec aqua-prod-nginx wget -qO- http://backend:8080/actuator/health/liveness` | Backend is still booting or crashed; verify backend logs. |
| **Rate Limit 429 Errors** | Inspect client IP header forwarding in Nginx log | Adjust `RATE_LIMIT_LOGIN_PER_MINUTE` in `.env.prod`. |
