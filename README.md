# UtaBridge

A Spring Boot REST API that helps non-Japanese speakers read song titles, artist names, and other Japanese text in their
native script. Originally designed to integrate with Spotify to translate Japanese metadata into readable formats using
Google Cloud Translation API.

## Features

- REST API built with Spring Boot 3.x
- Translation of Japanese text using Google Cloud Translation API
- JWT-based authentication
- PostgreSQL database with Flyway migrations
- Rate limiting with Resilience4j
- OpenAPI/Swagger documentation

## Quick Local Setup

### Prerequisites

- Docker and Docker Compose
- A Google Cloud Platform (GCP) account

### 1. Clone the Repository

```bash
git clone https://github.com/kurayami07734/utabridge-server
cd utabridge-server
```

### 2. Set Up Google Cloud Project

<details>

<summary>Steps to create GCP project</summary>

#### Create a GCP Project

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Click on the project selector dropdown at the top of the page
3. Click "New Project"
4. Enter a project name (e.g., `utabridge-dev`)
5. Click "Create"

#### Enable Required APIs

1. In your GCP project, go to "APIs & Services" > "Library"
2. Search for and enable the following APIs:
    - **Cloud Translation API** - For text translation

#### Create a Service Account

1. Go to "IAM & Admin" > "Service Accounts"
2. Click "Create Service Account"
3. Enter a name (e.g., `utabridge-service-account`)
4. Click "Create and Continue"
5. Grant the following roles:
    - **Cloud Translation API User** (`roles/cloudtranslate.user`)
6. Click "Continue" then "Done"

#### Generate Credentials JSON

1. In the Service Accounts list, click on your newly created service account
2. Go to the "Keys" tab
3. Click "Add Key" > "Create new key"
4. Select "JSON" format
5. Click "Create" - this will download a JSON file to your computer
6. **Keep this file secure** - it contains your service account credentials
7. Place this credential json file in secrets/gcp-key.json

#### Get Your GCP Project ID

1. In the Google Cloud Console, the project ID is displayed in the project selector dropdown
2. Note this value - you'll need it for configuration

</details>

### 3. Configure Environment Variables

Create a `.env` file in the project root by copying `.env.example`:

```bash
cp .env.example .env
```

Edit the `.env` file with your values. See the Environment Variables section below.

### 4. Create a docker volume

> Note: This is only needed to be done once.

```bash
docker volume create --name utabridge_postgres_data
```

> Note: Keeping an external volume to prevent accidental deletion of volume using `docker compose down -v`

### 5. Run the Application

```bash
docker compose up
```

The application will start on `http://localhost:8080`

### 6. Access the API Documentation

Once running, access the Swagger UI at:

```
http://localhost:8080/api/docs.html
```

