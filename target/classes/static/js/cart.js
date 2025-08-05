// START GENAI
class Cart {
    constructor() {
        this.items = JSON.parse(localStorage.getItem('cart')) || [];
        this.updateCartCount();
        this.updateCartDisplay();
    }

    addItem(product, quantity = 1) {
        const existingItem = this.items.find(item => item.id === product.id);

        if (existingItem) {
            existingItem.quantity += quantity;
        } else {
            this.items.push({ ...product, quantity });
        }

        this.save();
        this.updateCartCount();
        this.updateCartDisplay();
    }

    removeItem(productId) {
        this.items = this.items.filter(item => item.id !== productId);
        this.save();
        this.updateCartCount();
        this.updateCartDisplay();
    }

    updateQuantity(productId, quantity) {
        if (quantity <= 0) {
            this.removeItem(productId);
            return;
        }

        const item = this.items.find(item => item.id === productId);
        if (item) {
            item.quantity = quantity;
            this.save();
            this.updateCartCount();
            this.updateCartDisplay();
        }
    }

    getTotal() {
        return this.items.reduce((total, item) => total + (item.price * item.quantity), 0);
    }

    clear() {
        this.items = [];
        this.save();
        this.updateCartCount();
        this.updateCartDisplay();
    }

    save() {
        localStorage.setItem('cart', JSON.stringify(this.items));
    }

    updateCartCount() {
        const count = this.items.reduce((sum, item) => sum + item.quantity, 0);
        document.getElementById('cart-count').textContent = count;
    }

    updateCartDisplay() {
        const cartItems = document.getElementById('cart-items');
        const cartTotal = document.getElementById('cart-total');

        cartItems.innerHTML = this.items.map(item => `
            <div class="py-4 flex justify-between">
                <div class="flex items-center">
                    <img src="${item.imageUrl}" alt="${item.name}" class="h-16 w-16 object-cover rounded">
                    <div class="ml-4">
                        <h3 class="font-medium">${item.name}</h3>
                        <div class="flex items-center mt-1">
                            <button onclick="cart.updateQuantity(${item.id}, ${item.quantity - 1})" class="text-gray-500 hover:text-blue-600">-</button>
                            <span class="mx-2">${item.quantity}</span>
                            <button onclick="cart.updateQuantity(${item.id}, ${item.quantity + 1})" class="text-gray-500 hover:text-blue-600">+</button>
                        </div>
                    </div>
                </div>
                <div class="text-right">
                    <p class="font-medium">$${(item.price * item.quantity).toFixed(2)}</p>
                    <button onclick="cart.removeItem(${item.id})" class="text-sm text-red-600 hover:text-red-800">Remove</button>
                </div>
            </div>
        `).join('');

        cartTotal.textContent = `$${this.getTotal().toFixed(2)}`;
    }
}

const cart = new Cart();

// Initialize cart functionality
document.addEventListener('DOMContentLoaded', () => {
    // Add to cart buttons
    document.querySelectorAll('.add-to-cart').forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            const productData = JSON.parse(button.dataset.product);
            cart.addItem(productData);

            // Show confirmation
            const toast = document.createElement('div');
            toast.className = 'fixed bottom-4 right-4 bg-green-500 text-white px-4 py-2 rounded-lg shadow-lg';
            toast.textContent = 'Added to cart!';
            document.body.appendChild(toast);
            setTimeout(() => toast.remove(), 2000);
        });
    });

    // Cart drawer toggle
    const cartButton = document.getElementById('cart-button');
    const cartDrawer = document.getElementById('cart-drawer');
    const cartOverlay = document.getElementById('cart-overlay');

    cartButton.addEventListener('click', () => {
        cartDrawer.classList.remove('translate-x-full');
        cartOverlay.classList.remove('invisible', 'opacity-0');
    });

    document.getElementById('close-cart').addEventListener('click', () => {
        cartDrawer.classList.add('translate-x-full');
        cartOverlay.classList.add('invisible', 'opacity-0');
    });

    cartOverlay.addEventListener('click', () => {
        cartDrawer.classList.add('translate-x-full');
        cartOverlay.classList.add('invisible', 'opacity-0');
    });
});
