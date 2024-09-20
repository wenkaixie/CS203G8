import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAuth, onAuthStateChanged } from 'firebase/auth';

const ProtectedRoute = ({ children, allowedRoles }) => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [accessDenied, setAccessDenied] = useState(false);

    useEffect(() => {
        const auth = getAuth();

        const checkAuth = () => {
            setLoading(true);

            const unsubscribe = onAuthStateChanged(auth, async (user) => {
                if (user) {
                    // User is authenticated
                    const userRole = localStorage.getItem('userRole');

                    if (!allowedRoles.includes(userRole)) {
                        // User is authenticated but doesn't have the required role
                        setAccessDenied(true);
                    } else {
                        // User is authenticated and has the required role
                        setAccessDenied(false);
                    }
                } else {
                    // User is not authenticated, redirect to login
                    navigate('/login');
                }

                setLoading(false);
            });

            // Clean up the listener on component unmount
            return () => unsubscribe();
        };

        checkAuth();
    }, [navigate, allowedRoles]);

    const handleClosePopup = () => {
        setAccessDenied(false);
        navigate(-1); // Go back to the previous route
    };

    // Early return to prevent rendering children until loading is complete or access is granted
    if (loading) {
        return <div>Loading...</div>; // Optionally, return a loading spinner or similar
    }

    return (
        <>
            {accessDenied ? (
                <div className="popup">
                    <div className="popup-content">
                        <h2>Access Denied</h2>
                        <p>You do not have permission to view this page.</p>
                        <button onClick={handleClosePopup}>Close</button>
                    </div>
                </div>
            ) : (
                children // Render children only if access is not denied
            )}
        </>
    );
};

export default ProtectedRoute;
