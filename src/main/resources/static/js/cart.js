// Global variables
let cart = JSON.parse(localStorage.getItem('cart')) || [];

// Initialize cart when page loads
document.addEventListener('DOMContentLoaded', function() {
    loadCartItems();
    updateCartCount();
    
    // Setup checkout button
    const checkoutBtn = document.getElementById('proceed-checkout');
    if (checkoutBtn) {
        checkoutBtn.addEventListener('click', handleCheckout);
    }
    
    // Setup clear cart button
    const clearBtn = document.getElementById('clear-cart');
    if (clearBtn) {
        clearBtn.addEventListener('click', clearCart);
    }
});

// Load and display cart items
function loadCartItems() {
    const cartItemsContainer = document.getElementById('cart-items');
    const emptyCartDiv = document.getElementById('empty-cart');
    
    if (!cartItemsContainer) return;
    
    if (!cart || cart.length === 0) {
        cartItemsContainer.style.display = 'none';
        if (emptyCartDiv) emptyCartDiv.style.display = 'block';
        updateSummary();
        return;
    }
    
    cartItemsContainer.style.display = 'block';
    if (emptyCartDiv) emptyCartDiv.style.display = 'none';
    
    cartItemsContainer.innerHTML = '';
    
    cart.forEach((item, index) => {
        const cartItem = createCartItemElement(item, index);
        cartItemsContainer.appendChild(cartItem);
    });
    
    updateSummary();
}

// Create cart item HTML element
function createCartItemElement(item, index) {
    const cartItemDiv = document.createElement('div');
    cartItemDiv.className = 'cart-item';
    cartItemDiv.innerHTML = `
        <div class="item-details">
            <h3 class="item-name">${item.name}</h3>
            <p class="item-author">by ${item.author || 'Unknown Publisher'}</p>
            <p class="item-price">Price: $${item.price.toFixed(2)}</p>
            <p class="item-status">${item.isGuest ? '👤 Guest Item' : '🔑 Logged In Item'}</p>
        </div>
        <div class="item-quantity">
            <button onclick="updateQuantity(${index}, -1)" class="qty-btn">-</button>
            <span class="quantity">${item.quantity}</span>
            <button onclick="updateQuantity(${index}, 1)" class="qty-btn">+</button>
        </div>
        <div class="item-total">
            <span class="total-price">$${(item.price * item.quantity).toFixed(2)}</span>
        </div>
        <div class="item-actions">
            <button onclick="removeItem(${index})" class="remove-btn">Remove</button>
        </div>
    `;
    return cartItemDiv;
}

// Update item quantity
function updateQuantity(index, change) {
    if (cart[index]) {
        cart[index].quantity += change;
        if (cart[index].quantity <= 0) {
            cart.splice(index, 1);
        }
        saveCart();
        loadCartItems();
        updateCartCount();
    }
}

// Remove item from cart
function removeItem(index) {
    cart.splice(index, 1);
    saveCart();
    loadCartItems();
    updateCartCount();
}

// Update cart summary
function updateSummary() {
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const tax = subtotal * 0.08;
    const total = subtotal + tax;
    
    // Update summary display
    const summaryItems = document.getElementById('summary-items');
    const summarySubtotal = document.getElementById('summary-subtotal');
    const summaryTax = document.getElementById('summary-tax');
    const summaryTotal = document.getElementById('summary-total');
    const itemsCount = document.getElementById('items-count');
    
    if (summaryItems) summaryItems.textContent = totalItems;
    if (summarySubtotal) summarySubtotal.textContent = `$${subtotal.toFixed(2)}`;
    if (summaryTax) summaryTax.textContent = `$${tax.toFixed(2)}`;
    if (summaryTotal) summaryTotal.textContent = `$${total.toFixed(2)}`;
    if (itemsCount) itemsCount.textContent = `${totalItems} items`;
}

// Update cart count in navigation
function updateCartCount() {
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    const cartCountElements = document.querySelectorAll('#cart-count, .cart-count');
    
    cartCountElements.forEach(element => {
        if (element) {
            element.textContent = totalItems;
        }
    });
}

// Handle checkout process - FIXED VERSION
async function handleCheckout() {
    if (!cart || cart.length === 0) {
        alert('Your cart is empty!');
        return;
    }
    
    // Redirect to checkout page instead of processing here
    window.location.href = '/checkout';
}

// Clear entire cart
function clearCart() {
    if (confirm('Are you sure you want to clear your cart?')) {
        cart = [];
        saveCart();
        loadCartItems();
        updateCartCount();
    }
}

// Save cart to localStorage
function saveCart() {
    localStorage.setItem('cart', JSON.stringify(cart));
}

// Add item to cart (called from other pages)
function addToCart(bookId, name, price, author = '', isGuest = false) {
    const existingItem = cart.find(item => item.bookId === bookId);
    
    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({
            bookId: bookId,
            name: name,
            price: price,
            quantity: 1,
            author: author,
            isGuest: isGuest
        });
    }
    
    saveCart();
    updateCartCount();
    showToast(`Added "${name}" to cart!`);
}

// Show toast notification
function showToast(message) {
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #4CAF50;
        color: white;
        padding: 12px 24px;
        border-radius: 4px;
        z-index: 1000;
        animation: slideIn 0.3s ease;
    `;
    
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, 3000);
}