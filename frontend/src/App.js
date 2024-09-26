import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './components/routeProtection/ProtectedRoute';

// Import your pages/components
import UserHome from './screens/UserHome/UserHome';
import Login from './screens/Login/Login';
import CreateProfile from './screens/CreateProfile/CreateProfile';
import Signup from './screens/Signup/Signup';
import UserUpcomingTournament from './screens/UserUpcomingTournament/UserUpcomingTournament';
import UserTournamentParticipants from './screens/UserTournamentDetails/UserTournamentParticipants';
import UserTournamentOverview from './screens/UserTournamentDetails/UserTournamentOverview';
import UserTournamentMatch from './screens/UserTournamentDetails/UserTournamentMatch';

function App() {
  return (
    <Router>
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/createprofile" element={<CreateProfile />} />
        
        {/* User Protected Routes */}
        <Route
          path="/user/*"  // Wildcard allows for nested routes
          element={
            <ProtectedRoute allowedRoles={['User']}>
              <Routes>
                <Route path="home" element={<UserHome />} />
                <Route path="profile" element={<UserHome />} />
                <Route path="userupcomingtournament" element={<UserUpcomingTournament />} />
                <Route path="usertournamentparticipants" element={<UserTournamentParticipants />} />
                <Route path="usertournamentoverview" element={<UserTournamentOverview />} />
                <Route path="usertournamentmatch" element={<UserTournamentMatch />} />
                
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
                <Route path="home" element={<UserHome />} />
                <Route path="profile" element={<UserHome />} />
              </Routes>
            </ProtectedRoute>
          }
        />
      </Routes>
    </Router>
  );
}

export default App;
