// Authentication functionality
let currentUser = null;

document.addEventListener('DOMContentLoaded', function() {
    checkAuthStatus();
    setupAuthForms();
});

// Check authentication status on page load
function checkAuthStatus() {
    const user = localStorage.getItem('currentUser');
    if (user) {
        currentUser = JSON.parse(user);
        updateNavigation();
    }
}

// Update navigation links based on authentication status
function updateNavigation() {
    const loginLink = document.querySelector('a[href="login.html"]');
    const registerLink = document.querySelector('a[href="register.html"]');
    const adminLink = document.getElementById('admin-link');
    const logoutBtn = document.getElementById('logout-btn');
    const cartLink = document.querySelector('a[href="cart.html"]');

    if (currentUser) {
        if (loginLink) loginLink.style.display = 'none';
        if (registerLink) registerLink.style.display = 'none';
        if (logoutBtn) {
            logoutBtn.style.display = 'inline-block';
            logoutBtn.onclick = logout;
        }
        
        // Show admin link for admin users
        if (currentUser.role === 'admin' && adminLink) {
            adminLink.style.display = 'inline-block';
        } else if (adminLink) {
            adminLink.style.display = 'none';
        }

        if (cartLink) cartLink.style.display = 'inline-block';

    } else {
        // Not logged in
        if (loginLink) loginLink.style.display = 'inline-block';
        if (registerLink) registerLink.style.display = 'inline-block';
        if (logoutBtn) logoutBtn.style.display = 'none';
        if (adminLink) adminLink.style.display = 'none';
        if (cartLink) cartLink.style.display = 'inline-block';
    }
}

// Setup authentication forms' submit handlers
function setupAuthForms() {
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');

    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }
}

// Handle login form submission
async function handleLogin(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    
    const loginData = {
        username: formData.get('username'),
        password: formData.get('password')
    };

    try {
        const response = await fetch('/user/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginData)
        });

        const result = await response.json();

        if (result.success) {
            // Store user data
            currentUser = result;
            localStorage.setItem('currentUser', JSON.stringify(result));
            
            alert('Login successful!');
            
            // Redirect based on role
            if (result.role === 'admin') {
                window.location.href = 'admin.html';
            } else {
                window.location.href = 'index.html';
            }
        } else {
            alert('Login failed: ' + result.message);
        }
    } catch (error) {
        console.error('Login error:', error);
        alert('Login failed: ' + error.message);
    }
}

// Handle registration form submission
async function handleRegister(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    
    const registerData = {
        username: formData.get('username'),
        firstName: formData.get('firstName'),
        lastName: formData.get('lastName'),
        phoneNumber: formData.get('phoneNumber'),
        password: formData.get('password')
    };

    try {
        const response = await fetch('/user/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(registerData)
        });

        const result = await response.json();

        if (result.success) {
            alert('Registration successful! Please login.');
            window.location.href = 'login.html';
        } else {
            alert('Registration failed: ' + result.message);
        }
    } catch (error) {
        console.error('Registration error:', error);
        alert('Registration failed: ' + error.message);
    }
}

// Logout function
function logout() {
    currentUser = null;
    localStorage.removeItem('currentUser');
    window.location.href = 'index.html';
}

// Authentication functionality for Thymeleaf integration
document.addEventListener('DOMContentLoaded', function() {
    setupDropdownNavigation();
    setupGuestCheckout();
    initializeCartFromSession();
});

// Setup dropdown navigation for Login/Register
function setupDropdownNavigation() {
    const dropdownBtn = document.querySelector('.dropdown-btn');
    const dropdownContent = document.querySelector('.dropdown-content');
    
    if (dropdownBtn && dropdownContent) {
        dropdownBtn.addEventListener('click', function(e) {
            e.preventDefault();
            dropdownContent.style.display = dropdownContent.style.display === 'block' ? 'none' : 'block';
        });

        // Close dropdown when clicking outside
        document.addEventListener('click', function(e) {
            if (!dropdownBtn.contains(e.target) && !dropdownContent.contains(e.target)) {
                dropdownContent.style.display = 'none';
            }
        });
    }
}

// Setup guest checkout functionality
function setupGuestCheckout() {
    const guestCheckoutBtn = document.getElementById('guest-checkout-btn');
    if (guestCheckoutBtn) {
        guestCheckoutBtn.addEventListener('click', function() {
            // Show login notice
            const loginNotice = document.getElementById('login-notice');
            if (loginNotice) {
                loginNotice.style.display = 'block';
                loginNotice.scrollIntoView({ behavior: 'smooth' });
            }
        });
    }
}

// Initialize cart from localStorage for guest users
function initializeCartFromSession() {
    // Only run this if user is not logged in (no server session)
    if (!window.userSession || !window.userSession.isLoggedIn) {
        const guestCart = getGuestCart();
        updateCartCount();
    }
}

// Guest cart management (localStorage) - MISSING FUNCTIONS ADDED
function getGuestCart() {
    const cart = localStorage.getItem('guestCart');
    return cart ? JSON.parse(cart) : [];
}

function saveGuestCart(cart) {
    localStorage.setItem('guestCart', JSON.stringify(cart));
}

function addToGuestCart(bookId, bookName, price) {
    const guestCart = getGuestCart();
    const existingItem = guestCart.find(item => item.bookId === bookId);
    
    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        guestCart.push({
            bookId: bookId,
            name: bookName,
            price: price,
            quantity: 1
        });
    }
    
    saveGuestCart(guestCart);
    updateCartCount();
    return true;
}

function updateCartCount() {
    const cartCountElement = document.getElementById('cart-count');
    if (!cartCountElement) return;

    let totalItems = 0;
    
    if (window.userSession && window.userSession.isLoggedIn) {
        // For logged-in users, get from server
        fetchServerCartCount();
    } else {
        // For guest users, get from localStorage
        const guestCart = getGuestCart();
        totalItems = guestCart.reduce((sum, item) => sum + item.quantity, 0);
        cartCountElement.textContent = totalItems;
    }
}

async function fetchServerCartCount() {
    const cartCountElement = document.getElementById('cart-count');
    if (!cartCountElement) return;
    
    try {
        const response = await fetch('/api/cart/count');
        if (response.ok) {
            const count = await response.json();
            cartCountElement.textContent = count;
        }
    } catch (error) {
        console.error('Error fetching cart count:', error);
    }
}

// Transfer guest cart to server when user logs in
async function transferGuestCartToServer() {
    const guestCart = getGuestCart();
    if (guestCart.length === 0) return;
    
    try {
        const response = await fetch('/api/cart/transfer', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(guestCart)
        });
        
        if (response.ok) {
            // Clear guest cart after successful transfer
            localStorage.removeItem('guestCart');
            updateCartCount(); // Refresh cart count from server
        }
    } catch (error) {
        console.error('Error transferring guest cart:', error);
    }
}