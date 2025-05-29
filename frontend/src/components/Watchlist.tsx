import {useState, useEffect, useCallback} from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import MovieDetailDeep from "./MovieDetailDeep.tsx";
import { IMovie } from "../types/Movie.ts";

interface WatchlistProps {
    user: string | undefined;
    fetchWatchlistStatus: (user: string, slug: string) => Promise<boolean>;
    toggleWatchlist: (user: string, slug: string, inWL: boolean) => Promise<void>;
}

export default function Watchlist(props: Readonly<WatchlistProps>) {
    const { user, toggleWatchlist, fetchWatchlistStatus } = props;
    const navigate = useNavigate();
    const [movies, setMovies] = useState<IMovie[]>([]);
    const [selectedMovie, setSelectedMovie] = useState<IMovie | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState("");

    async function fetchWatchlist() {
        console.log("Watchlist: Starting to fetch watchlist for user:", user);
        if (!user || user === "Unauthorized") {
            console.log("Watchlist: User is not logged in. Redirecting to home.");
            navigate("/");
            return;
        }
        try {
            console.log("Watchlist: Fetching user details for watchlist.");
            const userRes = await axios.get(`/api/users/active/${user}`);
            console.log("Watchlist: User details received:", userRes.data);
            const favorites: string[] = userRes.data.favorites;
            if (!favorites || favorites.length === 0) {
                console.log("Watchlist: No favorites found for user.");
                setMovies([]);
                return;
            }
            console.log("Watchlist: Favorites found:", favorites);
            const moviePromises = favorites.map((slug) => {
                console.log("Watchlist: Fetching movie details for slug:", slug);
                return axios.get(`/api/movies/${slug}`);
            });
            const responses = await Promise.all(moviePromises);
            const moviesData = responses.map(res => res.data as IMovie);
            console.log("Watchlist: Movies fetched:", moviesData);
            setMovies(moviesData);
        } catch (err) {
            console.error("Watchlist: Error fetching watchlist:", err);
            setError("Failed to load watchlist.");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        fetchWatchlist();
    }, [user, navigate]);

    if (loading) return <p>Loading watchlist...</p>;
    if (error) return <p>{error}</p>;

    return (
        <div className="watchlist-container">
            <h2>Your Watchlist</h2>
            {movies.length === 0 ? (
                <p>Your watchlist is empty.</p>
            ) : selectedMovie ? (
                <MovieDetailDeep
                    user={user}
                    movie={selectedMovie}
                    onBack={() => {
                        console.log("Watchlist: Returning to watchlist view from movie detail.");
                        setSelectedMovie(null);
                        // Refresh the movies list in case of changes
                        setLoading(true);
                        fetchWatchlist();
                    }}
                 fetchWatchlistStatus={fetchWatchlistStatus}
                    toggleWatchlist={toggleWatchlist}
                />
            ) : (
                <div className="movies-list">
                    {movies.map((movie) => (
                        <div
                            key={movie.netzkinoId}
                            onClick={() => {
                                console.log("Watchlist: Movie clicked:", movie.title);
                                setSelectedMovie(movie);
                            }}
                            onKeyUp={() => {
                                console.log("Watchlist: Movie keyup triggered:", movie.title);
                                setSelectedMovie(movie);
                            }}
                            role="button"
                            tabIndex={0}
                            className="movie-item"
                            style={{
                                cursor: "pointer",
                                padding: "8px",
                            }}
                        >
                            <h3>{movie.title}</h3>
                            <img src={movie.imgImdb} alt={movie.title} width="500" />
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
