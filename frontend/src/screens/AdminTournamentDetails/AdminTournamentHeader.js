import { useParams, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import axios from 'axios';
import './AdminTournamentHeader.css';
import Navbar from '../../components/adminNavbar/AdminNavbar';

const AdminDetailsHeader = ({ activeTab, tournamentTitle, playerCount, onEditClick, isEditMode, onSaveClick, onCancelClick }) => {
    const { tournamentId } = useParams();
    const navigate = useNavigate();
    const [tournamentData, setTournamentData] = useState({});
    const [isSaving, setIsSaving] = useState(false); // Saving state for handling async save
    const [isEditModeInternal, setIsEditModeInternal] = useState(isEditMode); // Manage internal edit state

    useEffect(() => {
        const fetchTournamentData = async () => {
            try {
                const response = await axios.get(`http://localhost:8080/api/tournaments/${tournamentId}`);
                setTournamentData(response.data);
            } catch (error) {
                console.error('Failed to fetch tournament data:', error);
            }
        };
        fetchTournamentData();
    }, [tournamentId]);

    const handleEditClick = () => {
        setIsEditModeInternal(true); // Enter edit mode
        onEditClick && onEditClick(); // Notify parent component, if required
    };

    const handleCancelClick = () => {
        setIsEditModeInternal(false); // Exit edit mode
        onCancelClick(); // Reset data in the parent component
    };

    const handleSaveClick = async () => {
        setIsSaving(true);
        try {
            // PUT request to update the tournament details
            await axios.put(`http://localhost:8080/api/tournaments/${tournamentId}`, tournamentData);
            setIsEditModeInternal(false); // Exit edit mode on success
            onSaveClick(); // Save data in the parent component
        } catch (error) {
            console.error("Error updating tournament:", error);
        } finally {
            setIsSaving(false);
        }
    };

    const handleDeleteClick = async () => {
        try {
            // DELETE request to delete the tournament
            await axios.delete(`http://localhost:9696/api/saga/tournaments/${tournamentId}`);
            navigate('/admin/home'); // Redirect to the home after deletion
        } catch (error) {
            console.error("Error deleting tournament:", error);
        }
    };

    const handleTabNavigation = (tab) => {
        navigate(`/admin/tournament/${tournamentId}/${tab}`);
    };

    return (
        <div className="header-wrapper">
            <Navbar />

            <div className="header-container">
                <div className="title-info-wrapper">
                    <div className="header-left">
                        <button className="back-button" onClick={() => navigate('/admin/home')}>
                            {'<'}
                        </button>
                        <h1 className="tournament-title">{tournamentTitle || 'Tournament'}</h1>
                    </div>

                    <div className="info-container">
                        <div className="info-box">
                            <span className="icon">🕒</span>
                            <div>
                                <span className="info-text">
                                    {tournamentData.startDatetime
                                        ? new Date(tournamentData.startDatetime).toLocaleDateString('en-US', {
                                              year: 'numeric',
                                              month: 'short',
                                              day: 'numeric',
                                          })
                                        : 'N/A'}
                                    {' - '}
                                    {tournamentData.endDatetime
                                        ? new Date(tournamentData.endDatetime).toLocaleDateString('en-US', {
                                              year: 'numeric',
                                              month: 'short',
                                              day: 'numeric',
                                          })
                                        : 'N/A'}
                                </span>
                            </div>
                        </div>

                        <div className="info-box">
                            <span className="icon">💰</span>
                            <div>
                                <span className="info-text">${tournamentData.prize || 'N/A'}</span>
                                <span className="sub-text">Total prize pool</span>
                            </div>
                        </div>

                        <div className="info-box">
                            <span className="icon">✔️</span>
                            <div>
                                <span className="info-text">{tournamentData.capacity - playerCount || 'N/A'}</span>
                                <span className="sub-text">Available slots</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="admin-action-container">
                    {isEditModeInternal ? (
                        <div className="edit-actions">
                            <button className="save-button" onClick={handleSaveClick} disabled={isSaving}>
                                {isSaving ? 'Saving...' : 'Save'}
                            </button>
                            <button className="cancel-button" onClick={handleCancelClick}>
                                Cancel
                            </button>
                        </div>
                    ) : (
                        <div className="admin-buttons">
                            <button className="edit-button" onClick={handleEditClick}>
                                Edit
                            </button>
                            <button className="delete-button" onClick={handleDeleteClick}>
                                Delete
                            </button>
                        </div>
                    )}
                </div>
            </div>

            <div className="banner">
                <ul className="subtabs">
                    {['overview', 'participants', 'games'].map((tab) => (
                        <li
                            key={tab}
                            className={`subtab-item ${activeTab === tab ? 'active' : ''}`}
                            onClick={() => handleTabNavigation(tab)}
                        >
                            {tab.charAt(0).toUpperCase() + tab.slice(1)}
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
};

export default AdminDetailsHeader;
