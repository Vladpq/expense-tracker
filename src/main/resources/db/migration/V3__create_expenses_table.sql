CREATE TABLE expenses
(
    id           BIGSERIAL PRIMARY KEY,
    amount       NUMERIC(12, 2) NOT NULL,
    expense_date DATE           NOT NULL,
    category_id  BIGINT         NOT NULL,
    user_id      BIGINT         NOT NULL,
    description  VARCHAR(100),
    created_at   TIMESTAMP      NOT NULL DEFAULT now(),

    CONSTRAINT fk_expenses_categories FOREIGN KEY (category_id) REFERENCES categories,
    CONSTRAINT fk_expenses_users FOREIGN KEY (user_id) REFERENCES users,
    CONSTRAINT chk_expenses_amount_positive CHECK ( amount > 0 )
);

CREATE INDEX idx_expenses_user_id_expense_date ON expenses (user_id, expense_date);
CREATE INDEX idx_expense_category_id ON expenses (category_id);