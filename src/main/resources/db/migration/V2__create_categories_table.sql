CREATE TABLE categories
(
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    user_id BIGINT       NOT NULL,

    CONSTRAINT fk_categories_users FOREIGN KEY (user_id) REFERENCES users,
    CONSTRAINT uq_categories_user_id_name UNIQUE (user_id, name)
)