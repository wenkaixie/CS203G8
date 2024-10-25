import React, { useState } from 'react';
import './CreateTournamentCard.css';
import axios from 'axios';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import CloseIcon from '@mui/icons-material/Close';
import { getAuth } from "firebase/auth";

const CreateTournamentCard = () => {
  const auth = getAuth();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [tournamentData, setTournamentData] = useState({
    name: '',
    description: '',
    startDate: '',
    endDate: '',
    type: '',
    prizePool: '',
    slots: '',
    location: '',
    ageLimit: '',
    eloRequirement: ''
  });

  const handleChange = (e) => {
    setTournamentData({
      ...tournamentData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post('http://localhost:7070/admin/createTournament', {
        ...tournamentData,
        userId: auth.currentUser.uid
      });
      setIsModalOpen(false); // Close the modal after submission
    } catch (error) {
      console.error("Error creating tournament:", error);
    }
  };

  return (
    <div>
      <div className="create-tournament">
        <h2>Create a new tournament</h2>
        <AddCircleIcon
          sx={{ fontSize: '2.5rem', cursor: 'pointer', color: '#8F3013' }}
          onClick={() => setIsModalOpen(true)}
        />
      </div>

      {/* Modal Popup */}
      {isModalOpen && (
        <div className="modal-background">
          <div className="modal-content">
            <div className="modal-header">
              <h2>Create Tournament</h2>
              <CloseIcon 
                className="close-icon"
                sx={{ cursor: 'pointer' }}
                onClick={() => setIsModalOpen(false)}
              />
            </div>
            <form onSubmit={handleSubmit} className="create-tournament-form">
              <div>
                  <label>
                      <div>Tournament Name</div> {/* Capitalized */}
                      <input 
                      type="text" 
                      name="name" 
                      value={tournamentData.name} 
                      onChange={handleChange} 
                      required 
                      />
                  </label>
              </div>
              <div>
                  <label>
                      Description
                      <textarea 
                      name="description" 
                      value={tournamentData.description} 
                      onChange={handleChange} 
                      />
                  </label>
              </div>
              <div className="date-inputs">
                  <label>
                  Tournament Start Date
                  <input 
                      type="date" 
                      name="startDate" 
                      value={tournamentData.startDate} 
                      onChange={handleChange} 
                  />
                  </label>
                  <label>
                  Tournament End Date
                  <input 
                      type="date" 
                      name="endDate" 
                      value={tournamentData.endDate} 
                      onChange={handleChange} 
                  />
                  </label>
              </div>
              <div>
                  <label>
                      Tournament Type
                      <select name="type" value={tournamentData.type} onChange={handleChange}>
                      <option value="swiss">Swiss Format</option>
                      <option value="elim">Single Elimination Format</option>
                      </select>
                  </label>
              </div>
              <div className="small-inputs">
                  <label>
                  Prize Pool
                  <input 
                      type="number" 
                      name="prizePool" 
                      min="0"
                      value={tournamentData.prizePool} 
                      onChange={handleChange} 
                  />
                  </label>
                  <label>
                  Slots
                  <input 
                      type="number" 
                      name="slots" 
                      min="0"
                      value={tournamentData.slots} 
                      onChange={handleChange} 
                  />
                  </label>
              </div>
              <div>
                  <label>
                      Tournament Location
                      <input 
                      type="text" 
                      name="location" 
                      value={tournamentData.location} 
                      onChange={handleChange} 
                      required 
                      />
                  </label>
              </div>
              <div className="small-inputs">
                  <label>
                  Age Limit
                  <input 
                      type="number" 
                      name="ageLimit" 
                      min="0"
                      value={tournamentData.ageLimit} 
                      onChange={handleChange} 
                  />
                  </label>
                  <label>
                  Elo Requirement
                  <input 
                      type="number" 
                      name="eloRequirement" 
                      min="0"
                      value={tournamentData.eloRequirement} 
                      onChange={handleChange} 
                  />
                  </label>
              </div>
              <div className='submit-container'>
                  <button type="submit" className="submit-btn">Submit</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default CreateTournamentCard;