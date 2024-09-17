import React from 'react';
import ProtectedRoute from './ProtectedRoute'; // Adjust the path as necessary

const AdminRoute = ({ children }) => {
    return <ProtectedRoute allowedRoles={['Admin']}>{children}</ProtectedRoute>;
};

export default AdminRoute;

