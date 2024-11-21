CS203G8 - MatchUp

Hello!

--------------------------------------------------------------------------------------------------------------------

This is how to run the web application locally!

FRONTEND
1. Add .env.local to /frontend directory
2. cd to /frontend directory
3. npm install
4. npm start

BACKEND
1. Add serviceAccountKey.json to the root directory
2. cd to /backend/shared-library directory 
3. Run the following commands to build the shared library
    > mvn clean install    
    > mvn clean compile    
4. Run each microservice/orchestrator application at the given locations
    > backend/adminManagementService/src/main/java/csd/adminmanagement/AdminManagementService.java    
    > backend/playerManagementService/src/main/java/csd/playermanagement/PlayerManagementApplication.java    
    > backend/sagaOrchestrator/src/main/java/csd/saga/SagaOrchestratorApplication.java    
    > backend/tournamentService/src/main/java/csd/tournament/TournamentMicroservice.java    

--------------------------------------------------------------------------------------------------------------------

This is how to run the web application on Docker!

FRONTEND
1. Add .env.local to /frontend directory
2. cd to /frontend directory
3. npm install
4. npm start

BACKEND
1. Update the following urls at /backend/sagaOrchestrator/src/main/resources/application.properties
    > tournament.service.url=http://host.docker.internal:8080    
    > admin.service.url=http://host.docker.internal:7070    
    > player.service.url=http://host.docker.internal:9090    

2. Add serviceAccountKey.json to /backend directory
3. cd to /backend/shared-library directory 
4. Run the following commands to build the shared library
    > mvn clean install
    > mvn clean compile
5. cd to /backend directory
6. Run the following commands to build the docker images for each microservice/orchestrator
    > docker build -f adminManagementService/Dockerfile -t admin-management-service .    
    > docker build -f playerManagementService/Dockerfile -t player-management-service .    
    > docker build -f tournamentService/Dockerfile -t tournament-service .    
    > docker build -f sagaOrchestrator/Dockerfile -t saga-orchestrator .    
7. Set up the docker network for orchestrator to communicate with the microservices
    > docker network create microservices-network    
8. In /backend directory, run the following commands to run the docker images for each microservice/orchestrator
    > docker run -p 7070:7070 admin-management-service    
    > docker run -p 9090:9090 player-management-service    
    > docker run -p 8080:8080 tournament-service    
    > docker run --rm --network microservices-network -p 9696:9696 saga-orchestrator

--------------------------------------------------------------------------------------------------------------------

Some Login Credentials that you can use for the website
1. User
> Email: hello2@gmail.com    
> Password: hello2@gmail.com    

2. Admin
> Email: testadmin1@gmail.com    
> Password: testadmin1@gmail.com    

That's all! Have fun!