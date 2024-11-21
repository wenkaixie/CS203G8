CS203G8

How to run the project.

FRONTEND
1. Add .env.local to /frontend directory
2. cd to /frontend directory
3. npm install
4. npm start

BACKEND
1. Add serviceAccountKey.json to /backend directory
2. cd to /backend directory
3. Run the following commands to build the docker images for each microservice
    > docker build -f adminManagementService/Dockerfile -t admin-management-service .
    > docker build -f playerManagementService/Dockerfile -t player-management-service .
    > docker build -f tournamentService/Dockerfile -t tournament-service .
    > docker build -f sagaOrchestrator/Dockerfile -t saga-orchestrator .
4. Run the following commands to run the docker images for each microservice
    > docker run -p 7070:7070 admin-management-service    
    > docker run -p 9090:9090 player-management-service    
    > docker run -p 8080:8080 tournament-service    
    > docker run -p 9696:9696 saga-orchestrator    
