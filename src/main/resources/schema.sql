CREATE TABLE IF NOT EXISTS products (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    price DOUBLE NOT NULL,
    quantity INT NOT NULL
    );

INSERT INTO products (name, description, price, quantity)
VALUES ('Laptop', 'High performance laptop', 999.99, 10),
       ('Smartphone', 'Latest smartphone model', 699.99, 20),
       ('Headphones', 'Noise cancelling headphones', 199.99, 30);