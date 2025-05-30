package org.example.backend.controller;

import org.example.backend.model.Movie;
import org.example.backend.repo.MovieRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.util.AssertionErrors.assertEquals;



@AutoConfigureMockMvc
@SpringBootTest
@TestPropertySource(properties = {
        "TMDB_API_KEY=dummy-api-key",
        "NETZKINO_ENV=test-environment"
})
class MovieControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MovieRepo movieRepo;

    @DirtiesContext
    @Test
    void searchMovies_shouldReturnMatchingMovies_whenQueryMatchesTitle() throws Exception {
        // Arrange: Save test movies
        Movie movie1 = new Movie(
                "1",
                12345,
                "search-movie-1",
                "The Best Movie",
                "2024",
                "An amazing test movie description",
                "Director One",
                "Star One",
                "img1-netzkino",
                "img1-netzkino-small",
                "img1-imdb",
                List.of("best", "movie"),
                List.of(LocalDate.now())
        );
        Movie movie2 = new Movie(
                "2",
                67890,
                "search-movie-2",
                "Another Great Movie",
                "2025",
                "Another great test movie",
                "Director Two",
                "Star Two",
                "img2-netzkino",
                "img2-netzkino-small",
                "img2-imdb",
                List.of("great", "movie"),
                List.of(LocalDate.now())
        );
        movieRepo.save(movie1);
        movieRepo.save(movie2);

        // Act & Assert: Search for "best" and expect movie1 to be returned
        mvc.perform(MockMvcRequestBuilders.get("/api/movies/search")
                        .param("query", "best"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        [
                            {
                                "id": "1",
                                "netzkinoId": 12345,
                                "slug": "search-movie-1",
                                "title": "The Best Movie",
                                "year": "2024",
                                "overview": "An amazing test movie description",
                                "regisseur": "Director One",
                                "stars": "Star One",
                                "imgNetzkino": "img1-netzkino",
                                "imgNetzkinoSmall": "img1-netzkino-small",
                                "imgImdb": "img1-imdb",
                                "queries": ["best", "movie"],
                                "dateFetched": ["%s"]
                            }
                        ]
                        """.formatted(LocalDate.now())));

        // Verify: Ensure the correct movie is found
        assertTrue(movieRepo.existsBySlug("search-movie-1"));
    }

    @DirtiesContext
    @Test
    void searchMovies_shouldReturnBadRequest_whenQueryIsEmpty() throws Exception {
        // Act & Assert: Expect a 400 Bad Request if no query is provided
        mvc.perform(MockMvcRequestBuilders.get("/api/movies/search"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error")
                        .value("Search query cannot be null or empty."));
    }



    @DirtiesContext
    @Test
    void getAllMovies_shouldReturnEmptyList_whenRepositoryIsEmpty() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/movies"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("[]"));
    }

    @DirtiesContext
    @Test
    void getAllMovies_shouldReturnListWithOneObject_whenOneObjectWasSavedInRepository() throws Exception {
        Movie movie = new Movie(
                "1",
                12345,
                "test-movie",
                "Test Movie",
                "2024",
                "A test movie description",
                "Test Director",
                "Test Stars",
                "test-netzkino-img",
                "test-netzkino-img-small",
                "test-imdb-img",
                List.of("test-query"),
                List.of(LocalDate.now())
        );
        movieRepo.save(movie);

        mvc.perform(MockMvcRequestBuilders.get("/api/movies"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        [
                         {
                             "id": "1",
                             "netzkinoId": 12345,
                             "slug": "test-movie",
                             "title": "Test Movie",
                             "year": "2024",
                             "overview": "A test movie description",
                             "regisseur": "Test Director",
                             "stars": "Test Stars",
                             "imgNetzkino": "test-netzkino-img",
                             "imgNetzkinoSmall": "test-netzkino-img-small",
                             "imgImdb": "test-imdb-img",
                             "queries": ["test-query"],
                             "dateFetched": ["%s"]
                         }
                        ]
                        """.formatted(LocalDate.now())));
    }

    @DirtiesContext
    @Test
    void getMovieBySlug_shouldReturnMovie_whenMovieExists() throws Exception {
        // Arrange: Create and save a test movie
        Movie movie = new Movie(
                "1",
                12345,
                "test-movie",
                "Test Movie",
                "2024",
                "A test movie description",
                "Test Director",
                "Test Stars",
                "test-netzkino-img",
                "test-netzkino-img-small",
                "test-imdb-img",
                List.of("test-query"),
                List.of(LocalDate.now())
        );
        movieRepo.save(movie);

        // Act & Assert: Make a GET request and check response
        mvc.perform(MockMvcRequestBuilders.get("/api/movies/test-movie"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                             "id": "1",
                             "netzkinoId": 12345,
                             "slug": "test-movie",
                             "title": "Test Movie",
                             "year": "2024",
                             "overview": "A test movie description",
                             "regisseur": "Test Director",
                             "stars": "Test Stars",
                             "imgNetzkino": "test-netzkino-img",
                             "imgNetzkinoSmall": "test-netzkino-img-small",
                             "imgImdb": "test-imdb-img",
                             "queries": ["test-query"],
                             "dateFetched": ["%s"]
                        }
                        """.formatted(LocalDate.now())));
    }

    @DirtiesContext
    @Test
    void getMovieBySlug_shouldReturnNotFound_whenMovieDoesNotExist() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/movies/non-existent-movie"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Movie with slug non-existent-movie not found."));
    }

    @DirtiesContext
    @Test
    void createMovie_shouldReturnCreatedMovie_whenValidRequestIsSent() throws Exception {
        // Arrange: Define the movie JSON payload
        String movieJson = """
        {
            "id": "1",
            "netzkinoId": 12345,
            "slug": "test-movie",
            "title": "Test Movie",
            "year": "2024",
            "overview": "A test movie description",
            "regisseur": "Test Director",
            "stars": "Test Stars",
            "imgNetzkino": "test-netzkino-img",
            "imgNetzkinoSmall": "test-netzkino-img-small",
            "imgImdb": "test-imdb-img",
            "queries": ["test-query"],
            "dateFetched": ["%s"]
        }
        """.formatted(LocalDate.now());

        // Act & Assert: Send a POST request and verify response
        mvc.perform(MockMvcRequestBuilders.post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movieJson).with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk()) // Assuming successful creation returns 200 OK
                .andExpect(MockMvcResultMatchers.content().json(movieJson));

        // Verify: Check if movie was actually saved in the database
        assertTrue(movieRepo.existsBySlug("test-movie"));
    }

    @DirtiesContext
    @Test
    void updateMovie_shouldReturnUpdatedMovie_whenMovieExists() throws Exception {
        // Arrange: Save a test movie
        Movie existingMovie = new Movie(
                "1",
                12345,
                "test-movie",
                "Test Movie",
                "2024",
                "A test movie description",
                "Test Director",
                "Test Stars",
                "test-netzkino-img",
                "test-netzkino-img-small",
                "test-imdb-img",
                List.of("test-query"),
                List.of(LocalDate.now())
        );
        movieRepo.save(existingMovie);

        // Define updated movie JSON payload
        String updatedMovieJson = """
        {
            "id": "1",
            "netzkinoId": 12345,
            "slug": "test-movie",
            "title": "Updated Movie Title",
            "year": "2025",
            "overview": "Updated description",
            "regisseur": "Updated Director",
            "stars": "Updated Stars",
            "imgNetzkino": "updated-netzkino-img",
            "imgNetzkinoSmall": "updated-netzkino-img-small",
            "imgImdb": "updated-imdb-img",
            "queries": ["updated-query"],
            "dateFetched": ["%s"]
        }
        """.formatted(LocalDate.now());

        // Act & Assert: Send a PUT request and verify response
        mvc.perform(MockMvcRequestBuilders.put("/api/movies/test-movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedMovieJson).with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(updatedMovieJson));

        // Verify: Check if movie was actually updated in the database
        Movie updatedMovie = movieRepo.findBySlug("test-movie").orElseThrow();
        assertEquals("Updated Movie Title", updatedMovie.title(), "Updated Movie Title");
        assertEquals("Updated Director", updatedMovie.regisseur(), "Updated Director");
    }

    @DirtiesContext
    @Test
    void deleteMovie_shouldReturnNoContent_whenMovieExists() throws Exception {
        // Arrange: Save a test movie
        Movie existingMovie = new Movie(
                "1",
                12345,
                "test-movie",
                "Test Movie",
                "2024",
                "A test movie description",
                "Test Director",
                "Test Stars",
                "test-netzkino-img",
                "test-netzkino-img-small",
                "test-imdb-img",
                List.of("test-query"),
                List.of(LocalDate.now())
        );
        movieRepo.save(existingMovie);

        // Act & Assert: Send DELETE request and expect 204 No Content
        mvc.perform(MockMvcRequestBuilders.delete("/api/movies/test-movie").with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // Verify: Check that the movie is no longer in the database
        assertFalse(movieRepo.existsBySlug("test-movie"));
    }

    @DirtiesContext
    @Test
    void getDailyMovies_shouldReturnMoviesList_whenMoviesExist() throws Exception {
        // Arrange: Save two movies fetched today
        LocalDate today = LocalDate.now();
        Movie daily1 = new Movie(
                "daily-1",
                11111,
                "daily-movie-1",
                "Daily Movie One",
                "2025",
                "First daily test movie",
                "Director A",
                "Star A",
                "img-daily1-netzkino",
                "img-daily1-netzkino-small",
                "img-daily1-imdb",
                List.of("daily"),
                List.of(today)
        );
        Movie daily2 = new Movie(
                "daily-2",
                22222,
                "daily-movie-2",
                "Daily Movie Two",
                "2024",
                "Second daily test movie",
                "Director B",
                "Star B",
                "img-daily2-netzkino",
                "img-daily2-netzkino-small",
                "img-daily2-imdb",
                List.of("daily"),
                List.of(today)
        );
        movieRepo.save(daily1);
        movieRepo.save(daily2);

        // Act & Assert: call the daily endpoint
        mvc.perform(MockMvcRequestBuilders.get("/api/movies/daily"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                    [
                      {
                        "id": "daily-1",
                        "netzkinoId": 11111,
                        "slug": "daily-movie-1",
                        "title": "Daily Movie One",
                        "year": "2025",
                        "overview": "First daily test movie",
                        "regisseur": "Director A",
                        "stars": "Star A",
                        "imgNetzkino": "img-daily1-netzkino",
                        "imgNetzkinoSmall": "img-daily1-netzkino-small",
                        "imgImdb": "img-daily1-imdb",
                        "queries": ["daily"],
                        "dateFetched": ["%s"]
                      },
                      {
                        "id": "daily-2",
                        "netzkinoId": 22222,
                        "slug": "daily-movie-2",
                        "title": "Daily Movie Two",
                        "year": "2024",
                        "overview": "Second daily test movie",
                        "regisseur": "Director B",
                        "stars": "Star B",
                        "imgNetzkino": "img-daily2-netzkino",
                        "imgNetzkinoSmall": "img-daily2-netzkino-small",
                        "imgImdb": "img-daily2-imdb",
                        "queries": ["daily"],
                        "dateFetched": ["%s"]
                      }
                    ]
                    """.formatted(today, today)));

        // Verify: ensure they really are in the repo
        assertTrue(movieRepo.existsBySlug("daily-movie-1"));
        assertTrue(movieRepo.existsBySlug("daily-movie-2"));
    }


    @DirtiesContext
    @Test
    void searchMovies_shouldReturnTooManyRequests_whenRateLimitExceeded() throws Exception {
        // Arrange: Insert a movie to avoid external API calls.
        Movie rateLimitMovie = new Movie(
                "rate-limit",
                99999,
                "rate-limit-test-movie",
                "Rate Limit Test Movie",
                "2024",
                "Test description for rate limiting",
                "Test Director",
                "Test Stars",
                "test-img",
                "test-img-small",
                "test-img-imdb",
                List.of("ratelimittest"),
                List.of(LocalDate.now())
        );
        movieRepo.save(rateLimitMovie);

        // Act: Make the allowed number of requests first.
        int allowedRequests = 2;  // matches your Bucket4j configuration
        for (int i = 0; i < allowedRequests; i++) {
            mvc.perform(MockMvcRequestBuilders.get("/api/movies/search")
                            .param("query", "ratelimittest")
                            .with(csrf()))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        // The next request should exceed the rate limit and return 429 Too Many Requests.
        mvc.perform(MockMvcRequestBuilders.get("/api/movies/search")
                        .param("query", "ratelimittest")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isTooManyRequests());
    }

    @DirtiesContext
    @Test
    void searchMovies_shouldResetRateLimit_afterSomeDelay() throws Exception {
        // Arrange: Insert a movie to avoid external API calls.
        Movie rateLimitMovie = new Movie(
                "rate-limit",
                99999,
                "rate-limit-test-movie",
                "Rate Limit Test Movie",
                "2024",
                "Test description for rate limiting",
                "Test Director",
                "Test Stars",
                "test-img",
                "test-img-small",
                "test-img-imdb",
                List.of("ratelimittest"),
                List.of(LocalDate.now())
        );
        movieRepo.save(rateLimitMovie);

        // 1) Consume all available tokens.
        int allowedRequests = 2;  // as configured
        for (int i = 0; i < allowedRequests; i++) {
            mvc.perform(MockMvcRequestBuilders.get("/api/movies/search")
                            .param("query", "ratelimittest")
                            .with(csrf()))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        // 2) This next request should hit the rate limit (429).
        mvc.perform(MockMvcRequestBuilders.get("/api/movies/search")
                        .param("query", "ratelimittest")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isTooManyRequests());

        // 3) Wait long enough for tokens to refill.
        Thread.sleep(7000);  // adjust the delay based on your refill rate

        // 4) Now the request should succeed again.
        mvc.perform(MockMvcRequestBuilders.get("/api/movies/search")
                        .param("query", "ratelimittest")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }


}
