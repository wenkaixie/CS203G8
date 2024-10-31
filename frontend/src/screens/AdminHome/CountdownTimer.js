import React, { useState, useEffect } from 'react';
import './CountdownTimer.css';

const CountdownTimer = ({ targetDate }) => {
    
    const calculateTimeLeft = () => {
        const target = new Date(targetDate); // Ensure targetDate is parsed correctly
        const now = new Date();
        const difference = target.getTime() - now.getTime(); // Get the difference in milliseconds
        let timeLeft = {};
  
        if (difference > 0) {
        // Calculate time remaining
        timeLeft = {
            days: Math.floor(difference / (1000 * 60 * 60 * 24)),
            hours: Math.floor((difference / (1000 * 60 * 60)) % 24),
            minutes: Math.floor((difference / 1000 / 60) % 60),
            seconds: Math.floor((difference / 1000) % 60),
        };
        } else {
        // Time has passed or timer has hit zero
        timeLeft = {
            days: 0,
            hours: 0,
            minutes: 0,
            seconds: 0,
        };
        }
        return timeLeft;
    };

  const [timeLeft, setTimeLeft] = useState(calculateTimeLeft());

  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft(calculateTimeLeft());
    }, 1);

    return () => clearInterval(timer);
  }, [targetDate]);

  return (
    <div className="countdown-container">
        <div className="date-container">
            <div className="date-box">
                <span className="date-day">{new Date(targetDate).getDate()}</span>
                <span className="date-month">{new Date(targetDate).toLocaleString('default', { month: 'short' }).toUpperCase()}</span>
                <span className="time">{new Date(targetDate).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
            </div>
        </div>
        <div className="countdown-box">
            <div className="countdown-element">
                <span className="count">{timeLeft.days}</span>
                <span className="label">DAY</span>
            </div>
            <div className="countdown-element">
                <span className="count">{timeLeft.hours}</span>
                <span className="label">HR</span>
            </div>
            <div className="countdown-element">
                <span className="count">{timeLeft.minutes}</span>
                <span className="label">MIN</span>
            </div>
            <div className="countdown-element">
                <span className="count">{timeLeft.seconds}</span>
                <span className="label">SEC</span>
            </div>
        </div>
    </div>
  );
};

export default CountdownTimer;