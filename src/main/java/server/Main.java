package server;

public class Main {
    public static void main(String[] args) {
        HttpServerApp server = new HttpServerApp(8080);
        server.start();

    }
}


/*
docker-compose up -d -> PostgreSQL Container läuft
docker ps -> Container up

docker exec -it mrp_db psql -U mrp_user -d mrp_db
\dt -> Tabellen anzeigen

netstat -ano | findstr :8080
mvn clean package -> (Port frei)

java -jar target/MRP_Khouja-1.0-SNAPSHOT.jar -> Server starten

http://localhost:8080/api/users/register
Post
Content-Type: application/json
{
    "username": "hallo",
    "password": "123",
}


http://localhost:8080/api/users/login
Post
Content-Type: application/json
{
  "username": "hallo",
  "password": "123"
}


http://localhost:8080/api/media
Post
Content-Type: application/json
Authorization: Bearer <TOKEN>
{
  "title": "StarWars",
  "description": "Sci-Fi, Action",
  "mediaType": "MOVIE",
  "releaseYear": 1977,
  "genres": ["Sci-Fi", "Action"],
  "ageRestriction": 12,
  "creatorId": 1
}


 */