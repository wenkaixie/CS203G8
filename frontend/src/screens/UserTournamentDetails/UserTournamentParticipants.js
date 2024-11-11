import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './UserTournamentParticipants.css';

const UserTournamentParticipants = () => {
    const { tournamentId } = useParams();
    const [participants, setParticipants] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [sortBy, setSortBy] = useState('');
    const [filteredParticipants, setFilteredParticipants] = useState([]);
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [playerCount, setPlayerCount] = useState(0);

    const navigate = useNavigate();

    useEffect(() => {
        const fetchTournamentData = async () => {
            try {
                // Fetch participants directly from the tournament endpoint
                const response = await axios.get(`${process.env.REACT_APP_API_URL}:8080/api/tournaments/${tournamentId}/users`);
                const participantsData = response.data; // Expect an array of participants

                setPlayerCount(participantsData.length);
                setParticipants(participantsData);
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
        setIsDropdownVisible(!isDropdownVisible);
    };

    const handleSortChange = (criteria) => {
        setSortBy(criteria);
        setIsDropdownVisible(false);
    };

    const handleGoToProfile = (authID) => {
        navigate(`/user/profile/${authID}`);
    }

    useEffect(() => {
        let updatedList = [...participants];

        if (searchTerm) {
            updatedList = updatedList.filter((participant) =>
                participant.name.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        if (sortBy) {
            updatedList = updatedList.sort((a, b) => {
                if (sortBy === 'name') return a.name.localeCompare(b.name);
                else if (sortBy === 'age') return b.age - a.age;
                else if (sortBy === 'rating') return b.elo - a.elo;
                return 0;
            });
        }

        setFilteredParticipants(updatedList);
    }, [searchTerm, sortBy, participants]);

    return (
        <div>
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
                                <th>ELO Rating</th>
                                <th>Tournaments Played This Season</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredParticipants.length > 0 ? (
                                filteredParticipants.map((participant, index) => (
                                    <tr key={participant.authId || index}>
                                        <td>{index + 1}</td>
                                        <td className='participant-name' onClick={() => handleGoToProfile(participant.authId)}>{participant.name || 'N/A'}</td>
                                        <td>{participant.nationality || 'N/A'}</td>
                                        <td>{participant.age ?? '0'}</td>
                                        <td>{participant.elo ?? '0'}</td>
                                        <td>{participant.registrationHistory?.length || '0'}</td>
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
