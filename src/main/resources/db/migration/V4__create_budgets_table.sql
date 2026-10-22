CREATE TABLE budgets
(
    id          BIGSERIAL PRIMARY KEY,
    amount      NUMERIC(12, 2) NOT NULL,
    month       DATE           NOT NULL,
    category_id BIGINT         NOT NULL,
    user_id     BIGINT         NOT NULL,

    CONSTRAINT fk_budgets_categories FOREIGN KEY (category_id) REFERENCES categories,
    CONSTRAINT fk_budgets_users FOREIGN KEY (user_id) REFERENCES users,
    CONSTRAINT uq_budgets_category_id_month UNIQUE (category_id, month),
    CONSTRAINT chk_budgets_amount_positive CHECK ( amount > 0 )
);

CREATE INDEX idx_budgets_user_id_month ON budgets (user_id, month);
