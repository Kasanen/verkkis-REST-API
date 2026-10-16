-- Luo N:M-liitostaulun tuotteiden lisäkategorioille (Product.categories).
-- Taulua ei ole alkuperäisessä skeemassa. REST API ei tarvitse sitä, mutta
-- N:M-mappaus (ManyToManyTest) vaatii sen, jos Product.categories-kokoelmaa
-- käytetään oikeaa kantaa vasten.
-- Ajetaan kerran: mariadb -u <käyttäjä> -p verkkis < sql/product_category_mapping.sql

CREATE TABLE IF NOT EXISTS product_category_mapping (
  product_id  INT(11) NOT NULL,
  category_id INT(11) NOT NULL,
  PRIMARY KEY (product_id, category_id),
  KEY idx_pcm_category (category_id),
  CONSTRAINT fk_pcm_product  FOREIGN KEY (product_id)  REFERENCES products (id) ON DELETE CASCADE,
  CONSTRAINT fk_pcm_category FOREIGN KEY (category_id) REFERENCES productcategories (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Alustetaan liitostaulu nykyisillä pääkategorioilla
INSERT IGNORE INTO product_category_mapping (product_id, category_id)
SELECT id, category_id FROM products WHERE category_id IS NOT NULL;
