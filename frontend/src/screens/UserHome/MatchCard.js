import React, { useState, useEffect } from 'react';
import './MatchCard.css';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { getAuth } from "firebase/auth";

const MatchCard = () => {
    const navigate = useNavigate();
    const auth = getAuth();
    const [match, setMatch] = useState(null);

    const fetchMatch = async () => {
        try {
            const response = await axios.get(`http://localhost:8080/api/rounds/latest/${auth.currentUser.uid}`);
            setMatch(response.data);
            console.log(response.data);
        } catch (error) {
            console.error('Error fetching match:', error);
        }
    };

    useEffect(() => {
        fetchMatch();
    }, []);

    const handleViewGamesHistory = () => {
        navigate('/user/home');
    };

    return (
        <div>
            <div>
                <h2>Recent games</h2>
                <h5>14 May</h5>
            </div>
            <div className="match-card">
                <h2 className="match-title">Summer cup (Round 1)</h2>
                <div className="match-details">
                    <div className="player">
                    <div className="player-icon"></div>
                    <p className="player-name">John</p>
                    <p className="player-country">USA</p>
                    </div>
                    <div className="match-score">
                    <span className="score">5</span>
                    <span className="dash">-</span>
                    <span className="score">2</span>
                    </div>
                    <div className="player">
                    <div className="player-icon"></div>
                    <p className="player-name">Mak</p>
                    <p className="player-country">SIN</p>
                    </div>
                </div>
                <div className="match-time">7.30pm</div>
            </div>
            <br></br>
            <div onClick={ handleViewGamesHistory } className='games-history'>
                <h6>View Tournament Details</h6>
            </div>
        </div>
    );
};

export default MatchCard;