import React, { useState, useEffect } from 'react'; 
import './UserTournamentMatch.css';
import Header from './UserDetailsHeader';
import { useParams } from 'react-router-dom';
import UserTournamentMatchDiagram from './UserTournamentMatchDiagram'; 

const UserTournamentMatch = () => {
    const { tournamentId } = useParams(); 
    const [matches, setMatches] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [filteredMatches, setFilteredMatches] = useState([]);
    const [sortBy, setSortBy] = useState('');
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [selectedRound, setSelectedRound] = useState('');
    const [availableRounds, setAvailableRounds] = useState([]);
    const [tournamentTitle, setTournamentTitle] = useState('Tournament');
    const [playerCount, setPlayerCount] = useState(0);
    const [activeView, setActiveView] = useState('diagram'); 

    useEffect(() => {
        const fetchTournamentDetails = async () => {
            try {
                const response = await fetch(`http://localhost:8080/api/tournaments/${tournamentId}`);
                if (!response.ok) throw new Error('Failed to fetch tournament details');

                const tournamentData = await response.json();
                setTournamentTitle(tournamentData.name || 'Tournament');
                setPlayerCount(tournamentData.users ? tournamentData.users.length : 0);
            } catch (error) {
                console.error('Error fetching tournament details:', error);
            }
        };
        fetchTournamentDetails();
    }, [tournamentId]);

    useEffect(() => {
        const fetchRounds = async () => {
            try {
                const response = await fetch(`http://localhost:8080/api/tournaments/${tournamentId}/allRounds`);
                const data = await response.json();
                setAvailableRounds(data);
            } catch (error) {
                console.error('Error fetching rounds:', error);
            }
        };
        fetchRounds();
    }, [tournamentId]);

    useEffect(() => {
        const fetchMatches = async () => {
            try {
                const roundId = selectedRound || '';
                const response = await fetch(`http://localhost:8080/api/rounds/${roundId}/matches`);
                const matchesData = await response.json();

                if (Array.isArray(matchesData)) {
                    setMatches(matchesData);
                    setFilteredMatches(matchesData);
                } else {
                    setMatches([]);
                    setFilteredMatches([]);
                }
            } catch (error) {
                console.error('Error fetching matches:', error);
                setMatches([]);
                setFilteredMatches([]);
            }
        };
        fetchMatches();
    }, [selectedRound]);

    const handleSearch = (e) => setSearchTerm(e.target.value);
    const toggleDropdown = () => setIsDropdownVisible(!isDropdownVisible);

    const handleRoundSelection = (round) => {
        setSelectedRound(round);
        setIsDropdownVisible(false);
    };

    const handleListViewClick = () => setActiveView('list');
    const handleDiagramViewClick = () => setActiveView('diagram');

    useEffect(() => {
        let updatedList = matches;
        if (searchTerm) {
            updatedList = updatedList.filter(
                (match) =>
                    match.player1.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    match.player2.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    match.date.toLowerCase().includes(searchTerm.toLowerCase()) ||
                    match.location.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }
        if (sortBy) {
            updatedList = updatedList.sort((a, b) =>
                sortBy === 'newest' ? new Date(b.date) - new Date(a.date) : new Date(a.date) - new Date(b.date)
            );
        }
        setFilteredMatches(updatedList);
    }, [searchTerm, matches, sortBy]);

    return (
        <div>
            <Header tournamentTitle={tournamentTitle} playerCount={playerCount} />
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
                                {availableRounds.map((round, index) => (
                                    <div
                                        className="dropdown-item"
                                        key={index}
                                        onClick={() => handleRoundSelection(round.trid)}
                                    >
                                        Round {round.roundNumber}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>

                {activeView === 'list' ? (
                    Array.isArray(filteredMatches) && filteredMatches.length > 0 ? (
                        <div className="match-list">
                            {filteredMatches.map((match, index) => (
                                <div key={index}>
                                    <div className="round-title">Round {match.roundNumber}</div>
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
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr>
                                                <td>{index + 1}</td>
                                                <td>{match.matchDate}</td>
                                                <td>{match.location}</td>
                                                <td>{match.player1}</td>
                                                <td>{match.rating1}</td>
                                                <td>{match.nationality1}</td>
                                                <td>{match.user1Score}</td>
                                                <td className="vs-text">VS</td>
                                                <td>{match.user2Score}</td>
                                                <td>{match.player2}</td>
                                                <td>{match.rating2}</td>
                                                <td>{match.nationality2}</td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <p>No matches found.</p>
                    )
                ) : (
                    <UserTournamentMatchDiagram />
                )}
            </div>
        </div>
    );
};

export default UserTournamentMatch;
