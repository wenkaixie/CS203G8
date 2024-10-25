import { React, useState } from 'react';
import './CalendarView.css';
import { Calendar, momentLocalizer } from 'react-big-calendar';
import moment from 'moment';
import { useNavigate } from 'react-router-dom';
import 'react-big-calendar/lib/css/react-big-calendar.css';


// Set up the localizer with Moment.js
const localizer = momentLocalizer(moment);

const CalendarView = ({ matches = [] }) => {
  console.log(matches);
  // const matches = [
  //   {
  //     ageLimit: 0,
  //     name: 'TESTING1',
  //     description: 'TESTS1',
  //     eloRequirement: 0,
  //     location: 'Los Angeles, CA',
  //     capacity: 50,
  //     startDatetime: '2024-10-23T06:58:00Z',
  //     endDatetime: '2024-10-25T17:00:00Z',
  //     tid: '07yGOapr9FG9HjQlMrM1',
  //     createdTimestamp: '2024-10-22T06:57:00.886421Z',
  //     trid: null,
  //     prize: 20000,
  //     status: 'CLOSED',
  //     users: ['YMFPwRdSR1VeVPR5PokUKxJSQdE2']
  //   },
  //   {
  //     ageLimit: 0,
  //     name: 'TESTING2',
  //     description: 'TESTS1',
  //     eloRequirement: 0,
  //     location: 'Los Angeles, CA',
  //     capacity: 50,
  //     startDatetime: '2024-11-20T07:28:00Z',
  //     endDatetime: '2024-11-24T17:00:00Z',
  //     tid: 'IEkiQCWzmWiAIzA85vB5',
  //     createdTimestamp: '2024-10-22T07:26:50.495025Z',
  //     trid: null,
  //     prize: 20000,
  //     status: 'CLOSED',
  //     users: ['YMFPwRdSR1VeVPR5PokUKxJSQdE2']
  //   }
  // ];

  // Convert matches to match Calendar format
  const formattedmatches = matches.map(event => ({
    title: event.name,
    start: new Date(event.startDatetime),
    end: new Date(event.endDatetime),
    description: event.description
  }));

  const navigate = useNavigate();
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [currentDate, setCurrentDate] = useState(new Date());

  const handleSelectEvent = (event) => {
    setSelectedEvent(event); // Set the selected event when clicked in the calendar
  };

  const handleGoToTournament = (tournamentId) => {
    navigate(`/user/tournament/${tournamentId}/overview`);
  };

  const handleGoToEventInCalendar = (event) => {
    setSelectedEvent(event); // Highlight the selected event
    setCurrentDate(new Date(event.startDatetime)); // Change the calendar view to the event's month
  };

  const renderSidebar = () => {
    return (
      <div className="sidebar">
        {matches.map((event, index) => (
          <div
            key={index}
            className={`event-details ${selectedEvent && selectedEvent.tid === event.tid ? 'highlight' : ''}`}
            onClick={() => handleGoToEventInCalendar(event)}
          >
            <h5 className="event-title">{event.name}</h5>
            <span className="event-time">
              {moment(event.startDatetime).format('MMM D, YYYY')} -{' '}
              {moment(event.endDatetime).format('MMM D, YYYY')}
            </span>
            <p className="event-description">{event.description}</p>
            <p className="event-location">{event.location}</p>
            <p className="event-prize">Prize: ${event.prize}</p>
            <div className="view-tournament-details" onClick={ () => handleGoToTournament(event.tid) }>View Tournament Details</div>
          </div>
        ))}
      </div>
    );
  };

  return (
    <div className="calendar-container">
      <div className="sidebar-container">{renderSidebar()}</div>
      <div className="calendar-component">
        <Calendar
          localizer={localizer}
          events={formattedmatches}
          startAccessor="start"
          endAccessor="end"
          style={{ height: 700 }}
          onNavigate={date => setCurrentDate(date)}
          date={currentDate}
          onSelectEvent={handleSelectEvent}
        />
      </div>
    </div>
  );
};

export default CalendarView;