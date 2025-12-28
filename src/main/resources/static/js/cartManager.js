class CartManager {
    constructor() {
        this.isLoggedIn = false;
        this.username = null;
        this.init();
    }

    init() {
        // Check if user is logged in
        this.checkLoginStatus();
        
        // Initialize cart as empty first
        this.cart = [];
        
        // Load cart based on login status (async)
        if (this.isLoggedIn) {
            this.loadUserCart();
        } else {
            this.loadGuestCart();
        }
        
        // Don't update count immediately - wait for cart to be loaded
        // updateCartCount() will be called after loadUserCart() or loadGuestCart() completes
    }

    checkLoginStatus() {
        // Get login status from page/session  
        const usernameElement = document.querySelector('[data-username]');
        const isLoggedInElement = document.querySelector('[data-logged-in]');
        
        this.isLoggedIn = isLoggedInElement && isLoggedInElement.dataset.loggedIn === 'true';
        this.username = usernameElement ? usernameElement.dataset.username : null;
        
        console.log('CartManager login check:', {
            isLoggedIn: this.isLoggedIn,
            username: this.username,
            elementFound: !!isLoggedInElement,
            dataValue: isLoggedInElement ? isLoggedInElement.dataset.loggedIn : 'none'
        });
    }

    // Check login status from server for more accuracy
    async checkLoginStatusFromServer() {
        try {
            const response = await fetch('/api/session-status');
            if (response.ok) {
                const status = await response.json();
                this.isLoggedIn = status.isLoggedIn;
                this.username = status.username;
                
                console.log('CartManager server login check:', status);
                return status;
            }
        } catch (error) {
            console.error('Error checking server login status:', error);
        }
        
        // Fallback to DOM-based check
        this.checkLoginStatus();
        return {
            isLoggedIn: this.isLoggedIn,
            username: this.username
        };
    }

    // Load cart for logged-in users from database
    async loadUserCart() {
        try {
            const response = await fetch('/api/user-cart');
            if (response.ok) {
                const cartItems = await response.json();
                this.cart = cartItems;
                console.log('Loaded user cart from database:', cartItems);
                
                // Debug: Log each item's details
                cartItems.forEach(item => {
                    console.log(`Item: ${item.name}, Price: ${item.price}, Quantity: ${item.quantity}`);
                });
                
                this.updateCartDisplay();
                this.updateCartCount();
                
                // Trigger a custom event to notify other parts of the page
                window.dispatchEvent(new CustomEvent('cartLoaded', { detail: { cart: cartItems } }));
            } else {
                console.error('Failed to load user cart');
                this.cart = [];
                this.updateCartCount();
            }
        } catch (error) {
            console.error('Error loading user cart:', error);
            this.cart = [];
            this.updateCartCount();
        }
    }

    // Load cart for guest users from localStorage
    loadGuestCart() {
        const savedCart = localStorage.getItem('guestCart');
        this.cart = savedCart ? JSON.parse(savedCart) : [];
        this.updateCartDisplay();
        this.updateCartCount();
        console.log('Loaded guest cart from localStorage:', this.cart);
        
        // Trigger a custom event to notify other parts of the page
        window.dispatchEvent(new CustomEvent('cartLoaded', { detail: { cart: this.cart } }));
    }

    // Add item to cart
    async addToCart(bookId, name, price, quantity = 1) {
        const item = {
            bookId: parseInt(bookId),
            name: name,
            price: parseFloat(price),
            quantity: parseInt(quantity)
            // REMOVED: author field to fix publisher display issues
        };

        if (this.isLoggedIn) {
            // Add to user cart in database
            try {
                const response = await fetch('/api/user-cart/add', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(item)
                });

                const result = await response.json();
                if (result.success) {
                    await this.loadUserCart(); // Refresh cart
                    this.showMessage('Item added to cart!', 'success');
                    return true;
                } else {
                    this.showMessage(result.message || 'Failed to add item to cart', 'error');
                    return false;
                }
            } catch (error) {
                console.error('Error adding to user cart:', error);
                this.showMessage('Error adding item to cart', 'error');
                return false;
            }
        } else {
            // For guest cart, check stock via API before adding
            try {
                const stockResponse = await fetch(`/api/books/${bookId}`);
                if (stockResponse.ok) {
                    const bookData = await stockResponse.json();
                    const existingItem = this.cart.find(cartItem => cartItem.bookId === item.bookId);
                    const currentCartQuantity = existingItem ? existingItem.quantity : 0;
                    const totalRequestedQuantity = currentCartQuantity + item.quantity;
                    
                    if (totalRequestedQuantity > bookData.quantity) {
                        const availableToAdd = bookData.quantity - currentCartQuantity;
                        let message;
                        if (availableToAdd <= 0) {
                            message = 'This book is already at maximum quantity in your cart (Stock: ' + bookData.quantity + ')';
                        } else {
                            message = 'Not enough stock. You can only add ' + availableToAdd + ' more (Stock: ' + bookData.quantity + ', In cart: ' + currentCartQuantity + ')';
                        }
                        this.showMessage(message, 'error');
                        return false;
                    }
                }
            } catch (error) {
                console.error('Error checking stock:', error);
                // Continue with add if stock check fails
            }
            
            // Add to guest cart in localStorage
            const existingItem = this.cart.find(cartItem => cartItem.bookId === item.bookId);
            
            if (existingItem) {
                existingItem.quantity += item.quantity;
            } else {
                this.cart.push(item);
            }
            
            this.saveGuestCart();
            this.updateCartDisplay();
            this.updateCartCount();
            this.showMessage('Item added to cart!', 'success');
            return true;
        }
    }

    // Update cart item quantity
    async updateQuantity(bookId, newQuantity) {
        bookId = parseInt(bookId);
        newQuantity = parseInt(newQuantity);

        if (newQuantity <= 0) {
            return this.removeFromCart(bookId);
        }

        if (this.isLoggedIn) {
            // Update in database
            const item = this.cart.find(item => item.bookId === bookId);
            if (!item) return false;

            try {
                const response = await fetch('/api/user-cart/update', {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        bookId: bookId,
                        quantity: newQuantity,
                        price: item.price
                    })
                });

                const result = await response.json();
                if (result.success) {
                    await this.loadUserCart(); // Refresh cart
                    return true;
                } else {
                    this.showMessage(result.message || 'Failed to update cart', 'error');
                    return false;
                }
            } catch (error) {
                console.error('Error updating cart:', error);
                return false;
            }
        } else {
            // Update guest cart with stock validation
            try {
                const stockResponse = await fetch(`/api/books/${bookId}`);
                if (stockResponse.ok) {
                    const bookData = await stockResponse.json();
                    if (newQuantity > bookData.quantity) {
                        this.showMessage('Quantity exceeds available stock. Available: ' + bookData.quantity, 'error');
                        return false;
                    }
                }
            } catch (error) {
                console.error('Error checking stock:', error);
                // Continue with update if stock check fails
            }
            
            const item = this.cart.find(item => item.bookId === bookId);
            if (item) {
                item.quantity = newQuantity;
                this.saveGuestCart();
                this.updateCartDisplay();
                this.updateCartCount();
                return true;
            }
            return false;
        }
    }

    // Remove item from cart
    async removeFromCart(bookId) {
        bookId = parseInt(bookId);

        if (this.isLoggedIn) {
            // Remove from database
            try {
                const response = await fetch(`/api/user-cart/remove?bookId=${bookId}`, {
                    method: 'DELETE'
                });

                const result = await response.json();
                if (result.success) {
                    await this.loadUserCart(); // Refresh cart
                    this.showMessage('Item removed from cart', 'success');
                    return true;
                } else {
                    this.showMessage(result.message || 'Failed to remove item', 'error');
                    return false;
                }
            } catch (error) {
                console.error('Error removing from cart:', error);
                return false;
            }
        } else {
            // Remove from guest cart
            this.cart = this.cart.filter(item => item.bookId !== bookId);
            this.saveGuestCart();
            this.updateCartDisplay();
            this.updateCartCount();
            this.showMessage('Item removed from cart', 'success');
            return true;
        }
    }

    // Clear entire cart
    async clearCart() {
        if (this.isLoggedIn) {
            try {
                const response = await fetch('/api/user-cart/clear', {
                    method: 'DELETE'
                });

                const result = await response.json();
                if (result.success) {
                    this.cart = [];
                    this.updateCartDisplay();
                    this.updateCartCount();
                    return true;
                }
                return false;
            } catch (error) {
                console.error('Error clearing cart:', error);
                return false;
            }
        } else {
            this.cart = [];
            localStorage.removeItem('guestCart');
            this.updateCartDisplay();
            this.updateCartCount();
            return true;
        }
    }

    // Sync guest cart to user cart after login/register
    async syncGuestCartToUser() {
        const guestCart = localStorage.getItem('guestCart');
        if (!guestCart) {
            console.log('No guest cart to sync');
            return true;
        }

        const guestCartItems = JSON.parse(guestCart);
        if (guestCartItems.length === 0) {
            console.log('Guest cart is empty, nothing to sync');
            return true;
        }

        console.log('Syncing guest cart to user:', guestCartItems);

        try {
            const response = await fetch('/api/user-cart/sync', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(guestCartItems)
            });

            const result = await response.json();
            if (result.success) {
                // Clear guest cart after successful sync
                localStorage.removeItem('guestCart');
                
                console.log(`Synced ${result.syncedItems || guestCartItems.length} items from guest cart to user cart`);
                return true;
            } else {
                console.error('Failed to sync guest cart:', result.message);
                return false;
            }
        } catch (error) {
            console.error('Error syncing guest cart:', error);
            return false;
        }
    }

    // Save guest cart to localStorage
    saveGuestCart() {
        localStorage.setItem('guestCart', JSON.stringify(this.cart));
    }

    // Get cart items
    getCart() {
        return this.cart || [];
    }

    // Get cart total
    getCartTotal() {
        return this.cart.reduce((total, item) => total + (item.price * item.quantity), 0);
    }

    // Get cart item count
    getCartItemCount() {
        return this.cart.reduce((count, item) => count + item.quantity, 0);
    }
    
    // Async method to get cart count from server (for logged-in users)
    async getCartCountFromServer() {
        if (!this.isLoggedIn) {
            return this.getCartItemCount();
        }
        
        try {
            const response = await fetch('/api/user-cart');
            if (response.ok) {
                const cartItems = await response.json();
                return cartItems.reduce((count, item) => count + item.quantity, 0);
            }
        } catch (error) {
            console.error('Error getting cart count from server:', error);
        }
        return 0;
    }

    // Update cart count display
    updateCartCount() {
        const cartCountElements = document.querySelectorAll('.cart-count, #cart-count');
        const count = this.getCartItemCount();
        
        console.log(`Updating cart count to: ${count}, elements found: ${cartCountElements.length}, cart items: ${this.cart.length}`); // Debug
        console.log('Current cart:', this.cart); // Debug
        
        cartCountElements.forEach(element => {
            element.textContent = count;
            element.style.display = count > 0 ? 'inline' : 'inline'; // Always show count, even if 0
            console.log('Updated element:', element, 'to count:', count); // Debug
        });
    }

    // Update cart display (override in specific pages)
    updateCartDisplay() {
        // This method should be overridden in specific pages
        console.log('Cart updated:', this.cart);
    }

    // Show message to user
    showMessage(message, type = 'info') {
        // Create or update message element
        let messageEl = document.getElementById('cart-message');
        if (!messageEl) {
            messageEl = document.createElement('div');
            messageEl.id = 'cart-message';
            messageEl.style.cssText = `
                position: fixed;
                top: 20px;
                right: 20px;
                padding: 15px 20px;
                border-radius: 5px;
                color: white;
                font-weight: bold;
                z-index: 1000;
                max-width: 300px;
                box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            `;
            document.body.appendChild(messageEl);
        }

        // Set message and color based on type
        messageEl.textContent = message;
        messageEl.className = `cart-message ${type}`;
        
        switch (type) {
            case 'success':
                messageEl.style.backgroundColor = '#4CAF50';
                break;
            case 'error':
                messageEl.style.backgroundColor = '#f44336';
                break;
            case 'warning':
                messageEl.style.backgroundColor = '#ff9800';
                break;
            default:
                messageEl.style.backgroundColor = '#2196F3';
        }

        messageEl.style.display = 'block';

        // Auto-hide after 3 seconds
        setTimeout(() => {
            messageEl.style.display = 'none';
        }, 3000);
    }

    // Handle user login
    async onUserLogin() {
        console.log('CartManager: onUserLogin called');
        this.checkLoginStatus();
        if (this.isLoggedIn) {
            console.log('CartManager: User is logged in, syncing cart');
            // Always sync guest cart to user account if there are guest items
            await this.syncGuestCartToUser();
            
            // Always load user cart from database after sync
            await this.loadUserCart();
            
            console.log('CartManager: Login cart sync complete');
        }
    }

    // Handle user logout  
    onUserLogout() {
        console.log('CartManager: onUserLogout called');
        this.isLoggedIn = false;
        this.username = null;
        
        // Don't clear cart data - it should persist per user account
        // Just switch to guest mode and load guest cart
        this.loadGuestCart();
        console.log('CartManager: Logout cart switch complete');
    }

    // Force refresh login status and reload cart
    async refreshLoginStatusAndCart() {
        console.log('CartManager: refreshLoginStatusAndCart called');
        
        // Check login status from server for accuracy
        await this.checkLoginStatusFromServer();
        
        if (this.isLoggedIn) {
            console.log('CartManager: User is logged in, syncing and loading cart');
            // First sync any guest cart items
            await this.syncGuestCartToUser();
            // Then load user cart from database
            await this.loadUserCart();
        } else {
            console.log('CartManager: User is not logged in, loading guest cart');
            this.loadGuestCart();
        }
    }
}

// Global cart manager instance
window.cartManager = new CartManager();

// Auto-sync cart after login/register
document.addEventListener('DOMContentLoaded', function() {
    console.log('CartManager: DOM loaded, checking for cart sync needs');
    
    // Wait a bit for page to fully load and cart manager to initialize
    setTimeout(async () => {
        // Check if we need to sync cart after login
        if (window.location.search.includes('cartSync=true') || 
            sessionStorage.getItem('needCartSync') === 'true') {
            
            console.log('CartManager: Cart sync needed, forcing refresh');
            
            if (window.cartManager) {
                await window.cartManager.refreshLoginStatusAndCart();
            }
            
            // Clear the flag
            sessionStorage.removeItem('needCartSync');
            
            // Remove cartSync parameter from URL
            if (window.location.search.includes('cartSync=true')) {
                const url = new URL(window.location);
                url.searchParams.delete('cartSync');
                window.history.replaceState({}, '', url);
            }
        }
    }, 500); // Half second delay to ensure everything is loaded
});