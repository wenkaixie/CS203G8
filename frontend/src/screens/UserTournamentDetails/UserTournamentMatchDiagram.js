import { SingleEliminationBracket, Match, createTheme } from '@g-loot/react-tournament-brackets';
import './UserTournamentMatchDiagram.css';
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';

const WhiteTheme = createTheme({
  textColor: { 
    main: '#8F3013',
    highlighted: '#000000',
    dark: '#3E414D'
  },
  matchBackground: { 
    wonColor: '#D4EDDA',
    lostColor: '#FFFFFF'
  },
  score: {
    background: { 
      wonColor: '#D4EDDA',
      lostColor: '#FFFFFF'
    },
    text: { 
      highlightedWonColor: '#333333',
      highlightedLostColor: '#333333'
    }
  },
  border: {
    color: '#F0F0F0',
    highlightedColor: '#E0E0E0'
  },
  roundHeaders: { 
    background: '#8F3013'
  },
  connectorColor: '#E0E0E0',
  connectorColorHighlight: '#8F3013',
  fontFamily: 'Josefin Sans'
});

export const WhiteThemeBracket = () => {
  const [matches, setMatches] = useState([]);
  const { tournamentId } = useParams();

  const fetchTournamentMatches = async () => {
    try {
      const response = await axios.get(`http://localhost:8080/api/tournaments2/${tournamentId}/matches`);
      setMatches(response.data);
      console.log(response.data);
    } catch (error) {
      console.error('Error fetching tournament matches:', error);
    }
  };

  useEffect(() => {
    fetchTournamentMatches();
  }, []);

  // const matches = [

  //   {
  //     id: 1, // matchID
  //     name: "Quarterfinal - Match 1", // Round Name/Number & Match Number
  //     nextMatchId: 5, // next matchID
  //     tournamentRoundText: "1", // Round Number
  //     startTime: "2021-05-30", // DateTime of the match
  //     state: "DONE", // Whether the match has concluded or not
  //     participants: [
  //       { id: "1", name: "Hikaru Nakamura", resultText: "6", isWinner: true }, // Participant no., Participant name, Participant Score, who won
  //       { id: "2", name: "John Charge", resultText: "0", isWinner: false }
  //     ]
  //   },
  //   {
  //     id: 2,
  //     name: "Quarterfinal - Match 2",
  //     nextMatchId: 5,
  //     tournamentRoundText: "1",
  //     startTime: "2021-05-30",
  //     state: "DONE",
  //     participants: [
  //       { id: "1", name: "Hikaru Nakamura", resultText: "6", isWinner: true },
  //       { id: "2", name: "John Charge", resultText: "0", isWinner: false }
  //     ]
  //   },
  //   {
  //     id: 3,
  //     name: "Quarterfinal - Match 3",
  //     nextMatchId: 6,
  //     tournamentRoundText: "1",
  //     startTime: "2021-05-30",
  //     state: "DONE",
  //     participants: [
  //       { id: "1", name: "Hikaru Nakamura", resultText: "6", isWinner: true },
  //       { id: "2", name: "John Charge", resultText: "0", isWinner: false }
  //     ]
  //   },
  //   {
  //     id: 4,
  //     name: "Quarterfinal - Match 4",
  //     nextMatchId: 6,
  //     tournamentRoundText: "1",
  //     startTime: "2021-05-30",
  //     state: "DONE",
  //     participants: [
  //       { id: "1", name: "Hikaru Nakamura", resultText: "6", isWinner: true },
  //       { id: "2", name: "John Charge", resultText: "0", isWinner: false }
  //     ]
  //   },

  //   // Semifinals
  //   {
  //     id: 5,
  //     name: "Semifinal - Match 1",
  //     nextMatchId: 7,
  //     tournamentRoundText: "2",
  //     startTime: "2021-06-01",
  //     state: "DONE",
  //     participants: [
  //       { id: "1", name: "Hikaru Nakamura", resultText: "6", isWinner: true },
  //       { id: "2", name: "John Charge", resultText: "0", isWinner: false }
  //     ]
  //   },
  //   {
  //     id: 6,
  //     name: "Semifinal - Match 2",
  //     nextMatchId: 7,
  //     tournamentRoundText: "2",
  //     startTime: "2021-06-01",
  //     state: "DONE",
  //     participants: [
  //       { id: "1", name: "Hikaru Nakamura", resultText: "6", isWinner: true },
  //       { id: "2", name: "John Charge", resultText: "0", isWinner: false }
  //     ]
  //   },

  //   // Semifinals
  //   {
  //     id: 7,
  //     name: "Finals",
  //     nextMatchId: null,
  //     tournamentRoundText: "3",
  //     startTime: "2021-06-05",
  //     state: "DONE",
  //     participants: [
  //       { id: "1", name: "Hikaru Nakamura", resultText: "6", isWinner: true },
  //       { id: "2", name: "John Charge", resultText: "0", isWinner: false }
  //     ]
  //   },
  // ]

  // Conditionally render the SingleEliminationBracket when matches are available
  return (
    <>
      {matches.length > 0 ? (
        <SingleEliminationBracket
          matches={matches}
          matchComponent={Match}
          theme={WhiteTheme}
          options={{
            style: {
              roundHeader: WhiteTheme.roundHeaders,
              connectorColor: WhiteTheme.connectorColor,
              connectorColorHighlight: WhiteTheme.connectorColorHighlight,
            },
          }}
          className="custom-bracket" 
        />
      ) : (
        <p>Loading tournament matches...</p> // Show loading message while data is being fetched
      )}
    </>
  );
};

const UserTournamentMatchDiagram = () => {
  return (
    <div style={{ display: "flex", alignItems: "center", justifyContent: "center" }}>
      <WhiteThemeBracket />
    </div>
  );
};

export default UserTournamentMatchDiagram;