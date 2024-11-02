import React, { useState, useEffect } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import { getAuth } from 'firebase/auth';
import axios from 'axios';
import './UserDetailsHeader.css';
import Navbar from '../../components/navbar/Navbar';
import RegistrationForm from './RegistrationForm';
import UserTournamentOverview from './UserTournamentOverview';
import UserTournamentParticipants from './UserTournamentParticipants';
import UserTournamentMatch from './UserTournamentMatch';

const UserDetailsHeader = () => {
    const { tournamentId } = useParams();
    const location = useLocation();
    const navigate = useNavigate();

    const [activeTab, setActiveTab] = useState('overview');
    const [isRegistered, setIsRegistered] = useState(false);
    const [showRegistrationForm, setShowRegistrationForm] = useState(false);
    const [tournamentData, setTournamentData] = useState({});
    const [numberOfPlayers, setNumberOfPlayers] = useState(0);
    const [isEligible, setIsEligible] = useState(false);
    const [registrationError, setRegistrationError] = useState('');
    const [userElo, setUserElo] = useState(null);
    const [authId, setAuthId] = useState(null);

    const handleTabChange = (tab) => {
        setActiveTab(tab);
    };

    useEffect(() => {
        const auth = getAuth();
        const user = auth.currentUser;

        if (user) {
            const authId = user.uid;
            setAuthId(authId);
            fetchUserDetails(authId);
        } else {
            console.error('No user is signed in');
        }
    }, [tournamentId]);

    useEffect(() => {
        const checkUserRegistration = async () => {
            if (!authId || !tournamentId) return;

            if (isRegistered) return;

            try {
                const response = await axios.get(
                    `http://localhost:9090/user/getUser/${authId}`
                );
                const data = response.data;

                const check = data.registrationHistory || [];

                if (check.includes(tournamentId)) {
                    setIsRegistered(true);
                }
            } catch (error) {
                console.error('Error checking registration:', error);
                setRegistrationError('Error checking registration status');
            }
        };

        checkUserRegistration();
    }, [authId, tournamentId, isRegistered]);

    const fetchUserDetails = async (authId) => {
        try {
            const response = await axios.get(`http://localhost:9090/user/getUser/${authId}`);
            const userData = response.data;

            setUserElo(userData.elo || 0);
        } catch (error) {
            console.error('Failed to fetch user data:', error);
        }
    };

    useEffect(() => {
        if (authId !== null) {
            fetchTournamentData(userElo);
        }
    }, [authId, userElo, tournamentId]);

    const fetchTournamentData = async (userElo) => {
        try {
            const response = await axios.get(
                `http://localhost:8080/api/tournaments/${tournamentId}`
            );
            const data = response.data;

            setTournamentData(data);

            const usersResponse = await axios.get(`http://localhost:8080/api/tournaments/${tournamentId}/users`);
            const usersArray = usersResponse.data
                .map(user => user.authId ? user.authId.trim() : null)
                .filter(authId => authId !== null && authId !== "");
            setNumberOfPlayers(usersArray.length);

            const isUserRegistered = usersArray.includes(authId);
            setIsRegistered(isUserRegistered);

            const isCapacityAvailable = data.capacity > usersArray.length;
            const isEloEligible = userElo >= data.eloRequirement;
            const isRegistrationOpen = data.status === "Open";

            setIsEligible(isCapacityAvailable && isEloEligible && isRegistrationOpen);
        } catch (error) {
            console.error('Failed to fetch tournament data:', error);
        }
    };

    const handleRegisterClick = async () => {
        if (isEligible && !isRegistered) {
            setShowRegistrationForm(true);
        } else if (isRegistered) {
            try {
                // Unregister the user in the player service using the new PUT endpoint
                const unregisterResponse = await axios.put(
                    `http://localhost:9090/user/unregisterTournament/${tournamentId}/${authId}`
                );

                if (unregisterResponse.status !== 200) {
                    throw new Error("Failed to unregister from the tournament in player service");
                }

                // Remove the user from the tournament in the tournament service
                const removeResponse = await axios.delete(
                    `http://localhost:8080/api/tournaments/${tournamentId}/players/${authId}`
                );

                if (removeResponse.status !== 200) {
                    throw new Error("Failed to remove user from the tournament in tournament service");
                }

                // Update state after successful unregistration
                setIsRegistered(false);
                setNumberOfPlayers((prev) => prev - 1);
            } catch (error) {
                console.error("Error during unregistration:", error);
                setRegistrationError("Failed to unregister. Please try again.");
            }
        }
    };

    const closeForm = () => {
        setShowRegistrationForm(false);
        setRegistrationError('');
    };

    const handleFormSubmit = () => {
        setIsRegistered(true);
        localStorage.setItem(`isRegistered_${tournamentId}`, 'true');
        setNumberOfPlayers((prev) => prev + 1);
        setShowRegistrationForm(false);
    };

    const availableSlots = tournamentData.capacity
        ? tournamentData.capacity - numberOfPlayers
        : 0;

    return (
        <div className="header-wrapper">
            <Navbar />

            <div className="header-container">
                <div className="title-info-wrapper">
                    <div className="header-left">
                        <button className="back-button" onClick={() => navigate(-1)}>
                            {'<'}
                        </button>
                        <h1 className="tournament-title">{tournamentData.name || 'Tournament'}</h1>
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
                                <span className="info-text">
                                    {availableSlots >= 0 ? availableSlots : 'N/A'}
                                </span>
                                <span className="sub-text">Available slots</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="registration-container">
                    <button
                        className={isRegistered ? 'unregister-button' : 'register-button'}
                        onClick={handleRegisterClick}
                        disabled={!isEligible}
                    >
                        {isRegistered ? 'Unregister' : isEligible ? 'Register' : 'Not Eligible'}
                    </button>
                    <div className="players-count">Players: {numberOfPlayers}</div>
                </div>
            </div>

            <div className="banner">
                <ul className="subtabs">
                    {['overview', 'participants', 'games'].map((tab) => (
                        <li
                            key={tab}
                            className={`subtab-item ${activeTab === tab ? 'active' : ''}`}
                            onClick={() => handleTabChange(tab)}
                        >
                            {tab.charAt(0).toUpperCase() + tab.slice(1)}
                        </li>
                    ))}
                </ul>
            </div>

            {/* Conditionally Render Based on activeTab */}
            {activeTab === 'overview' && <UserTournamentOverview />}
            {activeTab === 'participants' && <UserTournamentParticipants />}
            {activeTab === 'games' && <UserTournamentMatch />}

            {registrationError && <p className="error-message">{registrationError}</p>}

            {showRegistrationForm && (
                <RegistrationForm
                    closeForm={closeForm}
                    onSubmit={handleFormSubmit}
                    tournamentID={tournamentId}
                />
            )}
        </div>
    );
};

export default UserDetailsHeader;
