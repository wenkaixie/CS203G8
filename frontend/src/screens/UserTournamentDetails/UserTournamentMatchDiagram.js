import { SingleEliminationBracket, Match, createTheme } from '@g-loot/react-tournament-brackets';
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';

const WhiteTheme = createTheme({
  textColor: { 
    main: '#8F3013', // Dark brownish text for general text
    highlighted: '#000000', // Black highlighted text
    dark: '#3E414D' // Darker shade for contrasts
  },
  matchBackground: { 
    wonColor: '#F5F5F5', // Light off-white background for winning matches
    lostColor: '#F5F5F5' // Same color for lost matches, matching screenshot
  },
  score: {
    background: { 
      wonColor: '#E0E0E0', // Light gray background for score boxes (matches the screenshot)
      lostColor: '#E0E0E0'  // Same light gray for losing scores
    },
    text: { 
      highlightedWonColor: '#333333', // Dark gray for winning scores text
      highlightedLostColor: '#333333' // Dark gray for losing scores text
    }
  },
  border: {
    color: '#E0E0E0', // Light gray for match borders
    highlightedColor: '#E0E0E0' // Same light gray for highlighted borders
  },
  roundHeaders: { 
    background: '#8F3013', // Brownish red background for round headers
  },
  connectorColor: '#E0E0E0', // Light gray for connectors between matches
  connectorColorHighlight: '#8F3013', // Highlight connectors with brownish red color
  // theme configuration
  
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