CREATE TABLE IF NOT EXISTS todo_lists (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  list_name TEXT NOT NULL,
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);


CREATE TABLE IF NOT EXISTS recurrence_pattern (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(20) NOT NULL, -- 'DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY'
    text TEXT NOT NULL,
    -- Weekly fields
    days_of_week VARCHAR(20), -- 1, 3, 5 for Mon/Wed/Fri

    -- Monthly fields
    month_pattern_type VARCHAR(20), -- 'BY_DATE' OR 'BY_DAY'
    days_of_month VARCHAR(50), -- '1, 15, -1' for 1st, 15th, last day
    nth_weekday_occurrence INT, -- 1, 2, 3, -1
    nth_weekday_day INT, -- 0-6 (Sunday=0, for "2nd Tuesday")

    -- Yearly fields
    yearly_month INT, -- 1-12 for which month
    yearly_day INT, -- 1-31 for which day of that month

    -- Common fields
    time_of_day TIME NOT NULL, --When to trigger each day
    time_zone VARCHAR(50) NOT  NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    todo_list_id UUID NOT NULL REFERENCES todo_lists(id) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE, -- nullable = infinite
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- Checks for data integrity
    CHECK (type IN ('DAILY','WEEKLY','MONTHLY','YEARLY')),
    CHECK (month_pattern_type IS NULL OR month_pattern_type IN ('BY_DATE','BY_DAY')),
    CHECK (yearly_month IS NULL OR yearly_month BETWEEN 1 AND 12),
    CHECK (yearly_day IS NULL OR yearly_day BETWEEN 1 AND 31),
    CHECK (nth_weekday_day IS NULL OR nth_weekday_day BETWEEN 0 AND 6),
    CHECK (end_date IS NULL OR end_date >= start_date)
);


CREATE TABLE IF NOT EXISTS todos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  todo_list_id UUID NOT NULL REFERENCES todo_lists(id) ON DELETE CASCADE,
  text TEXT NOT NULL,
  priority INTEGER NOT NULL DEFAULT 1,
  due_date TIMESTAMP WITH TIME ZONE,
  completed BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  recurrence_pattern_id UUID REFERENCES recurrence_pattern(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uniq_recurrence_occurrence
    ON todos (recurrence_pattern_id, due_date)
    WHERE recurrence_pattern_id IS NOT NULL;
CREATE INDEX idx_todolists_user_id ON todo_lists(user_id);
CREATE INDEX idx_recurrence_user_id ON recurrence_pattern(user_id);
CREATE INDEX idx_recurrence_todolist_id ON recurrence_pattern(todo_list_id);
CREATE INDEX idx_todos_list_id ON todos(todo_list_id);
CREATE INDEX idx_todos_due_date ON todos(due_date);

