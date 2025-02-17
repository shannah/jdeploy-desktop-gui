-- V4__Add_last_opened.sql
ALTER TABLE projects ADD COLUMN last_opened INTEGER NOT NULL DEFAULT 0;
