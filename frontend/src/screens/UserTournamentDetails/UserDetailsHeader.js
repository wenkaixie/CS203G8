import React, { useState, useEffect } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import { getAuth } from 'firebase/auth';
import axios from 'axios';
import './UserDetailsHeader.css';
import Navbar from '../../components/navbar/Navbar';
import RegistrationForm from './RegistrationForm';

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
    const [userUid, setUserUid] = useState(null);
    const [userElo, setUserElo] = useState(null); // State to store user's Elo rating

    useEffect(() => {
        const path = location.pathname.split('/').pop();
        setActiveTab(path);
    }, [location.pathname]);

    useEffect(() => {
        const auth = getAuth();
        const user = auth.currentUser;

        if (user) {
            const uid = user.uid;
            setUserUid(uid);
            fetchUserDetails(uid); // Fetch user Elo and details
        } else {
            console.error('No user is signed in');
        }
    }, [tournamentId]);

    useEffect(() => {
        const checkUserRegistration = async () => {
            if (!userUid || !tournamentId) return; // Avoid unnecessary execution if data is missing
    
            // Check if we already know the registration status for this tournament
            if (isRegistered) {
                return; // No need to check again if already registered
            }
    
            try {
                // Fetch user data from backend
                const response = await axios.get(
                    `http://localhost:9090/user/getUser/${userUid}`
                );
                const data = response.data;
    
                const check = data.registrationHistory || [];
    
                // Check if the user is already registered for this tournament
                if (check.includes(tournamentId)) {
                    setIsRegistered(true);
                }
            } catch (error) {
                console.error('Error checking registration:', error);
                setRegistrationError('Error checking registration status');
            }
        };
    
        checkUserRegistration();
    }, [userUid, tournamentId, isRegistered]);  // Add isRegistered to dependencies
    

    // Fetch the user's Elo from the backend or Firebase
    const fetchUserDetails = async (uid) => {
        try {
            const response = await axios.get(`http://localhost:9090/user/getUser/${uid}`);
            const userData = response.data;

            console.log('User Data:', userData);

            setUserElo(userData.elo || 0); // Store user's Elo rating
            fetchTournamentData(userData.elo); // Pass Elo to the tournament eligibility function
        } catch (error) {
            console.error('Failed to fetch user data:', error);
        }
    };

    const fetchTournamentData = async (userElo) => {
        try {
            const response = await axios.get(
                `http://localhost:8080/api/tournaments/${tournamentId}`
            );
            const data = response.data;
            
            console.log('Tournament Data:', data);

            setTournamentData(data);
            setNumberOfPlayers(data.users ? data.users.length : 0);

            const isCapacityAvailable = data.capacity > (data.users ? data.users.length : 0);
            const isEloEligible = userElo >= data.eloRequirement;
            const isRegistrationOpen = data.status === "Registration Open";

            console.log('Eligibility Conditions:', {
                isCapacityAvailable,
                isEloEligible,
                isRegistrationOpen,
            });

            setIsEligible(isCapacityAvailable && isEloEligible && isRegistrationOpen);
        } catch (error) {
            console.error('Failed to fetch tournament data:', error);
        }
    };

    const handleRegisterClick = async () => {
        if (isEligible && !isRegistered) {
            // Show registration form if user is eligible and not registered
            setShowRegistrationForm(true);
        } else if (isRegistered) {
            // Attempt unregistration if already registered
            try {
                // Get user data first to retrieve the authId
                const responsetemp = await axios.get(`http://localhost:9090/user/getUser/${userUid}`);
                const userData = responsetemp.data;
                
                if (!userData || !userData.authId) {
                    throw new Error("User data not found or authId missing.");
                }
    
                // Make sure the backend is expecting 'authId' in this format
                const response = await axios.put(
                    `http://localhost:9090/user/unregisterTournament/${tournamentId}`, 
                    {
                        authId: userData.authId // Correctly passing the authId
                    }
                );
    
                if (response.status !== 200) {
                    throw new Error("Failed to unregister from the tournament");
                }
    
                // Only update the state after successful unregistration
                console.log(response.data.message); // Assuming the API returns a message
    
                // Update state only after successful unregistration
                setIsRegistered(false);
                setNumberOfPlayers((prev) => prev - 1); // Decrement the number of players
    
            } catch (error) {
                console.error("Error unregistering user:", error);
                setRegistrationError("Failed to unregister. Please try again.");
            }
        }
    };
     

    const closeForm = () => {
        setShowRegistrationForm(false);
        setRegistrationError('');
    };

    const handleFormSubmit = (authId) => {
        setIsRegistered(true);
        setNumberOfPlayers((prev) => prev + 1);
        setShowRegistrationForm(false);
    };

    const handleTabNavigation = (tab) => {
        navigate(`/user/tournament/${tournamentId}/${tab}`);
    };

    console.log(tournamentData.capacity, " + ", numberOfPlayers)

    const availableSlots = tournamentData.capacity
        ? tournamentData.capacity - numberOfPlayers
        : 0;

    return (
        <div className="header-wrapper">
            <Navbar />

            <div className="header-container">
                <div className="title-info-wrapper">
                    <div className="header-left">
                        <button className="back-button" onClick={() => navigate('/user/home')}>
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
                    {['overview', 'participants', 'games', 'discussion'].map((tab) => (
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
