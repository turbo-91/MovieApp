import styled from "styled-components";

const NavContainer = styled.nav`
    display: flex;
    justify-content: center;
    gap: 2rem;
    background-color: black;
    padding: 1rem;
`;

const NavLink = styled.a`
    color: white;
    text-decoration: none;
    font-size: 1.2rem;
    font-family: Helvetica, Arial, sans-serif;
    transition: color 0.2s ease-in-out;

    &:hover {
        color: grey;
    }
`;

export default function NavBar() {
    return (
        <NavContainer>
            <NavLink href="/">Home</NavLink>
            <NavLink href="/search">Search</NavLink>
            <NavLink href="/watchlist">Watchlist</NavLink>
        </NavContainer>
    );
}
