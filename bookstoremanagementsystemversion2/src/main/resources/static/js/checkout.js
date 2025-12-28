// Global variables
let cart = JSON.parse(localStorage.getItem('cart')) || [];

// Initialize checkout page
document.addEventListener('DOMContentLoaded', function() {
    loadCheckoutSummary();
    setupCheckoutForm();
    
    // Check if cart is empty
    if (!cart || cart.length === 0) {
        showError('Your cart is empty. Redirecting to shop...');
        setTimeout(() => {
            window.location.href = '/shop';
        }, 2000);
        return;
    }
});

// Load cart summary in checkout
function loadCheckoutSummary() {
    const summaryContainer = document.getElementById('cart-items-summary');
    
    if (!cart || cart.length === 0) {
        summaryContainer.innerHTML = '<p>No items in cart</p>';
        return;
    }
    
    let summaryHTML = '';
    let subtotal = 0;
    
    cart.forEach(item => {
        const itemTotal = item.price * item.quantity;
        subtotal += itemTotal;
        
        summaryHTML += `
            <div class="cart-summary-item">
                <div>
                    <strong>${item.name}</strong><br>
                    <small>Qty: ${item.quantity} × $${item.price.toFixed(2)}</small>
                </div>
                <div>$${itemTotal.toFixed(2)}</div>
            </div>
        `;
    });
    
    summaryContainer.innerHTML = summaryHTML;
    
    // Update totals
    const tax = subtotal * 0.08;
    const total = subtotal + tax;
    
    document.getElementById('checkout-subtotal').textContent = `$${subtotal.toFixed(2)}`;
    document.getElementById('checkout-tax').textContent = `$${tax.toFixed(2)}`;
    document.getElementById('checkout-total').textContent = `$${total.toFixed(2)}`;
}

// Setup checkout form
function setupCheckoutForm() {
    const form = document.getElementById('checkout-form');
    const submitBtn = document.getElementById('place-order-btn');
    
    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        if (!validateForm()) {
            return;
        }
        
        // Disable button and show loading
        submitBtn.disabled = true;
        document.getElementById('loading').style.display = 'block';
        hideError();
        
        try {
            // Prepare checkout data
            const formData = new FormData(form);
            const checkoutData = {
                items: cart,
                totalAmount: cart.reduce((sum, item) => sum + (item.price * item.quantity), 0),
                totalItems: cart.reduce((sum, item) => sum + item.quantity, 0),
                billingInfo: {
                    fullName: formData.get('fullName'),
                    email: formData.get('email'),
                    phone: formData.get('phone'),
                    address: formData.get('address'),
                    city: formData.get('city'),
                    zipCode: formData.get('zipCode'),
                    paymentMethod: formData.get('paymentMethod'),
                    notes: formData.get('notes')
                }
            };
            
            // Send checkout request
            fetch('/processCheckout', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(checkoutData)
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // Clear cart
                    localStorage.removeItem('cart');
                    
                    // Show success message
                    alert('Order placed successfully!');
                    
                    // Redirect to success page
                    if (data.redirectUrl) {
                        window.location.href = data.redirectUrl;
                    } else {
                        window.location.href = '/orderSuccess?orderId=' + data.orderId + 
                                               '&totalAmount=' + data.totalAmount + 
                                               '&totalItems=' + data.totalItems;
                    }
                } else {
                    alert('Error: ' + data.message);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('An error occurred while processing your order.');
            });
            
        } catch (error) {
            console.error('Checkout error:', error);
            showError('An error occurred during checkout. Please try again.');
        } finally {
            // Re-enable button and hide loading
            submitBtn.disabled = false;
            document.getElementById('loading').style.display = 'none';
        }
    });
}

// Validate form
function validateForm() {
    const requiredFields = ['fullName', 'email', 'phone', 'address', 'city', 'zipCode', 'paymentMethod'];
    let isValid = true;
    
    requiredFields.forEach(fieldName => {
        const field = document.getElementById(fieldName);
        if (!field.value.trim()) {
            field.style.borderColor = '#dc3545';
            isValid = false;
        } else {
            field.style.borderColor = '#ddd';
        }
    });
    
    // Validate email format
    const email = document.getElementById('email');
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (email.value && !emailRegex.test(email.value)) {
        email.style.borderColor = '#dc3545';
        showError('Please enter a valid email address.');
        isValid = false;
    }
    
    // Validate phone format
    const phone = document.getElementById('phone');
    const phoneRegex = /^[\+]?[\d\s\-\(\)]{10,}$/;
    if (phone.value && !phoneRegex.test(phone.value)) {
        phone.style.borderColor = '#dc3545';
        showError('Please enter a valid phone number.');
        isValid = false;
    }
    
    if (!isValid) {
        showError('Please fill in all required fields correctly.');
    }
    
    return isValid;
}

// Show error message
function showError(message) {
    const errorDiv = document.getElementById('error-message');
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
    errorDiv.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

// Hide error message
function hideError() {
    const errorDiv = document.getElementById('error-message');
    errorDiv.style.display = 'none';
}

// Auto-format phone number (optional enhancement)
document.addEventListener('DOMContentLoaded', function() {
    const phoneInput = document.getElementById('phone');
    if (phoneInput) {
        phoneInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length >= 6) {
                value = value.slice(0, 3) + '-' + value.slice(3, 6) + '-' + value.slice(6, 10);
            } else if (value.length >= 3) {
                value = value.slice(0, 3) + '-' + value.slice(3);
            }
            e.target.value = value;
        });
    }
});