import React from 'react';
import './Login.css';
import Container from 'react-bootstrap/Container';
import Icon from '../../assets/images/icon.jpg';
import { Img } from 'react-image';
import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import { FaGoogle } from 'react-icons/fa';


const Login = () => {
    return (
        <Container fluid className="login">
            <Container fluid className="login-slide-left">
                <Img
                    src={Icon}
                    height={'418px'}
                    width={'321px'}
                />
            </Container>
            <Container fluid className="login-slide-right">
                <Container fluid className='login-content'>
                    <h1>Sign In</h1>
                    <Container fluid className='login-details'>
                        <Form style={{ fontSize:"20px" }}>
                            <Form.Group className="mb-3" controlId="formBasicEmail">
                                <Form.Label>Email address</Form.Label>
                                <Form.Control type="email" placeholder="Enter email" />
                            </Form.Group>

                            <Form.Group className="mb-3" controlId="formBasicPassword">
                                <Form.Label>Password</Form.Label>
                                <Form.Control type="password" placeholder="Enter password" />
                            </Form.Group>
                            <Form.Group className="mb-3" controlId="formBasicCheckbox">
                                <Form.Check type="checkbox" label="Remember me" />
                            </Form.Group>
                            <Button variant="primary" className='login-button' type="submit" style={{ backgroundColor:"#8F3013", border:"0" }}>
                                Sign In
                            </Button>
                            <div className="divider-container">
                                <hr className="divider-line" />
                                <span className="divider-text">or</span>
                                <hr className="divider-line" />
                            </div>
                            <Button variant="primary" className="login-button googleButton" type="submit" style={{ backgroundColor:"#FFFFFF", color:"black", borderColor:"black" }}>
                                <FaGoogle />
                                Login with Google
                            </Button>
                            <div className="divider-container">
                                <span className="divider-text">Don't have an account? Sign up</span>
                            </div>
                        </Form>
                    </Container>
                </Container>
            </Container>
        </Container>
    );
}

export default Login;