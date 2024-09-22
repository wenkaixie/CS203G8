import React from 'react';
import './MatchTable.css';
// import player1Avatar from '../../assets/images/player1.png';
// import player2Avatar from '../../assets/images/player2.png';

const MatchTable = ( {date, matches} ) => {

  return (
    <div className="match-table-container">
      <h3>{date}</h3>
      <table className="match-table">
        <thead>
          <tr>
            <th>Time</th>
            <th>Tournament</th>
            <th>Round</th>
            <th>Player 1</th>
            <th>Nationality</th>
            <th></th>
            <th>Nationality</th>
            <th>Player 2</th>
          </tr>
        </thead>
        <tbody>
          {matches.map((match, index) => (
            <tr key={index}>
              <td>{match.time}</td>
              <td>{match.tournament}</td>
              <td>{match.round}</td>
              <td>
                <div className="player-info">
                  <img /* src={match.player1.avatar} */ alt="Player 1" className="player-avatar" />
                  {match.player1.name}
                </div>
              </td>
              <td>{match.player1.nationality}</td>
              <td>VS</td>
              <td>{match.player2.nationality}</td>
              <td>
                <div className="player-info">
                  <img /* src={match.player2.avatar} */ alt="Player 2" className="player-avatar" />
                  {match.player2.name}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default MatchTable;