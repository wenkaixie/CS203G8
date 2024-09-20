import React, { useState } from 'react';
import './UserCalendar.css';
import Navbar from '../../components/navbar/Navbar';
import { Form, InputGroup } from 'react-bootstrap';
import SearchIcon from '@mui/icons-material/Search';
import TuneIcon from '@mui/icons-material/Tune';
import MatchTable from './MatchTable';
import CalendarView from './CalendarView';

const UserCalendar = () => {
    const [activeView, setActiveView] = useState('list');

    const handleListButtonClick = () => {
    setActiveView('list');
    };

    const handleCalendarButtonClick = () => {
    setActiveView('calendar');
};

    // get matches data from api
    const matches = [
        {
            date: "Sep 21, 2024",
            matches: [
                {
                    time: "08:40am",
                    tournament: "Youth Chess Championships 2024",
                    round: 1,
                    player1: { name: "Hikaru Nakamura", nationality: "Japan" /* avatar: player1Avatar */ },
                    player2: { name: "Vincent Keymer", nationality: "Germany" /* avatar: player2Avatar */ },
                },
                {
                    time: "10:40am",
                    tournament: "Youth Chess Championships 2024",
                    round: 2,
                    player1: { name: "Hikaru Nakamura", nationality: "Japan" /* avatar: player1Avatar */ },
                    player2: { name: "Vincent Keymer", nationality: "Germany" /* avatar: player2Avatar */ },
                }
            ]
        },
        {
            date: "Sep 23, 2024",
            matches: [
                {
                    time: "08:40am",
                    tournament: "Youth Chess Championships 2024",
                    round: 3,
                    player1: { name: "Hikaru Nakamura", nationality: "Japan" /* avatar: player1Avatar */ },
                    player2: { name: "Vincent Keymer", nationality: "Germany" /* avatar: player2Avatar */ },
                }
            ]
        },
        {
            date: "Sep 24, 2024",
            matches: [
                {
                    time: "08:40am",
                    tournament: "Speed Chess Championships 2024",
                    round: 1,
                    player1: { name: "Hikaru Nakamura", nationality: "Japan" /* avatar: player1Avatar */ },
                    player2: { name: "Vincent Keymer", nationality: "Germany" /* avatar: player2Avatar */ },
                },
                {
                    time: "10:40am",
                    tournament: "Speed Chess Championships 2024",
                    round: 2,
                    player1: { name: "Hikaru Nakamura", nationality: "Japan" /* avatar: player1Avatar */ },
                    player2: { name: "Vincent Keymer", nationality: "Germany" /* avatar: player2Avatar */ },
                },
                {
                    time: "12:40pm",
                    tournament: "Speed Chess Championships 2024",
                    round: 3,
                    player1: { name: "Hikaru Nakamura", nationality: "Japan" /* avatar: player1Avatar */ },
                    player2: { name: "Vincent Keymer", nationality: "Germany" /* avatar: player2Avatar */ },
                }
            ]
        },
    ];
    
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