import React from 'react';
import './CalendarView.css';
import { Calendar, momentLocalizer } from 'react-big-calendar';
import moment from 'moment';
import 'react-big-calendar/lib/css/react-big-calendar.css';

// Set up the localizer with Moment.js
const localizer = momentLocalizer(moment);

const CalendarView = ({ matches }) => {

  function convertMatchesToEvents(matches) {
    const events = [];

    matches.forEach((dateGroup) => {
      const dateStr = dateGroup.date; // e.g., "Jun 21, 2024"
      dateGroup.matches.forEach((match) => {
        const timeStr = match.time; // e.g., "08:40am"
        const dateTimeStr = `${dateStr} ${timeStr}`; // Combine date and time

        // Parse the date and time using Moment.js
        const momentDate = moment(dateTimeStr, 'MMM D, YYYY hh:mma');

        if (!momentDate.isValid()) {
          console.error(`Invalid date: ${dateTimeStr}`);
          return; // Skip this match
        }

        const startDate = momentDate.toDate();

        // Assume each match lasts one hour
        const endDate = momentDate.clone().add(1, 'hours').toDate();

        // Construct the event title
        const title = `${match.tournament} - Round ${match.round}: ${match.player1.name} vs ${match.player2.name}`;

        const event = {
          title: title,
          start: startDate,
          end: endDate,
        };

        events.push(event);
      });
    });

    return events;
  }

  const events = convertMatchesToEvents(matches);

  const renderSidebar = () => {
    // Group events by date
    const eventsByDate = events.reduce((acc, event) => {
      const dateKey = moment(event.start).format('MMM D, YYYY');
      if (!acc[dateKey]) {
        acc[dateKey] = [];
      }
      acc[dateKey].push(event);
      return acc;
    }, {});

    return (
      <div className="sidebar">
        {Object.keys(eventsByDate).map((dateKey) => (
          <div key={dateKey} className="event-date">
            <h3>{dateKey}</h3>
            {eventsByDate[dateKey].map((event, index) => (
              <div key={index} className="event-details">
                <span className="event-time">{event.time}</span>
                <span className="event-title">{event.title}</span>
              </div>
            ))}
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
          events={events}
          startAccessor="start"
          endAccessor="end"
          style={{ height: 700 }}
        />
      </div>
    </div>
  );
};

export default CalendarView;