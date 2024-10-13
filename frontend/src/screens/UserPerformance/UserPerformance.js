import React, { useState } from 'react';
import './UserPerformance.css'; // Import the CSS file

const performanceData = [
  {
    tournament: 'Youth Chess Championships 2024',
    matches: [
      { stage: 'Finals', date: 'May 12, 2024', time: '10:00am - 12:15pm', player: 'John', score: '3 VS 2', opponent: 'Hikaru Nakamura', rank: 5 },
      { stage: 'Semifinals', date: 'May 10, 2024', time: '10:00am - 12:15pm', player: 'John', score: '3 VS 2', opponent: 'Hikaru Nakamura', rank: 3 },
      { stage: 'Quarterfinals', date: 'May 9, 2024', time: '10:00am - 12:15pm', player: 'John', score: '3 VS 2', opponent: 'Hikaru Nakamura', rank: 15 }
    ]
  }
];

const UserPerformance = () => {
  const [orderBy, setOrderBy] = useState('date');

  return (
    <div className="user-performance">
      <div className="performance-header">
        <button className="back-button">←</button>
        <h1>Performance</h1>
      </div>

      <div className="top-containers">
        {/* Rating Graph */}
        <div className="container rating-container">
          <h2>Rating</h2>
          <div className="graph-placeholder">Graph</div> {/* Placeholder for chart */}
        </div>

        {/* Performance Statistics */}
        <div className="container statistics-container">
          <h2>Performance Statistics</h2>
          <div className="statistics">
            <p>ELO: 1000</p>
            <p>World Rank: 45</p>
            <p>Local Rank: 21</p>
          </div>
        </div>

        {/* Games Stats and Achievements */}
        <div className="container games-achievements-container">
          <div className="games-stats">
            <p>Games: 53</p>
            <p>Wins: 31</p>
            <p>Win Rate: 58.4%</p>
            <p>Losses: 20</p>
            <p>Draws: 2</p>
          </div>
          <div className="achievements">
            <p>Active years: 2015 - 2024</p>
            <p>
              Achievements: 
              <br /> Qatar Masters 2018 (#1, #3) 
              <br /> King's Tournament 2015, 2016 (#2, #3) 
              <br /> Sinquefield Cup 2020 (#1)
            </p>
          </div>
        </div>
      </div>

      <section className="performance-history">
        <input type="text" className="search-bar" placeholder="Search for a tournament/game" />
        <select className="order-dropdown" onChange={(e) => setOrderBy(e.target.value)}>
          <option value="date">Order By</option>
        </select>

        {performanceData.map((tournament, index) => (
          <div key={index} className="tournament">
            <h3>{tournament.tournament}</h3>
            {tournament.matches.map((match, idx) => (
              <div key={idx} className="match">
                <div className="match-details">
                  <p>{match.stage}</p>
                  <p>{match.date} {match.time}</p>
                </div>
                <div className="match-score">
                  <p>{match.player}</p>
                  <p>{match.score}</p>
                  <p>{match.opponent}</p>
                  <p>Rank: #{match.rank}</p>
                </div>
              </div>
            ))}
          </div>
        ))}

        <button className="show-more">Show more</button>
      </section>
    </div>
  );
};

export default UserPerformance;
