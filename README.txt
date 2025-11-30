Media Ratings Platform

Die Media Ratings Platform ist ein einfacher RESTful Backend-Service. Die Anwendung ermöglicht es Benutzern, sich zu registrieren, einzuloggen und Medieninhalte zu erstellen. Der Fokus liegt auf einer soliden Backend-Architektur ohne den Einsatz großer Web-Frameworks.

Link zum GitHub Repository: https://github.com/YusufKhj/Media-Ratings-Platform

Features

Benutzer-Management:

Registrierung neuer Benutzer (http://localhost:8080/api/users/register)

Benutzer-Login (http://localhost:8080/api/users/login)

Medien-Management:

Erstellen von neuen Medien-Einträgen (http://localhost:8080/api/media).

Containerisierung:

Die gesamte Anwendung (Server + Datenbank) ist mit Docker und Docker Compose vollständig containerisiert.


Setup und Starten

Um die Anwendung auszuführen, werden folgende Werkzeuge benötigt:

Java JDK

Apache Maven

Docker & Docker Compose

Schritt-für-Schritt-Anleitung:

Projekt bauen:
führe folgenden Befehl aus, um die Anwendung zu kompilieren und zu packen:

mvn clean package


Dieser Befehl erstellt eine .jar-Datei im target-Verzeichnis.

Container starten:
Stelle sicher, dass Docker im Hintergrund läuft. Führe dann im selben Verzeichnis den folgenden Befehl aus:

docker-compose up --build -d


Der Server ist jetzt unter http://localhost:8080 erreichbar.

API Endpunkte

Alle Anfragen und Antworten verwenden das JSON-Format.

1. Benutzer registrieren

URL: /api/users/register

Methode: POST
Content-Type: application/json
Body (Beispiel):

{
    "username": "max",
    "password": "password123"
}


2. Benutzer einloggen

URL: /api/users/login
Content-Type: application/json
Methode: POST

Body (Beispiel):

{
    "username": "max",
    "password": "password123"
}


Antwort (Beispiel):

{
  "Token": "max-34534535"
}


3. Neuen Media-Eintrag erstellen (geschützt)

URL: /api/media

Methode: POST
Content-Type: application/json
Authorization: Bearer <Dein Token> (Kopiere den Token aus der Login-Antwort)

Body (Beispiel):

{
    "title": "StarWars",
    "description": "Sci-Fi, Action",
    "mediaType": "MOVIE",
    "releaseYear": 1977,
    "genres": ["Sci-Fi", "Action"],
    "ageRestriction": 12,
    "creatorId": 1
}