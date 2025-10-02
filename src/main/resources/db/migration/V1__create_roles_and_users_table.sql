-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-----------------------------
-- Roles Table
-----------------------------
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Seed default roles
INSERT INTO roles (name) VALUES ('ROLE_USER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;

-----------------------------
-- Users Table
-----------------------------
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-----------------------------
-- User-Roles Join Table
-----------------------------
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES users(id),
    CONSTRAINT fk_role FOREIGN KEY(role_id) REFERENCES roles(id)
);

-----------------------------
-- Days Table
-----------------------------
CREATE TABLE IF NOT EXISTS days (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    day_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_days_user_date UNIQUE (user_id, day_date)
);

-- Index for quick lookup of a user's days
CREATE INDEX IF NOT EXISTS idx_days_user_date ON days (user_id, day_date);

-----------------------------
-- Schedules Table
-----------------------------
CREATE TABLE IF NOT EXISTS schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    day_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_day FOREIGN KEY (day_id) REFERENCES days(id) ON DELETE CASCADE,
    CONSTRAINT uq_schedule_day UNIQUE (day_id) -- one schedule per day
);

-----------------------------
-- Tasks Table
-----------------------------
CREATE TABLE IF NOT EXISTS tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    schedule_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL, -- changed from TEXT for consistency
    completed BOOLEAN DEFAULT FALSE,
    position INTEGER NOT NULL, -- for ordering tasks within a schedule
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_schedule FOREIGN KEY(schedule_id) REFERENCES schedules(id) ON DELETE CASCADE
);

-- Indexes for task lookups and ordering
CREATE INDEX IF NOT EXISTS idx_tasks_schedule_id ON tasks(schedule_id);
CREATE INDEX IF NOT EXISTS idx_tasks_position ON tasks(schedule_id, position);

-----------------------------
-- Outline Items (Subtasks) Table
-----------------------------
CREATE TABLE IF NOT EXISTS outline_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL,
    text TEXT NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    indent_level INTEGER DEFAULT 0 CHECK (indent_level >= 0 AND indent_level <= 2),
    position INTEGER NOT NULL, -- for ordering within a task
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

-- Indexes for outline item lookups and ordering
CREATE INDEX IF NOT EXISTS idx_outline_items_task_id ON outline_items(task_id);
CREATE INDEX IF NOT EXISTS idx_outline_items_position ON outline_items(task_id, position);

-----------------------------
-- Trigger Function to Auto-Update "updated_at"
-----------------------------
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-----------------------------
-- Attach Triggers to Tables
-----------------------------
CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_days_updated_at
BEFORE UPDATE ON days
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_schedules_updated_at
BEFORE UPDATE ON schedules
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_tasks_updated_at
BEFORE UPDATE ON tasks
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_outline_updated_at
BEFORE UPDATE ON outline_items
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
