import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom'; // React Router's useNavigate hook

const ProtectedRoute = ({ children, allowedRoles }) => {
    const navigate = useNavigate(); // Replaces Next.js's useRouter
    const [loading, setLoading] = useState(true);
    const [accessDenied, setAccessDenied] = useState(false);

    // Helper function to convert ArrayBuffer to hex string
    function bufferToHex(buffer) {
        return [...new Uint8Array(buffer)]
            .map(b => b.toString(16).padStart(2, '0'))
            .join('');
    }

    // Hashing function using SHA-256
    async function hashToken(token) {
        const encoder = new TextEncoder();
        const data = encoder.encode(token);
        const hashBuffer = await crypto.subtle.digest('SHA-256', data);
        return bufferToHex(hashBuffer);
    }

    useEffect(() => {
        const checkAuth = async () => {
            setLoading(true); // Start loading when the auth check begins

            const token = localStorage.getItem('userToken');
            const hashedStoredToken = localStorage.getItem('hashedUserToken');
            
            if (!token || !hashedStoredToken) {
                // Redirect to login if token is missing
                navigate('/login');
                return;
            }

            // Hash the current token for comparison
            const hashedToken = await hashToken(token);

            if (hashedToken !== hashedStoredToken) {
                // Token does not match
                navigate('/login');
                return;
            }

            // If token matches, proceed with role check
            const userRole = localStorage.getItem('userRole');

            if (!allowedRoles.includes(userRole)) {
                // Show access denied popup if role is not allowed
                setAccessDenied(true);
                setLoading(false); // Stop loading after setting access denied
            } else {
                setAccessDenied(false);
                setLoading(false); // Stop loading when auth check is complete
            }
        };

        checkAuth();
    }, [navigate, allowedRoles]);

    const handleClosePopup = () => {
        setAccessDenied(false);
        navigate(-1); // Go back to the previous route
    };

    // Early return to prevent rendering children until loading is complete or access is granted
    if (loading) {
        return null; // Optionally, return a loading spinner or similar
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
