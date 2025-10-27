# Deployment Guide

## GitHub Secrets Configuration

This project uses GitHub Secrets to securely store sensitive information like API tokens. Follow these steps to configure secrets for deployment.

### Required Secrets

Navigate to your GitHub repository: **Settings → Secrets and variables → Actions → New repository secret**

Add the following secrets:

#### 1. VPS Connection Secrets (Already configured)
- `VPS_HOST` - Your VPS IP address or domain
- `VPS_USER` - SSH username for VPS
- `VPS_SSH_KEY` - Private SSH key for authentication

#### 2. GigaChat API Secret (New)
- **Name:** `GIGACHAT_API_TOKEN`
- **Value:** Your Base64-encoded GigaChat authorization token
- **Description:** Authorization token for GigaChat API OAuth authentication

### How to Get GigaChat Token

1. Go to [Sber AI Studio](https://developers.sber.ru/studio/workspaces)
2. Create or select a workspace
3. Navigate to API Keys section
4. Generate a new API key
5. The token should be in Base64 format (e.g., `MDVjZTI5OGU...`)
6. Copy this token and add it as `GIGACHAT_API_TOKEN` secret in GitHub

### Deployment Flow

When you push to the `master` branch:

1. **GitHub Actions** triggers the deployment workflow
2. **Secrets are passed** from GitHub to the VPS via SSH
3. **Environment variables** are exported in the deployment script
4. **Docker Compose** receives the environment variables
5. **Spring Boot** application reads them via `${GIGACHAT_API_TOKEN}`

### Architecture

```
GitHub Secrets (GIGACHAT_API_TOKEN)
    ↓
GitHub Actions Workflow (.github/workflows/deploy.yml)
    ↓
SSH to VPS with environment variables
    ↓
deploy.sh script (exports GIGACHAT_API_TOKEN)
    ↓
docker-compose.yml (passes to container)
    ↓
Spring Boot application.properties (${GIGACHAT_API_TOKEN})
    ↓
GigaChatService (uses for OAuth authentication)
```

## Local Development

For local development, create a `.env` file:

```bash
# Copy the example file
cp .env.example .env

# Edit .env and add your actual token
nano .env
```

Then run with Docker Compose:

```bash
docker-compose up -d
```

Docker Compose will automatically load variables from `.env` file.

## Security Best Practices

✅ **DO:**
- Store sensitive tokens in GitHub Secrets
- Use `.env` file for local development
- Add `.env` to `.gitignore`
- Rotate tokens regularly
- Use different tokens for development and production

❌ **DON'T:**
- Commit tokens to Git
- Share tokens in public channels
- Hardcode tokens in source code
- Use production tokens in development

## Verification

After deployment, verify that the environment variable is set:

```bash
# SSH to your VPS
ssh user@your-vps

# Check if the container has the environment variable
docker exec myapp-backend env | grep GIGACHAT
```

You should see:
```
GIGACHAT_API_TOKEN=your_token_here
GIGACHAT_MODEL=GigaChat
GIGACHAT_SCOPE=GIGACHAT_API_PERS
```

## Troubleshooting

### Token not working
- Verify the token is correctly set in GitHub Secrets
- Check that the token is in Base64 format
- Ensure the token hasn't expired
- Check VPS logs: `docker logs myapp-backend`

### Environment variable not passed
- Verify `envs: GIGACHAT_TOKEN` is set in GitHub Actions workflow
- Check that `export GIGACHAT_API_TOKEN` is in deploy.sh
- Ensure docker-compose.yml has the environment variable mapping

### OAuth authentication fails
- Check GigaChat OAuth endpoint is accessible from VPS
- Verify SSL certificates are properly configured
- Review application logs for detailed error messages

## Additional Resources

- [GigaChat API Documentation](https://developers.sber.ru/docs/ru/gigachat/api/overview)
- [GitHub Actions Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [Docker Compose Environment Variables](https://docs.docker.com/compose/environment-variables/)
