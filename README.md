# ReviewMe

## Project Overview

ReviewMe is a game review and creator-discovery platform built with Spring Boot, Thymeleaf, Spring Data JPA, and MySQL.

The platform allows users to discover games, read and submit community reviews, explore content creators, and access creator discount codes.

## Features

### User Features
- Account registration and login.
- Account-linked community reviews and ratings.
- Browse and search the game catalog.
- View published reviews.
- Community tags and game discovery.
- Follow content creators and view the following feed.

### Content Creator Features
- Creator dashboard.
- Public creator profiles.
- Separate 1–10 creator review scores.
- Creator relationship disclosures, including sponsorships, free products, affiliate relationships, and other disclosures.
- Create and manage discount codes.
- Discount-code expiration dates and affiliate disclosures.

### Administrator Features
- Administrator dashboard.
- User and role management.
- Review and content reporting.
- Report moderation and resolution.
- Discount-code verification.

## Technologies Used

- Java 21
- Spring Boot
- Spring Security
- Thymeleaf
- Spring Data JPA / Hibernate
- MySQL 8
- Maven
- Docker and Docker Compose

## Installation and Setup

### Requirements
- Docker Desktop
- Git (if cloning the repository)

### Step 1: Download the Project - using powershell or terminal

Clone the repository:

```bash
git clone https://github.com/lakehanning1776/ReviewMe_Final.git
cd ReviewMe_Final
```

Alternatively, download the repository as a ZIP file and extract it.

### Step 2: Configure Environment Variables

Create a `.env` file in the project root by copying `.env.example`.

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Mac/Linux:

```bash
cp .env.example .env
```

The example configuration includes:

- Database password: admin
- Administrator username: admin
- Administrator password: admin123

These are demonstration credentials for the local evaluation environment.

### Step 3: Start ReviewMe

Run:

```bash
docker compose up --build
```

Docker automatically builds the application, starts MySQL, waits for the database to become healthy, and launches ReviewMe.

### Step 4: Open the Application

Open your browser and visit:

http://localhost:8080

## Administrator Demo Account

The administrator account is initialized automatically when the application starts with a fresh database.

| Field | Value |
|---|---|
| Username | admin |
| Password | admin123 |
| Role | ADMIN |

Use this account to access administrator functions and manage account roles.

## Account Types

### Regular Users
Register through the application. Regular users can browse games, view reviews, submit reviews, and explore creators.

### Content Creators
Register an account and have an administrator assign the CREATOR role. Content creators can then access the creator dashboard and its available functions.

### Administrators
The administrator account provides access to administrative dashboards and management functions.

## Database

ReviewMe uses MySQL 8 through Docker Compose.

The database is named `demo30`. Docker Compose maintains database information in a named volume so that application restarts do not automatically erase stored information.

Hibernate uses `ddl-auto=update` to create or update database tables when the application starts.

## Known Limitations and Future Improvements

- Administrator removal of individual posts and reviews is not implemented.
- Administrator account banning and suspension functionality is not implemented.
- Additional moderation and account-management features could be added in future versions.

## Trust and Safety Notes

Creator disclosures are self-reported and do not independently prove a creator's relationship with a product or company.

Administrator verification indicates that a discount code was reviewed within the application; it does not guarantee that a retailer will honor the code.

The reporting workflow provides a process for administrator review and does not automatically determine whether reported content is fraudulent.

## Stopping the Application: 

Close existing powersheel or terminal and terminate the run.

In terminal or powershell

cd ~ /ReviewMe_Final

Then once in the ReviewMe_Final directory

To stop the containers, run:

```bash
docker compose down
```

To stop the containers and remove the database volume, run:

```bash
docker compose down -v
```

**Warning:** The second command permanently deletes the Docker database volume and its stored data.


Then to remove file while still in ReviewMe_Final
cd ~ 

Then

rm -rf ReviewMe_Final


Thanks for Reading to the end!











