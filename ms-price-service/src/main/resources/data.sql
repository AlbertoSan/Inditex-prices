-- Limpiar la tabla antes de insertar nuevos registros
--DELETE FROM price_entity;

-- Insertar datos en la tabla price_entity
INSERT INTO price_entity (id, brand_id, start_date, end_date, price_list, product_id, priority, price, curr) VALUES
(1, 1, '2020-06-14 00:00:00.0', '2020-12-31 23:59:59.0', 1, 35455, 0, 35.50, 'EUR'),
(2, 1, '2020-06-14 15:00:00.0', '2020-06-14 18:30:00.0', 2, 35455, 1, 25.45, 'EUR'),
(3, 1, '2020-06-15 00:00:00.0', '2020-06-15 11:00:00.0', 3, 35455, 1, 30.50, 'EUR'),
(4, 1, '2020-06-15 16:00:00.0', '2020-12-31 23:59:59.0', 4, 35455, 1, 38.95, 'EUR');
