import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import './AdminTournamentOverview.css';
import AdminDetailsHeader from './AdminTournamentHeader';
import CreateTournamentForm from './CreateTournamentForm'; // Import the form

const AdminTournamentOverview = () => {
    const { tournamentId } = useParams();
    const [numberOfPlayers, setNumberOfPlayers] = useState(0);
    const [tournamentData, setTournamentData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isEditMode, setIsEditMode] = useState(false);
    const [showCreateForm, setShowCreateForm] = useState(false); // State to control form visibility

    useEffect(() => {
        const fetchTournamentData = async () => {
            try {
                const response = await axios.get(`http://localhost:8080/api/tournaments/${tournamentId}`);
                setTournamentData(response.data);

                const usersArray = (response.data.users || []).filter(user => user.trim() !== "");
                setNumberOfPlayers(usersArray.length);

                setLoading(false);
            } catch (error) {
                setError(error.message);
                setLoading(false);
            }
        };

        fetchTournamentData();
    }, [tournamentId]);

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>Error: {error}</div>;
    }

    const handleFieldChange = (field, value) => {
        setTournamentData((prevData) => ({
            ...prevData,
            [field]: value,
        }));
    };

    const handleSaveClick = async () => {
        try {
            const updatedTournamentData = {
                ...tournamentData,
                startDatetime: new Date(tournamentData.startDatetime).toISOString(),
                endDatetime: new Date(tournamentData.endDatetime).toISOString(),
            };

            await axios.put(`http://localhost:8080/api/tournaments/${tournamentId}`, updatedTournamentData);
            setIsEditMode(false);
        } catch (error) {
            console.error("Error updating tournament:", error);
        }
    };

    const handleCancelClick = () => {
        setIsEditMode(false);
    };

    const handleEditClick = () => {
        setIsEditMode(true);
    };

    const handleCreateTournament = () => {
        setShowCreateForm(true);
    };

    const closeForm = () => {
        setShowCreateForm(false);
    };

    // Calculate prize breakdown based on total prize
    const calculatePrizeBreakdown = (totalPrize) => {
        let total = 0;
        if (typeof totalPrize === 'string') {
            total = parseFloat(totalPrize.replace(/[^0-9.-]+/g, ''));
        } else if (typeof totalPrize === 'number') {
            total = totalPrize;
        }
        return [
            { place: "1st", amount: `$${(total * 0.40).toLocaleString()}` },
            { place: "2nd", amount: `$${(total * 0.25).toLocaleString()}` },
            { place: "3rd", amount: `$${(total * 0.15).toLocaleString()}` },
            { place: "4th", amount: `$${(total * 0.10).toLocaleString()}` },
            { place: "5th", amount: `$${(total * 0.05).toLocaleString()}` },
        ];
    };

    const prizeBreakdown = tournamentData?.prize ? calculatePrizeBreakdown(tournamentData.prize) : [];

    return (
        <div>
            <AdminDetailsHeader
                activeTab="overview"
                tournamentTitle={tournamentData?.name || "Tournament Overview"}
                playerCount={numberOfPlayers}
                onEditClick={handleEditClick}
                isEditMode={isEditMode}
                onSaveClick={handleSaveClick}
                onCancelClick={handleCancelClick}
            />

            <div className="admin-tournament-overview">
                <div className="tournament-description">
                    {isEditMode ? (
                        <textarea
                            value={tournamentData.description || ""}
                            onChange={(e) => handleFieldChange('description', e.target.value)}
                        />
                    ) : (
                        <p>{tournamentData?.description || "No description available for this tournament."}</p>
                    )}
                </div>

                <div className="tournament-details">
                    <div className="detail-row">
                        <strong>Dates:</strong>
                        {isEditMode ? (
                            <>
                                <input
                                    type="date"
                                    value={tournamentData.startDatetime ? tournamentData.startDatetime.split('T')[0] : ''}
                                    onChange={(e) => handleFieldChange('startDatetime', e.target.value)}
                                />
                                <input
                                    type="date"
                                    value={tournamentData.endDatetime ? tournamentData.endDatetime.split('T')[0] : ''}
                                    onChange={(e) => handleFieldChange('endDatetime', e.target.value)}
                                />
                            </>
                        ) : (
                            <>
                                {new Date(tournamentData?.startDatetime).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })} — 
                                {' '}
                                {new Date(tournamentData?.endDatetime).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}
                            </>
                        )}
                    </div>

                    <div className="detail-row">
                        <strong>Tournament type:</strong>
                        {isEditMode ? (
                            <select
                                value={tournamentData?.type}
                                onChange={(e) => handleFieldChange('type', e.target.value)}
                            >
                                <option value="Elimination">Elimination</option>
                                <option value="Round-robin">Round-robin</option>
                            </select>
                        ) : (
                            tournamentData?.type || "Single-elimination"
                        )}
                    </div>

                    <div className="detail-row">
                        <strong>ELO requirement:</strong>
                        {isEditMode ? (
                            <input
                                type="number"
                                value={tournamentData?.eloRequirement}
                                onChange={(e) => handleFieldChange('eloRequirement', e.target.value)}
                            />
                        ) : (
                            tournamentData?.eloRequirement || "0"
                        )}
                    </div>

                    <div className="detail-row">
                        <strong>Total Participants:</strong>
                        {isEditMode ? (
                            <input
                                type="number"
                                value={tournamentData?.capacity}
                                onChange={(e) => handleFieldChange('capacity', e.target.value)}
                            />
                        ) : (
                            tournamentData?.capacity || "N/A"
                        )}
                    </div>

                    <div className="detail-row">
                        <strong>Current Number of Players:</strong> {numberOfPlayers}
                    </div>

                    <div className="detail-row">
                        <strong>Location:</strong>
                        {isEditMode ? (
                            <input
                                type="text"
                                value={tournamentData?.location}
                                onChange={(e) => handleFieldChange('location', e.target.value)}
                            />
                        ) : (
                            tournamentData?.location || "N/A"
                        )}
                    </div>

                    <div className="detail-row">
                        <strong>Format:</strong>
                        <p>{`${tournamentData?.name || "This tournament"} is a ${tournamentData?.capacity || numberOfPlayers}-player single-elimination knockout tournament. Players compete head-to-head in a series of matches, with the winner advancing to the next round, and the loser being eliminated. The tournament follows standard chess rules, with time controls of 90 minutes for the first 40 moves, followed by 30 minutes for the remainder of the game, with an additional 30 seconds per move starting from move one. The ultimate goal is to determine the champion through progressive elimination of participants.`}</p>
                    </div>

                    <div className="prizes-section">
                        <strong>Prizes:</strong>
                        <div className="prizes">
                            <div className="prize-total-box">
                                <div className="prize-icon">💰</div>
                                {isEditMode ? (
                                    <input
                                        type="number"
                                        value={tournamentData?.prize || 0}
                                        onChange={(e) => handleFieldChange('prize', e.target.value)}
                                    />
                                ) : (
                                    <div className="prize-amount">${tournamentData?.prize || "$0"}</div>
                                )}
                                <div className="prize-label">Total prize pool</div>
                            </div>
                            <div className="prize-breakdown-box">
                                <table className="prize-table">
                                    <tbody>
                                        {prizeBreakdown.map((prize, index) => (
                                            <tr key={index}>
                                                <td className="prize-place">{prize.place}</td>
                                                <td className="prize-amount-text">{prize.amount}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    <div className="organizer-container">
                        <div className="detail-row">
                            <strong>Organizer:</strong> {tournamentData?.organizer || "World Chess Organization"}
                        </div>
                    </div>

                    {!isEditMode && (
                        <button className="fixed-plus-button" onClick={handleCreateTournament}>
                            +
                        </button>
                    )}
                </div>
            </div>

            {showCreateForm && (
                <CreateTournamentForm
                    onClose={closeForm}
                    onSuccess={() => {
                        closeForm();
                        // Optionally refresh data here after form submission
                    }}
                />
            )}
        </div>
    );
};

export default AdminTournamentOverview;
