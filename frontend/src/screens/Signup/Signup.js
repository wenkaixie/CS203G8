import React, { useState, useEffect } from 'react';
import './Signup.css';
import './Popup.css';
import Container from 'react-bootstrap/Container';
import Icon from '../../assets/images/icon.jpg';
import { Img } from 'react-image';
import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import { FaGoogle } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';
import { getAuth, createUserWithEmailAndPassword, GoogleAuthProvider, onAuthStateChanged, signInWithPopup } from 'firebase/auth';
import { setDoc, doc, collection, query, where, getDocs } from 'firebase/firestore';
import { FirestoreDB } from '../../firebase/firebase_config';

const Signup = () => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [role, setRole] = useState(""); // Default role is empty, user must choose
    const [error, setError] = useState(null);
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false); // Loading state for auth check

    const auth = getAuth();
    const navigate = useNavigate();

    useEffect(() => {
        // Listen for authentication state changes
        const unsubscribe = onAuthStateChanged(auth, async (user) => {
            if (user) {
                try {
                    setLoading(true);
                    const userRole = await getUserRole(user.email); // Await role checking
                    console.log('User email:', user.email);    

                    // Redirect user to the appropriate profile page based on role
                    if (userRole === 'User') {
                        navigate('/user/profile');
                    } else if (userRole === 'Admin') {
                        navigate('/admin/profile');
                    }
                } catch (error) {
                    setError('Error retrieving user role.');
                } finally {
                    setLoading(false);
                }
            }
        });

        // Clean up the listener on component unmount
        return () => unsubscribe();
    }, [auth, navigate]);

    // Handler for finding user role in firebase
    const getUserRole = async (email) => {
        console.log('Checking user role');
    
        try {
          const roles = ['User', 'Admin'];
    
          for (const role of roles) {
            const roleQuery = query(collection(FirestoreDB, role), where('email', '==', email));
            const roleSnapshot = await getDocs(roleQuery);
    
            if (!roleSnapshot.empty) {
              console.log(`User is a ${role}`);
              return role;
            }
          }
    
          console.log('User not found in any role');
          return null;
        } catch (error) {
          console.error('Error checking user role:', error);
          return null;
        }
    };

    const handleEmailChange = (event) => {
        setEmail(event.target.value);
    };

    const handlePasswordChange = (event) => {
        setPassword(event.target.value);
    };

    const handleConfirmPasswordChange = (event) => {
        setConfirmPassword(event.target.value);
    };

    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    const handleClosePopup = () => {
        setError(null);
    };

    const handleRoleChange = (event) => {
        setRole(event.target.value);
    };

    // Handle sign up with email and password
    const handleSubmit = async (event) => {
        event.preventDefault();
        setError(null);
        
        if (password !== confirmPassword) {
            setError("Passwords do not match!");
            return;
        }

        if (role === "") {
            setError("Please select a role.");
            return;
        }

        try {
            setLoading(true);
            // Step 1: Create user in Firebase Authentication
            const userSignup = await createUserWithEmailAndPassword(auth, email, password);
            const user = userSignup.user;
            console.log('User created:', user.uid);
        
            // Step 2: Store user info in Firestore
            await setDoc(doc(FirestoreDB, role, user.uid), {
                email: user.email,
                role: role
            });

            // onAuthStateChanged will handle the redirection
        } catch (error) {
            console.error('Error creating user:', error.message);
            setError(`Sign-up failed: ${error.message}`);
        } finally {
            setLoading(false);
        }
    };

    // Handle Google Sign-Up
    const handleGoogleSignup = async () => {
        const provider = new GoogleAuthProvider();
        try {
            setLoading(true);
            const result = await signInWithPopup(auth, provider);
            const user = result.user;
            console.log('Google sign up success:', user);

            if (role === "") {
                setError("Please select a role.");
                return;
            }

            // Store user info in Firestore
            await setDoc(doc(FirestoreDB, role, user.uid), {
                email: user.email,
                role: role
            });

            // onAuthStateChanged will handle the redirection
        } catch (error) {
            console.error('Google sign up error:', error.message);
            setError(`Google sign-up failed: ${error.message}`);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container fluid className="signup">
            <Container fluid className="signup-slide-left">
                <Container fluid className='signup-content'>
                    <h1>Sign Up</h1>
                    <Container fluid className='signup-details'>
                        <Form style={{ fontSize:"20px" }} onSubmit={handleSubmit}>
                            <Form.Group className="mb-3" controlId="formBasicEmail">
                                <Form.Label>Email Address</Form.Label>
                                <Form.Control
                                    type="email"
                                    placeholder="Enter Email"
                                    value={email}
                                    onChange={handleEmailChange}
                                    required
                                />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicPassword">
                                <Form.Label>Password</Form.Label>
                                <Form.Control
                                    type={showPassword ? "text" : "password"}
                                    placeholder="Enter Password"
                                    value={password}
                                    onChange={handlePasswordChange}
                                    required
                                />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicPasswordRetype">
                                <Form.Label>Re-Enter Password</Form.Label>
                                <Form.Control
                                    type={showPassword ? "text" : "password"}
                                    placeholder="Re-enter Password"
                                    value={confirmPassword}
                                    onChange={handleConfirmPasswordChange}
                                    required
                                />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicCheckbox">
                                <Form.Check
                                    type="checkbox"
                                    label="Show Password"
                                    onChange={togglePasswordVisibility}
                                />
                            </Form.Group>
                            <Form.Group className="mb-3">
                            <Form.Label>Select Role</Form.Label>
                            <div style={{ display: 'flex', gap: '100px', justifyContent: 'center' }}>
                                <div style={{ textAlign: 'center' }}>
                                    <Form.Check 
                                        type="radio" 
                                        label="User" 
                                        value="User"
                                        name="role" 
                                        onChange={handleRoleChange} 
                                        checked={role === "User"}
                                    />
                                </div>
                                <div style={{ textAlign: 'center' }}>
                                    <Form.Check 
                                        type="radio" 
                                        label="Admin" 
                                        value="Admin"
                                        name="role" 
                                        onChange={handleRoleChange} 
                                        checked={role === "Admin"}
                                    />
                                </div>
                            </div>
                            </Form.Group>
                            <Button variant="primary" className='signup-button' type="submit" style={{ backgroundColor:"#8F3013", border:"0" }}>
                                Sign Up
                            </Button>
                            <div className="divider-container">
                                <hr className="divider-line" />
                                <span className="divider-text">or</span>
                                <hr className="divider-line" />
                            </div>
                            <Button variant="primary" className="signup-button googleButton" type="button" style={{ backgroundColor:"#FFFFFF", color:"black", borderColor:"black" }} onClick={handleGoogleSignup}>
                                <FaGoogle />
                                Sign Up with Google
                            </Button>
                        </Form>
                    </Container>
                </Container>
            </Container>
            <Container fluid className="signup-slide-right">
                <Img
                    src={Icon}
                    height={'418px'}
                    width={'321px'}
                />
            </Container>
            {error && (
                <div className="popup">
                    <div className="popup-content">
                        <h2>Error</h2>
                        <p>{error}</p>
                        <Button variant="secondary" onClick={handleClosePopup}>Close</Button>
                    </div>
                </div>
            )}
        </Container>
    );
}

export default Signup;
