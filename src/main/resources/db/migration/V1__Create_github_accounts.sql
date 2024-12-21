-- V1__Create_github_accounts.sql

CREATE TABLE github_accounts (
    id TEXT PRIMARY KEY,
    account_name TEXT NOT NULL,
    username TEXT NOT NULL,
    token TEXT
);
