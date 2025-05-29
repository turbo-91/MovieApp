import Layout from "./components/layout/Layout.tsx";
import {Route, Routes} from "react-router-dom";
import MoviesOfTheDay from "./components/MoviesOfTheDay.tsx";
import ProtectedRoute from "./components/ProtectedRoute.tsx";
import {useState} from "react";
import axios from "axios";
import SearchQuery from "./components/SearchQuery.tsx";
import Watchlist from "./components/Watchlist.tsx";


function App() {

    const [user, setUser] = useState<string | undefined>();
    axios.defaults.baseURL = window.location.host === 'localhost:5173'
        ? 'http://localhost:8080'
        : window.location.origin;

    axios.defaults.withCredentials = true;
    console.log("user in App", user)

    const fetchWatchlistStatus = async (user: string, slug: string): Promise<boolean> => {
        const resp = await axios.get<{ inWatchlist: boolean }>(
            `/api/users/watchlist/${user}/${slug}`
        );
        return resp.data.inWatchlist;
    };

    const toggleWatchlist = async (user: string | undefined, slug: string, currentlyInWatchlist: boolean) => {
        if (currentlyInWatchlist) {
            await axios.delete(`/api/users/watchlist/${user}/${slug}`);
        } else {
            await axios.post(`/api/users/watchlist/${user}/${slug}`);
        }
    };


    return (
            <Layout user={user} setUser={setUser}>
                <main>
                    <Routes>
                        <Route
                            path="/"
                            element={<MoviesOfTheDay user={user} fetchWatchlistStatus={fetchWatchlistStatus}
                                                     toggleWatchlist={toggleWatchlist}/>}

                        />
                        <Route element={<ProtectedRoute user={user}/>}>
                            <Route path="/search" element={<SearchQuery user={user} fetchWatchlistStatus={fetchWatchlistStatus}
                                                                        toggleWatchlist={toggleWatchlist} />} />
                            <Route path="/watchlist" element={<Watchlist user={user} fetchWatchlistStatus={fetchWatchlistStatus}
                                                                         toggleWatchlist={toggleWatchlist} />} />
                        </Route>
                    </Routes>
                </main>
            </Layout>
    );
}

export default App;
