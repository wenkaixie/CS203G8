import React, { useState } from 'react';
import './Signup.css';
import Container from 'react-bootstrap/Container';
import Icon from '../../assets/images/icon.jpg';
import { Img } from 'react-image';
import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import { FaGoogle, FaFacebook } from 'react-icons/fa'; // Import Facebook Icon
import { useNavigate } from 'react-router-dom';
import { getAuth, createUserWithEmailAndPassword, GoogleAuthProvider, FacebookAuthProvider, signInWithPopup, getIdToken } from 'firebase/auth';
import { setDoc, doc } from 'firebase/firestore';
import { FirestoreDB } from '../../firebase/firebase_config';

const Signup = () => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [role, setRole] = useState("User"); // Default role is "User"
    const [error, setError] = useState(null);
    const [showPassword, setShowPassword] = useState(false);
    const navigate = useNavigate();

    const auth = getAuth();

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

    // Handle sign up with email and password
    const handleSubmit = async (event) => {
        event.preventDefault();
        setError(null);
        
        if (password !== confirmPassword) {
            setError("Passwords do not match!");
            return;
        }

        try {
            // Step 1: Create user in Firebase Authentication
            const userSignup = await createUserWithEmailAndPassword(auth, email, password);
            const user = userSignup.user;
            console.log('User created:', user.uid);
        
            // Step 2: Get the ID token for the signed-up user
            const token = await getIdToken(user);
        
            // Step 3: Store the token in local storage
            localStorage.setItem('userDocID', user.uid);
            localStorage.setItem('userToken', token);
            console.log('Token and document ID stored in local storage');

            // Step 4: Navigate to the home page or dashboard after successful signup
            navigate('/home');
        } catch (error) {
            console.error('Error creating user:', error.message);
            setError(`Sign-up failed: ${error.message}`);
        }
    };

    // Handle Google Sign-Up
    const handleGoogleSignup = async () => {
        const provider = new GoogleAuthProvider();
        try {
            const result = await signInWithPopup(auth, provider);
            const user = result.user;
            console.log('Google sign up success:', user);

            const token = await getIdToken(user);

            // Store user info in Firestore
            await setDoc(doc(FirestoreDB, 'users', user.uid), {
                email: user.email,
                role: role
            });

            localStorage.setItem('userDocID', user.uid);
            localStorage.setItem('userToken', token);
            localStorage.setItem('userRole', role);

            navigate('/home');
        } catch (error) {
            console.error('Google sign up error:', error.message);
            setError(`Google sign-up failed: ${error.message}`);
        }
    };

    // Handle Facebook Sign-Up
    const handleFacebookSignup = async () => {
        const provider = new FacebookAuthProvider(); // Use FacebookAuthProvider for Facebook login
        try {
            const result = await signInWithPopup(auth, provider);
            const user = result.user;
            console.log('Facebook sign up success:', user);

            const token = await getIdToken(user);

            // Store user info in Firestore
            await setDoc(doc(FirestoreDB, 'users', user.uid), {
                email: user.email,
                role: role
            });

            localStorage.setItem('userDocID', user.uid);
            localStorage.setItem('userToken', token);
            localStorage.setItem('userRole', role);

            navigate('/home');
        } catch (error) {
            console.error('Facebook sign up error:', error.message);
            setError(`Facebook sign-up failed: ${error.message}`);
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
                                <Form.Label>Email address</Form.Label>
                                <Form.Control
                                    type="email"
                                    placeholder="Enter email"
                                    value={email}
                                    onChange={handleEmailChange}
                                    required
                                />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicPassword">
                                <Form.Label>Password</Form.Label>
                                <Form.Control
                                    type={showPassword ? "text" : "password"}
                                    placeholder="Enter password"
                                    value={password}
                                    onChange={handlePasswordChange}
                                    required
                                />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicPasswordRetype">
                                <Form.Label>Re-Enter Password</Form.Label>
                                <Form.Control
                                    type={showPassword ? "text" : "password"}
                                    placeholder="Re-enter password"
                                    value={confirmPassword}
                                    onChange={handleConfirmPasswordChange}
                                    required
                                />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicCheckbox">
                                <Form.Check
                                    type="checkbox"
                                    label="Show password"
                                    onChange={togglePasswordVisibility}
                                />
                            </Form.Group>
                            {error && <p className="error-message">{error}</p>}
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

                            {/* Add Facebook Button */}
                            <Button variant="primary" className="signup-button facebookButton" type="button" style={{ backgroundColor:"#3b5998", color:"white", borderColor:"#3b5998" }} onClick={handleFacebookSignup}>
                                <FaFacebook />
                                Sign Up with Facebook
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
        </Container>
    );
}

export default Signup;
