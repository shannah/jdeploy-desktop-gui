CREATE TABLE IF NOT EXISTS jdeploy_cloud_accounts (
    id TEXT PRIMARY KEY NOT NULL,
    account_name TEXT NOT NULL,
    server_url TEXT NOT NULL DEFAULT 'https://cloud.jdeploy.com',
    token TEXT
);
