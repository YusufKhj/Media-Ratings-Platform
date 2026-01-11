# Media Ratings Platform

Link zum GitHub Repository: https://github.com/YusufKhj/Media-Ratings-Platform

Ein vollständiges RESTful API Backend für eine Media-Bewertungsplattform, entwickelt mit Java und einer PostgreSQL-Datenbank. Die Plattform ermöglicht Benutzern das Erstellen, Bewerten und Verwalten von Medien-Einträgen (Filme, Serien, Spiele) sowie personalisierte Empfehlungen.

Containerisierung:
Die gesamte Anwendung (Server + Datenbank) ist mit Docker und Docker Compose vollständig containerisiert.

# Voraussetzungen
Java JDK 25 (oder kompatible Version)
Maven 3.6+ (Build-Tool)
Docker & Docker Compose (für Container-Deployment)
PostgreSQL 15 (enthalten in Docker)

Schritt-für-Schritt-Anleitung:

# Projekt bauen:
git clone https://github.com/YusufKhj/Media-Ratings-Platform

mvn clean package

docker-compose up --build -d

Der Server ist jetzt unter http://localhost:8080 erreichbar.


# API Endpunkte
Alle Anfragen und Antworten verwenden das JSON-Format.

