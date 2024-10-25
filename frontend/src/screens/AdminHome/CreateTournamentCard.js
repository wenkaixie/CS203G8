import React, { useState, useEffect } from 'react';
import './CreateTournamentCard.css';
import axios from 'axios';
import AddCircleIcon from '@mui/icons-material/AddCircle';
import CloseIcon from '@mui/icons-material/Close';
import { getAuth } from "firebase/auth";

const CreateTournamentCard = () => {
  const auth = getAuth();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [minStartDate, setMinStartDate] = useState('');
  const [minEndDate, setMinEndDate] = useState('');
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

  // Set the minimum start date to be one day after the current date
  useEffect(() => {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    setMinStartDate(tomorrow.toISOString().split('T')[0]); // Format date to YYYY-MM-DD
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setTournamentData({
      ...tournamentData,
      [name]: value
    });

    // Update the minimum end date to be after the selected start date
    if (name === 'startDate') {
      const newStartDate = new Date(value);
      const dayAfterStartDate = new Date(newStartDate);
      dayAfterStartDate.setDate(newStartDate.getDate() + 1);
      setMinEndDate(dayAfterStartDate.toISOString().split('T')[0]);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await axios.post('http://localhost:7070/admin/createTournament', {
        ...tournamentData,
        userId: auth.currentUser.uid
      });
      setIsModalOpen(false); // Close the modal after submission
      alert("Tournament Created Successfully!");
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
                    min={minStartDate} /* Start date must be at least 1 day after today */
                    required
                  />
                </label>
                <label>
                  Tournament End Date
                  <input 
                    type="date" 
                    name="endDate" 
                    value={tournamentData.endDate} 
                    onChange={handleChange} 
                    min={minEndDate} /* End date must be after the start date */
                    required
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