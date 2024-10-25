import { React, useState, useEffect } from 'react';
import './UserCalendar.css';
import Navbar from '../../components/navbar/Navbar';
import searchIcon from '../../assets/images/Search.png';
import MatchTable from './MatchTable';
import CalendarView from './CalendarView';
import moment from 'moment';
import { getAuth } from "firebase/auth";
import axios from 'axios';

const UserCalendar = () => {
    const [activeView, setActiveView] = useState('calendar');
    const [matches, setMatches] = useState([]);
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const [sortedTournaments, setSortedTournaments] = useState(null);
    const [searchTerm, setSearchTerm] = useState('');
    const [sortBy, setSortBy] = useState('');

    const auth = getAuth();

    const handleListButtonClick = () => {
        setActiveView('list');
    };

    const handleCalendarButtonClick = () => {
        setActiveView('calendar');
    };

    // Fetch ongoing tournaments
    const fetchTournaments = async () => {
        try {
            const response = await axios.get(`http://localhost:8080/api/tournaments/upcoming/${auth.currentUser.uid}`);
            setMatches(response.data);
        } catch (error) {
            console.error('Error fetching ongoing tournaments:', error);
        }
    };

    useEffect(() => {
        fetchTournaments();
    }, []);

    // Filter matches based on search term
    const filteredMatches = matches.filter(match => {
        const searchLower = searchTerm.toLowerCase();
        return (
            match.name.toLowerCase().includes(searchLower) || 
            match.location.toLowerCase().includes(searchLower)
        );
    });

    // Group matches by start date (ignoring time)
    const groupMatchesByDate = (matches) => {
        return matches.reduce((groupedMatches, match) => {
            const date = moment(match.startDatetime).format('YYYY-MM-DD'); // Group by date only
            if (!groupedMatches[date]) {
                groupedMatches[date] = [];
            }
            groupedMatches[date].push(match);
            return groupedMatches;
        }, {});
    };

    const groupedMatches = groupMatchesByDate(filteredMatches);

    // Handle search input
    const handleSearch = (e) => {
        setSearchTerm(e.target.value);
    };

    // Toggle dropdown visibility
    const toggleDropdown = () => {
        setIsDropdownVisible(!isDropdownVisible);
    };

    // Sorting the tournaments
    const handleSortChange = (criteria) => {
        let sortedList = [...matches];
        if (criteria === 'Name') {
            sortedList.sort((a, b) => a.name.localeCompare(b.name));
        } else if (criteria === 'Date') {
            sortedList.sort((a, b) => new Date(a.startDatetime) - new Date(b.startDatetime));
        } else if (criteria === 'Slots') {
            sortedList.sort((a, b) => a.capacity - b.capacity);
        } else if (criteria === 'EloRequirement') {
            sortedList.sort((a, b) => a.eloRequirement - b.eloRequirement);
        } else if (criteria === 'Prize') {
            sortedList.sort((a, b) => a.prizePool - b.prizePool);
        }
        setSortedTournaments(sortedList); // Set the sorted tournaments
        setSortBy(criteria);
        setIsDropdownVisible(false);
    };

    return (
        <div>
            <Navbar />
            <div className='user-calendar'>
                <div className='user-calendar-header'>
                    <div className='user-calendar-header-title'>
                        <h2>My Games</h2>
                    </div>
                    <div className='user-calendar-header-buttons'>
                        <button
                            onClick={handleCalendarButtonClick}
                            className={`user-calendar-header-button ${activeView === 'calendar' ? 'active' : ''}`}
                        >
                            Calendar
                        </button>
                        <button
                            onClick={handleListButtonClick}
                            className={`user-calendar-header-button ${activeView === 'list' ? 'active' : ''}`}
                        >
                            List
                        </button>
                    </div>
                </div>

                {activeView === 'list' && (
                    <>
                        <div className='user-calendar-query'>
                            <div className="search-bar">
                                <input
                                    type="text"
                                    placeholder="Search for a tournament"
                                    value={searchTerm}
                                    onChange={handleSearch}
                                />
                                <img src={searchIcon} alt="Search Icon" className="search-icon" />
                            </div>
                            <div className="buttons-container">
                                <div className="dropdown">
                                    <button className="order-button" onClick={toggleDropdown}>
                                        Order By {sortBy && `(${sortBy})`}
                                    </button>
                                    {isDropdownVisible && (
                                        <div className="dropdown-content">
                                            <div className="dropdown-item" onClick={() => handleSortChange('Name')}>
                                                Name
                                            </div>
                                            <div className="dropdown-item" onClick={() => handleSortChange('Date')}>
                                                Date
                                            </div>
                                            <div className="dropdown-item" onClick={() => handleSortChange('Slots')}>
                                                Slots
                                            </div>
                                            <div className="dropdown-item" onClick={() => handleSortChange('EloRequirement')}>
                                                ELO Requirement
                                            </div>
                                            <div className="dropdown-item" onClick={() => handleSortChange('Prize')}>
                                                Prize
                                            </div>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                        <div>
                            {/* Map over grouped matches by date */}
                            {Object.keys(groupedMatches).map((date, index) => (
                                <MatchTable key={index} date={date} matches={groupedMatches[date]} />
                            ))}
                        </div>
                    </>
                )}

                {activeView === 'calendar' && (
                    <div>
                        <CalendarView matches={filteredMatches} />
                    </div>
                )}
            </div>
        </div>
    );
};

export default UserCalendar;