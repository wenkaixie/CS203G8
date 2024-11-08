import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './AdminTournamentParticipants.css';
import AdminDetailsHeader from './AdminTournamentHeader';
import CreateTournamentForm from './CreateTournamentForm';

const AdminTournamentParticipants = () => {
    const { tournamentId } = useParams();
    const [participants, setParticipants] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [sortBy, setSortBy] = useState('');
    const [filteredParticipants, setFilteredParticipants] = useState([]);
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [numberOfPlayers, setNumberOfPlayers] = useState(0);
    const [tournamentData, setTournamentData] = useState(null);
    const [tournamentTitle, setTournamentTitle] = useState("Tournament");
    const [isEditMode, setIsEditMode] = useState(false);
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [showConfirmParticipantsButton, setShowConfirmParticipantsButton] = useState(JSON.parse(localStorage.getItem(`confirmButton_${tournamentId}`)) || false);
    const [successMessage, setSuccessMessage] = useState('');

    const navigate = useNavigate();

    useEffect(() => {
        const fetchTournamentData = async () => {
            try {
                const tournamentResponse = await axios.get(`http://localhost:8080/api/tournaments/${tournamentId}`);
                const tournament = tournamentResponse.data;
                setTournamentTitle(tournament.name || "Tournament");
                setTournamentData(tournament);

                const usersResponse = await axios.get(`http://localhost:8080/api/tournaments/${tournamentId}/users`);
                const usersArray = usersResponse.data;
                setNumberOfPlayers(usersArray.length);

                const participantDetails = usersArray.map((user) => ({
                    authId: user.authId,
                    elo: user.elo,
                    joinedAt: user.joinedAt,
                    name: user.name,
                    nationality: user.nationality,
                }));
                setParticipants(participantDetails);

                checkShowConfirmButton(tournament.startDatetime, tournament.minSignups, usersArray.length);
            } catch (error) {
                console.error('Error fetching tournament data:', error);
            }
        };

        fetchTournamentData();
    }, [tournamentId]);

    const checkShowConfirmButton = (startDatetime, minSignups, currentSignups) => {
        const now = new Date();
        const startDate = new Date(startDatetime);
        const timeDifference = startDate - now;
        console.log("Time:"+ timeDifference);
        const hoursDifference = timeDifference / (1000 * 60 * 60);
        
        const shouldShowButton = hoursDifference <= 24 && currentSignups >= minSignups;
        const savedState = localStorage.getItem(`confirmButton_${tournamentId}`);
        setShowConfirmParticipantsButton(savedState ? JSON.parse(savedState) : shouldShowButton);

    };

    const handleConfirmParticipants = async () => {
        try {
            await axios.post(`http://localhost:8080/api/tournaments/${tournamentId}/generateRounds`);
            setSuccessMessage("Rounds and matches generated successfully.");
            setShowConfirmParticipantsButton(false);
            localStorage.setItem(`confirmButton_${tournamentId}`, JSON.stringify(false));
            setTimeout(() => setSuccessMessage(''), 3000);
        } catch (error) {
            console.error('Error generating rounds:', error);
            setSuccessMessage("Failed to generate rounds. Please try again.");
        }
    };

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
        navigate(`/admin/profile/${authID}`);
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
                else if (sortBy === 'rating') return b.elo - a.elo;
                return 0;
            });
        }

        setFilteredParticipants(updatedList);
    }, [searchTerm, sortBy, participants]);

    const handleDeleteParticipant = async (authId) => {
        const confirmDelete = window.confirm("Are you sure you want to remove this player from the tournament?");
        if (confirmDelete) {
            try {
                await axios.delete(`http://localhost:8080/api/tournaments/${tournamentId}/players/${authId}`);
                setParticipants(participants.filter((participant) => participant.authId !== authId));
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
                tournamentTitle={tournamentTitle}
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
                                <th>ELO Rating</th>
                                <th>Registration Date</th>
                                {isEditMode && <th>Actions</th>}
                            </tr>
                        </thead>
                        <tbody>
                            {filteredParticipants.length > 0 ? (
                                filteredParticipants.map((participant, index) => (
                                    <tr key={participant.authId || index}>
                                        <td>{index + 1}</td>
                                        <td className='participant-name' onClick={() => handleGoToProfile(participant.authId)}>{participant.name || 'N/A'}</td>
                                        <td>{participant.nationality || 'null'}</td>
                                        <td>{participant.elo ?? '0'}</td>
                                        <td>
                                            {participant.joinedAt
                                                ? new Date(participant.joinedAt.seconds * 1000).toLocaleString('en-US', {
                                                    year: 'numeric',
                                                    month: 'long',
                                                    day: 'numeric',
                                                    hour: '2-digit',
                                                    minute: '2-digit',
                                                    second: '2-digit',
                                                    hour12: true
                                                }).replace(',', ' ')
                                                : "N/A"}
                                        </td>
                                        {isEditMode && (
                                            <td>
                                                <button
                                                    className="delete-participant-button"
                                                    onClick={() => handleDeleteParticipant(participant.authId)}
                                                >
                                                    ❌
                                                </button>
                                            </td>
                                        )}
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan={isEditMode ? "6" : "5"}>No participants available</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                {showConfirmParticipantsButton && (
                    <button className="confirm-participants-button" onClick={handleConfirmParticipants}>
                        Confirm Participants
                    </button>
                )}

                </div>

                {successMessage && (
                    <div className="success-message">
                        {successMessage}
                    </div>
                )}

                <button className="fixed-plus-button" onClick={handleCreateTournament}>
                    +
                </button>

                {showCreateForm && (
                    <CreateTournamentForm
                        onClose={closeForm}
                        onSuccess={() => {
                            closeForm();
                        }}
                    />
                )}
            </div>
        </div>
    );
};

export default AdminTournamentParticipants;
