// controllers/eloController.js
const { db } = require("../firebase/firebase.js"); 

const updateElo = async (req, res) => {
    const { userId1, userId2 } = req.query;
    const { AS1,AS2,Elo1,Elo2 } = req.body;

    if (!userId1 || !userId2 || Elo1 === undefined || Elo2 === undefined) {
        return res.status(400).json({ error: 'userId1, userId2, Elo1, and Elo2 are required.' });
    }

    if (![0, 0.5, 1].includes(AS1) || ![0, 0.5, 1].includes(AS2)) {
        return res.status(400).json({ error: 'AS1 and AS2 must be 0, 0.5, or 1.' });
    }

    //Expected Score (ES): The probability of a player winning, drawn, or losing, based on the difference between their Elo ratings and the opponent’s.
    //K-Factor (K): Following USCF calculation
    //Actual Score (AS): The outcome of the game (1 for win, 0.5 for draw, 0 for loss).
    const ES1 = 1 / (1 + Math.pow(10,((Elo2 - Elo1) / 400)));
    const ES2 = 1 / (1 + Math.pow(10,((Elo1 - Elo2) / 400)));
    let K1 = Elo1 < 2100 ? 32 : (Elo1 > 2400 ? 16 : 24);
    let K2 = Elo2 < 2100 ? 32 : (Elo2 > 2400 ? 16 : 24);
    const newElo1 = Elo1 + K1 * (AS1 - ES1);
    const newElo2 = Elo2 + K2 * (AS2 - ES2);

    try {
        await db.collection("Users").doc(userId1).update({
            Elo: newElo1
        });

        await db.collection("Users").doc(userId2).update({
            Elo: newElo2
        });

        return res.status(200).json({ code: 200, message: "Elo ratings successfully updated" });
    } catch (error) {
        console.error("Error updating Elo rating:", error);
        return res.status(500).json({ code: 500, message: `Error updating Elo ratings: ${error.message}` });
    }
};

const getElo = async (req,res) => {
    const userID = req.query.userID;
    try {
        const user = await db.collection("Users").doc(userID).get();
        if (!user.exists) {
            return res.status(404).json({ code: 404, message: "User not found" });
        }

        const userDetails = user.data();

        return res.status(200).json({
            Elo: userDetails.Elo,
        });
    } catch (error) {
        return res.status(500).json({code: 500, message: `Error getting user: ${error} `})
    }
}

module.exports = {
    updateElo,
    getElo
};


