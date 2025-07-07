🚀 Simple Chat Application Backend using Redis Pub/Sub
This is a simple chat application backend built with Spring Boot and Redis Pub/Sub that supports real-time messaging.
The project allows users to:

✅ Create chat rooms
✅ Join chat rooms
✅ Send messages in real-time
✅ Retrieve chat history

📂 Project Structure
chatapp-backend/
├── src/
├── pom.xml
├── README.md
└── ...

🛠️ Tech Stack
Java 17+
Spring Boot
Redis (In-memory)
Spring Data Redis
PowerShell (for API testing)

🚀 Features
✅ Chat room creation
✅ Chat room joining
✅ Real-time message broadcasting using Redis Pub/Sub
✅ Chat history retrieval
✅ Redis-based in-memory data storage

🖥️ Getting Started
✅ Prerequisites:
Java 17 or higher
Maven
Redis server (installed locally or via Docker)
Git

⚙️ Setup Instructions
1. Clone the Repository
git clone https://github.com/Mohitkundu360/chatapp-backend.git
cd chatapp-backend

2. Start Redis Server
If using WSL: 
sudo service redis-server start
Or directly via:
redis-server
If using Docker:

3. Run the Spring Boot Application
   mvn spring-boot:run

🧪 API Endpoints
1. ✅ Create Chat Room
    POST /api/chatapp/chatrooms
   Request Body:
   json
   {
     "roomName": "general"
   }

2. ✅ Join Chat Room
   POST /api/chatapp/chatrooms/{roomId}/join
   Request Body:
   {
  "participant": "guest_user"
}

3. ✅ Send Message
   POST /api/chatapp/chatrooms/{roomId}/messages
Request Body:
{
  "participant": "guest_user",
  "message": "Hello, everyone!"
}

4. ✅ Get Chat History
    GET /api/chatapp/chatrooms/{roomId}/messages?limit=10

📦 Submission Notes
All APIs have been tested using PowerShell.
Redis must be running for the chat room and message persistence.
Real-time messages are displayed in the Spring Boot console.

🙌 Author
Mohit Kundu
