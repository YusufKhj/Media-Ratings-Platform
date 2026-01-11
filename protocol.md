# Protokoll - MRP

Projekt: Media Ratings Platform - Intermediate
Yusuf Khouja
11.01.2026


# Repository: 
https://github.com/YusufKhj/Media-Ratings-Platform


# Architektur:

Presentation Layer (Controller & Router) <-> Business Logic Layer (Services) <-> Data Access Layer (Database & Utils)

Begründung:
Separation of Concerns - Controller kümmern sich nur um HTTP, Services um Business-Logik
Testbarkeit - Services können unabhängig von HTTP getestet werden
Wartbarkeit - Änderungen an DB betreffen nur Services, nicht Controller
Wiederverwendbarkeit - Services können von mehreren Controllern genutzt werden


# Technologien:
- Java JDK 25
- com.sun.net.Httpserver für einfachen HTTP-Server
- Jackson-Bibliothek für JSON-Verarbeitung über eigene Utility-Klasse (JsonUtil)
- Token-basierte Authentifizierung (TokenManager) und Passwort-Hashing
- Verwaltung mit Apache Maven (pom.xml)
- MySQL/PostgreSQL 15 über DbUtil
- Docker Container für Server (mrp_server) und PostgreSQL-Datenbank (mrp_db)


# Tests:
Unit-Tests - Testen die Business-Logik, nicht die Infrastruktur
 - MediaService - Authorization, Filter-Logik, Komplexe Queries (5 Tests)
 - RatingService - Duplicate Prevention, Moderation, Berechnungen (5 Tests)
 - Utilities - Password Hashing, Token Management (10 Tests)
 GESAMT - 20 UNITTESTS

Integration Test - Postman Tests für Funktionalität
 GESAMT - 18 TESTS

# SOLID Principles
1.  Single Responsibility Principle: 
Eine Klasse sollte nur einen Grund zur Änderung haben.
z.B.: JSonUtil - Nur für Json zuständig, HashUtil - Nur für Hashing der Passwörter, Controller nur für HTTP-Handling/ Request/ Response, ...
2. Open/Closed Principle (OCP): 
Offen für Erweiterung, geschlossen für Modifikation.
z.B.: MediaEntry.MediaType - Enum (Erweiterbar), Filter-System in MediaService.getAllMedia() - neue Filter können hinzugefügt werden, ...


# Lessons Learned:
- Ich habe gelernt, wie wichtig eine klare Trennung zwischen Controllern, Services und Hilfsklassen ist, um den Code übersichtlich und gut wartbar zu halten.
- Die Arbeit mit Login, Tokens und Berechtigungen hat mir ein besseres Verständnis dafür gegeben, wie Authentifizierung in einer Backend-Anwendung funktioniert.
- Frühzeitiges Testen mit realistischen Beispieldaten (z. B. Ratings, Genres) erleichtert das Debugging und verhindert leere oder unerwartete API-Antworten.
- Klare API-Routen, konsistente Parameterbenennung und gezieltes Logging sparen viel Zeit bei der Fehlersuche und Integration mit Postman.

# Zeitaufwand:
Projekt-Setup & Planung: (Maven, IDE, Architektur) - 3 Stunden
Datenbank-Schema & init.sql: - 7 Stunden
Server-Grundgerüst: (HttpServer, Routing) - 5 Stunden
Datenbankanbindung & Services: (User/Media/Rating-Logik) - 34 Stunden
Sicherheit: (Passwort-Hashing, Token-Auth) - 8 Stunden
Containerisierung: (Dockerfile, Docker Compose) - 2 Stunden
Debugging & Manuelles Testen: (Postman, Fehlersuche, Unittests) - 21 Stunden
Dokumentation: (README, Protokoll) - 4 Stunden
Gesamtaufwand: ca. 84 Stunden


# App Struktur

Media Ratings Platform/
│
├─ src/
│  ├─ main/
│    ├─ java/
│      ├─ controllers/
│      │  ├─ MediaController.java
│      │  ├─ UserController.java
│      │  ├─ RecommendationController.java
│      │  └─ RatingController.java
│      │
│      ├─ models/
│      │  ├─ MediaEntry.java
│      │  ├─ User.java
│      │  └─ Rating.java
│      │
│      ├─ services/
│      │  ├─ MediaService.java
│      │  ├─ RatingService.java
│      │  ├─ RecommendationService.java
│      │  └─ UserService.java
│      │
│      ├─ utils/
│      │  ├─ JsonUtil.java
│      │  ├─ JsonResponse.java
│      │  ├─ HashUtil.java
│      │  ├─ UserManager.java
│      │  ├─ DbUtil.java
│      │  └─ TokenManager.java
│      │
│      └─ server/
│      	├─ HttpServerApp.java
│       ├─ Main.java
│       ├─ Router.java
│       └─ RequestHandler.java
│
│
│
├─ Dockerfile
├─ docker-compose.yml
├─ init.sql
└─ pom.xml
