import React, { useState, useEffect } from 'react';
import './UserTournamentMatchTree.css'; // Assuming the custom CSS for tree structure
import Header from './UserDetailsHeader';

const UserTournamentMatchTree = () => {
    const [matches, setMatches] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [filteredMatches, setFilteredMatches] = useState([]);
    const [sortBy, setSortBy] = useState('');
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [playerCount, setPlayerCount] = useState(0);
    const [selectedRound, setSelectedRound] = useState('All rounds');

    // Dummy data with scores for testing
    const dummyData = [
        {
            round: 1,
            matches: [
                {
                    no: 1, date: 'Jun 21, 2024 08:40am', location: 'City Hall, Table 5',
                    player1: 'Hikaru Nakamura', rating1: 2000, nationality1: 'Japan', score1: 1,
                    player2: 'Vincent Keymer', rating2: 2100, nationality2: 'Germany', score2: 0
                },
                {
                    no: 2, date: 'Jun 21, 2024 09:45am', location: 'City Hall, Table 6',
                    player1: 'Magnus Carlsen', rating1: 2800, nationality1: 'Norway', score1: 0.5,
                    player2: 'Ian Nepomniachtchi', rating2: 2750, nationality2: 'Russia', score2: 0.5
                },
                {
                    no: 3, date: 'Jun 21, 2024 09:45am', location: 'City Hall, Table 6',
                    player1: 'John Carlsen', rating1: 2800, nationality1: 'Norway', score1: 1,
                    player2: 'Carlton', rating2: 2750, nationality2: 'Russia', score2: 0
                },
                {
                    no: 4, date: 'Jun 21, 2024 09:45am', location: 'City Hall, Table 6',
                    player1: 'Hadley', rating1: 2800, nationality1: 'Norway', score1: 0.5,
                    player2: 'Ramin', rating2: 2750, nationality2: 'Russia', score2: 0.5
                }
            ]
        },
        {
            round: 2,
            matches: [
                {
                    no: 1, date: 'Jun 21, 2024 08:40am', location: 'City Hall, Table 5',
                    player1: 'Hikaru Nakamura', rating1: 2000, nationality1: 'Japan', score1: 1,
                    player2: 'Magnus Carlsen', rating2: 2800, nationality2: 'Norway', score2: 0.5
                },
                {
                    no: 2, date: 'Jun 21, 2024 09:45am', location: 'City Hall, Table 6',
                    player1: 'John Carlsen', rating1: 2800, nationality1: 'Norway', score1: 1,
                    player2: 'Ian Nepomniachtchi', rating2: 2750, nationality2: 'Russia', score2: 0.5
                }
            ] 
        },
        {
            round: 3, // Quarterfinals
            matches: [] // To be generated
        },
        {
            round: 4, // Semifinals
            matches: [] // To be generated
        },
        {
            round: 5, // Finals
            matches: [] // To be generated
        }
    ];

    useEffect(() => {
        // Function to generate the next round of matches
        const generateNextRound = (prevRoundMatches) => {
            const nextRoundMatches = [];
            const winners = [];

            prevRoundMatches.forEach(match => {
                if (match.score1 > match.score2) {
                    winners.push(match.player1);
                } else if (match.score2 > match.score1) {
                    winners.push(match.player2);
                } else if (match.score1 === 0.5 && match.score2 === 0.5) {
                    // If both have 0.5, both proceed
                    winners.push(match.player1);
                    winners.push(match.player2);
                }
            });

            // Handle cases where players have no opponents in the next round
            for (let i = 0; i < winners.length; i += 2) {
                if (i + 1 < winners.length) {
                    nextRoundMatches.push({
                        player1: winners[i],
                        player2: winners[i + 1],
                        score1: 0, // Default score until the match is played
                        score2: 0
                    });
                } else {
                    // A player without an opponent automatically proceeds
                    nextRoundMatches.push({
                        player1: winners[i],
                        player2: null, // No opponent
                        score1: 0,
                        score2: null
                    });
                }
            }

            return nextRoundMatches;
        };

        // Generate rounds 2, 3, 4, 5 based on previous rounds
        dummyData[1].matches = generateNextRound(dummyData[0].matches); // Round 2 based on Round 1
        dummyData[2].matches = generateNextRound(dummyData[1].matches); // Quarterfinals based on Round 2
        dummyData[3].matches = generateNextRound(dummyData[2].matches); // Semifinals based on Quarterfinals
        dummyData[4].matches = generateNextRound(dummyData[3].matches); // Finals based on Semifinals

        setMatches(dummyData);
        const allMatches = dummyData.flatMap(roundData => roundData.matches);
        setFilteredMatches(allMatches);
    }, []); // Run only on component mount

    const handleSearch = (e) => {
        setSearchTerm(e.target.value);
    };

    const handleListViewClick = () => {
        const tournamentId = 1; // Replace with the actual tournament ID
        window.location.href = `/tournament/${tournamentId}/games`;
    };

    const toggleDropdown = () => {
        setIsDropdownVisible(!isDropdownVisible);
    };

    const handleSortChange = (criteria) => {
        setSortBy(criteria);
        setIsDropdownVisible(false); // Hide dropdown after selection
    };

    const getResult = (score1, score2) => {
        if (score1 > score2) return 'Won';
        if (score1 < score2) return 'Lost';
        return 'Draw';
    };

    // Handle round selection from the dropdown (without changing display yet)
    const handleRoundSelection = (round) => {
        setSelectedRound(round);
        // Filtering logic will be added here later if needed
    };

    return (
        <div>
            <Header
                tournamentTitle="Tournament 1"
                playerCount={playerCount} // Replace with actual count from database
            />

            <div className='tournament-tree'>
                {/* Search and Sort Controls */}
                <div className="controls-container">
                    <div className="view-buttons">
                        <button className="list-view-button" onClick={handleListViewClick}>
                            <img src={require('../../assets/images/List-view.png')} alt="List View" />
                        </button>
                        <button className="image-view-button">
                            <img src={require('../../assets/images/Image-view.png')} alt="Image View" />
                        </button>
                    </div>

                    <div className="search-bar">
                        <input
                            type="text"
                            placeholder="Search for a round/participant/date"
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
                        <img src={require('../../assets/images/Adjust.png')} alt="Filter Icon" className="filter-icon" />
                        <span>Filter</span>
                    </button>

                    {/* Order By Dropdown */}
                    <div className="dropdown">
                        <button className="order-button" onClick={toggleDropdown}>
                            All rounds
                        </button>
                        {isDropdownVisible && (
                            <div className="dropdown-content">
                                <div className="dropdown-item" onClick={() => handleRoundSelection('round1')}>
                                    Round 1
                                </div>
                                <div className="dropdown-item" onClick={() => handleRoundSelection('round2')}>
                                    Round 2
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                {/* Tournament Tree */}
                <div className="match-tree">
                    {dummyData.map((roundData, roundIndex) => (
                        <div className="round-column" key={roundIndex}>
                            <div className="round-title">
                                {roundIndex === 2 ? 'Quarterfinals' : roundIndex === 3 ? 'Semifinals' : roundIndex === 4 ? 'Finals' : `Round ${roundData.round}`}
                            </div>
                            {roundData.matches.map((match, matchIndex) => (
                                <div className="match-box" key={matchIndex}>
                                    <div className="player">
                                        {match.player1}
                                        <span className={`score ${match.score1 === 0 ? 'score-white' : 'score-grey'}`}>
                                            {match.score1}
                                        </span>
                                    </div>
                                    {match.player2 ? (
                                        <div className="player">
                                            {match.player2}
                                            <span className={`score ${match.score2 === 0 ? 'score-white' : 'score-grey'}`}>
                                                {match.score2}
                                            </span>
                                        </div>
                                    ) : (
                                        <div className="player">No opponent</div>
                                    )}
                                    
                                </div>
                            ))}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default UserTournamentMatchTree;
