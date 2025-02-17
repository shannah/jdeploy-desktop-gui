-- V2__Create_npm_accounts.sql

CREATE TABLE npm_accounts (
    id TEXT PRIMARY KEY,
    account_name TEXT NOT NULL,
    username TEXT,
    password TEXT
);
