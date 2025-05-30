package org.example.backend.service;

import org.example.backend.dtos.netzkino.CustomFields;
import org.example.backend.dtos.netzkino.NetzkinoResponse;
import org.example.backend.dtos.netzkino.Post;
import org.example.backend.dtos.tmdb.TmdbMovieResult;
import org.example.backend.dtos.tmdb.TmdbResponse;
import org.example.backend.exceptions.InvalidSearchQueryException;
import org.example.backend.model.Movie;
import org.example.backend.model.Query;
import org.example.backend.repo.MovieRepo;
import org.example.backend.repo.QueryRepo;
import org.example.backend.validation.SearchQueryValidator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.util.*;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MovieAPIService implements InitializingBean {

    private final MovieRepo movieRepository;
    private final RestTemplate restTemplate;
    private final QueryRepo queryRepository;
    private final String tmdbApiKey;
    private final String netzkinoEnv;

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/find/";
    private static final String TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/original";
    private static final String NETZKINO_URL = "https://api.netzkino.de.simplecache.net/capi-2.0a/search";

    private final Map<String, List<Movie>> searchCache = new ConcurrentHashMap<>();
    private final Map<LocalDate, List<Movie>> dailyCache = new ConcurrentHashMap<>();


    // comment: I plan to transfer predefinedNames into specific endpoint later in time
    private final List<String> predefinedNames = Arrays.asList(
            "Liam",
            "Noah",
            "Oliver",
            "James",
            "Elijah",
            "Mateo",
            "Theodore",
            "Henry",
            "Lucas",
            "William",
            "Benjamin",
            "Levi",
            "Sebastian",
            "Jack",
            "Ezra",
            "Michael",
            "Daniel",
            "Leo",
            "Owen",
            "Samuel",
            "Hudson",
            "Alexander",
            "Asher",
            "Luca",
            "Ethan",
            "John",
            "David",
            "Jackson",
            "Joseph",
            "Mason",
            "Luke",
            "Matthew",
            "Julian",
            "Dylan",
            "Elias",
            "Jacob",
            "Maverick",
            "Gabriel",
            "Logan",
            "Aiden",
            "Thomas",
            "Isaac",
            "Miles",
            "Grayson",
            "Santiago",
            "Anthony",
            "Wyatt",
            "Carter",
            "Jayden",
            "Ezekiel",
            "Caleb",
            "Cooper",
            "Josiah",
            "Charles",
            "Christopher",
            "Isaiah",
            "Nolan",
            "Cameron",
            "Nathan",
            "Joshua",
            "Kai",
            "Waylon",
            "Angel",
            "Lincoln",
            "Andrew",
            "Roman",
            "Adrian",
            "Aaron",
            "Wesley",
            "Ian",
            "Thiago",
            "Axel",
            "Brooks",
            "Bennett",
            "Weston",
            "Rowan",
            "Christian",
            "Theo",
            "Beau",
            "Eli",
            "Silas",
            "Jonathan",
            "Ryan",
            "Leonardo",
            "Walker",
            "Jaxon",
            "Micah",
            "Everett",
            "Robert",
            "Enzo",
            "Parker",
            "Jeremiah",
            "Jose",
            "Colton",
            "Luka",
            "Easton",
            "Landon",
            "Jordan",
            "Amir",
            "Gael",
            "Austin",
            "Adam",
            "Jameson",
            "August",
            "Xavier",
            "Myles",
            "Dominic",
            "Damian",
            "Nicholas",
            "Jace",
            "Carson",
            "Atlas",
            "Adriel",
            "Kayden",
            "Hunter",
            "River",
            "Greyson",
            "Emmett",
            "Harrison",
            "Vincent",
            "Milo",
            "Jasper",
            "Giovanni",
            "Jonah",
            "Zion",
            "Connor",
            "Sawyer",
            "Arthur",
            "Ryder",
            "Archer",
            "Lorenzo",
            "Declan",
            "Olivia",
            "Emma",
            "Charlotte",
            "Amelia",
            "Sophia",
            "Mia",
            "Isabella",
            "Ava",
            "Evelyn",
            "Luna",
            "Harper",
            "Sofia",
            "Camila",
            "Eleanor",
            "Elizabeth",
            "Violet",
            "Scarlett",
            "Emily",
            "Hazel",
            "Lily",
            "Gianna",
            "Aurora",
            "Penelope",
            "Aria",
            "Nora",
            "Chloe",
            "Ellie",
            "Mila",
            "Avery",
            "Layla",
            "Abigail",
            "Ella",
            "Isla",
            "Eliana",
            "Nova",
            "Madison",
            "Zoe",
            "Ivy",
            "Grace",
            "Lucy",
            "Willow",
            "Emilia",
            "Riley",
            "Naomi",
            "Victoria",
            "Stella",
            "Elena",
            "Hannah",
            "Valentina",
            "Maya",
            "Zoey",
            "Delilah",
            "Leah",
            "Lainey",
            "Lillian",
            "Paisley",
            "Genesis",
            "Madelyn",
            "Sadie",
            "Sophie",
            "Leilani",
            "Addison",
            "Natalie",
            "Josephine",
            "Alice",
            "Ruby",
            "Claire",
            "Kinsley",
            "Everly",
            "Emery",
            "Adeline",
            "Kennedy",
            "Maeve",
            "Audrey",
            "Autumn",
            "Athena",
            "Eden",
            "Iris",
            "Anna",
            "Eloise",
            "Jade",
            "Maria",
            "Caroline",
            "Brooklyn",
            "Quinn",
            "Aaliyah",
            "Vivian",
            "Liliana",
            "Gabriella",
            "Hailey",
            "Sarah",
            "Savannah",
            "Cora",
            "Madeline",
            "Natalia",
            "Ariana",
            "Lydia",
            "Lyla",
            "Clara",
            "Allison",
            "Aubrey",
            "Millie",
            "Melody",
            "Ayla",
            "Serenity",
            "Bella",
            "Skylar",
            "Josie",
            "Lucia",
            "Daisy",
            "Raelynn",
            "Eva",
            "Juniper",
            "Samantha",
            "Elliana",
            "Eliza",
            "Rylee",
            "Nevaeh",
            "Hadley",
            "Alaia",
            "Parker",
            "Julia",
            "Amara",
            "Rose",
            "Charlie",
            "Ashley",
            "Remi",
            "Georgia",
            "Adalynn",
            "Melanie",
            "Amira",
            "Margaret",
            "Piper"
    );

    private static final SecureRandom secureRandom = new SecureRandom();

    public MovieAPIService(MovieRepo movieRepository, RestTemplate restTemplate, QueryRepo queryRepository, @Value("${TMDB_API_KEY}") String tmdbApiKey, @Value("${NETZKINO_ENV}") String netzkinoEnv) {
        this.movieRepository = movieRepository;
        this.restTemplate = restTemplate;
        this.queryRepository = queryRepository;
        this.tmdbApiKey = tmdbApiKey;
        this.netzkinoEnv = netzkinoEnv;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Preload all query-based movies into searchCache
        queryRepository.findAll().forEach(q -> {
            movieRepository.findByQueriesContaining(q.query())
                    .ifPresent(list -> searchCache.put(q.query(), list));
        });
        LocalDate today = LocalDate.now();
        movieRepository
                .findByDateFetchedContaining(today)
                .filter(list -> !list.isEmpty())
                .ifPresent(list -> dailyCache.put(today, list));
    }


    public List<Movie> fetchMoviesBySearchQuery(String searchQuery) {
        validateSearchQuery(searchQuery);
        // 1) quick in-memory lookup
        if (searchCache.containsKey(searchQuery)) {
            return searchCache.get(searchQuery);
        }
        // 2) fallback to hitting DB (in case cache missed) or API
        List<Movie> existing = movieRepository.findByQueriesContaining(searchQuery)
                .orElse(List.of());
        if (!existing.isEmpty()) {
            searchCache.put(searchQuery, existing);
            return existing;
        }
        List<Movie> all = fetchAndStoreAllMovies(searchQuery);
        searchCache.put(searchQuery, all);
        return all;
    }

    private List<Movie> fetchAndStoreAllMovies(String query) {
        String url = buildNetzkinoUrl(query);
        ResponseEntity<NetzkinoResponse> resp = restTemplate.getForEntity(url, NetzkinoResponse.class);

        List<Movie> all = Optional.ofNullable(resp.getBody())
                .map(NetzkinoResponse::posts)
                .orElse(Collections.emptyList()).stream()
                .map(post -> processMoviePost(post, query, List.of()))   // no dateFetched
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        movieRepository.saveAll(all);
        queryRepository.save(new Query(query));
        return all;
    }

    public List<Movie> getMoviesOfTheDay(List<String> names) {
        LocalDate today = LocalDate.now();

        // 0) Return movies for any previously used query in 'names'
        if (names != null && !names.isEmpty()) {
            List<Query> usedQueries = queryRepository.findAll();
            for (String q : names) {
                boolean wasUsed = usedQueries.stream()
                        .anyMatch(existing -> existing.query().equals(q));
                if (wasUsed) {
                    // Fetch and return all movies matching this query
                    return movieRepository.findByQueriesContaining(q)
                            .orElse(Collections.emptyList());
                }
            }
        }

        // 1) In-memory cache
        if (dailyCache.containsKey(today)) {
            return dailyCache.get(today)
                    .stream()
                    .limit(5)
                    .collect(Collectors.toList());
        }

        // 2) Database fallback
        List<Movie> existingMovies = movieRepository.findByDateFetchedContaining(today)
                .orElse(Collections.emptyList());
        if (!existingMovies.isEmpty()) {
            List<Movie> limited = existingMovies.stream()
                    .limit(5)
                    .collect(Collectors.toList());
            dailyCache.put(today, limited);
            return limited;
        }

        // 3) Fetch new movies
        List<String> source = (names != null && !names.isEmpty()) ? names : predefinedNames;
        String queryForToday = source.get(secureRandom.nextInt(source.size()));

        List<Movie> fetched = fetchAndStoreMoviesForDay(queryForToday, List.of(today));
        dailyCache.put(today, fetched);
        return fetched;
    }

    public List<Movie> fetchAndStoreMoviesForDay(String query, List<LocalDate> dateFetched) {
        System.out.println("Fetching movies from external API using query: " + query);

        List<Movie> collectedMovies = new ArrayList<>();
        int maxRetries = 10;

        for (int retryCount = 0; collectedMovies.size() < 5 && retryCount < maxRetries; retryCount++) {
            String netzkinoURL = buildNetzkinoUrl(query);

            try {
                ResponseEntity<NetzkinoResponse> response = restTemplate.getForEntity(netzkinoURL, NetzkinoResponse.class);

                String finalQuery = query;
                List<Movie> newMovies = Optional.ofNullable(response.getBody())
                        .map(NetzkinoResponse::posts)
                        .orElse(Collections.emptyList()).stream()
                        .map(post -> processMoviePost(post, finalQuery, dateFetched))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                collectedMovies.addAll(newMovies);

                if (collectedMovies.size() >= 5) break;

            } catch (Exception e) {
                System.out.println("Error fetching movies: " + e.getMessage());
            }

            query = getRandomQuery();
            System.out.println("Retry " + (retryCount + 1) + ": Trying new query -> " + query);
        }

        if (collectedMovies.size() < 5) {
            throw new IllegalStateException("Failed to fetch 5 movies after " + maxRetries + " attempts.");
        }

        movieRepository.saveAll(collectedMovies);
        queryRepository.save(new Query(query));

        System.out.println("Stored " + collectedMovies.size() + " movies in database.");
        return collectedMovies;
    }

    private String buildNetzkinoUrl(String query) {
        return NETZKINO_URL + "?q=" + query + "&d=" + netzkinoEnv;
    }

    private String getRandomQuery() {
        return predefinedNames.get(secureRandom.nextInt(predefinedNames.size()));
    }

    private void validateSearchQuery(String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            throw new InvalidSearchQueryException("Search query cannot be null or empty.");
        }
        String sanitized = searchQuery.toLowerCase();
        SearchQueryValidator.validate(sanitized);
    }

    private Movie processMoviePost(Post post, String query, List<LocalDate> dateFetched) {
        if (post.custom_fields() == null) {
            System.out.println("Post has no custom fields, skipping...");
            return null;
        }

        String imdbId = extractImdbId(CustomFields.getOrDefault(post.custom_fields().IMDb_Link(), ""));
        if (imdbId.isEmpty()) {
            System.out.println("No valid IMDb ID found, skipping...");
            return null;
        }

        String imgImdb = fetchMoviePosterFromTmdb(imdbId);
        if ("N/A".equals(imgImdb)) {
            System.out.println("Image not found on TMDB, skipping movie: " + post.title());
            return null;
        }

        return formatMovieData(post, query, dateFetched, imgImdb);
    }



    public Movie formatMovieData(Post post, String query, List<LocalDate> dateFetched, String imgImdb) {
        return new Movie(
                post.slug(),
                post.id(),
                post.slug(),
                post.title().trim(),
                CustomFields.getOrDefault(post.custom_fields().Jahr(), "0").trim(),
                post.content().trim(),
                CustomFields.getOrDefault(post.custom_fields().Regisseur(), "Unknown").trim(),
                CustomFields.getOrDefault(post.custom_fields().Stars(), "Unknown").trim(),  // ✅ FIX: Trim to remove spaces
                CustomFields.getOrDefault(post.custom_fields().featured_img_all(), "").trim(),
                CustomFields.getOrDefault(post.custom_fields().featured_img_all_small(), "").trim(),
                imgImdb.trim(),
                List.of(query),
                dateFetched
        );
    }

    public String extractImdbId(String imdbLink) {
        System.out.println("extractImdbId: Received IMDb link: " + imdbLink);
        return Optional.ofNullable(imdbLink)
                .filter(link -> link.contains("tt"))
                .map(link -> link.split("/"))
                .stream()
                .flatMap(Arrays::stream)
                .filter(part -> part.startsWith("tt"))
                .findFirst()
                .map(id -> {
                    System.out.println("extractImdbId: Extracted IMDb ID: " + id);
                    return id;
                })
                .orElse("");
    }


    public String fetchMoviePosterFromTmdb(String imdbId) {
        if (imdbId == null || imdbId.isEmpty()) {
            System.out.println("fetchMoviePosterFromTmdb: IMDb ID is null or empty, returning N/A");
            return "N/A";
        }

        String tmdbURL = TMDB_BASE_URL + imdbId + "?api_key=" + tmdbApiKey + "&language=de&external_source=imdb_id";
        System.out.println("fetchMoviePosterFromTmdb: Fetching TMDB poster using URL: " + tmdbURL);

        try {
            ResponseEntity<TmdbResponse> response = restTemplate.getForEntity(tmdbURL, TmdbResponse.class);

            return Optional.ofNullable(response)
                    .map(ResponseEntity::getBody)
                    .map(TmdbResponse::movie_results)
                    .filter(results -> !results.isEmpty())
                    .map(results -> results.get(0))
                    .map(TmdbMovieResult::backdrop_path)
                    .filter(path -> !path.isEmpty())
                    .map(path -> {
                        String imageUrl = TMDB_IMAGE_URL + path;
                        System.out.println("fetchMoviePosterFromTmdb: Retrieved image URL: " + imageUrl);
                        return imageUrl;
                    })
                    .orElseGet(() -> {
                        System.out.println("fetchMoviePosterFromTmdb: No valid backdrop image found, returning N/A");
                        return "N/A";
                    });

        } catch (Exception e) {
            System.out.println("fetchMoviePosterFromTmdb: Error fetching TMDB poster for IMDb ID " + imdbId + ": " + e.getMessage());
            return "N/A";
        }
    }


}
