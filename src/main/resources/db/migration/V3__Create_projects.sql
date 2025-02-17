-- V3__Create_projects.sql

CREATE TABLE projects (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    path TEXT NOT NULL,
    npm_account_id TEXT,
    gitHub_account_id TEXT
);
