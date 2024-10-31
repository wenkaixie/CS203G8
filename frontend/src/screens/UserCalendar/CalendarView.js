import { React, useState } from 'react';
import './CalendarView.css';
import { Calendar, momentLocalizer } from 'react-big-calendar';
import moment from 'moment';
import { useNavigate } from 'react-router-dom';
import 'react-big-calendar/lib/css/react-big-calendar.css';

const localizer = momentLocalizer(moment);

const CalendarView = ({ ongoingMatches, upcomingMatches, pastMatches }) => {
  const navigate = useNavigate();
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [currentDate, setCurrentDate] = useState(new Date());

  // Combine all matches for the calendar view and assign unique colors across all groups
  const allMatches = [...ongoingMatches, ...upcomingMatches, ...pastMatches];
  const formattedMatches = allMatches.map((event, index) => ({
    title: event.name,
    start: new Date(event.startDatetime),
    end: new Date(event.endDatetime),
    description: event.description,
    color: `hsl(${(index * 137.5) % 360}, 70%, 80%)`, // Unique color based on overall index
    tid: event.tid,
  }));

  const handleSelectEvent = (event) => {
    setSelectedEvent(event);
  };

  const handleGoToTournament = (tournamentId) => {
    navigate(`/user/tournament/${tournamentId}/overview`);
  };

  const handleGoToEventInCalendar = (event) => {
    setSelectedEvent(event);
    setCurrentDate(new Date(event.startDatetime));
  };

  const renderSidebarSection = (title, matches, noDataMessage, offset = 0) => (
    <div className="sidebar-section">
      <h4>{title}</h4>
      {matches.length > 0 ? (
        matches.map((event, index) => (
          <div
            key={event.tid}
            className={`event-details ${selectedEvent && selectedEvent.tid === event.tid ? 'highlight' : ''}`}
            style={{ backgroundColor: `hsl(${((index + offset) * 137.5) % 360}, 70%, 80%)` }}
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
            <div className="view-tournament-details" onClick={() => handleGoToTournament(event.tid)}>
              View Tournament Details
            </div>
          </div>
        ))
      ) : (
        <p className="no-tournaments-message">{noDataMessage}</p>
      )}
    </div>
  );

  return (
    <div className="calendar-container">
      <div className="sidebar-container">
        {renderSidebarSection('Ongoing Tournaments', ongoingMatches, 'No ongoing tournaments available', 0)}
        {renderSidebarSection('Upcoming Tournaments', upcomingMatches, 'No upcoming tournaments available', ongoingMatches.length)}
        {renderSidebarSection('Past Tournaments', pastMatches, 'No past tournaments available', ongoingMatches.length + upcomingMatches.length)}
      </div>
      <div className="calendar-component">
        <Calendar
          localizer={localizer}
          events={formattedMatches}
          startAccessor="start"
          endAccessor="end"
          style={{ height: 700 }}
          onNavigate={(date) => setCurrentDate(date)}
          date={currentDate}
          onSelectEvent={handleSelectEvent}
          eventPropGetter={(event) => ({
            style: { backgroundColor: event.color },
          })}
        />
      </div>
    </div>
  );
};

export default CalendarView;