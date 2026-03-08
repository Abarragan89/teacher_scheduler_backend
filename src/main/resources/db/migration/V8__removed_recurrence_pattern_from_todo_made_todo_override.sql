-- Clean up old pre-generated recurring todos
DELETE FROM todos WHERE recurrence_pattern_id IS NOT NULL;

-- Remove the old column from todos
ALTER TABLE todos DROP COLUMN recurrence_pattern_id;

-- Create the new overrides table
CREATE TABLE todo_overrides (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pattern_id UUID NOT NULL REFERENCES recurrence_pattern(id) ON DELETE CASCADE,
    todo_list_id UUID NOT NULL REFERENCES todo_lists(id) ON DELETE CASCADE,
    original_date DATE NOT NULL,
    custom_title TEXT,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    notification_sent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    -- Ensures only one override per occurrence per pattern
    UNIQUE (pattern_id, original_date)
);