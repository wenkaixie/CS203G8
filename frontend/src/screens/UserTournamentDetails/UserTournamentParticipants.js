import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import './UserTournamentParticipants.css';
import Header from './UserDetailsHeader';

const UserTournamentParticipants = () => {
    const { tournamentId } = useParams();
    const [participants, setParticipants] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [sortBy, setSortBy] = useState('');
    const [filteredParticipants, setFilteredParticipants] = useState([]);
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [playerCount, setPlayerCount] = useState(0);

    const calculateAge = (dateOfBirth) => {
        const birthDate = new Date(dateOfBirth.seconds * 1000);
        const today = new Date();
        let age = today.getFullYear() - birthDate.getFullYear();
        const monthDiff = today.getMonth() - birthDate.getMonth();

        if (age === 0 && (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate()))) {
            return 0;
        }
        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
            age--;
        }
        return age;
    };

    useEffect(() => {
        const fetchTournamentData = async () => {
            try {
                // Fetch users directly from the Users subcollection within the tournament
                const usersResponse = await axios.get(`http://localhost:8080/api/tournaments/${tournamentId}/users`);
                const usersArray = usersResponse.data.map(authId => authId.trim()).filter(authId => authId !== "");
                setPlayerCount(usersArray.length);

                // Fetch user details for each participant
                const participantDetails = await Promise.all(
                    usersArray.map(async (authId) => {
                        const userResponse = await axios.get(`http://localhost:9090/user/getUser/${authId}`);
                        const userData = userResponse.data;
                        const age = calculateAge(userData.dateOfBirth);

                        // Fetch user rank for each participant
                        const rankResponse = await axios.get(`http://localhost:9090/user/getUserRank/${authId}`);
                        const userRank = rankResponse.data;

                        return { ...userData, age, userRank, authId };
                    })
                );

                setParticipants(participantDetails);
            } catch (error) {
                console.error('Error fetching participants:', error);
            }
        };

        fetchTournamentData();

        const handleRegistrationSuccess = () => fetchTournamentData();
        window.addEventListener('registrationSuccess', handleRegistrationSuccess);

        return () => {
            window.removeEventListener('registrationSuccess', handleRegistrationSuccess);
        };
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
        let updatedList = [...participants];

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
                } else if (sortBy === 'rating') {
                    return b.elo - a.elo;
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
                                    <tr key={participant.authId || index}>
                                        <td>{index + 1}</td>
                                        <td>{participant.name || 'null'}</td>
                                        <td>{participant.nationality || 'null'}</td>
                                        <td>{participant.age !== null && participant.age !== undefined ? participant.age : '0'}</td>
                                        <td>#{participant.userRank || 'null'}</td>
                                        <td>{participant.elo !== null && participant.elo !== undefined ? participant.elo : '0'}</td>
                                        <td>{participant.registrationHistory.length || '0'}</td>
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
