import { IMovie } from "../types/Movie.ts";
import axios from "axios";
import { useState, useEffect } from "react";

interface MovieDetailDeepProps {
    user: string | undefined; // GitHub ID of the user
    movie: IMovie;
    onBack: () => void; // Function to go back
    fetchWatchlistStatus: (user: string, slug: string) => Promise<boolean>;
    toggleWatchlist: (user: string, slug: string, inWL: boolean) => Promise<void>;
}

export default function MovieDetailDeep(props: Readonly<MovieDetailDeepProps>) {
    const { movie, user, onBack, fetchWatchlistStatus, toggleWatchlist } = props;
    const [message, setMessage] = useState("");
    const [isInWatchlist, setIsInWatchlist] = useState<boolean | null>(null);


    useEffect(() => {
        if (!user) return;
        fetchWatchlistStatus(user, movie.slug)
            .then(setIsInWatchlist)
            .catch(() => setIsInWatchlist(false));
    }, [user, movie.slug, fetchWatchlistStatus]);

    const handleToggle = async () => {
        if (isInWatchlist == null) return;
        await toggleWatchlist(user, movie.slug, isInWatchlist);
        setIsInWatchlist(!isInWatchlist);
    };


    return (
        <div>
            {/* ✅ Back Button */}
            <button onClick={onBack} style={{ marginBottom: "10px" }}>⬅ Back</button>

            <h2>
                {movie.title} ({movie.year})
            </h2>
            <img src={movie.imgImdb} alt={`${movie.title} poster`} style={{ maxWidth: "300px" }} />
            <p>{movie.overview}</p>
            <p>Starring: {movie.stars}</p>
            <p>Directed by: {movie.regisseur}</p>


            {/* ✅ Show Watchlist Button ONLY when user is logged in */}
            {user && user !== "Unauthorized" && (
                <button onClick={handleToggle} disabled={isInWatchlist === null}>
                    {isInWatchlist ? "Remove from Watchlist" : "Add to Watchlist"}
                </button>
            )}

            {message && <p>{message}</p>}
        </div>
    );
}
