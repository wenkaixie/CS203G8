import { SingleEliminationBracket, Match, createTheme } from '@g-loot/react-tournament-brackets';

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
});

export const WhiteThemeBracket = () => {

  const simpleSmallBracket = [
    // Quarterfinals
    {
      id: 1, // matchID
      name: "Quarterfinal - Match 1", // Round Name/Number & Match Number
      nextMatchId: 5, // next matchID
      tournamentRoundText: "1", // Round Number
      startTime: "2021-05-30", // DateTime of the match
      state: "DONE", // Whether the match has concluded or not
      participants: [
        { id: "1", name: "Hikaru Nakamura", resultText: "6", isWinner: true }, // Participant no., Participant name, Participant Score, who won
        { id: "2", name: "John Charge", resultText: "0", isWinner: false }
      ]
    },
    {
      id: 2,
      name: "Quarterfinal - Match 2",
      nextMatchId: 5,
      tournamentRoundText: "1",
      startTime: "2021-05-30",
      state: "DONE",
      participants: [
        { id: "1", name: "Hikaru Nakamura", resultText: "6", isWinner: true },
        { id: "2", name: "John Charge", resultText: "0", isWinner: false }
      ]
    },
    {
      id: 3,
      name: "Quarterfinal - Match 3",
      nextMatchId: 6,
      tournamentRoundText: "1",
      startTime: "2021-05-30",
      state: "DONE",
      participants: [
        { id: "1", name: "Hikaru Nakamura", resultText: "6", isWinner: true },
        { id: "2", name: "John Charge", resultText: "0", isWinner: false }
      ]
    },
    {
      id: 4,
      name: "Quarterfinal - Match 4",
      nextMatchId: 6,
      tournamentRoundText: "1",
      startTime: "2021-05-30",
      state: "DONE",
      participants: [
        { id: "1", name: "Hikaru Nakamura", resultText: "6", isWinner: true },
        { id: "2", name: "John Charge", resultText: "0", isWinner: false }
      ]
    },

    // Semifinals
    {
      id: 5,
      name: "Semifinal - Match 1",
      nextMatchId: 7,
      tournamentRoundText: "2",
      startTime: "2021-06-01",
      state: "DONE",
      participants: [
        { id: "1", name: "Hikaru Nakamura", resultText: "6", isWinner: true },
        { id: "2", name: "John Charge", resultText: "0", isWinner: false }
      ]
    },
    {
      id: 6,
      name: "Semifinal - Match 2",
      nextMatchId: 7,
      tournamentRoundText: "2",
      startTime: "2021-06-01",
      state: "DONE",
      participants: [
        { id: "1", name: "Hikaru Nakamura", resultText: "6", isWinner: true },
        { id: "2", name: "John Charge", resultText: "0", isWinner: false }
      ]
    },

    // Semifinals
    {
      id: 7,
      name: "Finals",
      nextMatchId: null,
      tournamentRoundText: "3",
      startTime: "2021-06-05",
      state: "DONE",
      participants: [
        { id: "1", name: "Hikaru Nakamura", resultText: "6", isWinner: true },
        { id: "2", name: "John Charge", resultText: "0", isWinner: false }
      ]
    },

  ];

  return (
    <SingleEliminationBracket
      matches={simpleSmallBracket}
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
  );
};

const UserTournamentMatchDiagram = () => {
  return (
    <div style={{display: "flex", alignItems:"center", justifyContent:"center"}}>
      <WhiteThemeBracket />
    </div>
  );
};

export default UserTournamentMatchDiagram;