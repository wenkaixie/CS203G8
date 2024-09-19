import React  from "react";
import './TournamentUpcoming.css';
import logoImage from '../../assets/images/logo.png';
import profileImage from '../../assets/images/chess-profile-pic.jpg';
import { Img } from "react-image";
import Image from 'react-bootstrap/Image';
import Button from 'react-bootstrap/Button';


import { useState, useEffect } from 'react';

// Example API URL where tournaments are fetched
export const API_URL = 'http://your-api-url/tournaments';

const TournamentUpcoming = () => {
    const [tournaments, setTournaments] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [sortBy, setSortBy] = useState('');
    const [filterStatus, setFilterStatus] = useState('');
    const [filteredTournaments, setFilteredTournaments] = useState([]);

    // Fetch tournaments from API
    useEffect(() => {
        fetch(API_URL)
            .then(response => response.json())
            .then(data => setTournaments(data))
            .catch(err => console.error(err));
    }, []);

    // Handle search input
    const handleSearch = (e) => {
        setSearchTerm(e.target.value);
    };

    // Handle sorting option
    const handleSortChange = (e) => {
        setSortBy(e.target.value);
    };

    // Handle filter option
    const handleFilterChange = (e) => {
        setFilterStatus(e.target.value);
    };

    // Filter and sort the tournament list based on user inputs
    useEffect(() => {
        let updatedList = tournaments;

        // Filter by status
        if (filterStatus) {
            updatedList = updatedList.filter(tournament => tournament.status === filterStatus);
        }

        // Filter by search term (tournament name)
        if (searchTerm) {
            updatedList = updatedList.filter(tournament => 
                tournament.name.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        // Sort the list
        if (sortBy) {
            updatedList = updatedList.sort((a, b) => {
                if (sortBy === 'date') {
                    return new Date(a.date) - new Date(b.date);
                } else if (sortBy === 'prize') {
                    return b.prize - a.prize;
                }
                return 0;
            });
        }

        setFilteredTournaments(updatedList);
    }, [searchTerm, sortBy, filterStatus, tournaments]);

    // Handle save option
    const handleSave = (tournamentId) => {
        // Logic to save a tournament (could be a POST request to an API)
        console.log(`Tournament ${tournamentId} saved!`);
    };

    return (
        <div>
            <h1>Tournament List</h1>

            {/* Search Bar */}
            <input
                type="text"
                placeholder="Search by name..."
                value={searchTerm}
                onChange={handleSearch}
            />

            {/* Sort Dropdown */}
            <select onChange={handleSortChange}>
                <option value="">Sort by</option>
                <option value="date">Date</option>
                <option value="prize">Prize</option>
            </select>

            {/* Filter Dropdown */}
            <select onChange={handleFilterChange}>
                <option value="">Filter by status</option>
                <option value="upcoming">Upcoming</option>
                <option value="ongoing">Ongoing</option>
                <option value="completed">Completed</option>
            </select>

            {/* Tournament List Table */}
            <table>
                <thead>
                    <tr>
                        <th>No</th>
                        <th>Name</th>
                        <th>Date</th>
                        <th>Location</th>
                        <th>Slots</th>
                        <th>Prize</th>
                        <th>Status</th>
                        <th>Save</th>
                    </tr>
                </thead>
                <tbody>
                    {filteredTournaments.map((tournament, index) => (
                        <tr key={tournament.id}>
                            <td>{index + 1}</td>
                            <td>{tournament.name}</td>
                            <td>{tournament.date}</td>
                            <td>{tournament.location}</td>
                            <td>{tournament.slots}</td>
                            <td>{tournament.prize}</td>
                            <td>{tournament.status}</td>
                            <td>
                                <button onClick={() => handleSave(tournament.id)}>Save</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default TournamentUpcoming;
