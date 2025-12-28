// Books functionality with guest cart support
let allBooks = [];
let filteredBooks = [];

document.addEventListener('DOMContentLoaded', function() {
    loadBooks();
    setupSearchAndFilter();
    updateCartCount();
});

function setupSearchAndFilter() {
    const searchBtn = document.getElementById('search-btn');
    const searchInput = document.getElementById('search-input');
    const typeFilter = document.getElementById('type-filter');

    if (searchBtn) {
        searchBtn.addEventListener('click', performSearchAndFilter);
    }

    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                performSearchAndFilter();
            }
        });
    }

    if (typeFilter) {
        typeFilter.addEventListener('change', performSearchAndFilter);
    }
}

async function loadBooks() {
    const loadingDiv = document.getElementById('loading-books');
    const booksGrid = document.getElementById('books-grid');

    if (loadingDiv) loadingDiv.style.display = 'block';
    if (booksGrid) booksGrid.innerHTML = '';

    try {
        console.log('Attempting to load books from /api/books...');
        const response = await fetch('/api/books'); // FIXED: Changed from /book/ to /api/books
        console.log('Response status from /api/books:', response.status);

        if (response.ok) {
            const data = await response.json();
            allBooks = data || [];
            console.log('Books loaded successfully:', allBooks);
            performSearchAndFilter();
        } else {
            console.error('Failed to load books. Status:', response.status);
            if (booksGrid) booksGrid.innerHTML = '<p class="error-message">Failed to load books. Please try again later.</p>';
        }
    } catch (error) {
        console.error('Error loading books:', error);
        if (booksGrid) booksGrid.innerHTML = '<p class="error-message">Error loading books. Please check your connection.</p>';
    } finally {
        if (loadingDiv) loadingDiv.style.display = 'none';
    }
}

function performSearchAndFilter() {
    const searchInput = document.getElementById('search-input');
    const typeFilter = document.getElementById('type-filter');

    const searchQuery = searchInput ? searchInput.value.toLowerCase().trim() : '';
    const selectedType = typeFilter ? typeFilter.value : '';

    filteredBooks = allBooks.filter(book => {
        const matchesSearch = searchQuery === '' ||
            (book.name && book.name.toLowerCase().includes(searchQuery)) ||
            (book.publisher && book.publisher.toLowerCase().includes(searchQuery)) ||
            (book.type && book.type.toLowerCase().includes(searchQuery));

        const matchesType = selectedType === '' || (book.type && book.type.toLowerCase() === selectedType.toLowerCase());
        
        return matchesSearch && matchesType;
    });

    displayBooks(filteredBooks);
}

function displayBooks(booksToDisplay) {
    const booksGrid = document.getElementById('books-grid');
    if (!booksGrid) {
        console.error('Books grid element not found!');
        return;
    }

    if (!booksToDisplay || booksToDisplay.length === 0) {
        booksGrid.innerHTML = '<p class="no-books">No books found matching your criteria.</p>';
        return;
    }

    booksGrid.innerHTML = booksToDisplay.map(book => `
        <div class="book-card">
            <div class="book-image-placeholder">📚</div>
            <div class="book-info">
                <h3 class="book-title">${book.name || 'N/A'}</h3>
                <p class="book-author">Publisher: ${book.publisher || 'N/A'}</p>
                <p class="book-type">Category: ${book.type || 'N/A'}</p>
                <p class="book-price">$${book.price ? book.price.toFixed(2) : 'N/A'}</p>
                <p class="book-quantity">In Stock: ${book.quantity !== null ? book.quantity : 'N/A'}</p>
                <button class="add-to-cart-btn" 
                        onclick="addToCart(${book.bookId}, '${book.name ? book.name.replace(/'/g, "\\'") : 'Book'}', ${book.price || 0})"
                        ${(book.quantity !== null && book.quantity <= 0) ? 'disabled' : ''}>
                    ${(book.quantity !== null && book.quantity <= 0) ? 'Out of Stock' : 'Add to Cart'}
                </button>
            </div>
        </div>
    `).join('');
}

// Updated addToCart function for both guests and logged-in users
async function addToCart(bookId, bookName, price) {
    // Check if user is logged in via server session
    if (window.userSession && window.userSession.isLoggedIn) {
        // Add to server cart for logged-in users
        await addToServerCart(bookId, bookName, price);
    } else {
        // Add to guest cart (localStorage)
        const success = addToGuestCart(bookId, bookName, price);
        if (success) {
            showAddToCartNotification(bookName);
        }
    }
    updateCartCount();
}

async function addToServerCart(bookId, bookName, price) {
    try {
        const response = await fetch('/api/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                bookId: bookId,
                quantity: 1,
                price: price
            })
        });

        if (response.ok) {
            showAddToCartNotification(bookName);
        } else {
            alert('Failed to add item to cart');
        }
    } catch (error) {
        console.error('Error adding to cart:', error);
        alert('Error adding to cart');
    }
}

function showAddToCartNotification(bookName) {
    // Create a temporary notification
    const notification = document.createElement('div');
    notification.className = 'cart-notification';
    notification.innerHTML = `
        <div class="notification-content">
            ✅ "${bookName}" added to cart!
        </div>
    `;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #4CAF50;
        color: white;
        padding: 15px 20px;
        border-radius: 5px;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        z-index: 1000;
        font-weight: 500;
    `;
    
    document.body.appendChild(notification);
    
    // Remove notification after 3 seconds
    setTimeout(() => {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    }, 3000);
}

// Updated cart count function
async function updateCartCount() {
    const cartCountElement = document.getElementById('cart-count');
    if (!cartCountElement) return;

    let totalItems = 0;
    
    if (window.userSession && window.userSession.isLoggedIn) {
        // For logged-in users, get from server
        try {
            const response = await fetch('/api/cart/count');
            if (response.ok) {
                totalItems = await response.json();
            }
        } catch (error) {
            console.error('Error fetching cart count:', error);
        }
    } else {
        // For guest users, get from localStorage
        const guestCart = getGuestCart();
        totalItems = guestCart.reduce((sum, item) => sum + item.quantity, 0);
    }
    
    cartCountElement.textContent = totalItems;
}