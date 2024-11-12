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
    const [ongoingMatches, setOngoingMatches] = useState([]);
    const [upcomingMatches, setUpcomingMatches] = useState([]);
    const [pastMatches, setPastMatches] = useState([]);
    const [isDataLoaded, setIsDataLoaded] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');

    const auth = getAuth();

    const handleListButtonClick = () => {
        setActiveView('list');
    };

    const handleCalendarButtonClick = () => {
        setActiveView('calendar');
    };

    const fetchTournaments = async () => {
        try {
            const response = await axios.get(`http://matchup-load-balancer-1173773587.ap-southeast-1.elb.amazonaws.com:8080/api/tournaments/user/${auth.currentUser.uid}`);
            const tournaments = response.data;
            const currentDate = moment();

            const ongoing = tournaments.filter(tournament =>
                moment(tournament.startDatetime).isSameOrBefore(currentDate) &&
                moment(tournament.endDatetime).isAfter(currentDate)
            );

            const upcoming = tournaments.filter(tournament =>
                moment(tournament.startDatetime).isAfter(currentDate)
            );

            const past = tournaments.filter(tournament =>
                moment(tournament.endDatetime).isBefore(currentDate)
            );

            setOngoingMatches(ongoing);
            setUpcomingMatches(upcoming);
            setPastMatches(past);
            setIsDataLoaded(true);
        } catch (error) {
            console.error('Error fetching tournaments:', error);
        }
    };

    useEffect(() => {
        fetchTournaments();
    }, []);

    const filterMatches = (matches) => {
        const searchLower = searchTerm.toLowerCase();
        return matches.filter(match =>
            match.name.toLowerCase().includes(searchLower) ||
            match.location.toLowerCase().includes(searchLower)
        );
    };

    const filteredOngoingMatches = filterMatches(ongoingMatches);
    const filteredUpcomingMatches = filterMatches(upcomingMatches);
    const filteredPastMatches = filterMatches(pastMatches);

    const handleSearch = (e) => {
        setSearchTerm(e.target.value);
    };

    return (
        <div>
            <Navbar />
            <div className='user-calendar'>
                <div className='user-calendar-header'>
                    <div className='user-calendar-header-title'>
                        <h2>My Tournaments Calendar</h2>
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

                {!isDataLoaded ? (
                    <p>Loading tournaments...</p>
                ) : (
                    <>
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
                                </div>
                                <MatchTable
                                    ongoingMatches={filteredOngoingMatches}
                                    upcomingMatches={filteredUpcomingMatches}
                                    pastMatches={filteredPastMatches}
                                />
                            </>
                        )}

                        {activeView === 'calendar' && (
                            <CalendarView
                                ongoingMatches={filteredOngoingMatches}
                                upcomingMatches={filteredUpcomingMatches}
                                pastMatches={filteredPastMatches}
                            />
                        )}
                    </>
                )}
            </div>
        </div>
    );
};

export default UserCalendar;