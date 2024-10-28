import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { getAuth } from 'firebase/auth';
import './AdminTournamentParticipants.css';
import AdminDetailsHeader from './AdminTournamentHeader';
import CreateTournamentForm from './CreateTournamentForm';

const AdminTournamentParticipants = () => {
    const { tournamentId } = useParams();
    const navigate = useNavigate();
    const [participants, setParticipants] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [sortBy, setSortBy] = useState('');
    const [filteredParticipants, setFilteredParticipants] = useState([]);
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [numberOfPlayers, setNumberOfPlayers] = useState(0);
    const [tournamentData, setTournamentData] = useState(null);
    const [isEditMode, setIsEditMode] = useState(false);
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [adminId, setAdminId] = useState(null);

    const calculateAge = (dateOfBirth) => {
        const birthDate = new Date(dateOfBirth.seconds * 1000);
        const today = new Date();
        let age = today.getFullYear() - birthDate.getFullYear();
        const monthDiff = today.getMonth() - birthDate.getMonth();
        if (age === 0 && (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate()))) return 0;
        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) age--;
        return age;
    };

    useEffect(() => {
        const fetchTournamentData = async () => {
            try {
                const response = await axios.get(http://localhost:8080/api/tournaments/${tournamentId});
                const tournamentData = response.data;
                const participantIds = tournamentData.users || [];

                const participantDetails = await Promise.all(
                    participantIds.map(async (userID) => {
                        const sanitizedUserID = userID.replace(/['"]/g, '');
                        const userResponse = await axios.get(http://localhost:9090/user/getUser/${sanitizedUserID});
                        const userData = userResponse.data;
                        const age = calculateAge(userData.dateOfBirth);

                        const rankResponse = await axios.get(http://localhost:9090/user/getUserRank/${sanitizedUserID});
                        const userRank = rankResponse.data;

                        return { ...userData, age, userRank };
                    })
                );

                setTournamentData(tournamentData);
                setParticipants(participantDetails);
                setNumberOfPlayers(participantDetails.length);
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

    const handleDeleteParticipant = async (playerId) => {
        const confirmDelete = window.confirm("Are you sure you want to remove this player from the tournament?");
        if (confirmDelete) {
            try {
                const response = await axios.delete(http://localhost:8080/api/tournaments/${tournamentId}/players/${playerId});
                console.log(response.data);
                setParticipants(participants.filter((participant) => participant.uid !== playerId));
                setNumberOfPlayers(numberOfPlayers - 1);
            } catch (error) {
                console.error('Error deleting participant:', error);
            }
        }
    };

    const handleCreateTournament = () => {
        setShowCreateForm(true);
    };

    const closeForm = () => {
        setShowCreateForm(false);
    };

    const handleEditClick = () => {
        setIsEditMode(!isEditMode);
    };

    const handleSaveClick = () => {
        setIsEditMode(false);
    };

    const handleCancelClick = () => {
        setIsEditMode(false);
    };

    return (
        <div>
            <AdminDetailsHeader
                activeTab="participants"
                tournamentTitle={tournamentData?.name || "Tournament"}
                playerCount={numberOfPlayers}
                onEditClick={handleEditClick}
                onSaveClick={handleSaveClick}
                onCancelClick={handleCancelClick}
                isEditMode={isEditMode}
            />

            <div className="admin-tournament-participants">
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
                                {isEditMode && <th>Remove</th>}
                            </tr>
                        </thead>
                        <tbody>
                            {filteredParticipants.length > 0 ? (
                                filteredParticipants.map((participant, index) => (
                                    <tr key={participant.uid || index}>
                                        <td>{index + 1}</td>
                                        <td>{participant.name || 'null'}</td>
                                        <td>{participant.nationality || 'null'}</td>
                                        <td>{participant.age ?? '0'}</td>
                                        <td>{participant.userRank || 'null'}</td>
                                        <td>{participant.elo ?? '0'}</td>
                                        <td>{participant.registrationHistory.length || '0'}</td>
                                        {isEditMode && (
                                            <td>
                                                <button
                                                    className="delete-participant-button"
                                                    onClick={() => handleDeleteParticipant(participant.uid)}
                                                >
                                                    ❌
                                                </button>
                                            </td>
                                        )}
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan={isEditMode ? "8" : "7"}>No participants available</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>

                <button className="fixed-plus-button" onClick={handleCreateTournament}>
                    +
                </button>

                {showCreateForm && (
                    <CreateTournamentForm
                        onClose={closeForm}
                        onSuccess={() => {
                            closeForm();
                            // Add any additional data handling, such as refetching data here
                        }}
                    />
                )}
            </div>
        </div>
    );
};

export default AdminTournamentParticipants;