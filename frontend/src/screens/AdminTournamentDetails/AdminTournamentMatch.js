import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './AdminTournamentMatch.css';
import Header from './AdminTournamentHeader';
import { useParams } from 'react-router-dom';
import UserTournamentMatchDiagram from './AdminTournamentMatchDiagram';

const AdminTournamentMatch = () => {
    const { tournamentId } = useParams();
    const [matches, setMatches] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [selectedRound, setSelectedRound] = useState(''); 
    const [availableRounds, setAvailableRounds] = useState([]); 
    const [tournamentTitle, setTournamentTitle] = useState('Tournament');
    const [playerCount, setPlayerCount] = useState(0);
    const [activeView, setActiveView] = useState('diagram');

    // Fetch tournament details
    useEffect(() => {
        const fetchTournamentDetails = async () => {
            try {
                const response = await axios.get(`http://localhost:8080/api/tournaments/${tournamentId}`);
                const tournamentData = response.data;
                setTournamentTitle(tournamentData.name || 'Tournament');

                // Fetch users directly from the Users subcollection within the tournament
                const usersResponse = await axios.get(`http://localhost:8080/api/tournaments/${tournamentId}/users`);
                const usersArray = usersResponse.data.map(authId => authId.trim()).filter(authId => authId !== "");
                setPlayerCount(usersArray.length);
            } catch (error) {
                console.error('Error fetching tournament details:', error);
            }
        };
        fetchTournamentDetails();
    }, [tournamentId]);

    // Fetch all rounds and matches for the tournament and enhance participant data
    useEffect(() => {
        const fetchRoundsAndMatches = async () => {
            try {
                const roundsResponse = await axios.get(`http://localhost:8080/api/tournaments/${tournamentId}/rounds`);
                const roundsData = roundsResponse.data; // Array of rounds

                const allMatches = [];
                const roundNumbers = [];

                for (const round of roundsData) {
                    roundNumbers.push(round.rid);

                    // each match is a map
                    for (const matchMap of round.matches) {
                        const match = matchMap; 

                        // Determine results based on `isWinner`
                        let participant1Result = 0;
                        let participant2Result = 0;

                        if (match.participants[0].isWinner && match.participants[1].isWinner) {
                            // Both participants are marked as winners (draw)
                            participant1Result = 0.5;
                            participant2Result = 0.5;
                        } else if (match.participants[0].isWinner) {
                            // Only participant 1 is a winner
                            participant1Result = 1;
                            participant2Result = 0;
                        } else if (match.participants[1].isWinner) {
                            // Only participant 2 is a winner
                            participant1Result = 0;
                            participant2Result = 1;
                        }

                        // Prepare match data with placeholder attributes for elo and nationality
                        allMatches.push({
                            ...match,
                            tournamentRoundText: round.rid,
                            participants: [
                                { ...match.participants[0], elo: null, nationality: null, resultText: participant1Result },
                                { ...match.participants[1], elo: null, nationality: null, resultText: participant2Result }
                            ]
                        });
                    }
                }

                setMatches(allMatches);
                setAvailableRounds(roundNumbers);
            } catch (error) {
                console.error('Error fetching rounds and matches:', error);
                setMatches([]);
            }
        };
        fetchRoundsAndMatches();
    }, [tournamentId]);

    const handleSearch = (e) => setSearchTerm(e.target.value);
    const toggleDropdown = () => setIsDropdownVisible(!isDropdownVisible);

    const handleRoundSelection = (round) => {
        setSelectedRound(round);
        setIsDropdownVisible(false);
    };

    const handleListViewClick = () => setActiveView('list');
    const handleDiagramViewClick = () => setActiveView('diagram');

    return (
        <div>
            <Header tournamentTitle={tournamentTitle} playerCount={playerCount} />
            <div className="admin-tournament-match">
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
                    availableRounds.map((round) => (
                        <div key={round}>
                            <h2 className="round-title">Round {round}</h2>
                            <table className="matches-table">
                                <thead>
                                    <tr>
                                        <th>No</th>
                                        <th>Date</th>
                                        <th>Location</th>
                                        <th>Player 1</th>
                                        <th>Rating</th>
                                        <th>Nationality</th>
                                        <th>Result</th>
                                        <th></th>
                                        <th>Result</th>
                                        <th>Player 2</th>
                                        <th>Rating</th>
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
                                                    <td>{new Date(match.startTime).toLocaleDateString()}</td>
                                                    <td>{match.location || 'N/A'}</td>
                                                    <td>{match.participants[0].name}</td>
                                                    <td>{match.participants[0].elo || 'N/A'}</td>
                                                    <td>{match.participants[0].nationality || 'N/A'}</td>
                                                    <td>{match.participants[0].resultText}</td>
                                                    <td className="vs-text">VS</td>
                                                    <td>{match.participants[1].resultText}</td>
                                                    <td>{match.participants[1].name}</td>
                                                    <td>{match.participants[1].elo || 'N/A'}</td>
                                                    <td>{match.participants[1].nationality || 'N/A'}</td>
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
                ) : (
                    <UserTournamentMatchDiagram />
                )}
            </div>
        </div>
    );
};

export default AdminTournamentMatch;
