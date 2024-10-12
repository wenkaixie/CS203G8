import * as React from 'react';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';
import Menu from '@mui/material/Menu';
import MenuIcon from '@mui/icons-material/Menu';
import Container from '@mui/material/Container';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import Tooltip from '@mui/material/Tooltip';
import MenuItem from '@mui/material/MenuItem';
import { Img } from 'react-image';
import logoImage from '../../assets/images/logo.png';
import { signOut } from 'firebase/auth';
import FBInstanceAuth from '../../firebase/firebase_auth'; // Assuming this is where Firebase auth is managed
import { useNavigate } from 'react-router-dom'; // Import useNavigate

const pages = ['Home', 'Tournaments', 'Calendar', 'Performance'];
const settings = ['Profile', 'Logout'];

function Navbar() {
  const [anchorElNav, setAnchorElNav] = React.useState(null);
  const [anchorElUser, setAnchorElUser] = React.useState(null);
  const [error, setError] = React.useState(null); // State for handling errors
  const navigate = useNavigate(); // useNavigate hook to handle navigation
  const auth = FBInstanceAuth.getAuth(); // Getting auth instance from Firebase

  const handleOpenNavMenu = (event) => {
    setAnchorElNav(event.currentTarget);
  };

  const handleOpenUserMenu = (event) => {
    setAnchorElUser(event.currentTarget);
  };

  const handleCloseNavMenu = () => {
    setAnchorElNav(null);
  };

  const handleCloseUserMenu = () => {
    setAnchorElUser(null);
  };

  const handleGoToHome = () => {
    navigate('/user/home');
  }

  const handleGoToTournaments = () => {
    navigate('/user/tournaments');
  }

  const handleGoToCalendar = () => {
    navigate('/user/calendar');
  }

  const handleGoToProfile = () => {
    navigate('/user/profile');
  }

  const handleLogout = async () => {
    try {
        await signOut(auth); // Signing out the user from Firebase
        console.log('User signed out from Firebase');

        // Navigate to login page
        navigate('/');
    } catch (error) {
        setError(`Unexpected error: ${error.message}`);
        console.error('Error during logout:', error);
    }
};

  return (
    <AppBar position="static" sx={{ backgroundColor: 'transparent', boxShadow: '0px 0px 0px rgba(0, 0, 0, 0)' }}>
      <Container maxWidth="xl">
        <Toolbar disableGutters>
          <Box sx={{ flexGrow: 1, display: { xs: 'flex', md: 'none' } }}>
            <IconButton
              size="large"
              aria-label="account of current user"
              aria-controls="menu-appbar"
              aria-haspopup="true"
              onClick={handleOpenNavMenu}
              color="black"
            >
              <MenuIcon />
            </IconButton>
            <Menu
              id="menu-appbar"
              anchorEl={anchorElNav}
              anchorOrigin={{
                vertical: 'bottom',
                horizontal: 'left',
              }}
              keepMounted
              transformOrigin={{
                vertical: 'top',
                horizontal: 'left',
              }}
              open={Boolean(anchorElNav)}
              onClose={handleCloseNavMenu}
              sx={{ display: { xs: 'block', md: 'none' } }}
            >
              {pages.map((page) => (
                page === 'Home' ? (
                  <MenuItem key={page} onClick={handleGoToHome}>
                    <Typography sx={{ textAlign: 'center' }}>{page}</Typography>
                  </MenuItem>
                ) : page === 'Calendar' ? (
                  <MenuItem key={page} onClick={handleGoToCalendar}>
                    <Typography sx={{ textAlign: 'center' }}>{page}</Typography>
                  </MenuItem>
                ) : page === 'Tournaments' ? (
                  <MenuItem key={page} onClick={handleGoToTournaments}>
                    <Typography sx={{ textAlign: 'center' }}>{page}</Typography>
                  </MenuItem>
                ) : (
                  <MenuItem key={page} onClick={handleCloseNavMenu}>
                    <Typography sx={{ textAlign: 'center' }}>{page}</Typography>
                  </MenuItem>)
              ))}
            </Menu>
          </Box>
          <Img src={logoImage} alt="Logo" width={150} height={50} onClick={handleGoToHome} style={{ cursor: 'pointer' }} />
          <Box sx={{ flexGrow: 1, display: { xs: 'none', md: 'flex' }, justifyContent: 'flex-end', gap: '20px', marginRight: '20px' }}>
            {pages.map((page) => (
              page === 'Home' ? (
                <Button
                  key={page}
                  onClick={handleGoToHome}
                  style={{ cursor: 'pointer' }}
                  sx={{ my: 2, color: 'black', display: 'block', fontFamily: 'Josefin Sans', fontWeight: '500', fontSize: '18px', textTransform: 'none' }}
                >
                  {page}
                </Button>
                ) : page === 'Tournaments' ? (
                  <Button
                    key={page}
                    onClick={handleGoToTournaments}
                    style={{ cursor: 'pointer' }}
                    sx={{ my: 2, color: 'black', display: 'block', fontFamily: 'Josefin Sans', fontWeight: '500', fontSize: '18px', textTransform: 'none' }}
                  >
                    {page}
                  </Button>
                ) : page === 'Calendar' ? (
                  <Button
                    key={page}
                    onClick={handleGoToCalendar}
                    style={{ cursor: 'pointer' }}
                    sx={{ my: 2, color: 'black', display: 'block', fontFamily: 'Josefin Sans', fontWeight: '500', fontSize: '18px', textTransform: 'none' }}
                  >
                    {page}
                  </Button>
                ) : (
                  <Button
                    key={page}
                    onClick={handleCloseNavMenu}
                    style={{ cursor: 'pointer' }}
                    sx={{ my: 2, color: 'black', display: 'block', fontFamily: 'Josefin Sans', fontWeight: '500', fontSize: '18px', textTransform: 'none' }}
                  >
                    {page}
                  </Button>
                )
            ))}
          </Box>
          <Box sx={{ flexGrow: 0 }}>
            <Tooltip title="Open settings">
              <IconButton onClick={handleOpenUserMenu} sx={{ p: 0 }}>
                <Avatar alt="User Avatar" src="/static/images/avatar/2.jpg" />
              </IconButton>
            </Tooltip>
            <Menu
              sx={{ mt: '45px' }}
              id="menu-appbar"
              anchorEl={anchorElUser}
              anchorOrigin={{
                vertical: 'top',
                horizontal: 'right',
              }}
              keepMounted
              transformOrigin={{
                vertical: 'top',
                horizontal: 'right',
              }}
              open={Boolean(anchorElUser)}
              onClose={handleCloseUserMenu}
            >
              {settings.map((setting) =>
                setting === 'Logout' ? (
                  <MenuItem key={setting} onClick={handleLogout}>
                    <Typography sx={{ textAlign: 'center' }}>{setting}</Typography>
                  </MenuItem>
                ) : setting === 'Profile' ? (
                  <MenuItem key={setting} onClick={handleGoToProfile}>
                    <Typography sx={{ textAlign: 'center' }}>{setting}</Typography>
                  </MenuItem>
                ) : (
                  <MenuItem key={setting} onClick={handleCloseUserMenu}>
                    <Typography sx={{ textAlign: 'center' }}>{setting}</Typography>
                  </MenuItem>
                )
              )}
            </Menu>
          </Box>
        </Toolbar>
      </Container>
    </AppBar>
  );
}

export default Navbar;
