import React, { useState, useEffect } from 'react';
import './UserCalendar.css';
import Navbar from '../../components/navbar/Navbar';
import { Form, InputGroup } from 'react-bootstrap';
import SearchIcon from '@mui/icons-material/Search';
import TuneIcon from '@mui/icons-material/Tune';
import MatchTable from './MatchTable';
import CalendarView from './CalendarView';
import axios from 'axios';

const UserCalendar = () => {
    const [activeView, setActiveView] = useState('list');
    const [matches, setMatches] = useState([]); // State to hold the matches
    const [loading, setLoading] = useState(true); // Loading state
    const [error, setError] = useState(null); // Error state

    const handleListButtonClick = () => {
        setActiveView('list');
    };

    const handleCalendarButtonClick = () => {
        setActiveView('calendar');
    };

    // Fetch matches data from the API
    useEffect(() => {
        const fetchMatches = async () => {
            try {
                const response = await axios.get('http://localhost:8080/api/tournaments'); // Update with your API endpoint
                console.log(response.data);
                // setMatches(response.data);
                setLoading(false);
            } catch (err) {
                setError(err.message);
                setLoading(false);
            }
        };

        fetchMatches();
    }, []);

    if (loading) {
        return <div>Loading...</div>; // Loading state
    }

    if (error) {
        return <div>Error: {error}</div>; // Error state
    }

    return (
        <div>
            <Navbar />
            <div className='user-calendar'>
                <div className='user-calendar-header'>
                    <div className='user-calendar-header-title'>
                        <h2>Upcoming Games</h2>
                    </div>
                    <div className='user-calendar-header-buttons'>
                        <button
                            onClick={handleListButtonClick}
                            className={`user-calendar-header-button ${activeView === 'list' ? 'active' : ''}`}
                        >
                            List
                        </button>
                        <button
                            onClick={handleCalendarButtonClick}
                            className={`user-calendar-header-button ${activeView === 'calendar' ? 'active' : ''}`}
                        >
                            Calendar
                        </button>
                    </div>
                </div>

                {activeView === 'list' && (
                    <>
                        <div className='user-calendar-query'>
                            <div className='searchbox'>
                                <Form className="d-flex">
                                    <InputGroup>
                                        <InputGroup.Text id="search-icon">
                                            <SearchIcon />
                                        </InputGroup.Text>
                                        <Form.Control
                                            type="search"
                                            placeholder="Search"
                                            aria-label="Search"
                                            aria-describedby="search-icon"
                                            style={{ backgroundColor: "#F8F9FA" }}
                                        />
                                    </InputGroup>
                                </Form>
                            </div>
                            <div className="filter-buttons">
                                <button className="filter-icon">
                                    <TuneIcon />
                                </button>
                                <button className="order-by">Order By</button>
                            </div>
                        </div>
                        <div>
                            {matches.map((day, index) => (
                                <MatchTable key={index} date={day.date} matches={day.matches} />
                            ))}
                        </div>
                    </>
                )}

                {activeView === 'calendar' && (
                    <div>
                        <CalendarView matches={matches} />
                    </div>
                )}
            </div>
        </div>
    );
};

export default UserCalendar;