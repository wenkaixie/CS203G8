import { SingleEliminationBracket, Match, createTheme } from '@g-loot/react-tournament-brackets';
import './AdminTournamentMatchDiagram.css';
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
      const response = await axios.get(
        `${process.env.REACT_APP_API_URL}:8080/api/tournaments/${tournamentId}/matches`
      );
      const fetchedMatches = response.data;
  
      // Modify each match's participants and format startTime
      const updatedMatches = fetchedMatches.map((match) => {
        // Format startTime to "DD-Mmm-YYYY HH:MM"
        const startDate = new Date(match.startTime);
        const formattedDate = startDate.toLocaleDateString("en-GB", {
          day: "2-digit",
          month: "short",
          year: "numeric",
        });
        const formattedTime = startDate.toLocaleTimeString("en-GB", {
          hour: "2-digit",
          minute: "2-digit",
          hour12: false,
        });
        const formattedStartTime = `${formattedDate} ${formattedTime}`; // Concatenate date and time
  
        return {
          ...match,
          startTime: formattedStartTime,
          participants: match.participants.map((participant) => ({
            ...participant,
            name: `${participant.isWinner ? "👑 " : ""}${participant.name} (${participant.elo || "N/A"})`,
          })),
        };
      });
  
      setMatches(updatedMatches);
      console.log(updatedMatches); // Log updated matches to verify
    } catch (error) {
      console.error("Error fetching tournament matches:", error);
    }
  };

  useEffect(() => {
    fetchTournamentMatches();
  }, []);

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
        <p>Tournament has not started.</p> // Show loading message while data is being fetched
      )}
    </>
  );
};

const AdminTournamentMatchDiagram = () => {
  return (
    <div style={{ display: "flex", alignItems: "center", justifyContent: "center" }}>
      <WhiteThemeBracket />
    </div>
  );
};

export default AdminTournamentMatchDiagram;