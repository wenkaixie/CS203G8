import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { getAuth } from 'firebase/auth';
import './CreateTournamentForm.css';

const CreateTournamentForm = ({ onClose, onSuccess }) => {
    const [page, setPage] = useState(1);
    const [formData, setFormData] = useState({
        name: '',
        description: '',
        location: '',
        ageLimit: 0,
        eloRequirement: 0,
        startDate: '',
        endDate: '',
        prizePool: 0,
        slots: 0,
        type: '',
        openRegistration: '',
        closeRegistration: '',
    });
    const [error, setError] = useState(null);
    const [showConfirmation, setShowConfirmation] = useState(false);
    const [isDropdownVisible, setIsDropdownVisible] = useState(false);
    const auth = getAuth();

    useEffect(() => {
        const now = new Date();
        now.setHours(now.getHours() + 8); // Adjust to UTC+8
        const formattedNow = now.toISOString().slice(0, 16); // "YYYY-MM-DDTHH:MM"
        setFormData((prevData) => ({
            ...prevData,
            openRegistration: formattedNow,
        }));
    }, []);

    // Helper function to check if a number is a power of 2
    const isPowerOfTwo = (number) => {
        return (number & (number - 1)) === 0 && number !== 0;
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prevData) => ({
            ...prevData,
            [name]: value,
        }));

        if (name === 'endDate') {
            const endDate = new Date(value);
            endDate.setHours(endDate.getHours() + 8);
            const closeRegDate = new Date(endDate.getTime() - 24 * 60 * 60 * 1000);
            const formattedCloseRegDate = closeRegDate.toISOString().slice(0, 16);
            setFormData((prevData) => ({
                ...prevData,
                closeRegistration: formattedCloseRegDate,
            }));
        }

        // Ensure slots are even and a power of 2 if "Elimination" type is selected
        if (name === 'slots') {
            const slotsValue = Number(value);
            if (slotsValue % 2 !== 0) {
                setError("Slots must be an even number.");
            } else if (formData.type === 'Single Elimination' && !isPowerOfTwo(slotsValue)) {
                setError("For single elimination tournaments, slots must be a power of 2.");
            } else {
                setError(null);
            }
        }
    };

    const handleTypeSelection = (type) => {
        setFormData((prevData) => ({
            ...prevData,
            type: type,
        }));
        setIsDropdownVisible(false);

        // Revalidate slots if the type is set to "Elimination"
        if (type === 'Single Elimination' && formData.slots && !isPowerOfTwo(formData.slots)) {
            setError("For single elimination tournaments, slots must be a power of 2.");
        } else {
            setError(null);
        }
    };

    const handleNext = () => setPage(2);
    const handleBack = () => setPage(1);

    const handleSubmit = async (e) => {
        e.preventDefault();
        const user = auth.currentUser;

        if (!user) {
            setError("User is not authenticated.");
            return;
        }

        // Validate slots
        const slotsValue = Number(formData.slots);
        if (slotsValue % 2 !== 0) {
            setError("Slots must be an even number.");
            return;
        } else if (formData.type === 'Single Elimination' && !isPowerOfTwo(slotsValue)) {
            setError("For single elimination tournaments, slots must be a power of 2.");
            return;
        }

        // Validate that end date is after start date
        const startDate = new Date(formData.startDate);
        const endDate = new Date(formData.endDate);
        if (endDate <= startDate) {
            setError("End date must be after the start date.");
            return;
        }

        try {
            // Convert tournament type to the required format
            const tournamentType = formData.type === 'Single Elimination' ? 'ELIMINATION' : 
            formData.type === 'Round Robin' ? 'ROUND_ROBIN' : formData.type;

            const formattedData = {
                adminId: user.uid,
                type: tournamentType,
                ageLimit: Number(formData.ageLimit),
                name: formData.name,
                description: formData.description,
                eloRequirement: Number(formData.eloRequirement),
                location: formData.location,
                capacity: slotsValue,
                prize: Number(formData.prizePool),
                startDatetime: formData.startDate + ":00Z",
                endDatetime: formData.endDate + ":00Z",
            };

            console.log("Formatted data being sent:", formattedData);

            await axios.post(
                `http://localhost:8080/api/tournaments`,
                formattedData
            );

            setShowConfirmation(true); 
            setTimeout(() => {
                setShowConfirmation(false);
                onClose();
                if (onSuccess) onSuccess();
            }, 2000);
        } catch (error) {
            console.error("Error creating tournament:", error);
            setError("Failed to create tournament. Please try again.");
        }
    };

    return (
        <div className="registration-overlay">
            <div className="registration-modal">
                <div className="registration-header">
                    <h2>Create Tournament</h2>
                    <button className="close-button" onClick={onClose}>×</button>
                </div>
                <form onSubmit={handleSubmit} className='registration-form'>
                    {page === 1 ? (
                        <div className="registration-body">
                            <label>Tournament Name</label>
                            <input
                                type="text"
                                className="full-width"
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                required
                            />

                            <label>Description</label>
                            <textarea
                                name="description"
                                className="full-width"
                                value={formData.description}
                                onChange={handleChange}
                            ></textarea>

                            <div className="input-row">
                                <div className="form-group half-width">
                                    <label>Start Date</label>
                                    <input
                                        type="datetime-local"
                                        name="startDate"
                                        value={formData.startDate}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                                <div className="form-group half-width">
                                    <label>End Date</label>
                                    <input
                                        type="datetime-local"
                                        name="endDate"
                                        value={formData.endDate}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                            </div>

                            <label>Tournament Type</label>
                            <div className="dropdown full-width">
                                <button
                                    type="button" // Prevents the form from auto-submitting
                                    className="dropdown-button"
                                    onClick={() => setIsDropdownVisible(!isDropdownVisible)}
                                >
                                    {formData.type || "Select Type"}
                                    <span className="dropdown-arrow">&#9662;</span>
                                </button>
                                {isDropdownVisible && (
                                    <div className="dropdown-content">
                                        <div
                                            className="dropdown-item"
                                            onClick={() => handleTypeSelection("Round Robin")}
                                        >
                                            Round Robin
                                        </div>
                                        <div
                                            className="dropdown-item"
                                            onClick={() => handleTypeSelection("Single Elimination")}
                                        >
                                            Single Elimination
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    ) : (
                        <div className="registration-body">
                            <label>Location</label>
                            <input
                                type="text"
                                className="full-width"
                                name="location"
                                value={formData.location}
                                onChange={handleChange}
                                required
                            />

                            <div className="input-row">
                                <div className="form-group half-width">
                                    <label>Age Limit</label>
                                    <input
                                        type="number"
                                        name="ageLimit"
                                        value={formData.ageLimit}
                                        onChange={handleChange}
                                    />
                                </div>
                                <div className="form-group half-width">
                                    <label>ELO Requirement</label>
                                    <input
                                        type="number"
                                        name="eloRequirement"
                                        value={formData.eloRequirement}
                                        onChange={handleChange}
                                    />
                                </div>
                            </div>

                            <div className="input-row">
                                <div className="form-group half-width">
                                    <label>Open Registration</label>
                                    <input
                                        type="datetime-local"
                                        name="openRegistration"
                                        value={formData.openRegistration}
                                        readOnly
                                    />
                                </div>
                                <div className="form-group half-width">
                                    <label>Close Registration</label>
                                    <input
                                        type="datetime-local"
                                        name="closeRegistration"
                                        value={formData.closeRegistration}
                                        readOnly
                                    />
                                </div>
                            </div>

                            <div className="input-row">
                                <div className="form-group half-width">
                                    <label>Prize Pool</label>
                                    <input
                                        type="number"
                                        name="prizePool"
                                        value={formData.prizePool}
                                        onChange={handleChange}
                                    />
                                </div>
                                <div className="form-group half-width">
                                    <label>Slots</label>
                                    <input
                                        type="number"
                                        name="slots"
                                        value={formData.slots}
                                        onChange={handleChange}
                                    />
                                </div>
                            </div>
                            {error && <p className="error-message">{error}</p>}
                        </div>
                    )}
                    <div className="registration-footer">
                        {page === 1 ? (
                            <button type="button" className="navigation-button next-button" onClick={handleNext}>
                                &gt;
                            </button>
                        ) : (
                            <>
                                <div className="buttonsContainer">
                                    <button type="button" className="navigation-button back-button" onClick={handleBack}>
                                        &lt;
                                    </button>
                                    <button type="submit" className="navigation-button create-button">
                                        Create
                                    </button>
                                </div>
                            </>
                        )}
                    </div>
                </form>
            </div>

            {showConfirmation && (
                <div className="confirmation-popup">
                    <p>Tournament created successfully!</p>
                </div>
            )}
        </div>
    );
};

export default CreateTournamentForm;