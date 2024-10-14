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
    const [userElo, setUserElo] = useState(null);
    const [authId, setAuthId] = useState(null); 


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
            fetchUserDetails(uid);
        } else {
            console.error('No user is signed in');
        }
    }, [tournamentId]);
    
    // Fetch user details and set authId
    const fetchUserDetails = async (uid) => {
        try {
            const response = await axios.get(`http://localhost:9090/user/getUser/${uid}`);
            const userData = response.data;
    
            setUserElo(userData.elo || 0);
            setAuthId(userData.authId); // Set authId here
        } catch (error) {
            console.error('Failed to fetch user data:', error);
        }
    };
    
    // Trigger fetchTournamentData only when authId is set
    useEffect(() => {
        if (authId !== null) {
            fetchTournamentData(userElo);
        }
    }, [authId, userElo, tournamentId]); // Dependencies include authId, userElo, and tournamentId
    
    const fetchTournamentData = async (userElo) => {
        try {
            const response = await axios.get(
                `http://localhost:8080/api/tournaments/${tournamentId}`
            );
            const data = response.data;
    
            setTournamentData(data);
    
            const usersArray = (data.users || []).filter(user => user.trim() !== "");
    
            // Check if the user's authId matches any of the registered users
            const isUserRegistered = usersArray.includes(authId);
            setIsRegistered(isUserRegistered);
    
            setNumberOfPlayers(usersArray.length);
    
            const isCapacityAvailable = data.capacity > usersArray.length;
            const isEloEligible = userElo >= data.eloRequirement;
            const isRegistrationOpen = new Date() < new Date(data.startDatetime);
    
            setIsEligible(isCapacityAvailable && isEloEligible && isRegistrationOpen);
        } catch (error) {
            console.error('Failed to fetch tournament data:', error);
        }
    };

    const handleRegisterClick = () => {
        if (isEligible && !isRegistered) {
            setShowRegistrationForm(true);
        }
    };

    const closeForm = () => {
        setShowRegistrationForm(false);
        setRegistrationError('');
    };

    const handleFormSubmit = (authId) => {
        setIsRegistered(true);
        localStorage.setItem(`isRegistered_${tournamentId}`, 'true'); 
        setNumberOfPlayers((prev) => prev + 1);
        setShowRegistrationForm(false);
    };

    const handleTabNavigation = (tab) => {
        navigate(`/user/tournament/${tournamentId}/${tab}`);
    };

    console.log(tournamentData.capacity, " + ", numberOfPlayers);

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
                        className={isRegistered ? 'registered-button' : 'register-button'}
                        onClick={handleRegisterClick}
                        disabled={!isEligible || isRegistered}
                    >
                        {isRegistered ? 'Registered' : isEligible ? 'Register' : 'Not Eligible'}
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
