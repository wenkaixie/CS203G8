import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './UserTournamentMatch.css';
import { useParams, useNavigate } from 'react-router-dom';
import UserTournamentMatchDiagram from './UserTournamentMatchDiagram';
import UserTournamentMatchTable from './UserTournamentMatchTable';

const UserTournamentMatch = () => {
    const { tournamentId } = useParams();
    const [matches, setMatches] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [selectedRound, setSelectedRound] = useState(''); 
    const [availableRounds, setAvailableRounds] = useState([]); 
    const [tournamentTitle, setTournamentTitle] = useState('Tournament');
    const [activeView, setActiveView] = useState('list');
    const [currentRound, setCurrentRound] = useState(null);
    const [tournamentType, setTournamentType] = useState('');

    const navigate = useNavigate();

    useEffect(() => {
        const fetchTournamentDetails = async () => {
            try {
                const response = await axios.get(`http://matchup-load-balancer-1173773587.ap-southeast-1.elb.amazonaws.com:8080/api/tournaments/${tournamentId}`);
                const tournamentData = response.data;
                setTournamentTitle(tournamentData.name || 'Tournament');
                setCurrentRound(tournamentData.currentRound);
                setTournamentType(tournamentData.type);
            } catch (error) {
                console.error('Error fetching tournament details:', error);
            }
        };
        fetchTournamentDetails();
    }, [tournamentId]);

    useEffect(() => {
        const fetchMatches = async () => {
            try {
                const response = await axios.get(`http://matchup-load-balancer-1173773587.ap-southeast-1.elb.amazonaws.com:8080/api/tournaments/${tournamentId}/matches`);
                const fetchedMatches = response.data;

                setMatches(
                    fetchedMatches.map(match => ({
                        ...match,
                        participants: (match.participants || []).map(participant => ({
                            ...participant,
                            displayResult: match.result === null
                                ? '-' // If no result yet, display '-'
                                : match.draw
                                    ? 0.5 // If match is a draw, display 0.5
                                    : participant && participant.isWinner
                                        ? 1 // If participant is the winner, display 1
                                        : 0 // If participant is not the winner and it's not a draw, display 0
                        })),
                        draw: match.draw // Keep track of draw status for the match
                    }))
                );

                const rounds = [...new Set(fetchedMatches.map(match => match.tournamentRoundText))];
                setAvailableRounds(rounds.sort((a, b) => b - a));
            } catch (error) {
                console.error('Error fetching matches:', error);
                setMatches([]);
            }
        };

        const fetchParticipantDetails = async (authId) => {
            try {
                const response = await axios.get(`http://matchup-load-balancer-1173773587.ap-southeast-1.elb.amazonaws.com:9090/user/getUser/${authId}`);
                return response.data;
            } catch (error) {
                console.error('Error fetching participant details:', error);
                return {};
            }
        };

        fetchMatches();
    }, [tournamentId]);

    const handleSearch = (e) => setSearchTerm(e.target.value);
    const toggleDropdown = () => setIsDropdownVisible(!isDropdownVisible);

    const handleRoundSelection = (round) => {
        setSelectedRound(round);
        setIsDropdownVisible(false);
    };

    const handleListViewClick = () => setActiveView('list');
    const handleDiagramViewClick = () => setActiveView('diagram');

    const handleGoToProfile = (authID) => {
        navigate(`/user/profile/${authID}`);
    }

    return (
        <div>
            <div className="user-tournament-match">
                <div className="controls-container">
                    <div className="view-buttons">
                        <button
                            className={`list-view-button ${activeView === 'list' ? 'active' : ''}`}
                            onClick={handleListViewClick}
                        >
                            <img
                                src={require('../../assets/images/List-view.png')}
                                alt="List View"
                                className="view-icon"
                            />
                        </button>
                        <button
                            className={`image-view-button ${activeView === 'diagram' ? 'active pink' : ''}`}
                            onClick={handleDiagramViewClick}
                        >
                            <img
                                src={require('../../assets/images/Image-view.png')}
                                alt="Image View"
                                className="view-icon"
                            />
                        </button>
                    </div>

                    <div className="search-bar">
                        <input
                            type="text"
                            placeholder="Search for a round/participant"
                            value={searchTerm}
                            onChange={handleSearch}
                        />
                        <img
                            src={require('../../assets/images/Search.png')}
                            alt="Search Icon"
                            className="search-icon"
                        />
                    </div>

                    <button className="filter-button">
                        <img
                            src={require('../../assets/images/Adjust.png')}
                            alt="Filter Icon"
                            className="filter-icon"
                        />
                        <span>Filter</span>
                    </button>

                    <div className="dropdown">
                        <button className="order-button" onClick={toggleDropdown}>
                            {selectedRound === '' ? 'All rounds' : `Round ${selectedRound}`}
                        </button>
                        {isDropdownVisible && (
                            <div className="dropdown-content">
                                <div className="dropdown-item" onClick={() => handleRoundSelection('')}>
                                    All rounds
                                </div>
                                {availableRounds.map((round) => (
                                    <div
                                        className="dropdown-item"
                                        key={round}
                                        onClick={() => handleRoundSelection(round)}
                                    >
                                        Round {round}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>

                {activeView === 'list' ? (
                    availableRounds
                        .filter((round) => selectedRound === '' || round === selectedRound) // Filter rounds based on selectedRound
                        .map((round) => (
                            <div key={round} className="table-container">
                                <h2 className="round-title">Round {round}</h2>
                                <table className="matches-table">
                                    <thead>
                                        <tr>
                                            <th>No</th>
                                            <th>Date</th>
                                            <th>Player 1</th>
                                            <th>ELO</th>
                                            <th>Nationality</th>
                                            <th>Result</th>
                                            <th></th>
                                            <th>Result</th>
                                            <th>Player 2</th>
                                            <th>ELO</th>
                                            <th>Nationality</th>
                                            <th>State</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {matches.filter((match) => match.tournamentRoundText === round).length > 0 ? (
                                            matches
                                                .filter((match) => match.tournamentRoundText === round)
                                                .map((match, index) => (
                                                    <tr key={index}>
                                                        <td>{index + 1}</td>
                                                        <td>{match.startTime ? new Date(match.startTime).toLocaleString() : "TBD"}</td>
                                                        <td className='participant-name' onClick={() => handleGoToProfile(match.participants[0].authId)}>{match.participants?.[0]?.name || '-'}</td>
                                                        <td>{match.participants?.[0]?.elo || '-'}</td>
                                                        <td>{match.participants?.[0]?.nationality || '-'}</td>
                                                        <td>{match.participants?.[0]?.displayResult ?? '-'}</td>
                                                        <td className="vs-text">VS</td>
                                                        <td>{match.participants?.[1]?.displayResult ?? '-'}</td>
                                                        <td className='participant-name' onClick={() => handleGoToProfile(match.participants[1].authId)}>{match.participants?.[1]?.name || '-'}</td>
                                                        <td>{match.participants?.[1]?.elo || '-'}</td>
                                                        <td>{match.participants?.[1]?.nationality || '-'}</td>
                                                        <td>{match.state}</td>
                                                    </tr>
                                                ))
                                        ) : (
                                            <tr>
                                                <td colSpan="13">No matches for this round.</td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        ))
                ) : tournamentType === 'ELIMINATION' ? (
                    <UserTournamentMatchDiagram />
                ) : (
                    matches.length > 0 && availableRounds.length > 0 && (
                        <UserTournamentMatchTable matches={matches}/>
                    )
                )}
            </div>
        </div>
    );
};

export default UserTournamentMatch;
