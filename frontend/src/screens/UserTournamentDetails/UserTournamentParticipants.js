import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
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
    const [tournamentTitle, setTournamentTitle] = useState(''); // State for the tournament title

    // Fetch tournament details and participants
    useEffect(() => {
        const fetchTournamentDetails = async () => {
            try {
                // Fetch tournament details including participants and title
                const response = await fetch(`http://localhost:8080/api/tournaments/${tournamentId}`);
                if (!response.ok) {
                    throw new Error('Failed to fetch tournament details');
                }
                const tournamentData = await response.json();
                
                // Set tournament title and player count
                setTournamentTitle(tournamentData.name || 'Tournament');
                setPlayerCount(tournamentData.users ? tournamentData.users.length : 0);

                // Fetch user details for each participant by their userID
                const participantDetails = await Promise.all(
                    (tournamentData.users || []).map(async (userID) => {
                        const userResponse = await fetch(`http://localhost:8080/user/getUser/${userID}`);
                        if (!userResponse.ok) {
                            throw new Error(`Failed to fetch user details for user ${userID}`);
                        }
                        return await userResponse.json();
                    })
                );

                setParticipants(participantDetails); // Set participants with user details
            } catch (error) {
                console.error('Error fetching tournament details and participants:', error);
            }
        };

        fetchTournamentDetails();
    }, [tournamentId]);

    const handleSearch = (e) => {
        setSearchTerm(e.target.value);
    };

    const toggleDropdown = () => {
        setIsDropdownVisible(!isDropdownVisible);
    };

    const handleSortChange = (criteria) => {
        setSortBy(criteria);
        setIsDropdownVisible(false);
    };

    useEffect(() => {
        let updatedList = participants;

        if (searchTerm) {
            updatedList = updatedList.filter((participant) =>
                participant.name.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        if (sortBy) {
            updatedList = updatedList.sort((a, b) => {
                if (sortBy === 'name') {
                    return a.name.localeCompare(b.name);
                } else if (sortBy === 'age') {
                    return b.age - a.age;
                } else if (sortBy === 'worldRank') {
                    return a.worldRank - b.worldRank;
                }
                return 0;
            });
        }

        setFilteredParticipants(updatedList);
    }, [searchTerm, sortBy, participants]);

    return (
        <div>
            <Header 
                activetab="participants" 
                tournamentTitle={tournamentTitle} // Display the fetched tournament title
                playerCount={playerCount} // Display the fetched player count
            />

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
                                    <div className="dropdown-item" onClick={() => handleSortChange('worldRank')}>World Rank</div>
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
                                <th>Rating</th>
                                <th>Games played this season</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredParticipants.map((participant, index) => (
                                <tr key={participant.id}>
                                    <td>{index + 1}</td>
                                    <td>{participant.name}</td>
                                    <td>{participant.nationality}</td>
                                    <td>{participant.age}</td>
                                    <td>{participant.worldRank}</td>
                                    <td>{participant.rating}</td>
                                    <td>{participant.gamesPlayed}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

            </div>

        </div>
    );
};

export default UserTournamentParticipants;
