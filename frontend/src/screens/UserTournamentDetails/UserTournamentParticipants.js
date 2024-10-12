import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import './UserTournamentParticipants.css';
import Header from './UserDetailsHeader';

const UserTournamentParticipants = () => {
    const { tournamentId } = useParams(); // Get the tournamentId from the URL
    const [participants, setParticipants] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [sortBy, setSortBy] = useState('');
    const [filteredParticipants, setFilteredParticipants] = useState([]);
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [playerCount, setPlayerCount] = useState(0);

    useEffect(() => {
        const fetchTournamentData = async () => {
            try {
                // Fetch tournament data
                const response = await axios.get(`http://localhost:8080/api/tournaments/${tournamentId}`);
                const tournamentData = response.data;

                const participantIds = tournamentData.users || []; // Users' UIDs

                // Fetch user details for each participant by their UID
                const participantDetails = await Promise.all(
                    participantIds.map(async (userID) => {
                        const sanitizedUserID = userID.replace(/['"]/g, ''); // Remove quotes if present
                        const userResponse = await axios.get(`http://localhost:9090/user/getUser/${sanitizedUserID}`);
                        return userResponse.data;
                    })
                );

                setParticipants(participantDetails); // Set participants with user details
                setPlayerCount(participantDetails.length);
            } catch (error) {
                console.error('Error fetching participants:', error);
            }
        };

        fetchTournamentData();
    }, [tournamentId]);

    const handleSearch = (e) => {
        setSearchTerm(e.target.value);
    };

    const toggleDropdown = () => {
        setIsDropdownVisible(!isDropdownVisible); // Toggle dropdown visibility
    };

    const handleSortChange = (criteria) => {
        setSortBy(criteria);
        setIsDropdownVisible(false); // Hide dropdown after selecting
    };

    useEffect(() => {
        let updatedList = [...participants]; // Make a shallow copy of participants

        // Filter by search term
        if (searchTerm) {
            updatedList = updatedList.filter((participant) =>
                participant.name.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        // Sort the list
        if (sortBy) {
            updatedList = updatedList.sort((a, b) => {
                if (sortBy === 'name') {
                    return a.name.localeCompare(b.name);
                } else if (sortBy === 'age') {
                    return b.age - a.age;
                } else if (sortBy === 'rating') {
                    return b.elo - a.elo; // Assuming "elo" is the rating field
                }
                return 0;
            });
        }

        setFilteredParticipants(updatedList);
    }, [searchTerm, sortBy, participants]);

    return (
        <div>
            <Header activetab="participants" tournamentTitle={tournamentId} playerCount={playerCount} />

            <div className="user-tournament-participants">
                <div className="controls-container">
                    <div className="search-bar">
                        <input
                            type="text"
                            placeholder="Search for a participant"
                            value={searchTerm}
                            onChange={handleSearch}
                        />
                        <img src={require('../../assets/images/Search.png')} alt="Search Icon" className="search-icon" />
                    </div>
                    <div className="buttons-container">
                        <button className="filter-button">
                            <img src={require('../../assets/images/Adjust.png')} alt="Filter Icon" className="filter-icon" />
                            <span>Filter</span>
                        </button>
                        <div className="dropdown">
                            <button className="order-button" onClick={toggleDropdown}>Order By</button>
                            {isDropdownVisible && (
                                <div className="dropdown-content">
                                    <div className="dropdown-item" onClick={() => handleSortChange('name')}>Name</div>
                                    <div className="dropdown-item" onClick={() => handleSortChange('age')}>Age</div>
                                    <div className="dropdown-item" onClick={() => handleSortChange('rating')}>Rating</div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                <div className="participants-list">
                    <table className="participants-table">
                        <thead>
                            <tr>
                                <th>No</th>
                                <th>Name</th>
                                <th>Nationality</th>
                                <th>Age</th>
                                <th>World rank</th>
                                <th>ELO Rating</th>
                                <th>Games played this season</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredParticipants.length > 0 ? (
                                filteredParticipants.map((participant, index) => (
                                    <tr key={participant.uid || index}>
                                        <td>{index + 1}</td>
                                        <td>{participant.name || 'null'}</td>
                                        <td>{participant.nationality || 'null'}</td>
                                        <td>{participant.age || 'null'}</td>
                                        <td>{participant.worldRank || 'null'}</td>
                                        <td>{participant.elo || 'null'}</td>
                                        <td>{participant.gamesPlayed || 'null'}</td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="7">No participants available</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default UserTournamentParticipants;
