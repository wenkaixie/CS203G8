const express = require("express");
const {getElo,updateElo} = require("../controllers/eloController.js");

const router = express.Router();

router.get('/profile', getElo);

router.post('/update', updateElo);

module.exports = router;