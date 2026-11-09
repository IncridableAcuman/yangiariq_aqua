# Continuous Integration & Continuous Deployment (CI/CD) Pipeline
## YANGIARIQ AQUA AI Platform — Milestone 08.18

This document details the automated continuous integration, testing, OCI image publishing, and protected deployment workflows implemented in GitHub Actions under `.github/workflows/`.

---

## 1. Pipeline Overview & Architecture

```text
  Pull Request to main/develop
             |
             v
   [ pr-verification.yml ]
   +-- Backend: Java 21, ./gradlew clean test, ArchUnit (106 rules)
   +-- Frontend: Node 22, npm ci, tsc -b, eslint, vitest, vite build
   +-- Docker: Compose config validation, Nginx config syntax test
             |
             | (PR Merged to main)
             v
     Push to 'main'
             |
             v
   [ main-publish.yml ]
   +-- Run full test verification suite
   +-- Build Docker backend image (multi-stage)
   +-- Build Docker frontend image (multi-stage)
   +-- Tag with commit SHA (short) and 'latest'
   +-- Publish to Container Registry (ghcr.io)
             |
             | (Manual Authorization Gate)
             v
[ Controlled Deployment: deploy.yml ]
   +-- Requires GitHub Environment Approval ('production')
   +-- Runs preflight checks on target host
   +-- Takes automated database backup
   +-- Deploys new container images
   +-- Verifies service readiness & health checks
   +-- Records deployment history (with rollback capability)
```

---

## 2. Workflows Specification

### 2.1 Pull Request Verification (`.github/workflows/pr-verification.yml`)
- **Triggers:** Pull requests targeting `main` or `develop`.
- **Concurrency:** `cancel-in-progress: true` prevents wasted runner minutes.
- **Jobs:**
  - `backend-test`: Compiles Java 21, executes all 836+ unit/integration tests, runs 106 ArchUnit rules, archives test reports.
  - `frontend-test`: Compiles TypeScript with zero errors, checks ESLint rules, executes all 275+ Vitest tests, performs Vite production bundle build.
  - `docker-verification`: Executes `docker compose -f docker-compose.prod.yml config` and tests Nginx configuration syntax with an ephemeral test container.
- **Security Rule:** Pull requests NEVER trigger automated deployments.

### 2.2 Main Branch Build & Publish (`.github/workflows/main-publish.yml`)
- **Triggers:** Push to `main`.
- **Jobs:**
  - `verify`: Re-runs full verification checks before building release artifacts.
  - `build-and-publish`: Sets up Docker Buildx, authenticates securely to GitHub Container Registry using `${{ secrets.GITHUB_TOKEN }}`, builds backend and frontend multi-stage images, tags with commit SHA, and pushes to registry.

### 2.3 Controlled Deployment (`.github/workflows/deploy.yml`)
- **Triggers:** Manual trigger via `workflow_dispatch`.
- **Inputs:**
  - `environment`: `staging` or `production`.
  - `image_tag`: Target container tag (default: `latest` or explicit semantic version).
- **Environment Gates:**
  - Utilizes GitHub's **Environments** feature. The `production` environment requires mandatory manual approval from designated deployment reviewers before the workflow can proceed.
- **Remote Execution:**
  - Connects to deployment server via SSH using secrets `DEPLOY_HOST`, `DEPLOY_USER`, and `DEPLOY_SSH_KEY`.
  - Executes `/opt/yangiariq-aqua/scripts/production/deploy.sh --tag=<tag>`.

---

## 3. Required Secrets Reference

When enabling automated publishing and remote deployment, configure the following secrets in repository settings:

| Secret Name | Scope | Description |
| :--- | :--- | :--- |
| `GITHUB_TOKEN` | Built-in | Used for pushing images to GitHub Container Registry (`ghcr.io`) |
| `DEPLOY_HOST` | Environment (`staging`/`production`) | Public IP or FQDN of the Ubuntu deployment host |
| `DEPLOY_USER` | Environment (`staging`/`production`) | SSH deployment username (`aquaadmin`) |
| `DEPLOY_SSH_KEY` | Environment (`staging`/`production`) | Private Ed25519/RSA SSH key for non-interactive login |
