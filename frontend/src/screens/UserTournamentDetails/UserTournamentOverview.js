import React from 'react';
import './UserTournamentOverview.css';
import UserDetailsHeader from './UserDetailsHeader';

const UserTournamentOverview = () => {
    // Dummy data for now, should be replaced with database values
    const tournamentData = {
        title: "Tournament 1",
        dateRange: "June 21, 2024 to July 01, 2024",
        type: "Swiss",
        totalParticipants: 30,
        location: "Budapest, Hungary. Activities will take place at the Hungarian National Gallery and the Intercontinental Budapest.",
        format: [
            "Teams compete in an 11-round Swiss",
            "Teams assign players to boards one, two, three, and four",
            "Matches consist of players from each board of one nation playing against the corresponding board of the opponent nation",
            "Time control: 90+30, with 30 minutes added after move 40",
            "Players cannot draw by agreement before move 30"
        ],
        prizes: {
            total: "$175,000",
            breakdown: [
                { place: "1st", amount: "$70,000" },
                { place: "2nd", amount: "$45,000" },
                { place: "3rd", amount: "$30,000" },
                { place: "4th", amount: "$20,000" },
                { place: "5th", amount: "$10,000" },
            ]
        },
        organizer: "World Chess Organization",
        partners: "Vestibulum vulputate, justo a pellentesque feugiat"
    };

    return (
        <div>
            {/* Header with registration details */}
            <UserDetailsHeader
                tournamentTitle={tournamentData.title}
                isRegistered={true} // Assuming user is registered
                playerCount={tournamentData.totalParticipants}
            />

            {/* Tournament Overview Content */}
            <div className="tournament-overview">
                <div className="tournament-description">
                    <p>
                        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec at rhoncus nibh.
                        Sed sed urna felis. Curabitur mollis blandit egestas. Ut viverra tincidunt elementum.
                        Pellentesque maximus ante id augue iaculis eleifend. Phasellus aliquam non pharetra ligula.
                        Maecenas tristique nulla mattis iaculis semper. Nullam dignissim sed nibh eget dictum.
                        Ut bibendum nisi ullamcorper vestibulum facilisis.
                    </p>
                </div>
                <div className="tournament-details">
                    <div className="detail-row">
                        <strong>Dates:</strong> {tournamentData.dateRange}
                    </div>
                    <div className="detail-row">
                        <strong>Tournament type:</strong> {tournamentData.type}
                    </div>
                    <div className="detail-row">
                        <strong>Total participants:</strong> {tournamentData.totalParticipants}
                    </div>
                    <div className="detail-row">
                        <strong>Location:</strong> {tournamentData.location}
                    </div>
                    <div className="detail-row">
                        <strong>Format:</strong>
                        <ul>
                            {tournamentData.format.map((item, index) => (
                                <li key={index}>{item}</li>
                            ))}
                        </ul>
                    </div>
                    <div className="detail-row">
                        <strong>Prizes:</strong>
                        <div className="prizes">
                            <div className="prize-total-box">
                                <div className="prize-icon">💰</div>
                                <div className="prize-amount">{tournamentData.prizes.total}</div>
                                <div className="prize-label">Total prize pool</div>
                            </div>
                            <div className="prize-breakdown-box">
                                <table className="prize-table">
                                    <tbody>
                                        {tournamentData.prizes.breakdown.map((prize, index) => (
                                            <tr key={index}>
                                                <td className="prize-place">{prize.place}</td>
                                                <td className="prize-amount-text">{prize.amount}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                    <div className="organizer-container">
                        <div className="detail-row">
                            <strong>Organizer:</strong> {tournamentData.organizer}
                        </div>
                        <div className="detail-row">
                            <strong>Official partners:</strong> {tournamentData.partners}
                        </div>
                    </div>
                </div>
            </div>
        </div >
    );
};

export default UserTournamentOverview;

