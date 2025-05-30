# Movie Search Application

- Integrated **movie search** powered by TMDB and Netzkino APIs, with in-memory caching to optimize external API calls
- **Daily Movie Picks** — rotating featured movies, cached by day to reduce overhead
- **Watchlist management** — users can add or remove movies to their personal collection
- Full CRUD support for Movies through RESTful APIs
- **Rate Limiting** on search endpoints (2 requests per 6 seconds) enforced via Bucket4j
- **Concurrent Caching** using ConcurrentHashMap for fast, thread-safe storage
- Global Exception Handling with custom responses and HTTP status codes (400, 404, 429, 500)
- Frontend built in React with styled components
- Frontend communication via Axios to backend APIs
- unit tests for core services and integration tests for REST endpoints

# Technologies

### Backend
[![My Skills](https://skillicons.dev/icons?i=java,maven,spring,mongodb&perline=4)](https://skillicons.dev)

### Frontend
[![My Skills](https://skillicons.dev/icons?i=typescript,react,styledcomponents,vite&perline=4)](https://skillicons.dev)

# Local development

### Backend
1. Clone the repository
2. Set environment variables
   ```
   export MONGODB_URI="mongodb+srv://<username>:<password>@your-cluster.mongodb.net/yourDb?retryWrites=true&w=majority"
   export NETZKINO_ENV="your-netzkino-api-key"
   export TMDB_API_KEY="your-tmdb-api-key"
   export OAUTH_GITHUB_ID="your-github-oauth-id"
   export OAUTH_GITHUB_SECRET="your-github-oauth-secret"
   ```
4. Run the application
   ```
   mvn spring-boot:run
   ```

### Frontend
1. Clone the repository
2. Install dependencies
   ```
   npm install
   ```
4. Run the development server
   ```
   npm run dev
   ```
