BEGIN;

CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE clients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(150) NOT NULL,
    email CITEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    money_amount_in_cents BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT clients_full_name_not_blank CHECK (btrim(full_name) <> ''),
    CONSTRAINT clients_email_not_blank CHECK (btrim(email::text) <> ''),
    CONSTRAINT clients_password_not_blank CHECK (btrim(password) <> ''),
    CONSTRAINT clients_money_amount_nonnegative CHECK (money_amount_in_cents >= 0)
);

CREATE TABLE authors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(150) NOT NULL,
    CONSTRAINT authors_full_name_not_blank CHECK (btrim(full_name) <> '')
);

CREATE TABLE books (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(250) NOT NULL,
    synopsis TEXT NOT NULL,
    author_id UUID NOT NULL,
    price_in_cents BIGINT NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT books_author_fk FOREIGN KEY (author_id)
        REFERENCES authors(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT books_title_not_blank CHECK (btrim(title) <> ''),
    CONSTRAINT books_synopsis_not_blank CHECK (btrim(synopsis) <> ''),
    CONSTRAINT books_price_nonnegative CHECK (price_in_cents >= 0),
    CONSTRAINT books_stock_nonnegative CHECK (stock >= 0)
);

CREATE TABLE categories (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name CITEXT NOT NULL UNIQUE,
    CONSTRAINT categories_name_not_blank CHECK (btrim(name::text) <> '')
);

CREATE TABLE books_categories (
    book_id UUID NOT NULL,
    category_id INTEGER NOT NULL,
    PRIMARY KEY (book_id, category_id),
    CONSTRAINT books_categories_book_fk FOREIGN KEY (book_id)
        REFERENCES books(id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT books_categories_category_fk FOREIGN KEY (category_id)
        REFERENCES categories(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ends_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT sessions_client_fk FOREIGN KEY (client_id)
        REFERENCES clients(id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT sessions_valid_period CHECK (ends_at > starts_at)
);

CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL,
    book_id UUID NOT NULL,
    book_title VARCHAR(250) NOT NULL,
    unit_price_in_cents BIGINT NOT NULL,
    total_in_cents BIGINT NOT NULL,
    amount_books INTEGER NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT invoices_client_fk FOREIGN KEY (client_id)
        REFERENCES clients(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT invoices_book_fk FOREIGN KEY (book_id)
        REFERENCES books(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT invoices_book_title_not_blank CHECK (btrim(book_title) <> ''),
    CONSTRAINT invoices_unit_price_nonnegative CHECK (unit_price_in_cents >= 0),
    CONSTRAINT invoices_total_nonnegative CHECK (total_in_cents >= 0),
    CONSTRAINT invoices_amount_books_positive CHECK (amount_books > 0)
);

-- Joins y filtros frecuentes.
CREATE INDEX idx_books_author_id ON books(author_id);
CREATE INDEX idx_books_price_in_cents ON books(price_in_cents);
CREATE INDEX idx_books_categories_category_book
    ON books_categories(category_id, book_id);
CREATE INDEX idx_sessions_client_starts_at
    ON sessions(client_id, starts_at DESC);
CREATE INDEX idx_sessions_client_ends_at
    ON sessions(client_id, ends_at);
CREATE INDEX idx_invoices_client_issued_at
    ON invoices(client_id, issued_at DESC);
CREATE INDEX idx_invoices_book_issued_at
    ON invoices(book_id, issued_at DESC);
CREATE INDEX idx_invoices_issued_at
    ON invoices(issued_at DESC);

-- Búsquedas parciales y sin distinguir mayúsculas/minúsculas.
CREATE INDEX idx_books_title_trgm
    ON books USING GIN (title gin_trgm_ops);
CREATE INDEX idx_books_synopsis_trgm
    ON books USING GIN (synopsis gin_trgm_ops);
CREATE INDEX idx_authors_full_name_trgm
    ON authors USING GIN (full_name gin_trgm_ops);

-- Útil para listar únicamente libros disponibles.
CREATE INDEX idx_books_available
    ON books(title) WHERE stock > 0;

COMMIT;
