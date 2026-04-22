-- Categories
INSERT INTO categories (name)
VALUES ('Electronics'),
       ('Fashion'),
       ('Food');

-- Products
INSERT INTO products (name, description, price, stock, sku, category_id)
VALUES ('Laptop', 'Gaming Laptop', 15000000, 10, 'SKU-001', 1),
       ('T-Shirt', 'Cotton T-Shirt', 100000, 50, 'SKU-002', 2),
       ('Chocolate', 'Dark Chocolate', 50000, 100, 'SKU-003', 3);