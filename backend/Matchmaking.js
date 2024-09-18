const { db } = require("../firebase/firebase.js"); // Ensure Firebase is initialized correctly

async function getPlayerEloMap(tournamentID) {
    const playerEloMap = new Map(); 
    try {
        const playersSnapshot = await db.collection('Tournaments').doc(tournamentID).collection('Players').get();

        const playerPromises = playersSnapshot.docs.map(async (playerDoc) => {
            const playerID = playerDoc.id;  
            const userDoc = await db.collection('Users').doc(playerID).get();

            if (userDoc.exists) {
                const userData = userDoc.data();
                const playerElo = userData.Elo;
                playerEloMap.set(playerID, playerElo);
            } else {
                console.error(`User ${playerID} does not exist in the users collection.`);
            }

        });

        await Promise.all(playerPromises);
        const playerArray = Array.from(playerEloMap);
        const sortedPlayerArray = playerArray.sort((a, b) => b[1] - a[1]); 
        const sortedPlayerEloMap = new Map(sortedPlayerArray);
        return sortedPlayerEloMap;
    } catch (error) {
        console.error('Error fetching player Elo:', error);
        throw error;
    }
}

//Matching follows seeding system where top player matched against bottom player,
//and tournament rules follows knockout (single elimination) system.

function matchUp(sortedPlayerEloMap) {
    const matchups = [];
    const playerArray = Array.from(sortedPlayerEloMap);
    let i = 0; 
    let j = playerArray.length - 1;  
    while (i < j) {
        const highEloPlayer = playerArray[i][0];  
        const lowEloPlayer = playerArray[j][0];
        matchups.push([highEloPlayer, lowEloPlayer]);
        i++;
        j--;
    }
    return matchups;
}

async function saveMatchUp(tournamentID, matchups) {
    try {
        const matchesRef = db.collection('Tournaments').doc(tournamentID).collection('Matches');
        const savePromises = matchups.map((matchup) => {
            return matchesRef.add({
                playerID1: matchup[0],     
                playerID2: matchup[1],   
            });
        });
        await Promise.all(savePromises);
        console.log('Matchups successfully saved');
    } catch (error) {
        console.error('Error saving matchups', error);
    }
}


// Export the function to be used in other files
module.exports = {
    getPlayerEloMap,
    matchUp,
    saveMatchUp,
};