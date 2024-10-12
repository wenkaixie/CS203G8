// import React from 'react';
// import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
// import Home from './screens/Home/Home';
// import Login from './screens/Login/Login';
// import UpdateProfile from './screens/UpdateProfile/UpdateProfile';
// import Signup from './screens/Signup/Signup';

// function App() {
//   return (
//     <Router>
//       <Routes>
//         <Route path="/login" element={<Login />} />
//         <Route path="/signup" element={<Signup />} />
//         <Route path="/home" element={<Home />} />
//         <Route path="/create_profile" element={<UpdateProfile />} />
//       </Routes>
//     </Router>
//   );
// }

// export default App;

import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './components/routeProtection/ProtectedRoute';

// Import your pages/components
import UserHome from './screens/UserHome/UserHome';
import Login from './screens/Login/Login';
import UpdateProfile from './screens/UpdateProfile/UpdateProfile';
import Signup from './screens/Signup/Signup';
import UserUpcomingTournament from './screens/UserUpcomingTournament/UserUpcomingTournament';
import UserTournamentParticipants from './screens/UserTournamentDetails/UserTournamentParticipants';
import UserTournamentOverview from './screens/UserTournamentDetails/UserTournamentOverview';
import UserTournamentMatch from './screens/UserTournamentDetails/UserTournamentMatch';
import UserCalendar from './screens/UserCalendar/UserCalendar';
import TournamentUpcoming from './screens/TournamentUpcoming/TournamentUpcoming';

function App() {
  return (
    <Router>
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<Login />} />
        <Route path="/signup" element={<Signup />} />

        {/* User Protected Routes */}
        <Route
          path="/user/*"
          element={
            <ProtectedRoute allowedRoles={['User']}>
              <Routes>
                <Route path="home" element={<UserHome />} />
                <Route path="profile" element={<UserHome />} />
                <Route path="tournaments" element={<UserUpcomingTournament />} />
                <Route path="profile" element={<UpdateProfile />} />
                <Route path="calendar" element={<UserCalendar />} />

                {/* Tournament Details Routes for Users */}
                <Route path="tournament/:tournamentId/overview" element={<UserTournamentOverview />} />
                <Route path="tournament/:tournamentId/participants" element={<UserTournamentParticipants />} />
                <Route path="tournament/:tournamentId/games" element={<UserTournamentMatch />} />
              </Routes>
            </ProtectedRoute>
          }
        />

        {/* Admin Protected Routes */}
        <Route
          path="/admin/*"
          element={
            <ProtectedRoute allowedRoles={['Admin']}>
              <Routes>
                {/* <Route path="/home" element={<AdminHome />} /> */}
              </Routes>
            </ProtectedRoute>
          }
        />
      </Routes>
    </Router>
  );
}

export default App;
